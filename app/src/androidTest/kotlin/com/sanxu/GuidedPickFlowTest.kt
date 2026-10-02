package com.sanxu

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/** The active position is the only number keyboard; all selections remain durable. */
class GuidedPickFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)

    @Test fun onlyActivePositionShowsDigitsAndSelectionsSurviveRecreation() {
        tag("nav_2").performClick()
        tag("method_POSITION").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("pos0_0").fetchSemanticsNodes().isNotEmpty()}
        val reset=compose.onAllNodesWithText("新建").fetchSemanticsNodes()
        compose.onNodeWithText(if(reset.isEmpty())"清空" else "新建").performClick()
        compose.waitForIdle()

        // Fails on 1.0.1: all thirty position buttons were simultaneously present.
        tag("pos1_0").assertDoesNotExist()
        tag("pos2_0").assertDoesNotExist()
        tag("pos0_0").assertIsDisplayed().assertWidthIsAtLeast(48.dp)
        tag("pos0_0").performClick();tag("pos0_1").performClick()
        tag("position_next_0").performClick()
        tag("pos0_0").assertDoesNotExist()
        tag("pos1_0").performClick()
        tag("position_next_1").performClick()
        tag("pos2_2").performClick();tag("pos2_7").performClick()
        tag("pick_count").assertTextContains("4 个号码",substring=true)
        tag("save_plan").assertIsDisplayed()
        tag("position_0").performClick()
        tag("pos0_0").assertIsSelected();tag("pos0_1").assertIsSelected()
        compose.activityRule.scenario.recreate()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("pos0_0").fetchSemanticsNodes().isNotEmpty()}
        tag("pos0_0").assertIsSelected();tag("pos0_1").assertIsSelected()
        tag("pick_count").assertTextContains("4 个号码",substring=true)
        for(position in 0..2) {
            tag("position_$position").performClick()
            for(digit in 0..9) {
                val node=tag("pos${position}_$digit")
                if(!node.fetchSemanticsNode().config[SemanticsProperties.Selected])node.performClick()
            }
        }
        tag("pick_count").assertTextContains("1000 个号码",substring=true)
        tag("save_plan").assertIsDisplayed()
        compose.onNodeWithContentDescription("查看 1000 个号码").assertIsDisplayed()
    }
}
