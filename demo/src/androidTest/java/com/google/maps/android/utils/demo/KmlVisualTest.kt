/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class KmlVisualTest : BaseVisualTest() {
    @Test
    fun verifyKmlLayerOverlay() = runBlocking {
        // Launch KmlDemoActivity directly
        val intent = Intent(context, KmlDemoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 15000)

        // Wait for the KML screen to load and map to render
        delay(10.seconds)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("kml_screenshot.png")

        // --- STEP 2: Define your verification prompt ---
        val prompt = """
            Task: Analyze the provided image and verify it against the following three strict criteria.
            Criteria Checklist:
            Location: The image must display a map of the Googleplex (look for text labels such as "Googleplex", "Amphitheatre Pkwy", or "Charleston Rd").
            Subject Matter: The map must feature highlighted building footprints (polygonal shapes overlaying the buildings).
            Color Palette: The building footprints must explicitly include all four of the following colors: Blue, Red, Green, and Yellow.

            Decision Logic:
            If ALL criteria are met, the test passes.
            If ANY criterion is not met, the test fails.

            Required Output Format:
            Provide your response in the following format:
            Test Result: [PASS / FAIL]
            Verification Details:
            Location Check: [State if Googleplex is confirmed]
            Footprint Check: [State if footprints are visible]
            Color Check: [List the colors found]
            Failure Explanation: [If FAIL, you must explain exactly which specific criterion was not met. If PASS, write "None".]
        """.trimIndent()

        // --- STEP 3: Analyze the image using Gemini ---
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        // --- STEP 4: Assert on Gemini's response ---
        assertWithMessage("Gemini's analysis failed: $geminiResponse").that(geminiResponse).contains("Test Result: PASS")
        assertWithMessage("Gemini's analysis failed: $geminiResponse").that(geminiResponse).doesNotContain("Test Result: FAIL")
    }
}
