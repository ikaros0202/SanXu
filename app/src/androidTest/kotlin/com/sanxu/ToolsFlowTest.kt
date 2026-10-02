package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class ToolsFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)
    private fun text(value:String)=compose.onNodeWithText(value)
    @Test fun darkSystemBarsAndLoadedTrend() {
        tag("nav_0").performClick()
        compose.onNodeWithContentDescription("偏好设置").performClick()
        text("夜蓝").performClick()
        compose.waitForIdle()
        compose.runOnIdle { assertThat(WindowCompat.getInsetsController(compose.activity.window,compose.activity.window.decorView).isAppearanceLightStatusBars).isFalse() }
        compose.onNodeWithContentDescription("返回").performClick()
        tag("nav_1").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("trend_ready").fetchSemanticsNodes().isNotEmpty()}
        tag("trend_ready").assertIsDisplayed()
        tag("nav_0").performClick();compose.onNodeWithContentDescription("偏好设置").performClick();text("纸白").performClick()
    }
    @Test fun filterTemplateBacktestAndNumberExclusion() {
        tag("nav_2").performClick();text("工具").performClick()
        text("全号码筛选").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithText("1000 个号码").fetchSemanticsNodes().isNotEmpty()}
        tag("add_condition").performClick()
        text("和值范围").performScrollTo().performClick()
        text("最小值").performTextInput("0");text("最大值").performTextInput("3")
        text("确认").performClick()
        tag("tools_count").assertTextEquals("20 个号码")
        tag("tool_templates").performClick()
        text("保存当前方法").performScrollTo().performClick()
        text("模板名称").performTextInput("小和值方法")
        text("保存").performClick()
        text("历史回测").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("backtest_ready").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithContentDescription("返回").performClick()
        tag("tools_done").performClick()
        tag("pick_count").assertTextEquals("20 个号码 · 20 注")
        compose.onNodeWithContentDescription("查看 20 个号码").performClick()
        text("000").performClick();text("保留 19 / 20 个").assertExists()
        text("全部恢复").performClick();text("保留 20 / 20 个").assertExists()
        compose.onNodeWithContentDescription("返回").performClick()
        text("工具").performClick()
        tag("tool_templates").performClick()
        text("小和值方法").performScrollTo().assertIsDisplayed()
        text("应用").performClick();tag("pick_count").assertTextEquals("20 个号码 · 20 注")
    }
    @Test fun persistedPlanAfterProcessDeath() {
        tag("nav_3").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("plan_amount").fetchSemanticsNodes().isNotEmpty()}
        tag("plan_amount").assertTextEquals("¥ 26")
        tag("plan_count").assertTextEquals("10 个号码")
        tag("plan_units").assertTextEquals("13 注 · 4 条记录")
    }
}
