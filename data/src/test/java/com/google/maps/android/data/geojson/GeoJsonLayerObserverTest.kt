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
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.data.Feature
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeoJsonLayerObserverTest {
    @Test
    fun addedPolygonStyleChange_removesAndRedrawsFeature() {
        val map = mockk<GoogleMap>(relaxed = true)
        val firstPolygon = mockk<Polygon>(relaxed = true)
        val secondPolygon = mockk<Polygon>(relaxed = true)
        val options = mutableListOf<PolygonOptions>()
        every { map.addPolygon(capture(options)) } returnsMany listOf(firstPolygon, secondPolygon)
        val layer = emptyLayer(map)
        val (feature, style) = polygonFeature(INITIAL_COLOR)

        layer.addLayerToMap()
        layer.addFeature(feature)
        style.fillColor = UPDATED_COLOR

        verify(exactly = 1) { firstPolygon.remove() }
        verify(exactly = 2) { map.addPolygon(any<PolygonOptions>()) }
        assertEquals(INITIAL_COLOR, options[0].fillColor)
        assertEquals(UPDATED_COLOR, options[1].fillColor)
    }

    @Test
    fun parsedFeatureStyleChangeWhileOffMap_isUsedWhenLayerIsReadded() {
        val map = mockk<GoogleMap>(relaxed = true)
        val firstPolygon = mockk<Polygon>(relaxed = true)
        val secondPolygon = mockk<Polygon>(relaxed = true)
        val options = mutableListOf<PolygonOptions>()
        every { map.addPolygon(capture(options)) } returnsMany listOf(firstPolygon, secondPolygon)
        val layer =
            GeoJsonLayer(
                map,
                JSONObject(
                    """{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,0]]]}}""",
                ),
            )
        val feature = layer.features.single()

        layer.addLayerToMap()
        layer.removeLayerFromMap()
        feature.polygonStyle!!.fillColor = UPDATED_COLOR

        layer.addLayerToMap()

        verify(exactly = 1) { firstPolygon.remove() }
        verify(exactly = 2) { map.addPolygon(any<PolygonOptions>()) }
        assertEquals(UPDATED_COLOR, options.last().fillColor)
    }

    @Test
    fun removedFeature_stopsObservingStyleChanges() {
        val map = mockk<GoogleMap>(relaxed = true)
        val polygon = mockk<Polygon>(relaxed = true)
        every { map.addPolygon(any<PolygonOptions>()) } returns polygon
        val layer = emptyLayer(map)
        val (feature, style) = polygonFeature(INITIAL_COLOR)
        layer.addLayerToMap()
        layer.addFeature(feature)

        assertEquals(1, feature.countObservers())
        layer.removeFeature(feature)
        assertEquals(0, feature.countObservers())
        style.fillColor = UPDATED_COLOR

        verify(exactly = 1) { polygon.remove() }
        verify(exactly = 1) { map.addPolygon(any<PolygonOptions>()) }
    }

    @Test
    fun featureWithoutGeometry_isIgnoredByLayerLifecycle() {
        val map = mockk<GoogleMap>(relaxed = true)
        val layer = emptyLayer(map)
        val feature = GeoJsonFeature(null, null, null, null)

        layer.addLayerToMap()
        layer.addFeature(feature)
        layer.removeLayerFromMap()
        layer.addLayerToMap()

        assertEquals(listOf(feature), layer.features.toList())
        verify(exactly = 0) { map.addPolygon(any<PolygonOptions>()) }
    }

    @Test
    fun multiPolygonStyleChange_redrawsAndRemovesAllChildrenWithParentClickLookup() {
        val map = mockk<GoogleMap>(relaxed = true)
        val polygons = List(4) { mockk<Polygon>(relaxed = true) }
        val options = mutableListOf<PolygonOptions>()
        val polygonClickListener = slot<GoogleMap.OnPolygonClickListener>()
        every { map.addPolygon(capture(options)) } returnsMany polygons
        every { map.setOnPolygonClickListener(capture(polygonClickListener)) } just Runs
        val layer = emptyLayer(map)
        val (feature, style) = multiPolygonFeature(INITIAL_COLOR)
        var clickedFeature: Feature? = null

        layer.addLayerToMap()
        layer.setOnFeatureClickListener { clickedFeature = it }
        layer.addFeature(feature)
        style.fillColor = UPDATED_COLOR

        assertEquals(listOf(INITIAL_COLOR, INITIAL_COLOR, UPDATED_COLOR, UPDATED_COLOR), options.map { it.fillColor })
        verify(exactly = 1) { polygons[0].remove() }
        verify(exactly = 1) { polygons[1].remove() }

        polygonClickListener.captured.onPolygonClick(polygons[2])
        assertSame(feature, clickedFeature)

        layer.removeFeature(feature)

        verify(exactly = 1) { polygons[2].remove() }
        verify(exactly = 1) { polygons[3].remove() }
    }

    @Test
    fun addingSameFeatureTwice_replacesRenderingWithoutDuplicatingLifecycle() {
        val map = mockk<GoogleMap>(relaxed = true)
        val firstPolygon = mockk<Polygon>(relaxed = true)
        val secondPolygon = mockk<Polygon>(relaxed = true)
        every { map.addPolygon(any<PolygonOptions>()) } returnsMany listOf(firstPolygon, secondPolygon)
        val layer = emptyLayer(map)
        val (feature, style) = polygonFeature(INITIAL_COLOR)

        layer.addLayerToMap()
        layer.addFeature(feature)
        layer.addFeature(feature)

        assertEquals(listOf(feature), layer.features.toList())
        assertEquals(1, feature.countObservers())
        verify(exactly = 1) { firstPolygon.remove() }

        layer.removeFeature(feature)
        style.fillColor = UPDATED_COLOR

        assertEquals(emptyList<GeoJsonFeature>(), layer.features.toList())
        assertEquals(0, feature.countObservers())
        verify(exactly = 1) { secondPolygon.remove() }
        verify(exactly = 2) { map.addPolygon(any<PolygonOptions>()) }
    }

    @Test
    fun equalModelFeatures_clickLookupUsesModelIdentity() {
        val map = mockk<GoogleMap>(relaxed = true)
        val firstPolygon = mockk<Polygon>(relaxed = true)
        val secondPolygon = mockk<Polygon>(relaxed = true)
        val polygonClickListener = slot<GoogleMap.OnPolygonClickListener>()
        every { map.addPolygon(any<PolygonOptions>()) } returnsMany listOf(firstPolygon, secondPolygon)
        every { map.setOnPolygonClickListener(capture(polygonClickListener)) } just Runs
        val layer = emptyLayer(map)
        val (firstFeature) = polygonFeature(INITIAL_COLOR)
        val (secondFeature) = polygonFeature(INITIAL_COLOR)
        var clickedFeature: Feature? = null

        layer.addLayerToMap()
        layer.setOnFeatureClickListener { clickedFeature = it }
        layer.addFeature(firstFeature)
        layer.addFeature(secondFeature)

        polygonClickListener.captured.onPolygonClick(firstPolygon)
        assertSame(firstFeature, clickedFeature)
        polygonClickListener.captured.onPolygonClick(secondPolygon)
        assertSame(secondFeature, clickedFeature)
    }

    private fun emptyLayer(map: GoogleMap): GeoJsonLayer =
        GeoJsonLayer(
            map,
            JSONObject("""{"type":"FeatureCollection","features":[]}"""),
        )

    private fun polygonFeature(fillColor: Int): Pair<GeoJsonFeature, GeoJsonPolygonStyle> {
        val geometry =
            GeoJsonPolygon(
                listOf(
                    listOf(
                        LatLng(0.0, 0.0),
                        LatLng(0.0, 1.0),
                        LatLng(1.0, 1.0),
                        LatLng(0.0, 0.0),
                    ),
                ),
            )
        val style = GeoJsonPolygonStyle().apply { this.fillColor = fillColor }
        return GeoJsonFeature(geometry, null, null, null).also { it.polygonStyle = style } to style
    }

    private fun multiPolygonFeature(fillColor: Int): Pair<GeoJsonFeature, GeoJsonPolygonStyle> {
        val firstPolygon = polygon(0.0)
        val secondPolygon = polygon(2.0)
        val style = GeoJsonPolygonStyle().apply { this.fillColor = fillColor }
        return GeoJsonFeature(GeoJsonMultiPolygon(listOf(firstPolygon, secondPolygon)), null, null, null)
            .also { it.polygonStyle = style } to style
    }

    private fun polygon(offset: Double): GeoJsonPolygon =
        GeoJsonPolygon(
            listOf(
                listOf(
                    LatLng(offset, offset),
                    LatLng(offset, offset + 1.0),
                    LatLng(offset + 1.0, offset + 1.0),
                    LatLng(offset, offset),
                ),
            ),
        )

    private companion object {
        const val INITIAL_COLOR = -15654349
        const val UPDATED_COLOR = -12298906
    }
}
