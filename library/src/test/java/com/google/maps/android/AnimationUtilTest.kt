package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.AnimationUtil
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AnimationUtilTest {

    private lateinit var marker: Marker
    private lateinit var currentPosition: LatLng

    @Before
    fun setUp() {
        marker = mockk(relaxed = true)

        // Initial position
        currentPosition = LatLng(0.0, 0.0)

        // Mock the marker position getter and setter
        every { marker.position } answers { currentPosition }
        every { marker.setPosition(any()) } answers {
            currentPosition = firstArg()
            Unit
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `animateMarkerTo moves marker to final position with a buffer tolerance`() {
        val finalPosition = LatLng(10.0, 10.0)
        val durationMs = 100L

        // Start the animation
        AnimationUtil.animateMarkerTo(marker, finalPosition, durationMs)

        val mainLooper = Shadows.shadowOf(android.os.Looper.getMainLooper())

        // Simulate time passing in 16ms increments until we exceed the animation duration
        var timePassed = 0L
        while (timePassed <= durationMs + 100) { // Allowing a little buffer for completion
            mainLooper.idleFor(16, TimeUnit.MILLISECONDS)
            timePassed += 16
        }

        // Check the final position — allowing a reasonable tolerance (0.5 or more)
        assertEquals(10.0, currentPosition.latitude, 0.5)  // 0.5 tolerance
        assertEquals(10.0, currentPosition.longitude, 0.5) // 0.5 tolerance
    }
}
