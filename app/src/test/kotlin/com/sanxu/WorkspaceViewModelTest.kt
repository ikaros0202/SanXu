package com.sanxu

import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import com.sanxu.core.model.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.repository.*
import com.sanxu.domain.livecontent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceViewModelTest {
    @Test fun undoEditingRestoresInputsAndDraftAsWellAsStoredRecord()= runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val plans=MemoryPlans()
        val vm=workspace(plans)
        try {
            advanceUntilIdle()
            val original=PlanEntry("source","2026300",PickInput(manual=listOf("007")),listOf("007"),1,note="原备注")
            plans.records.value=listOf(original)
            advanceUntilIdle()
            vm.edit(original)
            vm.updateDraft { it.copy(multiplier=4,note="新备注",input=it.input.copy(manual=listOf("012"))) }
            vm.save();advanceUntilIdle()
            vm.undo(requireNotNull(vm.editor.value.feedback));advanceUntilIdle()
            assertThat(plans.records.value.single().multiplier).isEqualTo(1)
            assertThat(vm.editor.value.draft.multiplier).isEqualTo(1)
            assertThat(vm.editor.value.draft.input.manual).containsExactly("007")
            assertThat(vm.editor.value.draft.note).isEqualTo("原备注")
            assertThat(plans.drafts["SINGLE"]).isEqualTo(vm.editor.value.draft)
        } finally { vm.viewModelScope.cancel();Dispatchers.resetMain() }
    }
    @Test fun doubleTapAndRetryUseOneSourceAndDraftsKeepTheirOwnMethod()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val plans=MemoryPlans();val vm=workspace(plans)
        try {
            advanceUntilIdle()
            vm.updateDraft{it.copy(issue="2026300",input=it.input.copy(manual=listOf("007")))}
            vm.save();vm.save();advanceUntilIdle()
            assertThat(plans.records.value).hasSize(1)
            assertThat(plans.calls).isEqualTo(1)
            vm.chooseMethod(PickMethod.DISTINCT);advanceUntilIdle()
            vm.updateInput{it.copy(digits=setOf(0,1,2))}
            vm.chooseMethod(PickMethod.SINGLE);advanceUntilIdle()
            assertThat(vm.editor.value.draft.input.manual).containsExactly("007")
            vm.chooseMethod(PickMethod.DISTINCT);advanceUntilIdle()
            assertThat(vm.editor.value.draft.input.digits).containsExactly(0,1,2)
        } finally {vm.viewModelScope.cancel();Dispatchers.resetMain()}
    }

    @Test fun uncollectedNumberSurvivesMethodSwitchAndNewViewModelWithoutChangingPlan()=runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val plans=MemoryPlans();val vm=workspace(plans)
        var restored:WorkspaceViewModel?=null
        try {
            advanceUntilIdle()
            vm.updateDraft { it.copy(issue="2026300",input=it.input.copy(manual=listOf("007"))) }
            vm.updateDraft { SingleNumberEditor.setValue(SingleNumberEditor.begin(it,"007"),"00") }
            vm.chooseMethod(PickMethod.POSITION);advanceUntilIdle()
            vm.updateInput { it.copy(positions=listOf(setOf(0,1),setOf(0),setOf(2,7))) }
            vm.chooseMethod(PickMethod.SINGLE);advanceUntilIdle()
            assertThat(vm.editor.value.draft.singleEdit).isEqualTo(SingleNumberEdit("00","007"))
            assertThat(vm.editor.value.draft.input.manual).containsExactly("007")
            vm.viewModelScope.cancel()
            val reloaded=workspace(plans);restored=reloaded;advanceUntilIdle()
            assertThat(reloaded.editor.value.draft.singleEdit).isEqualTo(SingleNumberEdit("00","007"))
            assertThat(plans.records.value).isEmpty()
            reloaded.newEntry();advanceUntilIdle()
            assertThat(reloaded.editor.value.draft.singleEdit).isEqualTo(SingleNumberEdit())
            reloaded.chooseMethod(PickMethod.POSITION);advanceUntilIdle()
            assertThat(PickEngine.generate(reloaded.editor.value.draft.input).numbers).containsExactly("002","007","102","107")
        } finally {vm.viewModelScope.cancel();restored?.viewModelScope?.cancel();Dispatchers.resetMain()}
    }

    private fun workspace(plans:PlanRepository)=WorkspaceViewModel(EmptyDraws,plans,EmptyLive,EmptyFeed,MemorySettings())
}
private class MemoryPlans:PlanRepository {
    val records=MutableStateFlow<List<PlanEntry>>(emptyList())
    val drafts=mutableMapOf<String,PickDraft>()
    var calls=0
    override val entries=records
    override val methods=MutableStateFlow<List<SavedMethod>>(emptyList())
    override suspend fun draft(key:String)=drafts[key]
    override suspend fun saveDraft(key:String,draft:PickDraft):Boolean {drafts[key]=draft;return true}
    override suspend fun save(entry:PlanEntry,expectedRevision:Int?):PlanWriteResult {
        calls++
        val current=records.value.firstOrNull{it.id==entry.id}
        if(expectedRevision!=null&&current?.revision!=expectedRevision) return PlanWriteResult.Failed(PlanFailure.CONFLICT)
        val saved=entry.copy(revision=if(current==null)0 else current.revision+1)
        records.value=records.value.filterNot{it.id==entry.id}+saved
        return PlanWriteResult.Saved(saved,0)
    }
    override suspend fun remove(id:String,expectedRevision:Int):PlanWriteResult {records.value=records.value.filterNot{it.id==id};return PlanWriteResult.Removed}
    override suspend fun saveMethod(method:SavedMethod)=true
    override suspend fun removeMethod(id:String)=true
}
private object EmptyDraws:DrawRepository {
    override val latestDraw=flowOf<DrawRecord?>(null)
    override val allDrawsAscending=flowOf(emptyList<DrawRecord>())
    override val syncMetadata=flowOf<DrawSyncMetadata?>(null)
    override fun observeRecent(limit:Int)=allDrawsAscending
    override fun observe(query:DrawQuery)=allDrawsAscending
    override suspend fun refresh()=SyncResult.Throttled
    override suspend fun syncOnForeground()=SyncResult.Throttled
}
private object EmptyFeed:FeedRepository {
    override val announcement=flowOf<YunnanAnnouncement?>(null)
    override fun announcement(issue:String)=announcement
    override suspend fun refresh(manual:Boolean)=SyncResult.Throttled
    override suspend fun refreshIssue(issue:String) {}
}
private object EmptyLive:LiveContentRepository {
    override val trialNumber=flowOf<TrialNumber?>(null)
    override val trialRefreshState=flowOf<LiveContentRefreshState>(LiveContentRefreshState.Idle)
    override val caibaoDocument=flowOf<CaibaoDocument?>(null)
    override val caibaoRefreshState=trialRefreshState
    override suspend fun refreshTrial(trigger:LiveRefreshTrigger)=LiveContentRefreshResult.Success
    override suspend fun refreshCaibao(trigger:LiveRefreshTrigger)=LiveContentRefreshResult.Success
    override suspend fun readCaibaoImage(document:CaibaoDocument)=CaibaoImageReadResult.Unavailable(LiveContentFailure.FILE_IO)
    override suspend fun invalidateCaibaoImage(document:CaibaoDocument) {}
    override suspend fun cleanCaibaoCache() {}
}
private class MemorySettings:SettingsStore {
    override val settings=MutableStateFlow(AppSettings())
    override suspend fun setTheme(theme:ThemeChoice){settings.value=settings.value.copy(theme=theme)}
    override suspend fun setReplayNotice(enabled:Boolean){settings.value=settings.value.copy(showReplayNotice=enabled)}
}
