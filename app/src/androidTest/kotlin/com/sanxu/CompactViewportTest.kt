package com.sanxu

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.graphics.ColorUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/** Partial visibility must not satisfy the one-screen contract. */
class CompactViewportTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun tag(value:String)=compose.onNodeWithTag(value)

    @Test fun homeActionFitsAboveNavigationWithoutScrolling() {
        tag("nav_0").performClick()
        compose.waitUntil(15_000){compose.onAllNodesWithTag("recent_2026258").fetchSemanticsNodes().isNotEmpty()}
        val navigation=tag("nav_0").getUnclippedBoundsInRoot()
        assertThat(tag("home_start_pick").getUnclippedBoundsInRoot().bottom.value).isAtMost(navigation.top.value)
    }

    @Test fun darkNavigationBackgroundMatchesItsLightIconsOnOlderAndroid() {
        tag("nav_0").performClick();compose.onNodeWithContentDescription("偏好设置").performClick()
        compose.onNodeWithText("深色").performClick();compose.waitForIdle()
        // DataStore changes arrive asynchronously, beyond Compose's current idle frame.
        compose.waitUntil(10_000){compose.onNodeWithText("深色").fetchSemanticsNode().config[SemanticsProperties.Selected]}
        if(Build.VERSION.SDK_INT<29)compose.runOnIdle {
            @Suppress("DEPRECATION") val color=compose.activity.window.navigationBarColor
            assertThat(ColorUtils.calculateLuminance(color)).isLessThan(.2)
        }
        compose.onNodeWithText("浅色").performClick()
    }

    @Test fun openingSheetDoesNotShowEnglishDragHandleTooltip() {
        tag("nav_2").performClick();tag("method_SINGLE").performClick()
        compose.onNodeWithContentDescription("玩法说明").performClick()
        compose.onNodeWithContentDescription("关闭弹层").assertIsDisplayed()
        val titleTop=compose.onNodeWithContentDescription("关闭弹层").getUnclippedBoundsInRoot().top
        compose.onAllNodes(isRoot()).onLast().performTouchInput { longClick(Offset(centerX,(titleTop-8.dp).toPx())) }
        compose.captureDesign("sheet-tooltip-regression")
        compose.waitForIdle()
        compose.onNodeWithText("Drag handle",useUnmergedTree=true).assertDoesNotExist()
        compose.onNodeWithContentDescription("关闭弹层").performClick()
    }
}
