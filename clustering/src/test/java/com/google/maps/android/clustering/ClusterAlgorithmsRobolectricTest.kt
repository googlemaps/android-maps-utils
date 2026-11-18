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
package com.google.maps.android.clustering

import android.app.Activity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.algo.CentroidNonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
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
import com.google.maps.android.robolectric.truth.isWithin
import com.google.maps.android.robolectric.truth.meters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Stateful Robolectric verification suite for [ClusterManager] algorithm integration
 * ([CentroidNonHierarchicalDistanceBasedAlgorithm], [NonHierarchicalViewBasedAlgorithm],
 * [GridBasedAlgorithm], [PreCachingAlgorithmDecorator]) and atomic [ClusterManager.diff] updates.
 *
 * Note: Maps shadows are declared directly on `@Config(shadows = [...])` because Robolectric's
 * `ConfigMerger` does not traverse meta-annotations, avoiding global `robolectric.properties` pollution.
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
class ClusterAlgorithmsRobolectricTest {

    data class TestPlace(
        val id: String,
        override var position: LatLng,
        override var title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = 0f,
    ) : ClusterItem {
        override fun equals(other: Any?): Boolean = other is TestPlace && other.id == id
        override fun hashCode(): Int = id.hashCode()
    }

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<TestPlace>

    private val nyc1 = TestPlace("1", LatLng(40.7128, -74.0060), "NYC 1")
    private val nyc2 = TestPlace("2", LatLng(40.7130, -74.0062), "NYC 2")
    private val nyc3 = TestPlace("3", LatLng(40.7132, -74.0064), "NYC 3")
    private val bronx = TestPlace("4", LatLng(40.8500, -73.9000), "Bronx")

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = createTestMapView(width = 400, height = 400, activity = activity).apply {
            onStart()
            onResume()
        }
        googleMap = mapView.getMapBlocking()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nyc1.position, 12f))
        clusterManager = ClusterManager(
            activity,
            googleMap,
            UnconfinedTestDispatcher(),
            UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun centroidAlgorithm_computesExactGeographicMeanCenterOfClusteredItems() {
        clusterManager.algorithm = CentroidNonHierarchicalDistanceBasedAlgorithm()
        clusterManager.addItems(listOf(nyc1, nyc2, nyc3, bronx))

        val clusters = clusterManager.algorithm.getClusters(12f)
        truthAssertThat(clusters).hasSize(2)

        val downtownCluster = clusters.first { it.size == 3 }
        val expectedMean = LatLng(
            (nyc1.position.latitude + nyc2.position.latitude + nyc3.position.latitude) / 3.0,
            (nyc1.position.longitude + nyc2.position.longitude + nyc3.position.longitude) / 3.0,
        )
        assertThat(downtownCluster.position).isWithin(1.meters).of(expectedMean)
    }

    @Test
    fun clusterManagerDiff_atomicallyAddsRemovesAndModifiesItems() {
        clusterManager.addItems(listOf(nyc1, nyc2, nyc3, bronx))
        truthAssertThat(clusterManager.algorithm.getClusters(12f)).hasSize(2)

        val nyc5 = TestPlace("5", LatLng(40.7129, -74.0061), "NYC 5")
        val updatedNyc1 = TestPlace("1", nyc1.position, "NYC 1 Updated")
        clusterManager.diff(
            add = listOf(nyc5),
            remove = listOf(bronx),
            modify = listOf(updatedNyc1),
        )

        val afterDiff = clusterManager.algorithm.getClusters(12f)
        truthAssertThat(afterDiff).hasSize(1)
        val singleCluster = afterDiff.single()
        truthAssertThat(singleCluster.size).isEqualTo(4)
        truthAssertThat(singleCluster.items.first { it.id == "1" }.title).isEqualTo("NYC 1 Updated")
    }

    @Test
    fun viewBasedAlgorithm_filtersToCurrentCameraViewportUsingMapViewDimensions() {
        val viewAlgo = NonHierarchicalViewBasedAlgorithm<TestPlace>(mapView.width, mapView.height)
        clusterManager.setAlgorithm(viewAlgo)
        clusterManager.addItems(listOf(nyc1, nyc2, nyc3, bronx))

        viewAlgo.onCameraChange(googleMap.cameraPosition)
        truthAssertThat(viewAlgo.shouldReclusterOnMapMovement()).isTrue()

        // At zoom 15 centered on Lower Manhattan (400x400 viewport), Bronx (~17km away) is off-screen
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nyc1.position, 15f))
        viewAlgo.onCameraChange(googleMap.cameraPosition)
        val visibleClusters = clusterManager.algorithm.getClusters(15f)
        val visibleItems = visibleClusters.flatMap { it.items }

        truthAssertThat(visibleItems).containsAtLeast(nyc1, nyc2, nyc3)
        truthAssertThat(visibleItems).doesNotContain(bronx)
    }

    @Test
    fun gridAndPreCachingAlgorithms_partitionNearbyAndDistantItemsConsistentWithZoom() {
        clusterManager.algorithm = GridBasedAlgorithm()
        clusterManager.addItems(listOf(nyc1, nyc2, nyc3, bronx))
        truthAssertThat(clusterManager.algorithm.getClusters(12f).sumOf { it.size }).isEqualTo(4)

        clusterManager.algorithm = PreCachingAlgorithmDecorator(NonHierarchicalDistanceBasedAlgorithm())
        clusterManager.addItems(listOf(nyc1, nyc2, nyc3, bronx))
        val cachedAtZoom12 = clusterManager.algorithm.getClusters(12f)
        val cachedAtZoom20 = clusterManager.algorithm.getClusters(20f)

        truthAssertThat(cachedAtZoom12).hasSize(2)
        truthAssertThat(cachedAtZoom20).hasSize(4)
    }
}
