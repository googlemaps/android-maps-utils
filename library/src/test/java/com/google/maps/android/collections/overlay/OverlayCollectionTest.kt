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

package com.google.maps.android.collections.overlay

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [OverlayCollection].
 */
@RunWith(RobolectricTestRunner::class)
class OverlayCollectionTest {

    private lateinit var map: GoogleMap
    private lateinit var manager: OverlayManager
    private lateinit var collection: OverlayCollection

    private lateinit var mockMarker: Marker
    private lateinit var mockCircle: Circle
    private lateinit var mockPolygon: Polygon
    private lateinit var mockPolyline: Polyline
    private lateinit var mockGroundOverlay: GroundOverlay

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        mockMarker = mockk(relaxed = true)
        mockCircle = mockk(relaxed = true)
        mockPolygon = mockk(relaxed = true)
        mockPolyline = mockk(relaxed = true)
        mockGroundOverlay = mockk(relaxed = true)

        every { map.addMarker(any<MarkerOptions>()) } returns mockMarker
        every { map.addMarker(any<AdvancedMarkerOptions>()) } returns mockMarker
        every { map.addCircle(any()) } returns mockCircle
        every { map.addPolygon(any()) } returns mockPolygon
        every { map.addPolyline(any()) } returns mockPolyline
        every { map.addGroundOverlay(any()) } returns mockGroundOverlay

