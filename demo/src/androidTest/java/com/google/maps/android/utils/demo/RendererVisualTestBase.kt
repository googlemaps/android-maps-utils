package com.google.maps.android.utils.demo

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.maps.android.visualtesting.GeminiVisualTestHelper
import org.junit.Assert.assertTrue
import java.io.File
import java.util.concurrent.TimeUnit

abstract class RendererVisualTestBase {

    protected val instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val uiDevice = UiDevice.getInstance(instrumentation)
    protected val context: Context = instrumentation.targetContext
    protected val geminiApiKey: String by lazy {
        val key = BuildConfig.GEMINI_API_KEY
        assertTrue("GEMINI_API_KEY is not set.", key != "YOUR_GEMINI_API_KEY")
        key
    }
    protected val helper = GeminiVisualTestHelper()

    protected fun launchActivity() {
        val intent = Intent(context, RendererDemoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
        // Wait for map initialization
        TimeUnit.SECONDS.sleep(3)
    }

    protected suspend fun clickButton(label: String) {
        // Ensure bottom sheet is expanded enough to find the button
        expandBottomSheet()
        
        // Try to find by text first for speed
        val element = uiDevice.findObject(By.textContains(label))
        if (element != null) {
            element.click()
        } else {
            // Fallback to AI if standard selector fails
            helper.performActionFromPrompt("Click the $label button or chip", uiDevice, geminiApiKey)
        }
        
        // Wait for action to settle (bottom sheet collapse, map render)
        TimeUnit.SECONDS.sleep(2)
    }

    protected fun expandBottomSheet() {
        val bottomSheet = uiDevice.findObject(By.res(context.packageName, "bottom_sheet"))
        if (bottomSheet != null) {
            val startY = uiDevice.displayHeight - 100
            val endY = uiDevice.displayHeight / 2
            uiDevice.swipe(uiDevice.displayWidth / 2, startY, uiDevice.displayWidth / 2, endY, 10)
            TimeUnit.SECONDS.sleep(1)
        }
    }

    protected fun collapseBottomSheet() {
        val bottomSheet = uiDevice.findObject(By.res(context.packageName, "bottom_sheet"))
        if (bottomSheet != null) {
            val startY = uiDevice.displayHeight - 200
            val endY = uiDevice.displayHeight - 100
            uiDevice.swipe(uiDevice.displayWidth / 2, startY, uiDevice.displayWidth / 2, endY, 10)
            TimeUnit.SECONDS.sleep(1)
        }
    }

    protected suspend fun verifyMapContent(description: String) {
        val screenshotFile = File(context.cacheDir, "visual_test_${System.currentTimeMillis()}.png")
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot", screenshotTaken)

        val screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot", screenshotBitmap != null)

        val prompt = """
            Analyze this screenshot of a map app.
            Check if the following condition is met:
            "$description"
            
            Also check if the bottom sheet is collapsed (showing only the top "peek" area with "Load File"/"Clear" buttons and "Presets" header, but NOT the full list of chips).
            
            CRITICAL: Start your response with YES if BOTH the condition is met AND the bottom sheet is collapsed.
            Start your response with NO if ANY condition is not met.
            Then explain your reasoning.
        """.trimIndent()

        val response = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
        println("Gemini Verification for '$description': $response")

        assertTrue(
            "Visual verification failed. Response: $response",
            response?.trim()?.startsWith("YES", ignoreCase = true) == true
        )
    }
}
