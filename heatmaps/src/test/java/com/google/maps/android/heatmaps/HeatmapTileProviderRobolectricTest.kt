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
package com.google.maps.android.heatmaps

import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptor
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptorFactory
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdate
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdateFactory
import com.google.maps.android.robolectric.shadows.ShadowCircle
import com.google.maps.android.robolectric.shadows.ShadowGoogleMap
import com.google.maps.android.robolectric.shadows.ShadowGroundOverlay
import com.google.maps.android.robolectric.shadows.ShadowMapView
import com.google.maps.android.robolectric.shadows.ShadowMapsInitializer
import com.google.maps.android.robolectric.shadows.ShadowMarker
import com.google.maps.android.robolectric.shadows.ShadowPolygon
import com.google.maps.android.robolectric.shadows.ShadowPolyline
import com.google.maps.android.robolectric.shadows.ShadowProjection
import com.google.maps.android.robolectric.shadows.ShadowSupportMapFragment
import com.google.maps.android.robolectric.shadows.ShadowTileOverlay
import com.google.maps.android.robolectric.shadows.ShadowUiSettings
import com.google.maps.android.robolectric.truth.assertThat
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.captureSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Stateful Robolectric and [GoldenImageDiff] verification suite for [HeatmapTileProvider]
 * PNG tile determinism, Gaussian kernel / gradient mutations, and live `TileOverlay` canvas
 * compositing on [GoogleMap].
 *
 * Note: Maps shadows are declared directly on `@Config(shadows = [...])` because Robolectric's
 * `ConfigMerger` does not traverse meta-annotations, avoiding global `robolectric.properties` pollution.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    shadows = [
        ShadowMapView::class,
        ShadowGoogleMap::class,
        ShadowCameraUpdate::class,
        ShadowCameraUpdateFactory::class,
        ShadowMarker::class,
        ShadowPolygon::class,
        ShadowPolyline::class,
        ShadowProjection::class,
        ShadowCircle::class,
        ShadowUiSettings::class,
        ShadowBitmapDescriptor::class,
        ShadowBitmapDescriptorFactory::class,
        ShadowMapsInitializer::class,
        ShadowSupportMapFragment::class,
        ShadowGroundOverlay::class,
        ShadowTileOverlay::class,
    ],
)
class HeatmapTileProviderRobolectricTest {

    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
    }

    @Test
    fun unweightedAndUnitWeightedProviders_generateBitIdenticalPngTiles() {
        val origin = LatLng(0.0, 0.0)
        val nearby = LatLng(0.05, 0.05)

        val unweighted = heatmapTileProviderWithData(
            latLngs = listOf(origin, nearby),
            radius = 25,
            opacity = 0.8,
        )
        val unitWeighted = heatmapTileProviderWithWeightedData(
            latLngs = listOf(origin.toWeightedLatLng(1.0), nearby.toWeightedLatLng(1.0)),
            radius = 25,
            opacity = 0.8,
        )

        val tileA = unweighted.getTile(1, 1, 1)
        val tileB = unitWeighted.getTile(1, 1, 1)
        truthAssertThat(tileA).isNotEqualTo(TileProvider.NO_TILE)
        truthAssertThat(tileA.width).isEqualTo(512)
        truthAssertThat(tileA.height).isEqualTo(512)

        val bmpA = checkNotNull(BitmapFactory.decodeByteArray(tileA.data, 0, tileA.data!!.size))
        val bmpB = checkNotNull(BitmapFactory.decodeByteArray(tileB.data, 0, tileB.data!!.size))
        GoldenImageDiff.assertMatches(expected = bmpA, actual = bmpB, maxDiffPercentage = 0.0)
    }

    @Test
    fun radiusAndGradientMutations_alterRasterizedPixelsAndClearTileOverlayCache() {
        val points = listOf(LatLng(0.0, 0.0), LatLng(0.05, 0.05))
        val provider = heatmapTileProviderWithData(latLngs = points, radius = 20, opacity = 0.8)
        val overlay = checkNotNull(googleMap.addTileOverlay(TileOverlayOptions().tileProvider(provider).zIndex(5f)))
        assertThat(googleMap).hasTileOverlayCount(1)

        val initialTile = provider.getTile(1, 1, 1)
        val initialBmp = checkNotNull(BitmapFactory.decodeByteArray(initialTile.data, 0, initialTile.data!!.size))

        provider.setRadius(45)
        overlay.clearTileCache()
        truthAssertThat(overlay.shadow.isTileCacheCleared).isTrue()

        val widerTile = provider.getTile(1, 1, 1)
        val widerBmp = checkNotNull(BitmapFactory.decodeByteArray(widerTile.data, 0, widerTile.data!!.size))
        val radiusDiff = GoldenImageDiff.compare(expected = initialBmp, actual = widerBmp)
        truthAssertThat(radiusDiff.matches).isFalse()
        truthAssertThat(radiusDiff.diffPixelCount).isGreaterThan(500)

        provider.setGradient(Gradient(intArrayOf(Color.BLUE, Color.CYAN), floatArrayOf(0.1f, 0.9f)))
        val recoloredTile = provider.getTile(1, 1, 1)
        val recoloredBmp = checkNotNull(BitmapFactory.decodeByteArray(recoloredTile.data, 0, recoloredTile.data!!.size))
        val gradientDiff = GoldenImageDiff.compare(expected = widerBmp, actual = recoloredBmp)
        truthAssertThat(gradientDiff.matches).isFalse()
        truthAssertThat(gradientDiff.diffPixelCount).isGreaterThan(500)
    }

    @Test
    fun heatmapTileOverlay_compositesNonBackgroundHotspotAtDataCoordinateAndRestoresOnRemoval() = runTest {
        val hotspot = LatLng(37.7749, -122.4194)
        val unheatedViewportCorner = LatLng(37.7950, -122.4450)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotspot, 12f))
        val baselineSnapshot = googleMap.captureSnapshot()

        val provider = heatmapTileProviderWithData(
            latLngs = listOf(hotspot),
            radius = 25,
            opacity = 1.0,
        )
        val overlay = checkNotNull(googleMap.addTileOverlay(TileOverlayOptions().tileProvider(provider)))

        // Projected hotspot screen pixel receives the rendered heatmap Gaussian kernel, whereas distant point stays background
        assertThat(googleMap)
            .hasNonBackgroundPixelAt(hotspot)
            .hasPixelColorAt(unheatedViewportCorner, Color.LTGRAY)

        overlay.remove()
        assertThat(googleMap).hasTileOverlayCount(0)
        GoldenImageDiff.assertMatches(
            expected = baselineSnapshot,
            actual = googleMap.captureSnapshot(),
            maxDiffPercentage = 0.0,
        )
    }
}
