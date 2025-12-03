/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.data.parser

import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.model.GroundOverlay
import com.google.maps.android.data.renderer.model.GroundOverlayStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroundOverlayTest {

    @Test
    fun testGroundOverlayParsing() {
        val stream = this.javaClass.classLoader!!.getResourceAsStream("ground_overlay.kml")
        val kml = KmlParser().parse(stream)
        val layer = KmlMapper.toLayer(kml)

        assertEquals(1, layer.features.size)
        val feature = layer.features[0]
        
        assertTrue(feature.geometry is GroundOverlay)
        val groundOverlay = feature.geometry as GroundOverlay
        
        assertEquals(37.91904192681665, groundOverlay.north, 0.000001)
        assertEquals(37.46543388598137, groundOverlay.south, 0.000001)
        assertEquals(15.35832653742206, groundOverlay.east, 0.000001)
        assertEquals(14.60128369746704, groundOverlay.west, 0.000001)
        assertEquals(-0.1556640799496235f, groundOverlay.rotation, 0.000001f)

        assertTrue(feature.style is GroundOverlayStyle)
        val style = feature.style as GroundOverlayStyle
        assertEquals("https://developers.google.com/kml/documentation/images/etna.jpg", style.iconUrl)
    }
}
