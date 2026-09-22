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
import com.google.maps.android.data.Layer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Regression test for https://github.com/googlemaps/android-maps-utils/issues/1746. */
@RunWith(RobolectricTestRunner::class)
class KmlLayerOnMapTest {
    private val emptyKml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document/>
        </kml>
        """.trimIndent()

    private fun newLayer(): KmlLayer {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return KmlLayer(mockk<GoogleMap>(relaxed = true), emptyKml.byteInputStream(), context)
    }

    @Test
    fun isLayerOnMap_isExposedThroughLayerBaseClass() {
        val layer: Layer = newLayer()

        assertFalse(layer.isLayerOnMap())
    }

    @Test
    fun isLayerOnMap_reflectsAddAndRemove() {
        val layer = newLayer()

        assertFalse(layer.isLayerOnMap())

        layer.addLayerToMap()
        assertTrue(layer.isLayerOnMap())

        layer.removeLayerFromMap()
        assertFalse(layer.isLayerOnMap())
    }

    @Test
    fun addLayerToMap_rendersPointsLinesAndPolygons() {
        mockkStatic(BitmapDescriptorFactory::class)
        every { BitmapDescriptorFactory.defaultMarker(any()) } returns mockk()
        try {
            val kml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <kml xmlns="http://www.opengis.net/kml/2.2">
                  <Document>
                    <Placemark>
                      <Point><coordinates>10.0,20.0</coordinates></Point>
                    </Placemark>
                    <Placemark>
                      <LineString><coordinates>10.0,20.0 11.0,21.0</coordinates></LineString>
                    </Placemark>
                    <Placemark>
                      <Polygon>
                        <outerBoundaryIs>
                          <LinearRing><coordinates>0.0,0.0 1.0,0.0 1.0,1.0 0.0,0.0</coordinates></LinearRing>
                        </outerBoundaryIs>
                      </Polygon>
                    </Placemark>
                  </Document>
                </kml>
            """.trimIndent()
            val context = ApplicationProvider.getApplicationContext<Context>()
            val layer = KmlLayer(mockk<GoogleMap>(relaxed = true), kml.byteInputStream(), context)
            layer.addLayerToMap()
            assertTrue(layer.isLayerOnMap())
        } finally {
            unmockkStatic(BitmapDescriptorFactory::class)
        }
    }
}
