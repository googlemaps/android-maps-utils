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
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.SquareCap
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Observable

/**
 * The GeoJSON styles notify their observers on every change (GeoJsonLayer redraws features from
 * those notifications) and convert to the Maps SDK options used to draw each geometry.
 */
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class GeoJsonStylesTest {
    private fun Observable.countNotifications(): () -> Int {
        var count = 0
        addObserver { _, _ -> count++ }
        return { count }
    }

    @Test
    fun `polygon style setters notify observers once each`() {
        val style = GeoJsonPolygonStyle()
        val notifications = style.countNotifications()

        style.fillColor = 0x7F00FF00
        style.setGeodesic(true)
        style.setStrokeColor(0xFFFF0000.toInt())
        style.setStrokeJointType(JointType.ROUND)
        style.setStrokePattern(listOf(Dash(10f), Gap(5f)))
        style.setStrokeWidth(3f)
        style.setZIndex(2f)
        style.setVisible(false)
        style.setClickable(false)

        assertThat(notifications()).isEqualTo(9)
    }

    @Test
    fun `polygon options carry every polygon style property`() {
        val style = GeoJsonPolygonStyle()
        style.fillColor = 0x7F00FF00
        style.setGeodesic(true)
        style.setStrokeColor(0xFFFF0000.toInt())
        style.setStrokeJointType(JointType.BEVEL)
        style.setStrokePattern(listOf(Dash(10f), Gap(5f)))
        style.setStrokeWidth(3f)
        style.setZIndex(2f)
        style.setVisible(false)
        style.setClickable(false)

        val options = style.toPolygonOptions()

        assertThat(options.fillColor).isEqualTo(0x7F00FF00)
        assertThat(options.isGeodesic).isTrue()
        assertThat(options.strokeColor).isEqualTo(0xFFFF0000.toInt())
        assertThat(options.strokeJointType).isEqualTo(JointType.BEVEL)
        assertThat(options.strokePattern).containsExactly(Dash(10f), Gap(5f)).inOrder()
        assertThat(options.strokeWidth).isEqualTo(3f)
        assertThat(options.zIndex).isEqualTo(2f)
        assertThat(options.isVisible).isFalse()
        assertThat(options.isClickable).isFalse()
        assertThat(style.toString()).contains("fill color=${0x7F00FF00}")
    }

    @Test
    fun `polygon style applies to polygons and collections`() {
        assertThat(GeoJsonPolygonStyle().getGeometryType())
            .asList()
            .containsExactly("Polygon", "MultiPolygon", "GeometryCollection")
    }

    @Test
    fun `polygon style defaults are clickable and visible`() {
        val style = GeoJsonPolygonStyle()

        assertThat(style.isClickable()).isTrue()
        assertThat(style.isVisible()).isTrue()
        assertThat(style.isGeodesic()).isFalse()
    }

    @Test
    fun `line string style setters notify observers once each`() {
        val style = GeoJsonLineStringStyle()
        val notifications = style.countNotifications()

        style.color = 0xFF0000FF.toInt()
        style.setClickable(false)
        style.setGeodesic(true)
        style.setWidth(6f)
        style.setZIndex(1f)
        style.setVisible(false)
        style.setPattern(listOf(Dash(4f)))
        style.setStartCap(RoundCap())
        style.setEndCap(SquareCap())

        assertThat(notifications()).isEqualTo(9)
    }

    @Test
    fun `polyline options carry the line string style properties`() {
        val style = GeoJsonLineStringStyle()
        style.color = 0xFF0000FF.toInt()
        style.setClickable(false)
        style.setGeodesic(true)
        style.setWidth(6f)
        style.setZIndex(1f)
        style.setVisible(false)

        val options = style.toPolylineOptions()

        assertThat(options.color).isEqualTo(0xFF0000FF.toInt())
        assertThat(options.isClickable).isFalse()
        assertThat(options.isGeodesic).isTrue()
        assertThat(options.width).isEqualTo(6f)
        assertThat(options.zIndex).isEqualTo(1f)
        assertThat(options.isVisible).isFalse()
        assertThat(style.toString()).contains("width=6.0")
    }

    /**
     * Inside `PolylineOptions().apply { }`, toPolylineOptions calls getPattern(), getStartCap()
     * and getEndCap() unqualified, which resolve to the new PolylineOptions' own getters rather
     * than the style's. The pattern and caps of a GeoJSON line style are therefore never drawn.
     */
    @Ignore("Known bug: toPolylineOptions drops the pattern and the start and end caps")
    @Test
    fun `polyline options carry the pattern and caps`() {
        val style = GeoJsonLineStringStyle()
        style.setPattern(listOf(Dash(4f), Gap(2f)))
        style.setStartCap(RoundCap())
        style.setEndCap(SquareCap())

        val options = style.toPolylineOptions()

        assertThat(options.startCap).isInstanceOf(RoundCap::class.java)
        assertThat(options.endCap).isInstanceOf(SquareCap::class.java)
        assertThat(options.pattern).containsExactly(Dash(4f), Gap(2f)).inOrder()
    }

    @Test
    fun `line string style applies to lines and collections`() {
        assertThat(GeoJsonLineStringStyle().getGeometryType())
            .asList()
            .containsExactly("LineString", "MultiLineString", "GeometryCollection")
    }

    @Test
    fun `point style setters notify observers once each`() {
        val style = GeoJsonPointStyle()
        val notifications = style.countNotifications()

        style.setAlpha(0.5f)
        style.setAnchor(0.2f, 0.8f)
        style.setDraggable(true)
        style.setFlat(true)
        style.setIcon(mockk<BitmapDescriptor>())
        style.setInfoWindowAnchor(0.5f, 0.1f)
        style.setRotation(30f)
        style.setSnippet("snippet")
        style.setTitle("title")
        style.setVisible(false)
        style.setZIndex(3f)

        assertThat(notifications()).isEqualTo(11)
    }

    @Test
    fun `marker options carry every point style property`() {
        val icon = mockk<BitmapDescriptor>()
        val style = GeoJsonPointStyle()
        style.setAlpha(0.5f)
        style.setAnchor(0.2f, 0.8f)
        style.setDraggable(true)
        style.setFlat(true)
        style.setIcon(icon)
        style.setInfoWindowAnchor(0.5f, 0.1f)
        style.setRotation(30f)
        style.setSnippet("snippet")
        style.setTitle("title")
        style.setVisible(false)
        style.setZIndex(3f)

        val options = style.toMarkerOptions()

        assertThat(options.alpha).isEqualTo(0.5f)
        assertThat(options.anchorU).isEqualTo(0.2f)
        assertThat(options.anchorV).isEqualTo(0.8f)
        assertThat(options.isDraggable).isTrue()
        assertThat(options.isFlat).isTrue()
        assertThat(options.icon).isSameInstanceAs(icon)
        assertThat(options.infoWindowAnchorU).isEqualTo(0.5f)
        assertThat(options.infoWindowAnchorV).isEqualTo(0.1f)
        assertThat(options.rotation).isEqualTo(30f)
        assertThat(options.snippet).isEqualTo("snippet")
        assertThat(options.title).isEqualTo("title")
        assertThat(options.isVisible).isFalse()
        assertThat(options.zIndex).isEqualTo(3f)
        assertThat(style.toString()).contains("title=title")
    }

    @Test
    fun `point style applies to points and collections`() {
        assertThat(GeoJsonPointStyle().getGeometryType())
            .asList()
            .containsExactly("Point", "MultiPoint", "GeometryCollection")
    }
}
