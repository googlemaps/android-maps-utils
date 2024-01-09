/*
 * Copyright 2024 Google LLC
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

