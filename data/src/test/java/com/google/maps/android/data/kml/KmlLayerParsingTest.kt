/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * How KmlLayer turns a KML document into its legacy model: containers for the document and
 * folders, placemarks with their properties and resolved styles, polygons and ground overlays.
 */
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class KmlLayerParsingTest {
    private fun layerOf(kml: String): KmlLayer =
        KmlLayer(
            mockk<GoogleMap>(relaxed = true),
            kml.trimIndent().byteInputStream(),
            ApplicationProvider.getApplicationContext<Context>(),
        )

    private fun kml(body: String): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2">
        $body
        </kml>
        """

    @Test
    fun `document becomes a root container with nested folders`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <name>Trip</name>
                  <description>Summer</description>
                  <Folder>
                    <name>Day 1</name>
                    <Folder><name>Morning</name></Folder>
                    <Placemark><name>Hotel</name><Point><coordinates>2.0,41.0</coordinates></Point></Placemark>
                  </Folder>
                  <Folder><name>Day 2</name></Folder>
                </Document>
                """
            )
        )

        assertThat(layer.hasContainers()).isTrue()
        assertThat(layer.hasPlacemarks()).isFalse()
        val root = layer.getContainers().single()
        assertThat(root.getContainerId()).isEqualTo("Trip")
        assertThat(root.getProperty("name")).isEqualTo("Trip")
        assertThat(root.getProperty("description")).isEqualTo("Summer")
        assertThat(root.getProperties()).containsExactly("name", "description")

        val folders = root.getContainers().toList()
        assertThat(folders.map { it.getProperty("name") }).containsExactly("Day 1", "Day 2").inOrder()
        val day1 = folders.first()
        assertThat(day1.hasContainers()).isTrue()
        assertThat(day1.getContainers().single().getProperty("name")).isEqualTo("Morning")
        assertThat(day1.hasPlacemarks()).isTrue()
        assertThat(day1.getPlacemarks().single().getProperty("name")).isEqualTo("Hotel")
        assertThat(folders[1].hasContainers()).isFalse()
        assertThat(folders[1].hasPlacemarks()).isFalse()
    }

    @Test
    fun `document without a name is called Document`() {
        val layer = layerOf(kml("<Document/>"))

        val root = layer.getContainers().single()
        assertThat(root.getContainerId()).isEqualTo("Document")
        assertThat(root.hasProperties()).isFalse()
    }

    @Test
    fun `top level placemark without a document is a layer placemark`() {
        val layer = layerOf(
            kml("<Placemark><name>Solo</name><Point><coordinates>1.0,2.0</coordinates></Point></Placemark>")
        )

        assertThat(layer.hasContainers()).isFalse()
        val placemark = layer.getPlacemarks().single()
        assertThat(placemark.getProperty("name")).isEqualTo("Solo")
        assertThat(placemark.getGeometry()!!.getGeometryObject()).isEqualTo(LatLng(2.0, 1.0))
    }

    @Test
    fun `placemark properties include extended data`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Placemark>
                    <name>Peak</name>
                    <description>Highest point</description>
                    <ExtendedData>
                      <Data name="elevation"><value>8848</value></Data>
                      <Data name="range"><value>Himalaya</value></Data>
                    </ExtendedData>
                    <Point><coordinates>86.925,27.988,8848</coordinates></Point>
                  </Placemark>
                </Document>
                """
            )
        )

        val placemark = layer.getContainers().single().getPlacemarks().single()
        assertThat(placemark.getProperty("name")).isEqualTo("Peak")
        assertThat(placemark.getProperty("description")).isEqualTo("Highest point")
        assertThat(placemark.getProperty("elevation")).isEqualTo("8848")
        assertThat(placemark.getProperty("range")).isEqualTo("Himalaya")
    }

    @Test
    fun `shared style is resolved from the placemark style url`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Style id="red"><PolyStyle><color>ff0000ff</color></PolyStyle></Style>
                  <Placemark>
                    <styleUrl>#red</styleUrl>
                    <Polygon><outerBoundaryIs><LinearRing>
                      <coordinates>0,0 1,0 1,1 0,0</coordinates>
                    </LinearRing></outerBoundaryIs></Polygon>
                  </Placemark>
                </Document>
                """
            )
        )

        val root = layer.getContainers().single()
        assertThat(root.getStyle("red")).isNotNull()
        val placemark = root.getPlacemarks().single()
        assertThat(placemark.getStyleId()).isEqualTo("red")
        assertThat(placemark.getPolygonOptions()!!.fillColor).isEqualTo(Color.RED)
    }

    @Test
    fun `style map keeps the normal style url`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <StyleMap id="highlightable">
                    <Pair><key>normal</key><styleUrl>#red</styleUrl></Pair>
                    <Pair><key>highlight</key><styleUrl>#blue</styleUrl></Pair>
                  </StyleMap>
                </Document>
                """
            )
        )

        assertThat(layer.getContainers().single().getStyleIdFromMap("highlightable"))
            .isEqualTo("#red")
    }

    /**
     * Style maps store the normal styleUrl with its leading '#' ("#red"), but shared styles are
     * keyed by bare id ("red"). Resolving a placemark through a StyleMap therefore looks up
     * "#red", finds nothing and leaves the placemark unstyled. Google Earth exports put a
     * StyleMap on almost every placemark.
     */
    @Ignore("Known bug: styles referenced through a StyleMap are never applied")
    @Test
    fun `style map resolves to the normal shared style`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Style id="red"><PolyStyle><color>ff0000ff</color></PolyStyle></Style>
                  <Style id="blue"><PolyStyle><color>ffff0000</color></PolyStyle></Style>
                  <StyleMap id="highlightable">
                    <Pair><key>normal</key><styleUrl>#red</styleUrl></Pair>
                    <Pair><key>highlight</key><styleUrl>#blue</styleUrl></Pair>
                  </StyleMap>
                  <Placemark>
                    <styleUrl>#highlightable</styleUrl>
                    <Polygon><outerBoundaryIs><LinearRing>
                      <coordinates>0,0 1,0 1,1 0,0</coordinates>
                    </LinearRing></outerBoundaryIs></Polygon>
                  </Placemark>
                </Document>
                """
            )
        )

        val placemark = layer.getContainers().single().getPlacemarks().single()
        assertThat(placemark.getPolygonOptions()).isNotNull()
        assertThat(placemark.getPolygonOptions()!!.fillColor).isEqualTo(Color.RED)
    }

    @Test
    fun `inline style wins over the shared style`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Style id="red"><PolyStyle><color>ff0000ff</color></PolyStyle></Style>
                  <Placemark>
                    <styleUrl>#red</styleUrl>
                    <Style><PolyStyle><color>ff00ff00</color></PolyStyle></Style>
                    <Polygon><outerBoundaryIs><LinearRing>
                      <coordinates>0,0 1,0 1,1 0,0</coordinates>
                    </LinearRing></outerBoundaryIs></Polygon>
                  </Placemark>
                </Document>
                """
            )
        )

        val placemark = layer.getContainers().single().getPlacemarks().single()
        assertThat(placemark.getPolygonOptions()!!.fillColor).isEqualTo(Color.GREEN)
    }

    @Test
    fun `polygon keeps its outer and inner boundaries`() {
        val layer = layerOf(
            kml(
                """
                <Placemark>
                  <Polygon>
                    <outerBoundaryIs><LinearRing>
                      <coordinates>0,0 10,0 10,10 0,10 0,0</coordinates>
                    </LinearRing></outerBoundaryIs>
                    <innerBoundaryIs><LinearRing>
                      <coordinates>2,2 3,2 3,3 2,2</coordinates>
                    </LinearRing></innerBoundaryIs>
                    <innerBoundaryIs><LinearRing>
                      <coordinates>5,5 6,5 6,6 5,5</coordinates>
                    </LinearRing></innerBoundaryIs>
                  </Polygon>
                </Placemark>
                """
            )
        )

        val polygon = layer.getPlacemarks().single().getGeometry() as KmlPolygon
        assertThat(polygon.getGeometryType()).isEqualTo("Polygon")
        assertThat(polygon.getOuterBoundaryCoordinates()).hasSize(5)
        assertThat(polygon.getOuterBoundaryCoordinates()[1]).isEqualTo(LatLng(0.0, 10.0))
        assertThat(polygon.getInnerBoundaryCoordinates()).hasSize(2)
        assertThat(polygon.getInnerBoundaryCoordinates()[1][0]).isEqualTo(LatLng(5.0, 5.0))
        assertThat(polygon.getGeometryObject()).hasSize(3)
    }

    @Test
    fun `polygon without holes has an empty inner boundary list`() {
        val polygon = KmlPolygon(listOf(LatLng(0.0, 0.0), LatLng(1.0, 0.0), LatLng(0.0, 0.0)), null)

        assertThat(polygon.getInnerBoundaryCoordinates()).isEmpty()
        assertThat(polygon.getGeometryObject()).hasSize(1)
        assertThat(polygon.toString()).contains("outer coordinates=")
    }

    @Test
    fun `ground overlay keeps its image, bounds and draw order`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <GroundOverlay>
                    <name>Survey</name>
                    <drawOrder>3</drawOrder>
                    <Icon><href>https://example.com/overlay.png</href></Icon>
                    <LatLonBox>
                      <north>10</north><south>-10</south><east>20</east><west>-20</west>
                    </LatLonBox>
                  </GroundOverlay>
                </Document>
                """
            )
        )

        val overlay = layer.getContainers().single().getGroundOverlays().single()
        assertThat(overlay.getImageUrl()).isEqualTo("https://example.com/overlay.png")
        assertThat(overlay.getLatLngBox().southwest).isEqualTo(LatLng(-10.0, -20.0))
        assertThat(overlay.getLatLngBox().northeast).isEqualTo(LatLng(10.0, 20.0))
        assertThat(overlay.getProperty("name")).isEqualTo("Survey")
        assertThat(overlay.getGroundOverlayOptions().zIndex).isEqualTo(3f)
    }

    @Test
    fun `all placemarks are collected across nested folders`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Placemark><name>a</name><Point><coordinates>0,0</coordinates></Point></Placemark>
                  <Folder>
                    <Placemark><name>b</name><Point><coordinates>0,0</coordinates></Point></Placemark>
                    <Folder>
                      <Placemark><name>c</name><Point><coordinates>0,0</coordinates></Point></Placemark>
                    </Folder>
                  </Folder>
                </Document>
                """
            )
        )

        assertThat(layer.getAllPlacemarks().map { it.getProperty("name") }).containsExactly("a", "b", "c")
    }

    /**
     * Shared styles are indexed with `it.id!!`. A shared Style without an id is unusual but
     * valid KML, and it makes the KmlLayer constructor throw a NullPointerException.
     */
    @Ignore("Known bug: a shared Style without an id crashes KmlLayer")
    @Test
    fun `shared style without an id is ignored`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <Style><PolyStyle><color>ff0000ff</color></PolyStyle></Style>
                  <Placemark><Point><coordinates>0,0</coordinates></Point></Placemark>
                </Document>
                """
            )
        )

        assertThat(layer.getContainers().single().hasPlacemarks()).isTrue()
    }

    /**
     * Ground overlays read `latLonBox!!`. Overlays positioned with gx:LatLonQuad, which Google
     * Earth writes for rotated overlays, have no LatLonBox and make the constructor throw.
     */
    @Ignore("Known bug: a ground overlay without a LatLonBox crashes KmlLayer")
    @Test
    fun `ground overlay without a LatLonBox does not crash the layer`() {
        val layer = layerOf(
            kml(
                """
                <Document>
                  <GroundOverlay>
                    <Icon><href>https://example.com/overlay.png</href></Icon>
                    <gx:LatLonQuad>
                      <coordinates>0,0 1,0 1,1 0,1</coordinates>
                    </gx:LatLonQuad>
                  </GroundOverlay>
                  <Placemark><Point><coordinates>0,0</coordinates></Point></Placemark>
                </Document>
                """
            )
        )

        assertThat(layer.getContainers().single().hasPlacemarks()).isTrue()
    }
}
