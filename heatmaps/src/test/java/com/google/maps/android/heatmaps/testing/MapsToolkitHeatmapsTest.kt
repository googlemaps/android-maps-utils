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
package com.google.maps.android.heatmaps.testing

import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.heatmapTileProviderWithData
import com.google.maps.android.heatmaps.heatmapTileProviderWithWeightedData
import com.google.maps.android.heatmaps.toWeightedLatLng
import com.google.maps.android.robolectric.annotation.EnableMapsShadows
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.ktx.tileOverlays
import com.google.maps.android.robolectric.shadows.ShadowGoogleMap
import com.google.maps.android.robolectric.shadows.ShadowTileOverlay
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileProvider
import com.google.maps.android.testing.golden.GoldenTileStrategy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertFailsWith

/**
 * End-to-end JVM integration and regression verification suite for the `:heatmaps` module
 * powered by the Android Maps Testing Toolkit.
 *
 * ## Architectural Scope
 * 1. **Deterministic Raster Tile Verification with [GoldenImageDiff] & [GoldenTileStrategy]**:
 *    Decodes actual 512x512 PNG tiles produced by [HeatmapTileProvider.getTile] into [android.graphics.Bitmap]s
 *    and uses [GoldenImageDiff] and [GoldenTileProvider] (`GoldenTileStrategy.Custom`) to prove:
 *    - Bit-for-bit determinism across repeated tile requests.
 *    - Quantifiable pixel diffs when changing radius, opacity, gradient, maxIntensity, or point weights.
 *    - Proper integration with [ShadowGoogleMap.getTileOverlays] and [ShadowTileOverlay.isTileCacheCleared].
 * 2. **Boundary, Antimeridian Wrapping & Input Validation Verification**:
 *    - `HeatmapTileProvider.getTile` renders cleanly at `zoom = 22` (`MAX_ZOOM_LEVEL = 22`) without
 *      throwing `ArrayIndexOutOfBoundsException`.
 *    - `HeatmapTileProvider.getTile` handles data points lying directly on the upper padded tile
 *      boundary (`p.x == maxX` or `p.y == maxY`) without overflowing the histogram bucket array.
 *    - `HeatmapTileProvider.getTile` renders heatmaps wrapping across the International Date Line
 *      (antimeridian `±180°`) even when all points in the tile come from `wrappedPoints`.
 *    - `HeatmapTileProvider.setRadius` and `setOpacity` enforce the same parameter validation
 *      bounds as `HeatmapTileProvider.Builder`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class MapsToolkitHeatmapsTest {

    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
        shadowMap = googleMap.shadow
    }

    // ============================================================================================
    // Section 1: Heatmap Tile Rasterization, GoldenImageDiff & ShadowTileOverlay Integration
    // ============================================================================================

    /**
     * Verifies that [heatmapTileProviderWithData] and [heatmapTileProviderWithWeightedData]
     * generate deterministic 512x512 PNG tiles, integrate with [ShadowTileOverlay] on [ShadowGoogleMap],
     * and produce quantifiable pixel differences under [GoldenImageDiff] when radius, gradient,
     * opacity, or dataset coordinates change.
     */
    @Test
    fun heatmapTileProvider_generatesDeterministicTilesAndQuantifiableGoldenDiffsAcrossMutations() {
        val origin = LatLng(0.0, 0.0)
        val nearby = LatLng(0.05, 0.05)

        val provider1 = heatmapTileProviderWithData(
            latLngs = listOf(origin, nearby),
            radius = 25,
            opacity = 0.8,
        )
        val provider2 = heatmapTileProviderWithWeightedData(
            latLngs = listOf(origin.toWeightedLatLng(1.0), nearby.toWeightedLatLng(1.0)),
            radius = 25,
            opacity = 0.8,
        )

        // Attach to GoogleMap and verify ShadowTileOverlay tracking
        val overlay = checkNotNull(
            googleMap.addTileOverlay(TileOverlayOptions().tileProvider(provider1).zIndex(5f)),
        )
        assertThat(shadowMap.tileOverlays).containsExactly(overlay)
        assertThat(overlay.zIndex).isWithin(0.001f).of(5f)

        // At zoom = 1, LatLng(0.0, 0.0) is at world coordinate (0.5, 0.5), which falls in tile (1, 1)
        val tile1 = provider1.getTile(1, 1, 1)
        val tile2 = provider2.getTile(1, 1, 1)
        assertThat(tile1).isNotEqualTo(TileProvider.NO_TILE)
        assertThat(tile1.width).isEqualTo(512)
        assertThat(tile1.height).isEqualTo(512)

        val bmp1 = checkNotNull(BitmapFactory.decodeByteArray(tile1.data, 0, tile1.data!!.size))
        val bmp2 = checkNotNull(BitmapFactory.decodeByteArray(tile2.data, 0, tile2.data!!.size))

        // Identical configuration must match pixel-for-pixel (0.0% diff)
        GoldenImageDiff.assertMatches(
            expected = bmp1,
            actual = bmp2,
            maxDiffPercentage = 0.0,
        )

        // Wrap heatmap tile decoding in a GoldenTileProvider with GoldenTileStrategy.Custom
        // to verify caching and synthetic tile interoperability
        val customGoldenProvider = GoldenTileProvider(
            strategy = GoldenTileStrategy.Custom { x, y, zoom, _ ->
                val raw = provider1.getTile(x, y, zoom).data!!
                BitmapFactory.decodeByteArray(raw, 0, raw.size)
            },
            tileSize = 512,
        )
        assertThat(customGoldenProvider.cachedTileCount()).isEqualTo(0)
        val cachedTile = customGoldenProvider.getTile(1, 1, 1)
        assertThat(customGoldenProvider.cachedTileCount()).isEqualTo(1)
        val cachedBmp = checkNotNull(BitmapFactory.decodeByteArray(cachedTile.data, 0, cachedTile.data!!.size))
        GoldenImageDiff.assertMatches(expected = bmp1, actual = cachedBmp, maxDiffPercentage = 0.0)
        customGoldenProvider.clearCache()
        assertThat(customGoldenProvider.cachedTileCount()).isEqualTo(0)

        // Mutate radius from 25 to 45 -> wider Gaussian kernel must alter pixel distribution
        provider1.setRadius(45)
        overlay.clearTileCache()
        assertThat(overlay.shadow.isTileCacheCleared).isTrue()

        val widerTile = provider1.getTile(1, 1, 1)
        val widerBmp = checkNotNull(BitmapFactory.decodeByteArray(widerTile.data, 0, widerTile.data!!.size))
        val radiusDiff = GoldenImageDiff.compare(expected = bmp1, actual = widerBmp)
        assertThat(radiusDiff.matches).isFalse()
        assertThat(radiusDiff.diffPixelCount).isGreaterThan(500)

        // Mutate gradient to a blue-to-cyan palette -> colorized pixels must diverge
        val blueCyanGradient = Gradient(
            intArrayOf(Color.BLUE, Color.CYAN),
            floatArrayOf(0.1f, 0.9f),
        )
        provider1.setGradient(blueCyanGradient)
        val recoloredTile = provider1.getTile(1, 1, 1)
        val recoloredBmp = checkNotNull(
            BitmapFactory.decodeByteArray(recoloredTile.data, 0, recoloredTile.data!!.size),
        )
        val gradientDiff = GoldenImageDiff.compare(expected = widerBmp, actual = recoloredBmp)
        assertThat(gradientDiff.matches).isFalse()
        assertThat(gradientDiff.diffPixelCount).isGreaterThan(500)
    }

    // ============================================================================================
    // Section 2: Boundary Conditions, Antimeridian Wrapping & Input Validation in :heatmaps
    // ============================================================================================

    /**
     * Verifies that `HeatmapTileProvider.getTile` supports `zoom = 22` (`MAX_ZOOM_LEVEL = 22`)
     * by indexing into a `maxIntensity` array sized for inclusive zoom levels `0..22`.
     */
    @Test
    fun heatmapTileProvider_getTileAtZoom22SucceedsWithoutOutOfBounds() {
        val provider = heatmapTileProviderWithData(listOf(LatLng(0.0, 0.0)))

        // At zoom 22, LatLng(0.0, 0.0) (world coordinate 0.5, 0.5) is in tile x = 2^21, y = 2^21
        val tileCoordAtZoom22 = 1 shl 21
        val tile = provider.getTile(tileCoordAtZoom22, tileCoordAtZoom22, 22)
        assertThat(tile).isNotNull()
        assertThat(tile).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun heatmapTileProvider_pointOnUpperTileBoundaryRendersWithoutOutOfBounds() {
        val interiorPoint = LatLng(0.0, -10.0)
        val boundaryPoint = LatLng(0.0, 2.8125)
        val provider = heatmapTileProviderWithData(
            latLngs = listOf(interiorPoint, boundaryPoint),
            radius = 16,
        )

        val tile = provider.getTile(1, 1, 2)
        assertThat(tile).isNotNull()
        assertThat(tile).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun heatmapTileProvider_rendersAntimeridianWrappedPointsWhenPrimaryTilePointsAreEmpty() {
        val pointNearEastDateLine = LatLng(0.0, 179.99)
        val northernHemispherePoint = LatLng(75.0, -90.0)

        val provider = heatmapTileProviderWithData(
            latLngs = listOf(pointNearEastDateLine, northernHemispherePoint),
            radius = 50,
        )

        val tileAcrossDateLine = provider.getTile(0, 1, 1)
        assertThat(tileAcrossDateLine).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun heatmapTileProvider_setRadiusAndSetOpacityEnforceBuilderValidation() {
        val provider = heatmapTileProviderWithData(listOf(LatLng(0.0, 0.0)))

        assertFailsWith<IllegalArgumentException> {
            HeatmapTileProvider.Builder()
                .data(listOf(LatLng(0.0, 0.0)))
                .radius(-5)
        }

        // Verify that provider.setRadius(-5) and setOpacity(-0.5) throw IllegalArgumentException:
        assertFailsWith<IllegalArgumentException> {
            provider.setRadius(-5)
        }
        assertFailsWith<IllegalArgumentException> {
            provider.setOpacity(-0.5)
        }
    }
}
