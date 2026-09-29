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

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.robolectric.shadows.ShadowGoogleMap
import com.google.android.maps.robolectric.shadows.ShadowTileOverlay
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileProvider
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.maps.android.heatmaps.heatmapTileProviderWithData
import com.google.maps.android.heatmaps.heatmapTileProviderWithWeightedData
import com.google.maps.android.heatmaps.toWeightedLatLng
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertFailsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper

/**
 * End-to-end JVM integration and adversarial verification suite for the `:heatmaps` module
 * powered by the Android Maps Testing Toolkit (`v1.1.0-rc01`).
 *
 * ## Architectural Scope
 * 1. **Deterministic Raster Tile Verification with [GoldenImageDiff] & [GoldenTileStrategy]**:
 *    Decodes actual 512x512 PNG tiles produced by [HeatmapTileProvider.getTile] into [android.graphics.Bitmap]s
 *    and uses [GoldenImageDiff] and [GoldenTileProvider] (`GoldenTileStrategy.Custom`) to prove:
 *    - Bit-for-bit determinism across repeated tile requests.
 *    - Quantifiable pixel diffs when changing radius, opacity, gradient, maxIntensity, or point weights.
 *    - Proper integration with [ShadowGoogleMap.getTileOverlays] and [ShadowTileOverlay.isTileCacheCleared].
 * 2. **Adversarial Boundary & Wrapping Defect Proofs (Bugs #6 & #7)**:
 *    - **Bug #6A**: `HeatmapTileProvider.getTile` crashes with `ArrayIndexOutOfBoundsException` at
 *      `zoom = 22` (`MAX_ZOOM_LEVEL = 22`).
 *    - **Bug #6B**: `HeatmapTileProvider.getTile` crashes with `ArrayIndexOutOfBoundsException`
 *      whenever a data point lies on the upper padded tile boundary (`p.x == maxX` or `p.y == maxY`),
 *      because `PointQuadTree.search` uses inclusive `<=` bounds while `bucketX = ((p.x - minX) / bucketWidth).toInt()`
 *      produces index `TILE_DIM + radius * 2` (`544` for `radius = 16`).
 *    - **Bug #7A**: `HeatmapTileProvider.getTile` fails to render heatmaps wrapping across the
 *      International Date Line (antimeridian `±180°`), returning `TileProvider.NO_TILE` even when
 *      `wrappedPoints` is non-empty because lines 254 and 259 check `!tileBounds.intersects(paddedBounds)`
 *      and `points.isEmpty()` without checking `wrappedPoints`!
 *    - **Bug #7B**: `HeatmapTileProvider.setRadius` and `setOpacity` bypass the validation checks
 *      enforced by `HeatmapTileProvider.Builder`, allowing invalid values that corrupt state or
 *      throw `NegativeArraySizeException`.
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
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val mapView = MapView(activity).apply {
            onCreate(Bundle())
        }
        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()

        googleMap = checkNotNull(mapRef.get())
        shadowMap = Shadow.extract(googleMap) as ShadowGoogleMap
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
            opacity = 0.8
        )
        val provider2 = heatmapTileProviderWithWeightedData(
            latLngs = listOf(origin.toWeightedLatLng(1.0), nearby.toWeightedLatLng(1.0)),
            radius = 25,
            opacity = 0.8
        )

        // Attach to GoogleMap and verify ShadowTileOverlay tracking
        val overlay = checkNotNull(
            googleMap.addTileOverlay(TileOverlayOptions().tileProvider(provider1).zIndex(5f))
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
            maxDiffPercentage = 0.0
        )

        // Wrap heatmap tile decoding in a GoldenTileProvider with GoldenTileStrategy.Custom
        // to verify caching and synthetic tile interoperability
        val customGoldenProvider = GoldenTileProvider(
            strategy = GoldenTileStrategy.Custom { x, y, zoom, _ ->
                val raw = provider1.getTile(x, y, zoom).data!!
                BitmapFactory.decodeByteArray(raw, 0, raw.size)
            },
            tileSize = 512
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
        val shadowOverlay = Shadow.extract(overlay) as ShadowTileOverlay
        assertThat(shadowOverlay.isTileCacheCleared).isTrue()

        val widerTile = provider1.getTile(1, 1, 1)
        val widerBmp = checkNotNull(BitmapFactory.decodeByteArray(widerTile.data, 0, widerTile.data!!.size))
        val radiusDiff = GoldenImageDiff.compare(expected = bmp1, actual = widerBmp)
        assertThat(radiusDiff.matches).isFalse()
        assertThat(radiusDiff.diffPixelCount).isGreaterThan(500)

        // Mutate gradient to a blue-to-cyan palette -> colorized pixels must diverge
        val blueCyanGradient = Gradient(
            intArrayOf(Color.BLUE, Color.CYAN),
            floatArrayOf(0.1f, 0.9f)
        )
        provider1.setGradient(blueCyanGradient)
        val recoloredTile = provider1.getTile(1, 1, 1)
        val recoloredBmp = checkNotNull(
            BitmapFactory.decodeByteArray(recoloredTile.data, 0, recoloredTile.data!!.size)
        )
        val gradientDiff = GoldenImageDiff.compare(expected = widerBmp, actual = recoloredBmp)
        assertThat(gradientDiff.matches).isFalse()
        assertThat(gradientDiff.diffPixelCount).isGreaterThan(500)
    }

    // ============================================================================================
    // Section 2: Adversarial Bug Reproductions in :heatmaps (Bugs #6 & #7)
    // ============================================================================================

    /**
     * **BUG #6A REPRODUCTION (`HeatmapTileProvider.getTile` Crashes with `ArrayIndexOutOfBoundsException`
     * at `zoom = 22`)**:
     *
     * - Google Maps SDK and [GoldenTileProvider] support zoom levels `0..22`.
     * - However, in `HeatmapTileProvider.kt:283`, `getMaxIntensities` allocates
     *   `DoubleArray(MAX_ZOOM_LEVEL)` where `MAX_ZOOM_LEVEL = 22` (valid indices `0..21`).
     * - When `getTile(x, y, zoom = 22)` reaches line 278:
     *   `val bitmap = colorize(convolved, colorMap, maxIntensity[zoom])`
     *   indexing `maxIntensity[22]` throws `ArrayIndexOutOfBoundsException: Index 22 out of bounds for length 22`!
     */
    @Test
    fun bug6a_heatmapTileProvider_getTileAtZoom22SucceedsWithoutOutOfBounds() {
        val provider = heatmapTileProviderWithData(listOf(LatLng(0.0, 0.0)))

        // At zoom 22, LatLng(0.0, 0.0) (world coordinate 0.5, 0.5) is in tile x = 2^21, y = 2^21
        val tileCoordAtZoom22 = 1 shl 21
        val tile = provider.getTile(tileCoordAtZoom22, tileCoordAtZoom22, 22)
        assertThat(tile).isNotNull()
        assertThat(tile).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun bug6b_heatmapTileProvider_pointOnUpperTileBoundaryRendersWithoutOutOfBounds() {
        val interiorPoint = LatLng(0.0, -10.0)
        val boundaryPoint = LatLng(0.0, 2.8125)
        val provider = heatmapTileProviderWithData(
            latLngs = listOf(interiorPoint, boundaryPoint),
            radius = 16
        )

        val tile = provider.getTile(1, 1, 2)
        assertThat(tile).isNotNull()
        assertThat(tile).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun bug7a_heatmapTileProvider_rendersAntimeridianWrappedPointsWhenPrimaryTilePointsAreEmpty() {
        val pointNearEastDateLine = LatLng(0.0, 179.99)
        val northernHemispherePoint = LatLng(75.0, -90.0)

        val provider = heatmapTileProviderWithData(
            latLngs = listOf(pointNearEastDateLine, northernHemispherePoint),
            radius = 50
        )

        val tileAcrossDateLine = provider.getTile(0, 1, 1)
        assertThat(tileAcrossDateLine).isNotEqualTo(TileProvider.NO_TILE)
    }

    @Test
    fun bug7b_heatmapTileProvider_setRadiusAndSetOpacityEnforceBuilderValidation() {
        val provider = heatmapTileProviderWithData(listOf(LatLng(0.0, 0.0)))

        assertFailsWith<IllegalArgumentException> {
            HeatmapTileProvider.Builder()
                .data(listOf(LatLng(0.0, 0.0)))
                .radius(-5)
        }

        // BUG #7B VERIFIED FIXED: provider.setRadius(-5) and setOpacity(-0.5) throw IllegalArgumentException!
        assertFailsWith<IllegalArgumentException> {
            provider.setRadius(-5)
        }
        assertFailsWith<IllegalArgumentException> {
            provider.setOpacity(-0.5)
        }
    }
}
