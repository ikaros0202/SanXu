package com.sanxu

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.common.truth.Truth.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File

class WorkspaceFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)
    private fun text(value:String)=compose.onNodeWithText(value)
    private fun waitFor(value:String) { compose.waitUntil(15_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() } }
    private fun screenshot(name:String) {
        compose.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val folder=File(instrumentation.targetContext.getExternalFilesDir(null),"qa").apply{mkdirs()}
        val bitmap=instrumentation.uiAutomation.takeScreenshot() ?: compose.onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap()
        bitmap.useBitmap { value -> File(folder,"$name.png").outputStream().use { value.compress(Bitmap.CompressFormat.PNG,100,it) } }
    }
    @Test fun fourMethodsMergeUndoAndRestore() {
        tag("nav_2").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("issue_picker").fetchSemanticsNodes().isNotEmpty()}
        tag("issue_picker").performClick()
        tag("issue_input").performTextReplacement("2026300")
        tag("apply_issue").performClick()
        text("批量录入").performClick()
        tag("batch_input").performTextInput("012")
        text("加入号码").performClick()
        compose.onNodeWithContentDescription("增加倍数").performClick()
        tag("pick_amount").assertTextEquals("¥ 4")
        tag("save_plan").performClick()
        waitFor("已加入方案")
        text("查看方案").performClick()
        tag("plan_amount").assertTextEquals("¥ 4")

        tag("nav_2").performClick()
        tag("method_DISTINCT").performClick()
        compose.waitForIdle()
        listOf(0,1,2).forEach{tag("distinct_$it").performClick()}
        tag("pick_count").assertTextEquals("6 个号码 · 6 注")
        screenshot("pick-distinct-light")
        tag("save_plan").performClick()
        waitFor("重复号码已合并")
        screenshot("merge-dialog-light")
        text("查看方案").performClick()
        tag("plan_amount").assertTextEquals("¥ 16")
        tag("plan_count").assertTextEquals("6 个号码")
        tag("plan_units").assertTextEquals("8 注 · 2 条记录")
        screenshot("plans-merged-light")

        tag("nav_2").performClick()
        compose.onNodeWithContentDescription("增加倍数").performClick()
        tag("save_plan").performClick()
        waitFor("重复号码已合并")
        text("撤销本次").performClick()
        tag("nav_3").performClick()
        tag("plan_amount").assertTextEquals("¥ 16")
        compose.activityRule.scenario.recreate()
        tag("plan_amount").assertTextEquals("¥ 16")

        tag("nav_2").performClick()
        tag("method_GROUP").performClick()
        tag("group_0").performClick();tag("group_1").performClick()
        tag("pick_count").assertTextEquals("2 个号码 · 2 注")
        screenshot("pick-group-light")
        tag("group_GROUP6").performClick();tag("group_2").performClick()
        tag("pick_count").assertTextEquals("1 个号码 · 1 注")
        tag("save_plan").performClick();waitFor("已加入方案");text("查看方案").performClick()
        tag("plan_amount").assertTextEquals("¥ 18")
        tag("plan_count").assertTextEquals("7 个号码")

        tag("nav_2").performClick();tag("method_POSITION").performClick()
        listOf(0,1).forEach{tag("pos0_$it").performClick()}
        tag("pos1_0").performClick();listOf(2,7).forEach{tag("pos2_$it").performClick()}
        tag("pick_count").assertTextEquals("4 个号码 · 4 注")
        tag("pos2_9").assertIsDisplayed()
        tag("save_plan").assertIsDisplayed()
        compose.onNodeWithContentDescription("查看 4 个号码").assertIsDisplayed()
        val digitBounds=tag("pos2_9").getUnclippedBoundsInRoot()
        assertThat((digitBounds.right-digitBounds.left).value).isAtLeast(48f)
        assertThat((digitBounds.bottom-digitBounds.top).value).isAtLeast(48f)
        screenshot("pick-position-light")
        tag("save_plan").performClick();waitFor("重复号码已合并");text("查看方案").performClick()
        tag("plan_amount").assertTextEquals("¥ 26")
        tag("plan_count").assertTextEquals("10 个号码")
        tag("plan_units").assertTextEquals("13 注 · 4 条记录")

        tag("nav_0").performClick();screenshot("home-light")
        compose.onNodeWithContentDescription("偏好设置").performClick();text("墨绿").performClick()
        compose.onNodeWithContentDescription("返回").performClick();screenshot("home-dark")
        tag("nav_2").performClick();screenshot("pick-position-dark")
        tag("nav_3").performClick();screenshot("plans-dark")
        tag("nav_1").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("trend_ready").fetchSemanticsNodes().isNotEmpty()}
        screenshot("trend-dark")
        tag("nav_4").performClick()
        // No caibao screenshots are stored: third-party images are not part of the source deliverable.
        tag("nav_0").performClick();compose.onNodeWithContentDescription("偏好设置").performClick()
        text("纸白").performClick();compose.onNodeWithContentDescription("返回").performClick()
        tag("nav_3").performClick()
    }

    private inline fun Bitmap.useBitmap(block:(Bitmap)->Unit) { try { block(this) } finally { recycle() } }
}
