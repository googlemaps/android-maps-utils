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

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private val prompt = """
    Task: Act as a Visual QA system to verify an Android application screenshot. Compare the 
    provided image against the following technical specifications. Your goal is to determine if the 
    rendering is correct.

    1. Map Verification
    Confirm that a map is visible in the background. The map should be centered on the Sydney, 
    Australia area. Major landmarks or labels like Sydney, Hornsby, and Cronulla should be visible. 
    Be flexible with minor labels as they may change, but the general geographic layout must match the reference.
    2. Marker Content and Styling
    There must be exactly six markers on the screen. Verify each of the following:

    * Marker A: Text is "Default". Background is white. Orientation is standard horizontal.
    * Marker B: Text is "Custom color". Background is cyan/light blue. Orientation is standard horizontal.
    * Marker C: Text is "Rotated 90 degrees". Background is red. Both the bubble and the text are rotated 90 degrees clockwise.
    * Marker D: Text is "Rotate=90, ContentRotate=-90". Background is purple. The bubble is rotated 90 degrees clockwise, but the text inside remains horizontal.
    * Marker E: Text is "ContentRotate=90". Background is green. The bubble is horizontal, but the text inside is rotated 90 degrees clockwise.
    * Marker F: Text is "Mixing different fonts". Background is orange. The word "Mixing" must be in italics. The words "different fonts" must be in bold.

    3. Spatial Grid Verification
    Verify that the markers are positioned correctly relative to one another in a rough grid layout:

    * Top-Left: The Orange marker ("Mixing different fonts").
    * Top-Right: The Green marker ("ContentRotate=90").
    * Middle-Left: The Red marker ("Rotated 90 degrees").
    * Middle-Center: The White marker ("Default").
    * Bottom-Left: The Purple marker ("Rotate=90, ContentRotate=-90").
    * Bottom-Right: The Cyan marker ("Custom color").

    4. Output Requirements
    Provide your response in the following format:

    * Status: [PASSED or FAILED]
    * Justification: Provide a concise explanation for the classification. If FAILED, list the 
    specific markers or map elements that do not meet the criteria.
""".trimIndent()

@RunWith(AndroidJUnit4::class)
class IconGeneratorVisualTest : BaseVisualTest() {

    @Before
    fun setup() {
        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
    }

    @Test
    fun testIconMarkers() = runBlocking {
        // Launch IconGeneratorDemoActivity directly
        val intent = Intent(context, IconGeneratorDemoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
        
        // Wait for map rendering (Markers are bitmaps, so we can't search for text "Default")
        waitForMapRendering(5)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("clustering_screenshot.png")

        // --- STEP 3: Analyze the image using Gemini ---
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        // --- STEP 4: Assert on Gemini's response ---
        println("Gemini's analysis: $geminiResponse")
        
        requireNotNull(geminiResponse) { "Gemini response was null, check API key or network connection." }
        assertTrue(
            "Gemini validation failed. Response: $geminiResponse",
            geminiResponse.contains("PASSED", ignoreCase = true)
        )
    }
}
