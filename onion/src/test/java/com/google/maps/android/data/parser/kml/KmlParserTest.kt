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
        val geoData = parser.parse(stream)

        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)

        val feature = geoData.features[0]
        assertThat(feature.properties["name"]).isEqualTo("placemark1")
        assertThat(feature.properties["description"]).isEqualTo("basic placemark")
        assertThat(feature.geometry).isInstanceOf(Geometry.Point::class.java)

        val point = feature.geometry as Geometry.Point
        assertThat(point.lat).isWithin(0.0).of(0.0)
        assertThat(point.lon).isWithin(0.0).of(0.0)
    }

    @Test
    fun testAmuBasicPlacemarkPolygon() {
        val stream = File("src/test/resources/amu_basic_placemark.kml").inputStream()
        val geoData = parser.parse(stream)

        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)

        val feature = geoData.features[0]
        assertThat(feature.properties["name"]).isEqualTo("hollow box")
        assertThat(feature.geometry).isInstanceOf(Geometry.Polygon::class.java)

        val polygon = feature.geometry as Geometry.Polygon
        assertThat(polygon.shell.size).isEqualTo(5)
        assertThat(polygon.holes.size).isEqualTo(2)
    }

    @Test
    fun testAmuBallonGxPrefix() {
        val stream = File("src/test/resources/amu_ballon_gx_prefix.kml").inputStream()

        val kml = parser.parseAsKml(stream)

        //        val geoData = parser.parse(stream)
        //        assertThat(geoData).isNotNull()
        //        assertThat(geoData.features.isNotEmpty()).isTrue()

        // TODO: Test the gx:Tour when implemented
        // val feature = geoData.features[0]
        // assertThat(feature.properties["gx:balloonVisibility"]).isEqualTo("gx:balloonVisibility")

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
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        val feature = geoData.features[0]
        assertThat(feature.properties["description"].toString()).contains("This is a description with tags")
    }

    @Test
    fun testAmuDefaultBalloon() {
        val stream = File("src/test/resources/amu_default_balloon.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        val feature = geoData.features[0]
        assertThat(feature.properties["name"]).isEqualTo("default")
    }

    @Test
    fun testAmuDocumentNest() {
        val stream = File("src/test/resources/amu_document_nest.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        val feature = geoData.features[0]
        assertThat(feature.properties["name"]).isEqualTo("document placemark")
    }

    @Test
    fun testAmuDrawOrderGroundOverlay() {
        val stream = File("src/test/resources/amu_draw_order_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features).isEmpty() // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuEmptyHotspot() {
        val stream = File("src/test/resources/amu_empty_hotspot.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        assertThat(geoData.features[0].properties["name"]).isEqualTo("empty hotspot")
    }

    @Test
    fun testAmuExtendedData() {
        val stream = File("src/test/resources/amu_extended_data.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        val feature = geoData.features[0]
        assertThat(feature.properties["test_key"]).isEqualTo("test")
    }

    @Test
    fun testAmuGroundOverlayColor() {
        val stream = File("src/test/resources/amu_ground_overlay_color.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features).isEmpty() // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuGroundOverlay() {
        val stream = File("src/test/resources/amu_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features).isEmpty() // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuInlineStyle() {
        val stream = File("src/test/resources/amu_inline_style.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        // TODO: Assert style properties when implemented
    }

    @Test
    fun testAmuMultiGeometryPlacemarks() {
        val stream = File("src/test/resources/amu_multigeometry_placemarks.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1) // KML MultiGeometry is one feature with complex geometry
        assertThat(geoData.features[0].geometry).isInstanceOf(Geometry.GeometryCollection::class.java) // Currently mapping to LineString
    }

    @Test
    fun testAmuMultiplePlacemarks() {
        val stream = File("src/test/resources/amu_multiple_placemarks.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(2)
    }

    @Test
    fun testAmuNestedFolders() {
        val stream = File("src/test/resources/amu_nested_folders.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        assertThat(geoData.features[0].properties["name"]).isEqualTo("nested placemark")
    }

    @Test
    fun testAmuNestedMultiGeometry() {
        val stream = File("src/test/resources/amu_nested_multigeometry.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        assertThat(geoData.features[0].geometry).isInstanceOf(Geometry.GeometryCollection::class.java) // Currently mapping to LineString
    }

    @Test
    fun testAmuPolyStyleBooleanAlpha() {
        val stream = File("src/test/resources/amu_poly_style_boolean_alpha.kml").inputStream()
        val geoData = parser.parse(stream)
        assertThat(geoData).isNotNull()
        assertThat(geoData.features.size).isEqualTo(1)
        // TODO: Assert style properties when implemented
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
