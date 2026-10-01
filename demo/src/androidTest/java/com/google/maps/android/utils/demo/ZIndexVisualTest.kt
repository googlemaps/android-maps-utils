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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Visual regression and LLM-assisted verification test for z-index layering.
 *
 * Verifies that:
 * 1. Initial z-index values on [PolygonStyle], [LineStyle], and [PointStyle] dictate the
 *    visual stacking order on the Google Map via [MapViewRenderer].
 * 2. Dynamically lowering a shape's z-index (e.g., lowering Polygon 2 below Polygon 1)
 *    causes the shape to immediately render behind the higher z-index shape on the map.
 */
// [START maps_android_utils_zindex_visual_test]
@RunWith(AndroidJUnit4::class)
class ZIndexVisualTest : BaseVisualTest() {

    private fun launchZIndexDemo() {
        val intent = Intent(context, ZIndexDemoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 15000)
        waitForMapRendering(5)
    }

    /**
     * Verifies the initial stacking hierarchy:
     * - Polygon 1 (Coral) at z: 1.0
     * - Polygon 2 (Teal) at z: 2.0 (Teal is on top of Coral in the overlap)
     * - Polyline 1 (Gold) at z: 3.0
     * - Polyline 2 (Purple) at z: 4.0
     * - Center Marker (Green) at z: 5.0 (on top of all shapes)
     */
    @Test
    fun verifyInitialZIndexLayering() = runBlocking {
        launchZIndexDemo()

        val screenshotBitmap = captureScreenshot("zindex_initial_layering.png")

        val prompt = """
            Task: Analyze the provided Android map screenshot to verify visual z-index layering.
            Criteria Checklist:
            1. The map displays overlapping colored shapes (polygons and polylines) centered in an urban area.
            2. Polygon 1 (Coral / Orange) is on the left and Polygon 2 (Teal / Cyan) is on the right, overlapping in the center.
            3. In the central rectangular overlap region between the two polygons, Polygon 2 (Teal) is rendered on TOP of Polygon 1 (Coral).
            4. The crossing polylines (Gold and Purple) and the center green marker are rendered on top of the polygons.
            5. The bottom control card displays "Z-Index Layering" with controls for each shape.

            Decision Logic:
            If all criteria are met, answer YES.
            If any criterion is not met, answer NO.
            Then provide a brief explanation.
        """.trimIndent()

        verifyScreenshotWithGoldenFallback(
            testName = "zindex_initial_layering",
            screenshotBitmap = screenshotBitmap,
            prompt = prompt,
            passCondition = { it.contains("YES", ignoreCase = true) },
        )
    }

    /**
     * Verifies that dynamically lowering Polygon 2's z-index from 2.0 down to 0.0
     * causes Polygon 2 to move behind Polygon 1 (Coral at 1.0).
     */
    @Test
    fun verifyDynamicZIndexLayeringUpdate() = runBlocking {
        launchZIndexDemo()

        // Locate the minus button for Polygon 2 and click it twice (2.0 -> 1.0 -> 0.0)
        val polygon2Minus = uiDevice.findObject(By.res(context.packageName, "polygon2Minus"))
        assertNotNull("polygon2Minus button must be present in layout", polygon2Minus)

        polygon2Minus.click()
        TimeUnit.MILLISECONDS.sleep(500)
        polygon2Minus.click()
        TimeUnit.MILLISECONDS.sleep(1500)

        // Wait for the map to re-render
        waitForMapRendering(3)

        val updatedScreenshot = captureScreenshot("zindex_lowered_teal_layering.png")

        val prompt = """
            Task: Analyze the provided Android map screenshot to verify visual z-index layering after dynamic update.
            Criteria Checklist:
            1. Polygon 2 (Teal) z-index was lowered from 2.0 to 0.0, which is below Polygon 1 (Coral) at 1.0.
            2. In the central overlap region between the two polygons, Polygon 1 (Coral / Orange) is now visibly rendered ON TOP of Polygon 2 (Teal / Cyan).
            3. The crossing polylines and green marker remain above the polygons.
            4. The controls card reflects Polygon 2 at "z: 0.0" and Polygon 1 at "z: 1.0".

            Decision Logic:
            If all criteria are met, answer YES.
            If any criterion is not met, answer NO.
            Then provide a brief explanation.
        """.trimIndent()

        verifyScreenshotWithGoldenFallback(
            testName = "zindex_lowered_teal_layering",
            screenshotBitmap = updatedScreenshot,
            prompt = prompt,
            passCondition = { it.contains("YES", ignoreCase = true) },
        )
    }

    /**
     * Verifies that clicking the reset button restores all shapes to their initial z-index values
     * and stacking order after modifications.
     */
    @Test
    fun verifyResetZIndices() = runBlocking {
        launchZIndexDemo()

        // Modify Polygon 1's z-index (1.0 -> 2.0 -> 3.0)
        val polygon1Plus = uiDevice.findObject(By.res(context.packageName, "polygon1Plus"))
        assertNotNull("polygon1Plus button must be present in layout", polygon1Plus)
        polygon1Plus.click()
        TimeUnit.MILLISECONDS.sleep(300)
        polygon1Plus.click()
        TimeUnit.MILLISECONDS.sleep(500)

        // Click the reset button
        val resetButton = uiDevice.findObject(By.res(context.packageName, "resetButton"))
        assertNotNull("resetButton must be present in layout", resetButton)
        resetButton.click()
        TimeUnit.MILLISECONDS.sleep(1000)

        val resetScreenshot = captureScreenshot("zindex_reset_layering.png")

        val prompt = """
            Task: Analyze the provided Android map screenshot to verify visual z-index layering after resetting.
            Criteria Checklist:
            1. The map displays overlapping colored shapes (polygons and polylines) centered in an urban area.
            2. Polygon 1 (Coral) has been reset to "z: 1.0" and Polygon 2 (Teal) is at "z: 2.0".
            3. In the central overlap region, Polygon 2 (Teal) is visibly rendered ON TOP of Polygon 1 (Coral).
            4. The bottom control card displays the reset icon button and all initial z-index values (Coral: 1.0, Teal: 2.0, Gold: 3.0, Purple: 4.0, Green: 5.0).

            Decision Logic:
            If all criteria are met, answer YES.
            If any criterion is not met, answer NO.
            Then provide a brief explanation.
        """.trimIndent()

        verifyScreenshotWithGoldenFallback(
            testName = "zindex_reset_layering",
            screenshotBitmap = resetScreenshot,
            prompt = prompt,
            passCondition = { it.contains("YES", ignoreCase = true) },
        )
    }
}
// [END maps_android_utils_zindex_visual_test]