        manager = OverlayManager(map)
        collection = manager.newCollection("test_layer")
    }

    @Test
    fun testHeterogeneousAdditionAndQueries() {
        val marker = collection.add(MarkerOptions().position(LatLng(10.0, 10.0)))
        val advMarker = collection.add(AdvancedMarkerOptions().position(LatLng(15.0, 15.0)))
        val circle = collection.add(CircleOptions().center(LatLng(20.0, 20.0)))
        val polygon = collection.add(PolygonOptions().add(LatLng(0.0, 0.0), LatLng(1.0, 1.0), LatLng(1.0, 0.0)))
        val polyline = collection.add(PolylineOptions().add(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val groundOverlay = collection.add(GroundOverlayOptions().position(LatLng(5.0, 5.0), 100f))

        assertThat(collection.size).isEqualTo(6)
        assertThat(collection.isEmpty).isFalse()

        assertThat(collection.markers).containsExactly(marker, advMarker)
        assertThat(collection.circles).containsExactly(circle)
        assertThat(collection.polygons).containsExactly(polygon)
        assertThat(collection.polylines).containsExactly(polyline)
        assertThat(collection.groundOverlays).containsExactly(groundOverlay)
    }

    @Test
    fun testOperatorPlusAssign() {
        collection += MarkerOptions()
        collection += AdvancedMarkerOptions()
        collection += CircleOptions()
        collection += PolygonOptions()
        collection += PolylineOptions()
        collection += GroundOverlayOptions()

        assertThat(collection.size).isEqualTo(6)
    }

    @Test
    fun testBatchAddAll() {
        val markers = collection.addAll(listOf(MarkerOptions(), MarkerOptions()))
        val advMarkers = collection.addAllAdvancedMarkers(listOf(AdvancedMarkerOptions()))
        val circles = collection.addAll(listOf(CircleOptions()))
        val polygons = collection.addAll(listOf(PolygonOptions()))
        val polylines = collection.addAll(listOf(PolylineOptions()))
        val groundOverlays = collection.addAll(listOf(GroundOverlayOptions()))

        assertThat(markers).hasSize(2)
        assertThat(advMarkers).hasSize(1)
        assertThat(circles).hasSize(1)
        assertThat(polygons).hasSize(1)
        assertThat(polylines).hasSize(1)
        assertThat(groundOverlays).hasSize(1)
    }

    @Test
    fun testBatchVisibilityAndClearing() {
        val marker = collection.add(MarkerOptions())
        val polyline = collection.add(PolylineOptions())

        collection.hideAll()
        assertThat(collection.isVisible).isFalse()
        verify { mockMarker.isVisible = false }
        verify { mockPolyline.isVisible = false }

        collection.showAll()
        assertThat(collection.isVisible).isTrue()
        verify { mockMarker.isVisible = true }
        verify { mockPolyline.isVisible = true }

        collection.clear()
        assertThat(collection.isEmpty).isTrue()
        verify { mockMarker.remove() }
        verify { mockPolyline.remove() }
    }

    @Test
    fun testIndividualRemoval() {
        val marker = collection.add(MarkerOptions())
        assertThat(collection.size).isEqualTo(1)

        assertThat(marker.remove()).isTrue()
        assertThat(collection.isEmpty).isTrue()
        verify { mockMarker.remove() }

        // Second remove returns false
        assertThat(collection.remove(marker)).isFalse()
    }

    @Test
    fun testEventDelegation() {
        val marker = collection.add(MarkerOptions())
        val circle = collection.add(CircleOptions())
        val polygon = collection.add(PolygonOptions())
        val polyline = collection.add(PolylineOptions())
        val groundOverlay = collection.add(GroundOverlayOptions())

        // Individual overlay listeners
        var markerClicked = false
        marker.onClick {
            markerClicked = true
            true
        }

        var circleClicked = false
        circle.onClick { circleClicked = true }

        var polygonClicked = false
        polygon.onClick { polygonClicked = true }

        var polylineClicked = false
        polyline.onClick { polylineClicked = true }

        var groundOverlayClicked = false
        groundOverlay.onClick { groundOverlayClicked = true }

        // Dispatch via manager's legacy collections
        assertThat(manager.markerManager.onMarkerClick(mockMarker)).isTrue()
        assertThat(markerClicked).isTrue()

        manager.circleManager.onCircleClick(mockCircle)
        assertThat(circleClicked).isTrue()

        manager.polygonManager.onPolygonClick(mockPolygon)
        assertThat(polygonClicked).isTrue()

        manager.polylineManager.onPolylineClick(mockPolyline)
        assertThat(polylineClicked).isTrue()

        manager.groundOverlayManager.onGroundOverlayClick(mockGroundOverlay)
        assertThat(groundOverlayClicked).isTrue()
    }

    @Test
    fun testCollectionLevelListeners() {
        val marker = collection.add(MarkerOptions())
        var colMarkerClicked = false
        collection.onMarkerClick {
            colMarkerClicked = true
            true
        }

        var colInfoClicked = false
        collection.onInfoWindowClick { colInfoClicked = true }

        var colInfoLongClicked = false
        collection.onInfoWindowLongClick { colInfoLongClicked = true }

        val mockView = mockk<View>()
        collection.setCustomInfoWindow(
            infoWindow = { mockView },
            infoContents = { null }
        )

        assertThat(manager.markerManager.onMarkerClick(mockMarker)).isTrue()
        assertThat(colMarkerClicked).isTrue()

        manager.markerManager.onInfoWindowClick(mockMarker)
        assertThat(colInfoClicked).isTrue()

        manager.markerManager.onInfoWindowLongClick(mockMarker)
        assertThat(colInfoLongClicked).isTrue()

        assertThat(manager.markerManager.getInfoWindow(mockMarker)).isEqualTo(mockView)
        assertThat(manager.markerManager.getInfoContents(mockMarker)).isNull()

        // Test collection-level fallback listeners for other geometries
        val circle = collection.add(CircleOptions())
        val polygon = collection.add(PolygonOptions())
        val polyline = collection.add(PolylineOptions())
        val groundOverlay = collection.add(GroundOverlayOptions())

        var circleClicked = false
        var polygonClicked = false
        var polylineClicked = false
        var groundOverlayClicked = false

        collection.onCircleClick { circleClicked = true }
        collection.onPolygonClick { polygonClicked = true }
        collection.onPolylineClick { polylineClicked = true }
        collection.onGroundOverlayClick { groundOverlayClicked = true }

        manager.circleManager.onCircleClick(mockCircle)
        assertThat(circleClicked).isTrue()

        manager.polygonManager.onPolygonClick(mockPolygon)
        assertThat(polygonClicked).isTrue()

        manager.polylineManager.onPolylineClick(mockPolyline)
        assertThat(polylineClicked).isTrue()

        manager.groundOverlayManager.onGroundOverlayClick(mockGroundOverlay)
        assertThat(groundOverlayClicked).isTrue()
    }
}
