package com.google.maps.android.data.parser.kml

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.data.parser.Geometry
import com.google.maps.android.data.parser.kml.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class KmlParserTest {

    private val parser = KmlParser()

    @Test
    fun testAmuBasicPlacemarkPoint() {
        val stream = File("src/test/resources/amu_basic_placemark_point.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.document!!.placemarks).isNotEmpty()

        with(kml.document.placemarks.first()) {
            assertThat(name).isEqualTo("placemark1")
            assertThat(description).isEqualTo("basic placemark")
            assertThat(point).isNotNull()

            with(point!!) {
                assertThat(latLngAlt).isNear(LatLngAlt(0.0, 0.0, 0.0))
            }
        }
    }

    @Test
    fun testAmuBasicPlacemarkPolygon() {
        val stream = File("src/test/resources/amu_basic_placemark.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()

        with(kml.placemark!!) {
            assertThat(name).isEqualTo("hollow box")
            assertThat(polygon).isNotNull()

            with(polygon!!) {
                assertThat(outerBoundaryIs.linearRing.coordinates.trim().split(Regex("\\s+"))).hasSize(5)
                assertThat(innerBoundaryIs).hasSize(2)
            }
        }
    }

    @Test
    fun testAmuBalloonGxPrefix() {
        val stream = File("src/test/resources/amu_balloon_gx_prefix.kml").inputStream()

        val kml = parser.parseAsKml(stream)

        // TODO: Test the gx:Tour when implemented

        val placemarks = kml.findByPlacemarksById("underwater1")
        assertThat(placemarks.size).isEqualTo(1)
        with(placemarks[0]) {
            assertThat(name).isEqualTo("Underwater off the California Coast")
            assertThat(
                description!!.simplify()
            ).isEqualTo("The tour begins near the Santa Cruz Canyon, off the coast of California, USA.".simplify())

            assertThat(point).isNotNull()
            with(point!!) {
                assertThat(coordinates).isEqualTo("-119.749531,33.715059,0")
                assertThat(latLngAlt).isEqualTo(LatLngAlt(33.715059, -119.749531, 0.0))
                assertThat(altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_SEA_FLOOR)
            }
        }

    }

    @Test
    fun testAmuBasicFolder() {
        val stream = File("src/test/resources/amu_basic_folder.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.folder).isNotNull()

        with(kml.folder!!) {
            assertThat(name).isEqualTo("Basic Folder")
            assertThat(placemarks).isNotEmpty()
            with(placemarks.first()) {
                assertThat(id).isEqualTo("mountainpin1")
                assertThat(name).isEqualTo("Pin on a mountaintop")
                assertThat(point?.latLngAlt).isNear(
                    LatLngAlt(-43.60505741890396, 170.1435558771009, 0.0)
                )
            }
        }
    }

    @Test
    fun testAmuCdata() {
        val stream = File("src/test/resources/amu_cdata.kml").inputStream()
        val kml = parser.parseAsKml(stream)
        

        assertThat(kml.document!!.placemarks).isNotEmpty()
        
        with(kml.document.placemarks.first()) {
            assertThat(description).isNull()
        }
    }

    @Test
    fun testAmuDefaultBalloon() {
        val stream = File("src/test/resources/amu_default_balloon.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()
        with(kml.placemark!!) {
            assertThat(name).isEqualTo("My office")
            assertThat(description).isEqualTo("This is the location of my office.")
        }
    }

    @Test
    fun testAmuDocumentNest() {
        val stream = File("src/test/resources/amu_document_nest.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        with(kml.document!!) {
            assertThat(name).isEqualTo("Document with XML id")
            assertThat(folders).hasSize(3)
            assertThat(folders[0].name).isEqualTo("Folder 1")
            assertThat(folders[1].name).isEqualTo("Folder 2")
            assertThat(folders[2].name).isEqualTo("Folder 3")
        }
    }

    @Test
    fun testAmuDrawOrderGroundOverlay() {
        val stream = File("src/test/resources/amu_draw_order_ground_overlay.kml").inputStream()

        val kml = parser.parseAsKml(stream)

        assertThat(kml.groundOverlay).isNotNull()
        with(kml.groundOverlay!!) {
            assertThat(name).isEqualTo("Ground overlay with draw order")
            assertThat(icon?.href).isEqualTo("http://ge-map-overlays.appspot.com/images/blue-triangle.png")
            assertThat(drawOrder).isEqualTo(99)
            assertThat(latLonBox).isNear(
                LatLonBox(
                    north = 37.85,
                    south = 37.82,
                    east = -122.47,
                    west = -122.51
                )
            )
        }
    }

    @Test
    fun testAmuEmptyHotspot() {
        val inputStream = File("src/test/resources/amu_empty_hotspot.kml").inputStream()
        val kml = parser.parseAsKml(inputStream)

        assertThat(kml.document).isNotNull()

        with(kml.document!!) {
            assertThat(placemarks).hasSize(2)
            assertThat(placemarks[0].styleUrl).isEqualTo("#emptyHotspot")
            assertThat(placemarks[1].styleUrl).isEqualTo("#validHotspot")
            assertThat(styles).hasSize(2)

            val emptyHotspotStyle = styles.find { it.id == "emptyHotspot" }
            assertThat(emptyHotspotStyle).isNotNull()
            with(emptyHotspotStyle!!) {
                assertThat(id).isEqualTo("emptyHotspot")
                assertThat(iconStyle).isNotNull()
                assertThat(iconStyle!!.hotSpot).isNotNull()
                with(iconStyle.hotSpot!!) {
                    assertThat(x).isEqualTo(0.5)
                    assertThat(y).isEqualTo(1.0)
                    assertThat(xunits).isEqualTo("fraction")
                    assertThat(yunits).isEqualTo("fraction")
                }
            }

            val validHotspotStyle = styles.find { it.id == "validHotspot" }
            assertThat(validHotspotStyle).isNotNull()
            with(validHotspotStyle!!) {
                assertThat(id).isEqualTo("validHotspot")
                assertThat(iconStyle).isNotNull()
                assertThat(iconStyle!!.hotSpot).isNotNull()
                with(iconStyle.hotSpot!!) {
                    assertThat(x).isEqualTo(0.5234)
                    assertThat(y).isEqualTo(0.5062)
                    assertThat(xunits).isEqualTo("fraction")
                    assertThat(yunits).isEqualTo("fraction")
                }
            }
        }
    }

    @Test
    fun testAmuExtendedData() {
        val stream = File("src/test/resources/amu_extended_data.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()
        with(kml.placemark!!) {
            assertThat(name).isEqualTo("Club house")
            assertThat(extendedData).isNotNull()
            assertThat(extendedData!!.data).hasSize(3)
            with(extendedData.data[0]) {
                assertThat(name).isEqualTo("holeNumber")
                assertThat(value).isEqualTo("1")
            }
            with(extendedData.data[1]) {
                assertThat(name).isEqualTo("holeYardage")
                assertThat(value).isEqualTo("234")
            }
            with(extendedData.data[2]) {
                assertThat(name).isEqualTo("holePar")
                assertThat(value).isEqualTo("4")
            }
        }
    }

    @Test
    fun testAmuGroundOverlayColor() {
        val stream = File("src/test/resources/amu_ground_overlay_color.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.groundOverlay).isNotNull()
        with(kml.groundOverlay!!) {
            assertThat(name).isEqualTo("Ground overlay with draw order")
            assertThat(color).isEqualTo("7f000000")
            assertThat(drawOrder).isEqualTo(99)
            assertThat(latLonBox).isNear(
                LatLonBox(
                    north = 37.91904192681665,
                    south = 37.46543388598137,
                    east = 15.35832653742206,
                    west = 14.60128369746704
                )
            )
        }
    }

    @Test
    fun testAmuGroundOverlay() {
        val stream = File("src/test/resources/amu_ground_overlay.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.groundOverlay).isNotNull()
        with(kml.groundOverlay!!) {
            assertThat(name).isEqualTo("Ground overlay with draw order")
            assertThat(drawOrder).isEqualTo(88)
            assertThat(icon).isNotNull()
            assertThat(icon!!.href).isEqualTo("http://www.colorcombos.com/images/colors/FF00FF.png")
            assertThat(latLonBox).isNear(
                LatLonBox(
                    north = 37.85,
                    south = 37.82,
                    east = -122.47,
                    west = -122.51
                )
            )
        }
    }

    @Test
    fun testAmuInlineStyle() {
        val stream = File("src/test/resources/amu_inline_style.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()
        with(kml.placemark!!) {
            assertThat(name).isEqualTo("Adelaide")
            assertThat(style).isNotNull()
            with(style!!) {
                assertThat(lineStyle).isNotNull()
                assertThat(lineStyle!!.color).isEqualTo("ff000000")
                assertThat(polyStyle).isNotNull()
                assertThat(polyStyle!!.color).isEqualTo("ffffffff")
            }
        }
    }

    @Test
    fun testAmuMultiGeometryPlacemarks() {
        val stream = File("src/test/resources/amu_multigeometry_placemarks.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()

        with(kml.placemark!!) {
            assertThat(name).isEqualTo("Placemark Test")
            assertThat(multiGeometry).isNotNull()
            with(multiGeometry!!) {
                assertThat(points).hasSize(1)
                assertThat(points.first()).isNear(LatLngAlt(40.4308, -3.6726))
                assertThat(lineStrings).hasSize(1)
                with(lineStrings[0]) {
                    assertThat(points).hasSize(2)
                    assertThat(points[0]).isNear(LatLngAlt(40.4364, -3.6655))
                    assertThat(points[1]).isNear(LatLngAlt(40.4308, -3.6726))
                }
                assertThat(polygons).hasSize(1)
                with(polygons[0]) {
                    with(outerBoundaryIs.linearRing) {
                        assertThat(points).hasSize(5)
                        assertThat(points).containsExactly(
                            LatLngAlt(37.818844, -122.366278, 30.0),
                            LatLngAlt(37.819267, -122.365248, 30.0),
                            LatLngAlt(37.819861, -122.365640, 30.0),
                            LatLngAlt(37.819429, -122.366669, 30.0),
                            LatLngAlt(37.818844, -122.366278, 30.0)
                        )
                    }
                    with(innerBoundaryIs[0].linearRing) {
                        assertThat(points).hasSize(5)
                        assertThat(points).containsExactly(
                            LatLngAlt(37.818977, -122.366212, 30.0),
                            LatLngAlt(37.819294, -122.365424, 30.0),
                            LatLngAlt(37.819731, -122.365704, 30.0),
                            LatLngAlt(37.819402, -122.366488, 30.0),
                            LatLngAlt(37.818977, -122.366212, 30.0)
                        )
                    }
                    with(innerBoundaryIs[1].linearRing) {
                        assertThat(points).hasSize(5)
                        assertThat(points).containsExactly(
                            LatLngAlt(37.818977, -122.366212, 42.0),
                            LatLngAlt(37.819294, -122.365424, 42.0),
                            LatLngAlt(37.819731, -122.365704, 42.0),
                            LatLngAlt(37.819402, -122.366488, 42.0),
                            LatLngAlt(37.818977, -122.366212, 42.0)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testAmuMultiplePlacemarks() {
        val stream = File("src/test/resources/amu_multiple_placemarks.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.document).isNotNull()
        with(kml.document!!) {
            assertThat(name).isEqualTo("My Golf Course Example")
            assertThat(placemarks).hasSize(2)

            with(placemarks[0]) {
                assertThat(name).isEqualTo("Club house")
                assertThat(point?.latLngAlt).isNear(LatLngAlt(33.5043, -111.956))
                assertThat(extendedData).isNotNull()
                with(extendedData!!.data[0]) {
                    assertThat(name).isEqualTo("holeNumber")
                    assertThat(value).isEqualTo("1")
                }
                with(extendedData.data[1]) {
                    assertThat(name).isEqualTo("holeYardage")
                    assertThat(value).isEqualTo("234")
                }
                with(extendedData.data[2]) {
                    assertThat(name).isEqualTo("holePar")
                    assertThat(value).isEqualTo("4")
                }
            }
            with(placemarks[1]) {
                assertThat(name).isEqualTo("By the lake")
                assertThat(point?.latLngAlt).isNear(LatLngAlt(33.5024, -111.95))
                assertThat(extendedData).isNotNull()
                with(extendedData!!.data[0]) {
                    assertThat(name).isEqualTo("holeNumber")
                    assertThat(value).isEqualTo("5")
                }
                with(extendedData.data[1]) {
                    assertThat(name).isEqualTo("holeYardage")
                    assertThat(value).isEqualTo("523")
                }
                with(extendedData.data[2]) {
                    assertThat(name).isEqualTo("holePar")
                    assertThat(value).isEqualTo("5")
                }
            }
        }
    }

    @Test
    fun testAmuNestedFolders() {
        val stream = File("src/test/resources/amu_nested_folders.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.folder).isNotNull()
        with(kml.folder!!) {
            assertThat(folders).hasSize(2)
            assertThat(folders[0].folders).isEmpty()
            assertThat(folders[1].folders).isEmpty()
            assertThat(placemarks).isEmpty()
        }
    }

    @Test
    fun testAmuNestedMultiGeometry() {
        val stream = File("src/test/resources/amu_nested_multigeometry.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.placemark).isNotNull()
        with(kml.placemark!!) {
            assertThat(name).isEqualTo("multiPointLine")
            assertThat(description).isEqualTo("Nested MultiGeometry structure")
            assertThat(multiGeometry).isNotNull()
            with(multiGeometry!!) {
                assertThat(points).hasSize(1)
                assertThat(points.first()).isNear(LatLngAlt(42.07, -71.0))
                assertThat(lineStrings).hasSize(1)
                with(lineStrings.first()) {
                    assertThat(points).hasSize(3)
                    assertThat(points[0]).isNear(LatLngAlt(42.05, -71.0))
                    assertThat(points[1]).isNear(LatLngAlt(42.07, -71.0))
                    assertThat(points[2]).isNear(LatLngAlt(42.07, -71.05))
                }
                assertThat(multiGeometries).hasSize(1)
                with(multiGeometries.first()) {
                    assertThat(points).hasSize(1)
                    assertThat(points.first()).isNear(LatLngAlt(42.1, -71.2))
                    assertThat(lineStrings).hasSize(1)
                    with(lineStrings.first()) {
                        assertThat(points).hasSize(3)
                        assertThat(points[0]).isNear(LatLngAlt(42.1, -71.1))
                        assertThat(points[1]).isNear(LatLngAlt(42.1, -71.2))
                        assertThat(points[2]).isNear(LatLngAlt(42.0833, -71.2))
                    }
                }
            }
        }
    }

    @Test
    fun testAmuPolyStyleBooleanAlpha() {
        val stream = File("src/test/resources/amu_poly_style_boolean_alpha.kml").inputStream()
        val kml = parser.parseAsKml(stream)

        assertThat(kml.document).isNotNull()
        with(kml.document!!) {
            assertThat(styles).hasSize(1)
            val style = styles.find { it.id == "fireadvisory" }
            assertThat(style).isNotNull()
            with(style!!) {
                assertThat(id).isEqualTo("fireadvisory")
                assertThat(polyStyle).isNotNull()
                with(polyStyle!!) {
                    assertThat(color).isEqualTo("6400E9FF")
                    assertThat(fill).isTrue()
                    assertThat(outline).isFalse()
                }
            }
            assertThat(placemarks).hasSize(1)
            with(placemarks.first()) {
                assertThat(styleUrl).isEqualTo("#fireadvisory")
                assertThat(name).isEqualTo("Lamont County")
                with(multiGeometry!!.polygons[0]) {
                    assertThat(extrude).isFalse()
                    assertThat(altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)

                    // Outer Boundary
                    with(outerBoundaryIs.linearRing) {
                        assertThat(points).hasSize(204)
                        assertThat(points.take(3)).containsExactly(
                            LatLngAlt(53.9768295288086, -112.160865783691, 0.0),
                            LatLngAlt(53.9792251586914, -112.167808532715, 0.0),
                            LatLngAlt(53.9850082397461, -112.178894042969, 0.0)
                        )
                        assertThat(points.takeLast(3)).containsExactly(
                            LatLngAlt(53.8604164123535, -112.195640563965, 0.0),
                            LatLngAlt(53.8604316711426, -112.183418273926, 0.0),
                            LatLngAlt(53.9768295288086, -112.160865783691, 0.0)
                        )
                    }

                    // Inner Boundary 1
                    with(innerBoundaryIs[0].linearRing) {
                        assertThat(points).hasSize(16)
                        assertThat(points.take(3)).containsExactly(
                            LatLngAlt(53.8764839172363, -112.3466796875, 0.0),
                            LatLngAlt(53.8768043518066, -112.34423828125, 0.0),
                            LatLngAlt(53.8844604492188, -112.344253540039, 0.0)
                        )
                        assertThat(points.takeLast(3)).containsExactly(
                            LatLngAlt(53.8750610351563, -112.34423828125, 0.0),
                            LatLngAlt(53.8758239746094, -112.34423828125, 0.0),
                            LatLngAlt(53.8764839172363, -112.3466796875, 0.0)
                        )
                    }

                    // Inner Boundary 2
                    with(innerBoundaryIs[1].linearRing) {
                        assertThat(points).hasSize(11)
                        assertThat(points.take(3)).containsExactly(
                            LatLngAlt(53.7740745544434, -112.789962768555, 0.0),
                            LatLngAlt(53.7740859985352, -112.752563476563, 0.0),
                            LatLngAlt(53.7379913330078, -112.752510070801, 0.0)
                        )
                        assertThat(points.takeLast(3)).containsExactly(
                            LatLngAlt(53.7599258422852, -112.801826477051, 0.0),
                            LatLngAlt(53.7670745849609, -112.801948547363, 0.0),
                            LatLngAlt(53.7740745544434, -112.789962768555, 0.0)
                        )
                    }

                    // Inner Boundary 3
                    with(innerBoundaryIs[2].linearRing) {
                        assertThat(points).hasSize(14)
                        assertThat(points.take(3)).containsExactly(
                            LatLngAlt(53.6872520446777, -112.668632507324, 0.0),
                            LatLngAlt(53.6872940063477, -112.656639099121, 0.0),
                            LatLngAlt(53.6945533752441, -112.656684875488, 0.0)
                        )
                        assertThat(points.takeLast(3)).containsExactly(
                            LatLngAlt(53.6725959777832, -112.65657043457, 0.0),
                            LatLngAlt(53.6798553466797, -112.656600952148, 0.0),
                            LatLngAlt(53.6872520446777, -112.668632507324, 0.0)
                        )
                    }

                    // Inner Boundary 4
                    with(innerBoundaryIs[3].linearRing) {
                        assertThat(points).hasSize(29)
                        assertThat(points.take(3)).containsExactly(
                            LatLngAlt(53.5984230041504, -112.36075592041, 0.0),
                            LatLngAlt(53.599365234375, -112.359878540039, 0.0),
                            LatLngAlt(53.5995483398438, -112.360336303711, 0.0)
                        )
                        assertThat(points.takeLast(3)).containsExactly(
                            LatLngAlt(53.5936508178711, -112.358947753906, 0.0),
                            LatLngAlt(53.5965805053711, -112.358947753906, 0.0),
                            LatLngAlt(53.5984230041504, -112.36075592041, 0.0)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testAmuPolyStyleBooleanNumeric() {
        val stream = File("src/test/resources/amu_poly_style_boolean_numeric.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        // TODO: Assert style properties when implemented
    }

    @Test
    fun testAmuUnknownFolder() {
        val stream = File("src/test/resources/amu_unknown_folder.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        assertThat(geoData.features[0].properties["name"]).isEqualTo("unknown placemark")
    }

    @Test
    fun testAmuUnsupported() {
        val stream = File("src/test/resources/amu_unsupported.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features).isEmpty() // Expecting nothing to be parsed from unsupported elements
    }

    @Test
    fun testAmuVisibilityGroundOverlay() {
        val stream = File("src/test/resources/amu_visibility_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features).isEmpty() // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuWrongNotExistCoordinates() {
        val stream = File("src/test/resources/amu_wrong_not_exist_coordinates.kml").inputStream()
        assertFailsWith<Exception> {
            parser.parse(stream)
        }
    }

    @Test
    fun testAmuWrongNotExistLatitudeCoordinates() {
        val stream = File("src/test/resources/amu_wrong_not_exist_latitude_coordinates.kml").inputStream()
        assertFailsWith<Exception> {
            parser.parse(stream)
        }
    }

    // Add more specific tests for properties, styles, and geometry details as the parser evolves
}
