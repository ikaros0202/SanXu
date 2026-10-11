package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/** Code coverage for the new direct editor; this round does not execute instrumentation. */
class DirectPickFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)

    @Test fun positionsCanBeEditedLastFirstAndSurviveRecreation() {
        tag("nav_2").performClick();compose.waitForStudio()
        tag("method_POSITION").performClick();compose.waitForStudio();tag("new_pick").performClick()
        for(position in 0..2) tag("position_$position").assertIsDisplayed().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        tag("position_next_0").assertDoesNotExist()
        tag("pos0_0").assertDoesNotExist()
        compose.setStudioDigits("position_2","27")
        compose.setStudioDigits("position_0","01")
        compose.setStudioDigits("position_1","0")
        tag("pick_count").assertTextContains("4 个号码",substring=true)
        tag("save_plan").assertIsDisplayed()
        compose.activityRule.scenario.recreate();compose.waitForStudio()
        tag("pick_count").assertTextContains("4 个号码",substring=true)
        tag("keep_007").performClick()
        tag("pick_count").assertTextContains("3 个号码",substring=true)
        compose.setStudioDigits("position_2","27")
        tag("pick_count").assertTextContains("3 个号码",substring=true)
        tag("keep_007").performClick()
        tag("pick_count").assertTextContains("4 个号码",substring=true)
        for(position in 0..2) compose.setStudioDigits("position_$position","0123456789")
        tag("pick_count").assertTextContains("1000 个号码",substring=true)
        tag("save_plan").assertIsDisplayed()
        compose.onNodeWithContentDescription("查看 1000 个号码").assertIsDisplayed()
    }

    @Test fun singleInputRequiresCollectionAndReEditingCanReplaceOrMerge() {
        tag("nav_2").performClick();compose.waitForStudio()
        tag("method_SINGLE").performClick();compose.waitForStudio();tag("new_pick").performClick()
        compose.setStudioNumber("007",position=2)
        tag("pick_count").assertTextContains("0 个号码",substring=true)
        tag("collect_number").performClick()
        tag("pick_count").assertTextContains("1 个号码",substring=true)
        compose.setStudioNumber("012")
        tag("collect_number").performClick()
        tag("result_007").performClick()
        compose.setStudioNumber("012",position=1)
        tag("pick_count").assertTextContains("2 个号码",substring=true)
        tag("collect_number").performClick()
        tag("result_007").assertDoesNotExist()
        tag("pick_count").assertTextContains("1 个号码",substring=true)
        tag("keep_012").performClick()
        tag("pick_count").assertTextContains("0 个号码",substring=true)
        tag("keep_012").performClick()
        tag("pick_count").assertTextContains("1 个号码",substring=true)
        compose.setStudioNumber("00")
        compose.activityRule.scenario.recreate();compose.waitForStudio()
        tag("single_digit_0").performClick()
        tag("single_number_input").assertTextContains("00",substring=true)
        compose.onNodeWithContentDescription("关闭弹层").performClick()
        tag("collect_number").assertIsNotEnabled()
    }
}
