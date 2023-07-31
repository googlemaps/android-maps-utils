package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StreetViewUtilsTest {

    lateinit var latLng : LatLng

    val apiKey = "AN_API_KEY"

    @Before
    fun setUp() {
        latLng = LatLng(37.7749, -122.4194) // San Francisco coordinates

        // Mock the network behavior using MockK
        mockkObject(StreetViewUtils)
        coEvery { StreetViewUtils.fetchStreetViewData(any(), any()) } returns Status.NOT_FOUND
        coEvery { StreetViewUtils.fetchStreetViewData(latLng, apiKey) } returns Status.OK
    }

    @Test
    fun testLocationFoundOnStreetView() = runBlocking {
        val status = StreetViewUtils.fetchStreetViewData(latLng, apiKey)
        assertEquals(Status.OK, status)
    }

    @Test
    fun testLocationNotFoundOnStreetView() = runBlocking {
        val status = StreetViewUtils.fetchStreetViewData(LatLng(10.0, 20.0), apiKey)
        assertEquals(Status.NOT_FOUND, status)
    }
}

