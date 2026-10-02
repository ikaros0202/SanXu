package com.sanxu.data.local

import androidx.room3.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.repository.PlanFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName="plan_entries", indices=[Index("issue")])
data class PlanEntryEntity(@PrimaryKey val id:String,val issue:String,val payload:String,val revision:Int)
@Entity(tableName="pick_drafts")
data class DraftEntity(@PrimaryKey val key:String,val payload:String)
@Entity(tableName="saved_methods")
data class MethodEntity(@PrimaryKey val id:String,val title:String,val inputJson:String,val window:Int)
data class PlanChange(val entry:PlanEntryEntity,val repeated:Int,val alreadyApplied:Boolean=false)
class PlanStoreException(val reason:PlanFailure): IllegalStateException(reason.name)

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_entries ORDER BY issue DESC, id") fun observeEntries():Flow<List<PlanEntryEntity>>
    @Query("SELECT * FROM plan_entries WHERE issue=:issue") suspend fun forIssue(issue:String):List<PlanEntryEntity>
    @Query("SELECT * FROM plan_entries WHERE id=:id") suspend fun entry(id:String):PlanEntryEntity?
    @Query("SELECT MAX(issue) FROM draws") suspend fun lastDraw():String?
    @Upsert suspend fun putEntry(entry:PlanEntryEntity)
    @Query("DELETE FROM plan_entries WHERE id=:id") suspend fun deleteEntry(id:String)
    @Query("SELECT * FROM pick_drafts WHERE `key`=:key") suspend fun draft(key:String):DraftEntity?
    @Upsert suspend fun putDraft(draft:DraftEntity)
    @Query("SELECT * FROM saved_methods ORDER BY title") fun observeMethods():Flow<List<MethodEntity>>
    @Upsert suspend fun putMethod(method:MethodEntity)
    @Query("DELETE FROM saved_methods WHERE id=:id") suspend fun deleteMethod(id:String)

    @Transaction
    suspend fun saveChecked(desired:PlanEntry,expectedRevision:Int?):PlanChange {
        val old=entry(desired.id)
        if(expectedRevision==null && old!=null) return PlanChange(old,0,true)
        if(expectedRevision!=null && (old==null || old.revision!=expectedRevision)) throw PlanStoreException(PlanFailure.CONFLICT)
        if(lastDraw()?.let { desired.issue<=it || (old!=null && old.issue<=it) }==true) throw PlanStoreException(PlanFailure.ALREADY_DRAWN)
        val previous=forIssue(desired.issue).filterNot { it.id==desired.id }.map { Json.decodeFromString<PlanEntry>(it.payload) }
        val existingNumbers=PickEngine.merge(previous).bets.filter { it.play==desired.play }.map { it.number }.toSet()
        val saved=desired.copy(revision=if(old==null) 0 else old.revision+1,createdAt=old?.let { Json.decodeFromString<PlanEntry>(it.payload).createdAt } ?: desired.createdAt)
        val entity=PlanEntryEntity(saved.id,saved.issue,Json.encodeToString(saved),saved.revision)
        putEntry(entity)
        return PlanChange(entity,saved.numbers.count(existingNumbers::contains))
    }

    @Transaction
    suspend fun removeChecked(id:String,revision:Int) {
        val old=entry(id) ?: return
        if(old.revision!=revision) throw PlanStoreException(PlanFailure.CONFLICT)
        if(lastDraw()?.let { old.issue<=it }==true) throw PlanStoreException(PlanFailure.ALREADY_DRAWN)
        deleteEntry(id)
    }
}
