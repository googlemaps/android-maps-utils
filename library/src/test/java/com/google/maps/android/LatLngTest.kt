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
 *
 */

package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class LatLngTest {
    private val earthRadius = 6371009.0

    @Test
    fun `test that latLng can be destructured`() {
        val latLng = LatLng(2.0, 3.0)
        val (lat, lng) = latLng
        assertThat(lat).isWithin(1e-6).of(2.0)
        assertThat(lng).isWithin(1e-6).of(3.0)
    }

    @Test
    fun `single LatLng encoding`() {
        val line = listOf(LatLng(1.0, 2.0))
        assertThat(line.latLngListEncode()).isEqualTo("_ibE_seK")
    }

    @Test
    fun `single LatLng decoding`() {
        val lineEncoded = "_yfyF_ocsF"
        val line = lineEncoded.toLatLngList()
        assertThat(line.first()).isEqualTo(LatLng(41.0, 40.0))
    }

    @Test
    fun `closed polygon true`() {
        val latLngList = listOf(LatLng(1.0, 2.0), LatLng(3.0, 4.0), LatLng(1.0, 2.0))
        assertThat(latLngList.isClosedPolygon()).isTrue()
    }

    @Test
    fun `closed polygon false`() {
        val latLngList = listOf(LatLng(1.0, 2.0), LatLng(3.0, 4.0))
        assertThat(latLngList.isClosedPolygon()).isFalse()
    }

    @Test
    fun `simplify endpoints are still equal`() {
        val lineEncoded = "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@"
        val line = lineEncoded.toLatLngList()
        val simplifiedLine = line.simplify(tolerance = 5.0)
        assertThat(simplifiedLine).hasSize(20)
        assertThat(simplifiedLine.first()).isEqualTo(line.first())
        assertThat(simplifiedLine.last()).isEqualTo(line.last())
    }

    @Test
    fun `heading is accurate`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        assertThat(up.sphericalHeading(down)).isWithin(1e-6).of(-180.0)
    }

    @Test
    fun `withOffset is accurate`() {
        val up = LatLng(90.0, 135.0)
        val down = up.withSphericalOffset(earthRadius, 180.0)
        assertThat(down.latitude).isWithin(1e-6).of(32.704220486917684)
        assertThat(down.longitude).isWithin(1e-6).of(-135.0)
    }

    @Test
    fun `computeOffsetOrigin is accurate`() {
        val front = LatLng(0.0, 0.0)
        assertThat(front.computeSphericalOffsetOrigin(0.0, 0.0)).isEqualTo(front)

        val result = LatLng(0.0, 45.0).computeSphericalOffsetOrigin(
            distance = Math.PI * earthRadius / 4.0,
            heading = 90.0
        )!!
        assertThat(result.latitude).isWithin(1e-6).of(0.0)
        assertThat(result.longitude).isWithin(1e-6).of(0.0)
    }

    @Test
    fun `compute interpolation`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)

        val zeroFraction = up.withSphericalLinearInterpolation(down, 0.0)
        assertThat(zeroFraction.latitude).isWithin(1e-6).of(90.0)
        assertThat(zeroFraction.longitude).isWithin(1e-6).of(0.0)

        val halfFraction = up.withSphericalLinearInterpolation(down, 0.5)
        assertThat(halfFraction.latitude).isWithin(1e-6).of(0.0)
        assertThat(halfFraction.longitude).isWithin(1e-6).of(0.0)

        val oneFraction = up.withSphericalLinearInterpolation(down, 1.0)
        assertThat(oneFraction.latitude).isWithin(1e-6).of(-90.0)
        assertThat(oneFraction.longitude).isWithin(1e-6).of(0.0)
    }

    @Test
    fun `compute spherical distance`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        assertThat(up.sphericalDistance(down)).isWithin(1e-6).of(Math.PI * earthRadius)
    }

    @Test
    fun `validate spherical path length`() {
        assertThat(emptyList<LatLng>().sphericalPathLength()).isWithin(1e-6).of(0.0)

        val latLngs = listOf(LatLng(0.0, 0.0), LatLng(0.1, 0.1))
        val expectation = earthRadius * Math.sqrt(2.0) * Math.toRadians(0.1)
        assertThat(latLngs.sphericalPathLength()).isWithin(1e-1).of(expectation)
    }

    @Test
    fun `validate spherical polygon area`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        val right = LatLng(0.0, 90.0)
        val polygon = listOf(up, down, right, up)
        assertThat(polygon.sphericalPolygonArea()).isWithin(1e-6).of(1.2751647824926386E14)
        println(polygon.sphericalPolygonSignedArea())
    }

    @Test
    fun `validate signed spherical polygon area`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        val right = LatLng(0.0, 90.0)
        val polygon = listOf(up, down, right, up)
        val reversedPolygon = listOf(up, right, down, up)
        assertThat(reversedPolygon.sphericalPolygonSignedArea())
            .isWithin(1e-6)
            .of(-polygon.sphericalPolygonSignedArea())
    }

    @Test
    fun `contains location evaluates to true`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(-90.0, 45.0))
        assertThat(latLngList.containsLocation(LatLng(30.0, 45.0), geodesic = true)).isTrue()
    }

    @Test
    fun `contains location evaluates to false`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(-90.0, 45.0))
        assertThat(latLngList.containsLocation(LatLng(-30.0, 45.0), geodesic = true)).isFalse()
    }

    @Test
    fun `isOnEdge location evaluates to true`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(-90.0, 45.0))
        assertThat(latLngList.isOnEdge(LatLng(0.0, 45.0), geodesic = true)).isTrue()
    }

    @Test
    fun `isOnEdge location evaluates to false`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(-90.0, 45.0))
        assertThat(latLngList.isOnEdge(LatLng(0.0, -45.0), geodesic = true)).isFalse()
    }

    @Test
    fun `isLocationOnPath location evaluates to true`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(0.0, 180.0))
        assertThat(latLngList.isLocationOnPath(LatLng(0.0, 45.0), geodesic = true)).isTrue()
    }

    @Test
    fun `isLocationOnPath location evaluates to false`() {
        val latLngList = listOf(LatLng(0.0, 0.0), LatLng(0.0, 90.0), LatLng(0.0, 180.0))
        assertThat(latLngList.isLocationOnPath(LatLng(0.0, -45.0), geodesic = true)).isFalse()
    }
}
