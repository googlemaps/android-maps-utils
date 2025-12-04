package com.google.maps.android.utils.demo

import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.maps.android.visualtesting.GeminiVisualTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class KmlVisualTest {
    @Test
    fun verifyKmlLayerOverlay() = runBlocking {
        // Read Gemini API Key from BuildConfig
        val geminiApiKey = BuildConfig.GEMINI_API_KEY
        assertTrue("GEMINI_API_KEY is not set in secrets.properties. Please add GEMINI_API_KEY=YOUR_API_KEY to your secrets.properties file.",
            geminiApiKey != "YOUR_GEMINI_API_KEY")

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val uiDevice = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext

        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)


        // Scroll down to find the "KML Layer Overlay" button
        val appViews = UiScrollable(UiSelector().scrollable(true))
        appViews.scrollIntoView(UiSelector().text("KML LAYER OVERLAY"))

        // Wait for the KML screen to load and map to render
        TimeUnit.SECONDS.sleep(10)

        // Wait for the app to load and find the "KML Layer Overlay" button
        val kmlButton = uiDevice.wait(Until.findObject(By.text("KML LAYER OVERLAY")), 10000)

        if (kmlButton == null) {
            // Dump window hierarchy to logcat for debugging
            val outputStream = ByteArrayOutputStream()
            uiDevice.dumpWindowHierarchy(outputStream)
            Log.e("KmlVisualTest", "Could not find KML button. UI Hierarchy:\n" + outputStream.toString("UTF-8"))

            // Take a screenshot for visual inspection
            val screenshotFile = File(context.cacheDir, "test_failure_screenshot.png")
            uiDevice.takeScreenshot(screenshotFile)
            Log.e("KmlVisualTest", "Debug screenshot saved to device cache.")
        }

        assertNotNull("KML Layer Overlay button not found. Check logcat for UI hierarchy dump and debug screenshot.", kmlButton)
        kmlButton.wait(Until.clickable(true), 5000)
        kmlButton.click()



        // Capture a screenshot
        val screenshotFile = File(context.cacheDir, "kml_screenshot.png")
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot", screenshotTaken)

        val screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot file into a bitmap", screenshotBitmap != null)

        // --- STEP 2: Define your verification prompt ---
        val prompt = "Please act as a UI tester and analyze this screenshot to verify the application is rendering the map overlays correctly. Check the image against the following three acceptance criteria:\n" +
                "Geographic Context: Confirm the map is focused on the Googleplex campus in Mountain View, bounded by Amphitheatre Pkwy (north) and Charleston Rd (south).\n" +
                "Polygon Rendering: Verify the presence of at least two distinct colored building footprints:\n" +
                "If the polygons are rendered in the correct colors with the correct labels and street context, confirm that the visual test has PASSED.\n" +
                "\n"

        // --- STEP 3: Analyze the image using Gemini ---
        val geminiResponse = GeminiVisualTestHelper().analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        // --- STEP 4: Assert on Gemini's response ---
        println("Gemini's analysis: $geminiResponse")
        // Example assertion: Check if Gemini confirms the presence of clusters
        assertTrue(
            "PASSED",
            geminiResponse!!.contains("PASSED", ignoreCase = true)
        )
    }
}
