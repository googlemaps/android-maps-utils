package com.google.maps.android.utils.demo

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RendererVisualTest : RendererVisualTestBase() {

    @Test
    fun testPeaksLayer() = runBlocking {
        launchActivity()
        clickButton("Peaks")
        collapseBottomSheet()
        verifyMapContent("Does the map show a map of Colorado/Rocky Mountains area with many yellow pushpin markers and red pushpins with stars scattered across the mountainous areas?")
    }

    @Test
    fun testRangesLayer() = runBlocking {
        launchActivity()
        clickButton("Ranges")
        collapseBottomSheet()
        verifyMapContent("Did the rendering operation successfully display a mosaic of semi-transparent colored and outlined polygons that together cover the majority of the Colorado map area?")
    }

    @Test
    fun testComplexKmlLayer() = runBlocking {
        launchActivity()
        clickButton("Complex KML")
        collapseBottomSheet()
        verifyMapContent("Does the map show a green box with a hole in it in the upper left area and several push pins in the bottom right area of view?")
    }

    @Test
    fun testComplexGeoJsonLayer() = runBlocking {
        launchActivity()
        clickButton("Complex GeoJSON")
        collapseBottomSheet()
        verifyMapContent("Does the map show at least two red push pins a black line drawn near Lower Manhattan, a black trapezoid drawn around the Central Park Zoo and a short black line connected to the southern most red push pin?")
    }

    @Test
    fun testGroundOverlayLayer() = runBlocking {
        launchActivity()
        clickButton("Ground Overlay")
        collapseBottomSheet()
        verifyMapContent("Does the map show an image overlay of Mount Etna, a volcano, superimposed on the base map near Sicily?")
    }

    @Test
    fun testBrightAngelLayer() = runBlocking {
        launchActivity()
        clickButton("Bright Angel")
        collapseBottomSheet()
        verifyMapContent("Does the map show a the Bright Angel Trail in Grand Canyon represented by a winding line?")
    }

    @Test
    fun testClearMap() = runBlocking {
        launchActivity()
        clickButton("Peaks")
        // Wait for peaks to load
        java.util.concurrent.TimeUnit.SECONDS.sleep(2)
        clickButton("Clear")
        collapseBottomSheet()
        verifyMapContent("Is the map mostly empty, showing only the base map and potentially one default red marker (Googleplex), without the many yellow/red peak markers?")
    }
}
