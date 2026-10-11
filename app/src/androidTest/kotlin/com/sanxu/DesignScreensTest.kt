package com.sanxu

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** Executed after the 26-yuan golden workflow, on both owned phone sizes. */
class DesignScreensTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)
    private fun text(value:String)=compose.onNodeWithText(value)
    private fun desc(value:String)=compose.onNodeWithContentDescription(value)
    private fun back()=desc("返回").performClick()
    private fun close()=desc("关闭弹层").performClick()
    private fun waitTag(value:String)=compose.waitUntil(15_000){compose.onAllNodesWithTag(value).fetchSemanticsNodes().isNotEmpty()}
    private fun shot(value:String)=compose.captureDesign(value)

    @Test fun selectedThemeAndFullTemplateTitleAreVisible() {
        val fullTitle="这是一条用于检查完整标题可读性与列表排版的四十字筛选方法模板名称测试记录文本示例甲".take(40)
        for((theme,suffix) in listOf("浅色" to "light","深色" to "dark")) {
            tag("nav_0").performClick();desc("偏好设置").performClick();text(theme).performClick()
            compose.waitUntil(10_000){text(theme).fetchSemanticsNode().config[SemanticsProperties.Selected]}
            text(theme).assertIsSelected();shot("settings-$suffix");back()
            tag("nav_2").performClick();text("工具").performClick();tag("tool_templates").performClick()
            text(fullTitle).performClick();text("方法详情").assertIsDisplayed()
            compose.onAllNodesWithText(fullTitle).onLast().assertIsDisplayed();shot("template-full-title-$suffix");close();back()
        }
        tag("nav_0").performClick();desc("偏好设置").performClick();text("浅色").performClick();back()
    }

    @Test fun allWorkspacesLongContentAndFullPoolRemainUsable() {
        for((theme,suffix) in listOf("浅色" to "light","深色" to "dark")) {
            tag("nav_0").performClick();desc("偏好设置").performClick();text(theme).performClick()
            shot("settings-$suffix");back();tag("recent_2026258").assertIsDisplayed();tag("home_start_pick").assertIsDisplayed()
            assertThat(tag("home_start_pick").getUnclippedBoundsInRoot().bottom.value).isAtMost(tag("nav_0").getUnclippedBoundsInRoot().top.value)
            shot("home-$suffix")
            tag("latest_draw_details").performClick()
            compose.waitUntil(15_000){compose.onAllNodesWithContentDescription("关闭弹层").fetchSemanticsNodes().isNotEmpty()}
            shot("history-detail-$suffix");close();shot("history-$suffix");back()
            tag("nav_1").performClick();waitTag("trend_ready");shot("trend-$suffix")
            desc("走势操作说明").performClick();shot("trend-help-$suffix");close()
            tag("nav_2").performClick();tag("method_SINGLE").performClick();shot("pick-single-$suffix")
            text("批量录入").performClick();shot("batch-$suffix");close()
            tag("method_GROUP").performClick();shot("pick-group-$suffix")
            tag("method_DISTINCT").performClick();shot("pick-distinct-$suffix")
            tag("method_POSITION").performClick()
            for(position in 0..2) compose.setStudioDigits("position_$position","0123456789")
            tag("pick_count").assertTextEquals("1000 个号码 · 1000 注")
            desc("查看 1000 个号码").assertIsDisplayed();tag("save_plan").assertIsDisplayed()
            assertThat(desc("查看 1000 个号码").getUnclippedBoundsInRoot().bottom.value).isAtMost(tag("save_plan").getUnclippedBoundsInRoot().top.value)
            shot("pick-position-full-$suffix")
            tag("issue_picker").performClick();tag("issue_input").performTextReplacement("1999001")
            Espresso.closeSoftKeyboard();shot("issue-$suffix");close()
            tag("issue_picker").assertTextContains("2026300",substring=true)
            desc("查看 1000 个号码").performClick();shot("numbers-$suffix")
            tag("candidate_000").performClick();text("保留 999 / 1000 个").assertExists()
            tag("numbers_grid").performScrollToIndex(999);tag("candidate_999").assertIsDisplayed()
            text("全部恢复").performClick();text("保留 1000 / 1000 个").assertExists();back()
            desc("玩法说明").performClick();shot("pick-help-$suffix");close()
            text("工具").performClick();text("全号码筛选").performClick()
            shot("tools-filter-$suffix");tag("add_condition").performClick();shot("condition-types-$suffix")
            text("和值范围").performClick();shot("condition-editor-$suffix");close()
            tag("tool_templates").performClick()
            if(suffix=="light"&&compose.onAllNodes(hasText("这是一条用于检查完整标题",substring=true)).fetchSemanticsNodes().isEmpty()) {
                text("保存当前方法").performClick()
                text("模板名称").performTextInput("这是一条用于检查完整标题可读性与列表排版的四十字筛选方法模板名称测试记录文本示例甲".take(40))
                text("保存").performClick()
            }
            shot("templates-$suffix")
            tag("tool_note").performClick()
            val note="记录百十个位选择与和值观察，保留前导零，开奖后再核对。".repeat(10).take(200)
            tag("draft_note").performTextReplacement(note);Espresso.closeSoftKeyboard()
            text("200/200").assertExists();tag("tools_done").assertIsDisplayed();shot("notes-$suffix")
            text("历史回测").performClick();waitTag("backtest_ready");shot("backtest-$suffix");back()
            tag("tools_done").performClick();tag("nav_3").performClick();tag("plan_amount").assertTextEquals("¥ 26")
            tag("plan_numbers").performClick();shot("plans-$suffix")
            tag("plan_sources").performClick();shot("sources-$suffix")
            compose.onAllNodes(hasContentDescription("记录操作",substring=true)).onFirst().performClick()
            text("查看详情").performClick();shot("source-detail-$suffix");close()
            compose.onAllNodes(hasContentDescription("记录操作",substring=true)).onFirst().performClick()
            text("删除记录").performClick();shot("source-delete-$suffix");text("取消").performClick()
        }
        // A real edit followed by undo must preserve all other source contributions.
        compose.onAllNodes(hasContentDescription("编辑记录",substring=true)).onFirst().performClick()
        tag("save_plan").assertTextEquals("更新记录");shot("source-edit-dark")
        compose.incrementStudioMultiplier();tag("save_plan").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithText("撤销本次").fetchSemanticsNodes().isNotEmpty()}
        text("撤销本次").performClick();tag("nav_3").performClick()
        tag("plan_amount").assertTextEquals("¥ 26");tag("plan_units").assertTextEquals("13 注 · 4 条记录")
        tag("nav_0").performClick();desc("偏好设置").performClick();text("浅色").performClick();back()
    }
}
