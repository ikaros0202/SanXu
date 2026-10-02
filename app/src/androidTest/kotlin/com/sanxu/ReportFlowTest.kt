package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Uses validated cached public content. Deliberately never saves an image screenshot. */
class ReportFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun cachedReportDecodesAndZoomControlsWork() {
        compose.onNodeWithTag("nav_4").performClick()
        compose.waitUntil(30_000){compose.onAllNodes(hasContentDescription("彩吧彩报第三版",substring=true)).fetchSemanticsNodes().isNotEmpty()}
        compose.onNode(hasContentDescription("彩吧彩报第三版",substring=true)).assertIsDisplayed()
        compose.onNodeWithContentDescription("缩放控制").performClick()
        compose.onNodeWithContentDescription("放大").performClick()
        compose.onNodeWithText("150% · 复位").assertExists().performClick()
        compose.onNodeWithText("100% · 复位").assertExists()
        compose.onNodeWithContentDescription("上一期").assertExists()
        compose.onNodeWithContentDescription("下一期").assertExists()
    }
}
