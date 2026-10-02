package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Runner is invoked after disabling network on the owned emulator. */
class OfflineFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun retainsDrawsAndPlansAndShowsRetryState() {
        compose.onNodeWithTag("nav_0").performClick()
        compose.waitUntil(30_000){compose.onAllNodesWithContentDescription("更新开奖").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithContentDescription("更新开奖").performClick()
        compose.waitUntil(45_000){compose.onAllNodes(hasText("更新未成功",substring=true)).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("第 2026263 期").assertExists()
        compose.waitUntil(30_000){compose.onAllNodesWithContentDescription("更新开奖").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithContentDescription("更新开奖").assertExists()
        compose.onNodeWithTag("nav_3").performClick()
        compose.onNodeWithTag("plan_amount").assertTextEquals("¥ 26")
        compose.onNodeWithTag("plan_units").assertTextEquals("13 注 · 4 条记录")
    }
}
