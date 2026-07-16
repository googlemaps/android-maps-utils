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
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression test for GeoJSON point markers rendered through the legacy [GeoJsonLayer] bridge:
 * the bridge must not hand the renderer a fully transparent point color, which would make every
 * point marker invisible (the renderer derives marker alpha from the style color's alpha channel).
 */
@RunWith(RobolectricTestRunner::class)
class GeoJsonLayerMarkerVisibilityTest {
    @Before
    fun setUp() {
        mockkStatic(BitmapDescriptorFactory::class)
        every { BitmapDescriptorFactory.defaultMarker(any()) } returns mockk()
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapDescriptorFactory::class)
    }

    @Test
    fun pointFeature_isRenderedFullyOpaque() {
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockMarker = mockk<Marker>(relaxed = true)
        val optionsSlot = slot<MarkerOptions>()
        every { mockMap.addMarker(capture(optionsSlot)) } returns mockMarker

        val geoJson =
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": { "name": "A point" },
                  "geometry": { "type": "Point", "coordinates": [-111.620, 41.942] }
                }
              ]
            }
            """.trimIndent()

        val layer = GeoJsonLayer(mockMap, JSONObject(geoJson))
        layer.addLayerToMap()

        assertEquals(1.0f, optionsSlot.captured.alpha, 0.001f)
    }
}
