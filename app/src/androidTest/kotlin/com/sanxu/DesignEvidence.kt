package com.sanxu

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** Native Android evidence. Callers must never capture third-party report images. */
internal fun ComposeTestRule.captureDesign(name:String) {
    waitForIdle()
    val runner=InstrumentationRegistry.getInstrumentation()
    runner.waitForIdleSync()
    SystemClock.sleep(450)
    waitForIdle()
    val folder=File(runner.targetContext.getExternalFilesDir(null),"impeccable-qa").apply{mkdirs()}
    val bitmap=runner.uiAutomation.takeScreenshot() ?: onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap()
    try { File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) } }
    finally { bitmap.recycle() }
}
