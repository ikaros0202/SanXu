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
            compose.onNodeWithTag("plan_sources").performClick()
            compose.onAllNodes(hasContentDescription("编辑记录",substring=true)).assertCountEquals(0)
            compose.onNodeWithContentDescription("记录操作 straight").performClick()
            compose.onAllNodesWithText("删除记录").assertCountEquals(0)
            compose.onNodeWithText("查看详情").performClick()
            compose.onNodeWithContentDescription("关闭弹层").performClick()
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

/** Controlled failure fixtures; no remote document or third-party image is loaded. */
class EmptyWorkspaceDesignTest {
    @get:Rule val compose=createComposeRule()
    @Test fun emptyPlansAndUnavailableReportsHaveUsableActionsInBothThemes() {
        val emptyPlans=object:PlanRepository by ReplayPlans() {
            override val entries=flowOf(emptyList<PlanEntry>())
        }
        val unavailable=object:LiveContentRepository by ReplayLive {
            var attempts=0
            override val caibaoRefreshState=flowOf<LiveContentRefreshState>(LiveContentRefreshState.Failed(LiveContentFailure.NETWORK))
            override suspend fun refreshCaibao(trigger:LiveRefreshTrigger):LiveContentRefreshResult {
                attempts++
                return LiveContentRefreshResult.Failed(LiveContentFailure.NETWORK)
            }
        }
        val vm=WorkspaceViewModel(ReplayDraws(),emptyPlans,unavailable,ReplayFeed,ReplaySettings)
        val dark=androidx.compose.runtime.mutableStateOf(false)
        try {
            compose.setContent { SanxuTheme(dark=dark.value){SanxuApp(vm)} }
            for(night in listOf(false,true)) {
                compose.runOnIdle {dark.value=night}
                val suffix=if(night)"dark" else "light"
                compose.onNodeWithTag("nav_3").performClick()
                compose.onNodeWithText("还没有本期方案").assertExists()
                compose.captureDesign("fixture-plans-empty-$suffix")
                compose.onNodeWithText("去选号").performClick()
                compose.onNodeWithTag("save_plan").assertIsNotEnabled()
                compose.captureDesign("fixture-pick-empty-$suffix")
                compose.onNodeWithTag("nav_1").performClick()
                compose.onNodeWithText("暂无开奖记录").assertExists()
                compose.captureDesign("fixture-trend-empty-$suffix")
                compose.onNodeWithTag("nav_4").performClick()
                compose.onNodeWithText("这一期彩报暂不可用").assertIsDisplayed()
                compose.onNodeWithContentDescription("缩放控制").assertIsNotEnabled()
                compose.onNodeWithContentDescription("上一期").assertIsNotEnabled()
                compose.captureDesign("fixture-report-unavailable-$suffix")
                val before=unavailable.attempts
                compose.onNodeWithText("重试").performClick()
                compose.waitUntil(5_000){unavailable.attempts>before}
            }
        } finally {vm.viewModelScope.cancel()}
    }
}
