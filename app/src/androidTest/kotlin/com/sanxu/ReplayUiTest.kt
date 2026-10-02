package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewModelScope
import com.sanxu.core.model.*
import com.sanxu.domain.attributes.DrawNumber
import com.sanxu.domain.filter.PlayType
import com.sanxu.domain.livecontent.*
import com.sanxu.domain.pick.*
import com.sanxu.domain.repository.*
import com.sanxu.ui.SanxuTheme
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import org.junit.Rule
import org.junit.Test

class ReplayUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun correctedDrawRecalculatesAndLocksOriginalSources() {
        val official=ReplayDraws()
        val plans=ReplayPlans()
        val vm=WorkspaceViewModel(official,plans,ReplayLive,ReplayFeed,ReplaySettings)
        try {
            compose.setContent { SanxuTheme { PlansPage(vm){} } }
            compose.waitUntil(10_000){compose.onAllNodesWithTag("plan_amount").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithTag("plan_amount").assertTextEquals("¥ 10")
            compose.runOnIdle { official.draws.value=listOf(record("012")) }
            compose.onNodeWithText("已开奖 1").performClick()
            compose.onNodeWithTag("replay_result").assertTextEquals("开奖 012   ·   命中 5 注")
            compose.onAllNodesWithText("编辑这条").assertCountEquals(0)
            compose.onAllNodesWithText("删除").assertCountEquals(0)
            compose.runOnIdle { official.draws.value=listOf(record("013")) }
            compose.onNodeWithTag("replay_result").assertTextEquals("开奖 013   ·   本期未命中")
            compose.onNodeWithTag("plan_amount").assertTextEquals("¥ 10")
        } finally {vm.viewModelScope.cancel()}
    }
    private fun record(number:String)=DrawRecord("2026300","2026-11-06",DrawNumber.parse(number),"https://www.cwl.gov.cn/",number)
}
private class ReplayDraws:DrawRepository {
    val draws=MutableStateFlow<List<DrawRecord>>(emptyList())
    override val allDrawsAscending=draws
    override val latestDraw=draws.map{it.lastOrNull()}
    override val syncMetadata=flowOf<DrawSyncMetadata?>(null)
    override fun observe(query:DrawQuery)=draws
    override fun observeRecent(limit:Int)=draws
    override suspend fun refresh()=SyncResult.Throttled
    override suspend fun syncOnForeground()=SyncResult.Throttled
}
private class ReplayPlans:PlanRepository {
    override val entries=flowOf(listOf(
        PlanEntry("straight","2026300",PickInput(manual=listOf("012")),listOf("012"),3),
        PlanEntry("group","2026300",PickInput(method=PickMethod.GROUP,play=PlayType.GROUP6,byNumber=true,manual=listOf("012")),listOf("012"),2),
    ))
    override val methods=flowOf(emptyList<SavedMethod>())
    override suspend fun draft(key:String):PickDraft?=null
    override suspend fun saveDraft(key:String,draft:PickDraft)=true
    override suspend fun save(entry:PlanEntry,expectedRevision:Int?)=PlanWriteResult.Failed(PlanFailure.ALREADY_DRAWN)
    override suspend fun remove(id:String,expectedRevision:Int)=PlanWriteResult.Failed(PlanFailure.ALREADY_DRAWN)
    override suspend fun saveMethod(method:SavedMethod)=true
    override suspend fun removeMethod(id:String)=true
}
private object ReplayLive:LiveContentRepository {
    override val trialNumber=flowOf<TrialNumber?>(null)
    override val trialRefreshState=flowOf<LiveContentRefreshState>(LiveContentRefreshState.Idle)
    override val caibaoDocument=flowOf<CaibaoDocument?>(null)
    override val caibaoRefreshState=trialRefreshState
    override suspend fun refreshTrial(trigger:LiveRefreshTrigger)=LiveContentRefreshResult.Success
    override suspend fun refreshCaibao(trigger:LiveRefreshTrigger)=LiveContentRefreshResult.Success
    override suspend fun readCaibaoImage(document:CaibaoDocument)=CaibaoImageReadResult.Unavailable(LiveContentFailure.FILE_IO)
    override suspend fun invalidateCaibaoImage(document:CaibaoDocument){}
    override suspend fun cleanCaibaoCache(){}
}
private object ReplayFeed:FeedRepository {
    override val announcement=flowOf<YunnanAnnouncement?>(null)
    override fun announcement(issue:String)=announcement
    override suspend fun refresh(manual:Boolean)=SyncResult.Throttled
    override suspend fun refreshIssue(issue:String){}
}
private object ReplaySettings:SettingsStore {
    override val settings=flowOf(AppSettings())
    override suspend fun setTheme(theme:ThemeChoice){}
    override suspend fun setReplayNotice(enabled:Boolean){}
}
