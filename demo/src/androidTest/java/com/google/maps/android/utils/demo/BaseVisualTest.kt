/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.utils.demo

import android.app.Instrumentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.maps.android.visualtesting.GeminiVisualTestHelper
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.io.File
import java.io.FileOutputStream

abstract class BaseVisualTest {

    protected val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val uiDevice = UiDevice.getInstance(instrumentation)
    protected val context: Context = instrumentation.targetContext
    protected val helper = GeminiVisualTestHelper()

    protected val geminiApiKey: String by lazy {
        val key = BuildConfig.GEMINI_API_KEY
        assertTrue(
            "GEMINI_API_KEY is not set in secrets.properties. Please add GEMINI_API_KEY=YOUR_API_KEY to your secrets.properties file.",
            key != "YOUR_GEMINI_API_KEY"
        )
        key
    }

    protected fun captureScreenshot(filename: String = "screenshot_${System.currentTimeMillis()}.png"): Bitmap {
        val screenshotFile = File(context.cacheDir, filename)
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot: $filename", screenshotTaken)

        val bitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot file: $filename", bitmap != null)
        return bitmap
    }

    /**
     * Waits for the map to render.
     * Since MapView content (tiles, markers) is rendered on a GL surface and not exposed as
     * accessibility nodes, we cannot rely on UiAutomator looking for text/markers.
     * We use a stable delay to ensure rendering is complete.
     */
    protected fun waitForMapRendering(seconds: Long = 3) {
        // Optional: Wait for map container if possible
        // uiDevice.wait(Until.hasObject(By.descContains("Google Map")), 5000)
        
        try {
            java.util.concurrent.TimeUnit.SECONDS.sleep(seconds)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Verifies the screenshot against a golden image.
     * If the golden matches, the test passes immediately.
     * If it fails (or no golden exists), it falls back to Gemini for visual verification.
     * If Gemini passes, it saves/updates the golden image and logs a warning to review it.
     */
    protected suspend fun verifyScreenshotWithGoldenFallback(
        testName: String,
        screenshotBitmap: Bitmap,
        prompt: String,
        passCondition: (String) -> Boolean
    ) {
        val goldenFile = File(context.getExternalFilesDir(null), "goldens/$testName.png")
        
        var isPixelMatch = false
        if (goldenFile.exists()) {
            val goldenBitmap = BitmapFactory.decodeFile(goldenFile.absolutePath)
            isPixelMatch = compareBitmaps(screenshotBitmap, goldenBitmap)
        }

        if (isPixelMatch) {
            Log.i("VisualTest", "Screenshot matched golden perfectly for '$testName'. Fast pass.")
            return
        }

        Log.w("VisualTest", "Screenshot did not match golden for '$testName'. Falling back to Gemini.")
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
        
        requireNotNull(geminiResponse) { "Gemini response was null for test '$testName'" }
        
        val passed = passCondition(geminiResponse)
        
        if (passed) {
            // Update golden since Gemini approved it
            goldenFile.parentFile?.mkdirs()
            FileOutputStream(goldenFile).use { out ->
                screenshotBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.w("VisualTest", "Gemini approved the new UI state for '$testName'. Golden image updated at ${goldenFile.absolutePath}. Please review this image manually to prevent baking in hallucinations.")
        } else {
            fail("Visual verification failed for '$testName'. Gemini response: $geminiResponse")
        }
    }

    private fun compareBitmaps(b1: Bitmap, b2: Bitmap): Boolean {
        if (b1.width != b2.width || b1.height != b2.height) return false
        val pixels1 = IntArray(b1.width * b1.height)
        val pixels2 = IntArray(b2.width * b2.height)
        b1.getPixels(pixels1, 0, b1.width, 0, 0, b1.width, b1.height)
        b2.getPixels(pixels2, 0, b2.width, 0, 0, b2.width, b2.height)
        return pixels1.contentEquals(pixels2)
    }
}
