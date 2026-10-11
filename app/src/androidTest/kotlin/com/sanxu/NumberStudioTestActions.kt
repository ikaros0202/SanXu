package com.sanxu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.espresso.Espresso

internal fun ComposeContentTestRule.waitForStudio() {
    waitUntil(15_000) { onAllNodesWithTag("new_pick").fetchSemanticsNodes().isNotEmpty() }
}

internal fun ComposeContentTestRule.setStudioDigits(target: String, value: String) {
    waitUntil(15_000) { onAllNodesWithTag(target).fetchSemanticsNodes().isNotEmpty() }
    onNodeWithTag(target).performClick()
    onNodeWithTag("digits_input").performTextReplacement(value)
    Espresso.closeSoftKeyboard()
    onNodeWithTag("apply_digits").performClick()
}

internal fun ComposeContentTestRule.incrementStudioMultiplier() {
    onNodeWithTag("edit_multiplier").performClick()
    onNodeWithContentDescription("增加倍数").performClick()
    onNodeWithTag("apply_multiplier").performClick()
}

internal fun ComposeContentTestRule.setStudioNumber(value: String, position: Int = 0) {
    onNodeWithTag("single_digit_$position").performClick()
    onNodeWithTag("single_number_input").performTextReplacement(value)
    Espresso.closeSoftKeyboard()
    onNodeWithTag("apply_number").performClick()
}
