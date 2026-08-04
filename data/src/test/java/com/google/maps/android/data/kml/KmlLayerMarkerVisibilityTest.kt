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
package com.google.maps.android.data.kml

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression test for KML point markers rendered through the legacy [KmlLayer] bridge: the bridge
 * must hand the renderer an ARGB color with a non-zero alpha channel. Passing the raw marker hue
 * (or 0) as if it were ARGB made the derived marker alpha 0, so every KML point was invisible.
 */
@RunWith(RobolectricTestRunner::class)
class KmlLayerMarkerVisibilityTest {
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
    fun pointPlacemark_isRenderedFullyOpaque() {
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockMarker = mockk<Marker>(relaxed = true)
        val optionsSlot = slot<MarkerOptions>()
        every { mockMap.addMarker(capture(optionsSlot)) } returns mockMarker

        val kml =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Placemark>
                  <name>A point</name>
                  <Point>
                    <coordinates>-111.620,41.942,0</coordinates>
                  </Point>
                </Placemark>
              </Document>
            </kml>
            """.trimIndent()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val layer = KmlLayer(mockMap, kml.byteInputStream(), context)
        layer.addLayerToMap()

        assertEquals(1.0f, optionsSlot.captured.alpha, 0.001f)
    }
}
