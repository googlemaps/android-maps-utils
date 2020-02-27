package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.projection.SphericalMercatorProjection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LatLngTest {

    private val earthRadius = 6371009.0
    private lateinit var projection: SphericalMercatorProjection

    @Suppress("DEPRECATION")
    @Before
    fun setUp() {
        projection = mock {
            on { toPoint(any()) } doReturn com.google.maps.android.projection.Point(0.0, 0.0)
        }
    }

    @Test
    fun `single LatLng encoding`() {
        val line = listOf(LatLng(1.0, 2.0))
        assertEquals("_ibE_seK", line.latLngListEncode())
    }

    @Test
    fun `single LatLng decoding`() {
        val lineEncoded = "_yfyF_ocsF"
        val line = lineEncoded.toLatLngList()
        assertEquals(LatLng(41.0, 40.0), line.first())
    }

    @Test
    fun `closed polygon true`() {
        val latLngList = listOf(LatLng(1.0, 2.0), LatLng(3.0, 4.0), LatLng(1.0, 2.0))
        assertTrue(latLngList.isClosedPolygon())
    }

    @Test
    fun `closed polygon false`() {
        val latLngList = listOf(LatLng(1.0, 2.0), LatLng(3.0, 4.0))
        assertFalse(latLngList.isClosedPolygon())
    }

    @Test
    fun `simplify endpoints are still equal`() {
        val lineEncoded = "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@"
        val line = lineEncoded.toLatLngList()
        val simplifiedLine = line.simplify(tolerance = 5.0)
        assertEquals(20, simplifiedLine.size)
        assertEquals(line.first(), simplifiedLine.first())
        assertEquals(line.last(), simplifiedLine.last())
    }

    @Test
    fun `heading is accurate`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        assertEquals(-180.0, up.sphericalHeading(down), 1e-6)
    }

    @Test
    fun `withOffset is accurate`() {
        val up = LatLng(90.0, 135.0)
        val down = up.withSphericalOffset(earthRadius, 180.0)
        assertEquals(32.704220486917684, down.latitude, 1e-6)
        assertEquals(-135.0, down.longitude, 1e-6)
    }

    @Test
    fun `computeOffsetOrigin is accurate`() {
        val front = LatLng(0.0, 0.0)
        assertEquals(front, front.computeSphericalOffsetOrigin(0.0, 0.0))

        val result = LatLng(0.0, 45.0).computeSphericalOffsetOrigin(
            distance = Math.PI * earthRadius / 4.0,
            heading = 90.0
        )!!
        assertEquals(0.0, result.latitude, 1e-6)
        assertEquals(0.0, result.longitude, 1e-6)
    }

    @Test
    fun `compute interpolation`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)

        val zeroFraction = up.withSphericalLinearInterpolation(down, 0.0)
        assertEquals(90.0, zeroFraction.latitude, 1e-6)
        assertEquals(0.0, zeroFraction.longitude, 1e-6)

        val halfFraction = up.withSphericalLinearInterpolation(down, 0.5)
        assertEquals(0.0, halfFraction.latitude, 1e-6)
        assertEquals(0.0, halfFraction.longitude, 1e-6)

        val oneFraction = up.withSphericalLinearInterpolation(down, 1.0)
        assertEquals(-90.0, oneFraction.latitude, 1e-6)
        assertEquals(0.0, oneFraction.longitude, 1e-6)
    }

    @Test
    fun `compute spherical distance`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        assertEquals(Math.PI * earthRadius, up.sphericalDistance(down), 1e-6)
    }

    @Test
    fun `validate spherical path length`() {
        assertEquals(0.0, emptyList<LatLng>().sphericalPathLength(), 1e-6)

        val latLngs = listOf(LatLng(0.0, 0.0), LatLng(0.1, 0.1))
        val expectation = earthRadius * Math.sqrt(2.0) * Math.toRadians(0.1)
        assertEquals(expectation, latLngs.sphericalPathLength(), 1e-1)
    }

    @Test
    fun `validate spherical polygon area`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        val right = LatLng(0.0, 90.0)
        val polygon = listOf(up, down, right, up)
        assertEquals(1.2751647824926386E14, polygon.sphericalPolygonArea(), 1e-6)
        println(polygon.sphericalPolygonSignedArea())
    }

    @Test
    fun `validate signed spherical polygon area`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        val right = LatLng(0.0, 90.0)
        val polygon = listOf(up, down, right, up)
        val reversedPolygon = listOf(up, right, down, up)
        assertEquals(
            -polygon.sphericalPolygonSignedArea(),
            reversedPolygon.sphericalPolygonSignedArea(),
            1e-6
        )
    }

    @Test
    fun `validate toPoint passthrough`() {
        val latLng = LatLng(1.0, 2.0)
        latLng.toPoint(projection)
        verify(projection).toPoint(latLng)
    }
}