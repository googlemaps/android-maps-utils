/*
 * Copyright 2025 Google LLC
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

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HeatmapTileProviderTest {

    @Test
    fun testBuilder_weightedData() {
        val data = listOf(WeightedLatLng(LatLng(0.0, 0.0)))
        val provider = HeatmapTileProvider.Builder().weightedData(data).build()
        assertThat(provider).isNotNull()
    }

    @Test
    fun testBuilder_data() {
        val data = listOf(LatLng(0.0, 0.0))
        val provider = HeatmapTileProvider.Builder().data(data).build()
        assertThat(provider).isNotNull()
    }

    @Test
    fun testBuilder_noData() {
        try {
            HeatmapTileProvider.Builder().build()
            fail("Should have thrown IllegalStateException")
        } catch (_: IllegalArgumentException) {
            // success
        }
    }

    @Test
    fun testBuilder_emptyData() {
        try {
            HeatmapTileProvider.Builder().data(emptyList())
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // success
        }
    }
    
    @Test
    fun testBuilder_radius() {
        val data = listOf(LatLng(0.0, 0.0))
        val provider = HeatmapTileProvider.Builder().data(data).radius(20).build()
        assertThat(provider).isNotNull()
    }

    @Test
    fun testBuilder_invalidRadius() {
        val data = listOf(LatLng(0.0, 0.0))
        try {
            HeatmapTileProvider.Builder().data(data).radius(0)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // success
        }
        try {
            HeatmapTileProvider.Builder().data(data).radius(100)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // success
        }
    }

    @Test
    fun testBuilder_opacity() {
        val data = listOf(LatLng(0.0, 0.0))
        val provider = HeatmapTileProvider.Builder().data(data).opacity(0.5).build()
        assertThat(provider).isNotNull()
    }

    @Test
    fun testBuilder_invalidOpacity() {
        val data = listOf(LatLng(0.0, 0.0))
        try {
            HeatmapTileProvider.Builder().data(data).opacity(-1.0)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // success
        }
        try {
            HeatmapTileProvider.Builder().data(data).opacity(2.0)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // success
        }
    }

    @Test
    fun testGetTile() {
        val data = listOf(LatLng(0.0, 0.0))
        val provider = HeatmapTileProvider.Builder().data(data).build()
        val tile = provider.getTile(512, 512, 10)
        assertThat(tile).isNotNull()
        assertThat(tile).isNotEqualTo(TileProvider.NO_TILE)
        assertThat(tile.width).isEqualTo(512)
        assertThat(tile.height).isEqualTo(512)
    }

    @Test
    fun testGetTile_noPointsInTile() {
        // Point is at (0,0), so tile at (1,1) should be empty
        val data = listOf(LatLng(0.0, 0.0))
        val provider = HeatmapTileProvider.Builder().data(data).build()
        // A zoom level high enough that (0,0) and (1,1) are far apart
        val tile = provider.getTile(1, 1, 20)
        assertThat(tile).isEqualTo(TileProvider.NO_TILE)
    }
}
