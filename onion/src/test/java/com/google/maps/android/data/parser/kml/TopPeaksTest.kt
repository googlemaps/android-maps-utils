package com.google.maps.android.data.parser.kml

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.data.parser.kml.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class TopPeaksTest {

    private val parser = KmlParser()

    @Test
    fun testTopPeaksLoaded() {
        val stream = File("src/test/resources/top_peaks.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.document).isNotNull()
        
        with(kml.document!!) {
            assertThat(folders).isNotEmpty()
            val folder = folders.first()
            assertThat(folder.name).isEqualTo("14ers (14,000 ft and above)")
            assertThat(folder.placemarks).isNotEmpty()

            // Verify bounding box
            val scene = com.google.maps.android.data.renderer.mapper.KmlMapper.toScene(kml)
            assertThat(scene.boundingBox).isNotNull()
            // Approximate bounds for Colorado 14ers
            assertThat(scene.boundingBox?.northeast?.latitude).isGreaterThan(37.0)
            assertThat(scene.boundingBox?.southwest?.latitude).isLessThan(41.0)
            
            with(folder.placemarks.first { it.name!!.startsWith("Mount Elbert") }) {
                assertThat(point).isNotNull()
                assertThat(point!!.coordinates).isNear(LatLngAlt(
                    latitude = 39.1178,
                    longitude = -106.4454,
                    altitude = 4401.2
                ))
                assertThat(styleUrl).isEqualTo("#14erStyle")
            }
        }
    }
}
