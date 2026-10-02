package com.sanxu.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sanxu.data.local.*
import com.sanxu.data.repository.LocalPlanRepository
import com.sanxu.domain.pick.*
import com.sanxu.domain.repository.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class PlanStoreTest {
    @Test fun duplicateRetryEditDeleteAndReopen(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "sanxu-test-${System.nanoTime()}.db"
        fun open() = Room.databaseBuilder(context,SanxuDatabase::class.java,name).setDriver(BundledSQLiteDriver()).build()
        var db = open()
        var repo = LocalPlanRepository(db.planDao())
        val input = PickInput(manual=listOf("007"))
        val a = PlanEntry("a","2026300",input,listOf("007"),2)
        assertTrue(repo.save(a) is PlanWriteResult.Saved)
        assertTrue((repo.save(a) as PlanWriteResult.Saved).alreadyApplied)
        val b = a.copy(id="b",multiplier=3)
        assertEquals(1,(repo.save(b) as PlanWriteResult.Saved).repeatedNumbers)
        assertEquals(10L,PickEngine.merge(repo.entries.first()).amountYuan)
        assertTrue(repo.saveDraft("SINGLE",PickDraft(input,"2026300",2)))
        db.close()
        db=open(); repo=LocalPlanRepository(db.planDao())
        assertEquals("007",repo.draft("SINGLE")!!.input.manual.single())
        assertEquals(2,repo.entries.first().size)
        assertTrue(repo.save(b.copy(multiplier=4),0) is PlanWriteResult.Saved)
        assertEquals(PlanFailure.CONFLICT,(repo.remove("b",0) as PlanWriteResult.Failed).reason)
        assertTrue(repo.remove("b",1) is PlanWriteResult.Removed)
        assertEquals(4L,PickEngine.merge(repo.entries.first()).amountYuan)
        db.close(); context.deleteDatabase(name)
    }
    @Test fun drawnIssueRejectsWritesAndUndo() = runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val db=Room.inMemoryDatabaseBuilder(context,SanxuDatabase::class.java).setDriver(BundledSQLiteDriver()).build()
        val repo=LocalPlanRepository(db.planDao())
        val entry=PlanEntry("x","2026300",PickInput(manual=listOf("012")),listOf("012"),1)
        assertTrue(repo.save(entry) is PlanWriteResult.Saved)
        db.drawDao().commitValidatedSync(listOf(DrawEntity("2026300","2026-11-10",0,1,2,"https://www.cwl.gov.cn/","fp")),SyncMetadataEntity())
        assertEquals(PlanFailure.ALREADY_DRAWN,(repo.remove("x",0) as PlanWriteResult.Failed).reason)
        assertEquals(PlanFailure.ALREADY_DRAWN,(repo.save(entry.copy(multiplier=2),0) as PlanWriteResult.Failed).reason)
        db.close()
    }
}
