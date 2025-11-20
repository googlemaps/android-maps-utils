package com.google.maps.android.data.parser.kml

import com.google.maps.android.data.parser.Geometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class KmlParserTest {

    private val parser = KmlParser()

    @Test
    fun testAmuBasicPlacemarkPoint() {
        val stream = File("src/test/resources/amu_basic_placemark_point.kml").inputStream()
        val geoData = parser.parse(stream)

        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)

        val feature = geoData.features[0]
        assertEquals("placemark1", feature.properties["name"])
        assertEquals("basic placemark", feature.properties["description"])
        assertTrue(feature.geometry is Geometry.Point)

        val point = feature.geometry as Geometry.Point
        assertEquals(0.0, point.lat, 0.0)
        assertEquals(0.0, point.lon, 0.0)
    }

    @Test
    fun testAmuBasicPlacemarkPolygon() {
        val stream = File("src/test/resources/amu_basic_placemark.kml").inputStream()
        val geoData = parser.parse(stream)

        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)

        val feature = geoData.features[0]
        assertEquals("hollow box", feature.properties["name"])
        assertTrue(feature.geometry is Geometry.Polygon)

        val polygon = feature.geometry as Geometry.Polygon
        assertEquals(5, polygon.shell.size)
        assertEquals(2, polygon.holes.size)
    }

    @Test
    fun testAmuBallonGxPrefix() {
        val stream = File("src/test/resources/amu_ballon_gx_prefix.kml").inputStream()

        val kml = parser.parseAsKml(stream)

        //        val geoData = parser.parse(stream)
        //        assertNotNull(geoData)
        //        assertTrue(geoData.features.isNotEmpty())

        // TODO: Test the gx:Tour when implemented
        // val feature = geoData.features[0]
        // assertEquals("gx:balloonVisibility", feature.properties["gx:balloonVisibility"])

        val placemarks = kml.findByPlacemarksById("underwater1")
        assertEquals(1, placemarks.size)
        with(placemarks[0]) {
            assertEquals("Underwater off the California Coast", name)
            assertEquals(
                "The tour begins near the Santa Cruz Canyon, off the coast of California, USA.".simplify(),
                description!!.simplify()
            )

            assertNotNull(point)
            with(point!!) {
                assertEquals("-119.749531,33.715059,0", coordinates)
                assertEquals(LatLngAlt(33.715059, -119.749531, 0.0), latLngAlt)
                assertEquals(AltitudeMode.CLAMP_TO_SEA_FLOOR, altitudeMode)
            }
        }

    }

    @Test
    fun testAmuBasicFolder() {
        val stream = File("src/test/resources/amu_basic_folder.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(2, geoData.features.size)
        assertEquals("Basic Folder", geoData.features[0].properties["name"])
    }

    @Test
    fun testAmuCdata() {
        val stream = File("src/test/resources/amu_cdata.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertTrue(feature.properties["description"].toString().contains("This is a description with tags"))
    }

    @Test
    fun testAmuDefaultBalloon() {
        val stream = File("src/test/resources/amu_default_balloon.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertEquals("default", feature.properties["name"])
    }

    @Test
    fun testAmuDocumentNest() {
        val stream = File("src/test/resources/amu_document_nest.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertEquals("document placemark", feature.properties["name"])
    }

    @Test
    fun testAmuDrawOrderGroundOverlay() {
        val stream = File("src/test/resources/amu_draw_order_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertTrue(geoData.features.isEmpty()) // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuEmptyHotspot() {
        val stream = File("src/test/resources/amu_empty_hotspot.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        assertEquals("empty hotspot", geoData.features[0].properties["name"])
    }

    @Test
    fun testAmuExtendedData() {
        val stream = File("src/test/resources/amu_extended_data.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertEquals("test", feature.properties["test_key"])
    }

    @Test
    fun testAmuGroundOverlayColor() {
        val stream = File("src/test/resources/amu_ground_overlay_color.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertTrue(geoData.features.isEmpty()) // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuGroundOverlay() {
        val stream = File("src/test/resources/amu_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertTrue(geoData.features.isEmpty()) // GroundOverlay not yet parsed as Feature
    }

    @Test
    fun testAmuInlineStyle() {
        val stream = File("src/test/resources/amu_inline_style.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        // TODO: Assert style properties when implemented
    }

    @Test
    fun testAmuMultiGeometryPlacemarks() {
        val stream = File("src/test/resources/amu_multigeometry_placemarks.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size) // KML MultiGeometry is one feature with complex geometry
        assertTrue(geoData.features[0].geometry is Geometry.GeometryCollection) // Currently mapping to LineString
    }

    @Test
    fun testAmuMultiplePlacemarks() {
        val stream = File("src/test/resources/amu_multiple_placemarks.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(2, geoData.features.size)
    }

    @Test
    fun testAmuNestedFolders() {
        val stream = File("src/test/resources/amu_nested_folders.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        assertEquals("nested placemark", geoData.features[0].properties["name"])
    }

    @Test
    fun testAmuNestedMultiGeometry() {
        val stream = File("src/test/resources/amu_nested_multigeometry.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
assertTrue(geoData.features[0].geometry is Geometry.GeometryCollection) // Currently mapping to LineString
    }

    @Test
    fun testAmuPolyStyleBooleanAlpha() {
        val stream = File("src/test/resources/amu_poly_style_boolean_alpha.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        // TODO: Assert style properties when implemented
    }

    @Test
    fun testAmuPolyStyleBooleanNumeric() {
        val stream = File("src/test/resources/amu_poly_style_boolean_numeric.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        // TODO: Assert style properties when implemented
    }

    @Test
    fun testAmuUnknownFolder() {
        val stream = File("src/test/resources/amu_unknown_folder.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertEquals(1, geoData.features.size)
        assertEquals("unknown placemark", geoData.features[0].properties["name"])
    }

    @Test
    fun testAmuUnsupported() {
        val stream = File("src/test/resources/amu_unsupported.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertTrue(geoData.features.isEmpty()) // Expecting nothing to be parsed from unsupported elements
    }

    @Test
    fun testAmuVisibilityGroundOverlay() {
        val stream = File("src/test/resources/amu_visibility_ground_overlay.kml").inputStream()
        val geoData = parser.parse(stream)
        assertNotNull(geoData)
        assertTrue(geoData.features.isEmpty()) // GroundOverlay not yet parsed as Feature
    }

    @Test(expected = Exception::class)
    fun testAmuWrongNotExistCoordinates() {
        val stream = File("src/test/resources/amu_wrong_not_exist_coordinates.kml").inputStream()
        parser.parse(stream)
    }

    @Test(expected = Exception::class)
    fun testAmuWrongNotExistLatitudeCoordinates() {
        val stream = File("src/test/resources/amu_wrong_not_exist_latitude_coordinates.kml").inputStream()
        parser.parse(stream)
    }

    // Add more specific tests for properties, styles, and geometry details as the parser evolves
}
