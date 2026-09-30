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

package com.google.maps.android.collections

import com.google.maps.android.MarkerDragEndEvent
import com.google.maps.android.MarkerDragEvent
import com.google.maps.android.MarkerDragStartEvent

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.collections.CircleManager
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class CollectionManagersTest {

    @Mock
    private lateinit var markerCollection: MarkerManager.Collection

    @Mock
    private lateinit var polylineCollection: PolylineManager.Collection

    @Mock
    private lateinit var polygonCollection: PolygonManager.Collection

    @Mock
    private lateinit var circleCollection: CircleManager.Collection

    @Mock
    private lateinit var groundOverlayCollection: GroundOverlayManager.Collection

    @Mock
    private lateinit var marker: Marker

    @Mock
    private lateinit var polyline: Polyline

    @Mock
    private lateinit var polygon: Polygon

    @Mock
    private lateinit var circle: Circle

    @Mock
    private lateinit var groundOverlay: GroundOverlay

    @Captor
    private lateinit var markerClickListener: ArgumentCaptor<GoogleMap.OnMarkerClickListener>

    @Captor
    private lateinit var infoWindowClickListener: ArgumentCaptor<GoogleMap.OnInfoWindowClickListener>

    @Captor
    private lateinit var infoWindowLongClickListener: ArgumentCaptor<GoogleMap.OnInfoWindowLongClickListener>

    @Captor
    private lateinit var polylineClickListener: ArgumentCaptor<GoogleMap.OnPolylineClickListener>

    @Captor
    private lateinit var polygonClickListener: ArgumentCaptor<GoogleMap.OnPolygonClickListener>

    @Captor
    private lateinit var circleClickListener: ArgumentCaptor<GoogleMap.OnCircleClickListener>

    @Captor
    private lateinit var groundOverlayClickListener: ArgumentCaptor<GoogleMap.OnGroundOverlayClickListener>

    private var activeMarkerClickListener: GoogleMap.OnMarkerClickListener? = null

    @Before
    public fun setUp() {
        activeMarkerClickListener = null
        // Stub setOnMarkerClickListener to track the active listener on the collection manager
        `when`(markerCollection.setOnMarkerClickListener(any())).thenAnswer { invocation ->
            activeMarkerClickListener = invocation.arguments[0] as? GoogleMap.OnMarkerClickListener
            null
        }
    }

    @Test
    public fun testMarkerCollectionClickEvents(): Unit = runTest {
        val deferred = async {
            markerCollection.clickEvents().first()
        }
        advanceUntilIdle()
        // Trigger the event via our tracked active listener slot!
        assertThat(activeMarkerClickListener).isNotNull()
        activeMarkerClickListener?.onMarkerClick(marker)
        assertThat(deferred.await()).isEqualTo(marker)
    }

    @Test
    public fun testConcurrentCollectionMarkerClick(): Unit = runTest {
        val flow = markerCollection.clickEvents()
        val collector1Events = mutableListOf<Marker>()
        val collector2Events = mutableListOf<Marker>()

        // Start Collector 1
        val job1 = launch {
            flow.collect { collector1Events.add(it) }
        }
        advanceUntilIdle()
        assertThat(activeMarkerClickListener).isNotNull()
        val listener1 = activeMarkerClickListener

        // Start Collector 2 - this will overwrite the listener on the mock
        val job2 = launch {
            flow.collect { collector2Events.add(it) }
        }
        advanceUntilIdle()
        val listener2 = activeMarkerClickListener

        // Verify they are different listener instances and listener2 hijacked the slot
        assertThat(listener1).isNotEqualTo(listener2)
        assertThat(activeMarkerClickListener).isEqualTo(listener2)

        // Simulate click via the active listener
        activeMarkerClickListener?.onMarkerClick(marker)
        advanceUntilIdle()

        // Only collector 2 should receive the event because it hijacked the single-listener slot
        assertThat(collector1Events).isEmpty()
        assertThat(collector2Events).containsExactly(marker)

        // Cancel collector 1. This triggers awaitClose and clears the listener slot (sets to null)
        job1.cancel()
        advanceUntilIdle()

        // Assert that the active listener slot is now null!
        assertThat(activeMarkerClickListener).isNull()

        // Try to trigger a click again via the active listener (which is now null)
        activeMarkerClickListener?.onMarkerClick(marker)
        advanceUntilIdle()

        // Collector 2 is now BROKEN and receives no further events because Collector 1's cleanup cleared the shared slot!
        assertThat(collector2Events).containsExactly(marker)

        job2.cancel()
    }

    @Test
    public fun testMarkerCollectionInfoWindowClickEvents(): Unit = runTest {
        val deferred = async {
            markerCollection.infoWindowClickEvents().first()
        }
        advanceUntilIdle()
        verify(markerCollection).setOnInfoWindowClickListener(infoWindowClickListener.capture())
        infoWindowClickListener.value.onInfoWindowClick(marker)
        assertThat(deferred.await()).isEqualTo(marker)
    }

    @Test
    public fun testMarkerCollectionInfoWindowLongClickEvents(): Unit = runTest {
        val deferred = async {
            markerCollection.infoWindowLongClickEvents().first()
        }
        advanceUntilIdle()
        verify(markerCollection).setOnInfoWindowLongClickListener(infoWindowLongClickListener.capture())
        infoWindowLongClickListener.value.onInfoWindowLongClick(marker)
        assertThat(deferred.await()).isEqualTo(marker)
    }

    @Test
    public fun testPolylineCollectionClickEvents(): Unit = runTest {
        val deferred = async {
            polylineCollection.clickEvents().first()
        }
        advanceUntilIdle()
        verify(polylineCollection).setOnPolylineClickListener(polylineClickListener.capture())
        polylineClickListener.value.onPolylineClick(polyline)
        assertThat(deferred.await()).isEqualTo(polyline)
    }

    @Test
    public fun testPolygonCollectionClickEvents(): Unit = runTest {
        val deferred = async {
            polygonCollection.clickEvents().first()
        }
        advanceUntilIdle()
        verify(polygonCollection).setOnPolygonClickListener(polygonClickListener.capture())
        polygonClickListener.value.onPolygonClick(polygon)
        assertThat(deferred.await()).isEqualTo(polygon)
    }

    @Test
    public fun testCircleCollectionClickEvents(): Unit = runTest {
        val deferred = async {
            circleCollection.clickEvents().first()
        }
        advanceUntilIdle()
        verify(circleCollection).setOnCircleClickListener(circleClickListener.capture())
        circleClickListener.value.onCircleClick(circle)
        assertThat(deferred.await()).isEqualTo(circle)
    }

    @Test
    public fun testGroundOverlayCollectionClickEvents(): Unit = runTest {
        val deferred = async {
            groundOverlayCollection.clickEvents().first()
        }
        advanceUntilIdle()
        verify(groundOverlayCollection).setOnGroundOverlayClickListener(groundOverlayClickListener.capture())
        groundOverlayClickListener.value.onGroundOverlayClick(groundOverlay)
        assertThat(deferred.await()).isEqualTo(groundOverlay)
    }
}
