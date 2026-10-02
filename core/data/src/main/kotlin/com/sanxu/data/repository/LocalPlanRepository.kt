package com.sanxu.data.repository

import com.sanxu.data.local.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPlanRepository @Inject constructor(private val dao:PlanDao):PlanRepository {
    override val entries=dao.observeEntries().map { rows -> rows.map { Json.decodeFromString<PlanEntry>(it.payload) } }
    override val methods=dao.observeMethods().map { rows -> rows.map { SavedMethod(it.id,it.title,Json.decodeFromString<PickInput>(it.inputJson),it.window) } }
    override suspend fun draft(key:String):PickDraft? = dao.draft(key)?.let { Json.decodeFromString<PickDraft>(it.payload) }
    override suspend fun saveDraft(key:String,draft:PickDraft):Boolean = safely { dao.putDraft(DraftEntity(key,Json.encodeToString(draft))) }
    override suspend fun save(entry:PlanEntry,expectedRevision:Int?):PlanWriteResult {
        if(!entry.issue.matches(Regex("20[0-9]{5}")) || entry.issue.takeLast(3).toInt() !in 1..366) return PlanWriteResult.Failed(PlanFailure.INVALID_ISSUE)
        val result=PickEngine.generate(entry.input)
        if(result.problem!=null || entry.multiplier !in 1..99 || entry.id.isBlank() || result.numbers!=entry.numbers) return PlanWriteResult.Failed(PlanFailure.INVALID_INPUT)
        return try {
            val change=dao.saveChecked(entry,expectedRevision)
            PlanWriteResult.Saved(Json.decodeFromString<PlanEntry>(change.entry.payload),change.repeated,change.alreadyApplied)
        } catch(e:CancellationException) { throw e }
        catch(e:PlanStoreException) { PlanWriteResult.Failed(e.reason) }
        catch(_:Exception) { PlanWriteResult.Failed(PlanFailure.STORAGE) }
    }
    override suspend fun remove(id:String,expectedRevision:Int):PlanWriteResult = try {
        dao.removeChecked(id,expectedRevision); PlanWriteResult.Removed
    } catch(e:CancellationException) { throw e }
    catch(e:PlanStoreException) { PlanWriteResult.Failed(e.reason) }
    catch(_:Exception) { PlanWriteResult.Failed(PlanFailure.STORAGE) }
    override suspend fun saveMethod(method:SavedMethod):Boolean = if(method.title.isBlank()) false else safely { dao.putMethod(MethodEntity(method.id,method.title,Json.encodeToString(method.input),method.window)) }
    override suspend fun removeMethod(id:String):Boolean = safely { dao.deleteMethod(id) }
    private suspend fun safely(action:suspend ()->Unit):Boolean = try { action(); true } catch(e:CancellationException) { throw e } catch(_:Exception) { false }
}
