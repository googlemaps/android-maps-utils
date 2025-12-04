package com.google.maps.android.utils.demo

import android.app.Instrumentation
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.maps.android.visualtesting.GeminiVisualTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ClusteringVisualTest {

    private lateinit var geminiApiKey: String
    private lateinit var instrumentation: Instrumentation
    private lateinit var uiDevice: UiDevice
    private lateinit var context: Context

    @Before
    fun setup() {
        // Read Gemini API Key from BuildConfig
        geminiApiKey = BuildConfig.GEMINI_API_KEY
        assertTrue(
            "GEMINI_API_KEY is not set in secrets.properties. Please add GEMINI_API_KEY=YOUR_API_KEY to your secrets.properties file.",
            geminiApiKey != "YOUR_GEMINI_API_KEY"
        )

        instrumentation = InstrumentationRegistry.getInstrumentation()
        uiDevice = UiDevice.getInstance(instrumentation)
        context = instrumentation.targetContext

        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
    }

    @Test
    fun naturalLanguageClickTest() = runBlocking {
        // Use a natural language prompt to perform the click action
        val helper = GeminiVisualTestHelper()
        helper.performActionFromPrompt("Click the CLUSTERING button", uiDevice, geminiApiKey)

        // Wait for the clustering screen to load and map to render
        TimeUnit.SECONDS.sleep(5)

        // Capture a screenshot to verify the result of the action
        val screenshotFile = File(context.cacheDir, "natural_lang_click_screenshot.png")
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot after AI action", screenshotTaken)

        val screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot file into a bitmap", screenshotBitmap != null)

        // --- Perform a visual assertion on the new screen ---
        val prompt = "Does this image show a map with several markers clustered together? Answer only YES or NO."
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        println("Gemini's analysis after natural language click: $geminiResponse")
        assertTrue(
            "Visual verification failed. Gemini did not confirm the presence of a map with clusters.",
            geminiResponse?.contains("YES", ignoreCase = true) == true
        )
    }

    @Test
    fun verifyClusteringScreenContent() = runBlocking {
        // Wait for the app to load and find the "Clustering" button
        val clusteringButton = uiDevice.wait(Until.findObject(By.text("CLUSTERING")), 10000)

        if (clusteringButton == null) {
            // Dump window hierarchy to logcat for debugging
            val outputStream = ByteArrayOutputStream()
            uiDevice.dumpWindowHierarchy(outputStream)
            Log.e("ClusteringVisualTest", "Could not find clustering button. UI Hierarchy:\n${outputStream.toString("UTF-8")}")

            // Take a screenshot for visual inspection
            val screenshotFile = File(context.cacheDir, "test_failure_screenshot.png")
            uiDevice.takeScreenshot(screenshotFile)
            Log.e("ClusteringVisualTest", "Debug screenshot saved to device cache.")
        }

        assertNotNull("Clustering button not found. Check logcat for UI hierarchy dump and debug screenshot.", clusteringButton)
        clusteringButton.click()

        // Wait for the clustering screen to load and map to render
        TimeUnit.SECONDS.sleep(5)

        // Capture a screenshot
        val screenshotFile = File(context.cacheDir, "clustering_screenshot.png")
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot", screenshotTaken)

        val screenshotBitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot file into a bitmap", screenshotBitmap != null)

        // --- STEP 2: Define your verification prompt ---
        val prompt = """
            Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly. Check the image against the following three acceptance criteria:
            Geographic Bounds: Confirm the map is centered on North London and Hertfordshire, specifically showing landmarks like St Albans, Enfield, and the M25 ring road.
            Primary Cluster: Verify the presence of a Red cluster marker labeled '200+' located centrally over the North London area.
            Secondary Cluster: Verify the presence of a Blue cluster marker labeled '10+' located to the southwest of the green marker.
            If all three elements are present and legible, just confirm that the visual test has PASSED. If any element is missing or incorrect, please detail the discrepancy.

        """.trimIndent()

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