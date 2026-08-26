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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests verifying property bindings and interactions on wrapper classes.
 */
@RunWith(RobolectricTestRunner::class)
class MapOverlayTest {

    @Test
    fun testMarkerOverlayPropertiesAndControls() {
        val mockMarker = mockk<Marker>(relaxed = true)
        every { mockMarker.position } returns LatLng(1.0, 2.0)
        every { mockMarker.isVisible } returns true
        every { mockMarker.zIndex } returns 5f
        every { mockMarker.tag } returns "tag1"
        every { mockMarker.title } returns "title"
        every { mockMarker.snippet } returns "snippet"
        every { mockMarker.isDraggable } returns true
        every { mockMarker.alpha } returns 0.8f
        every { mockMarker.rotation } returns 45f
        every { mockMarker.isFlat } returns true
        every { mockMarker.isInfoWindowShown } returns false

        val wrapper = MarkerOverlay(mockMarker) { true }

        assertThat(wrapper.position).isEqualTo(LatLng(1.0, 2.0))
        assertThat(wrapper.isVisible).isTrue()
        assertThat(wrapper.zIndex).isEqualTo(5f)
        assertThat(wrapper.tag).isEqualTo("tag1")
        assertThat(wrapper.title).isEqualTo("title")
        assertThat(wrapper.snippet).isEqualTo("snippet")
        assertThat(wrapper.isDraggable).isTrue()
        assertThat(wrapper.alpha).isEqualTo(0.8f)
        assertThat(wrapper.rotation).isEqualTo(45f)
        assertThat(wrapper.isFlat).isTrue()
        assertThat(wrapper.isInfoWindowShown).isFalse()

        // Mutators
        wrapper.position = LatLng(3.0, 4.0)
        verify { mockMarker.position = LatLng(3.0, 4.0) }

        wrapper.isVisible = false
        verify { mockMarker.isVisible = false }

        wrapper.zIndex = 10f
        verify { mockMarker.zIndex = 10f }

        wrapper.tag = "tag2"
        verify { mockMarker.tag = "tag2" }

        wrapper.title = "new_title"
        verify { mockMarker.title = "new_title" }

        wrapper.snippet = "new_snippet"
        verify { mockMarker.snippet = "new_snippet" }

        wrapper.isDraggable = false
        verify { mockMarker.isDraggable = false }

        wrapper.alpha = 1.0f
        verify { mockMarker.alpha = 1.0f }

        wrapper.rotation = 90f
        verify { mockMarker.rotation = 90f }

        wrapper.isFlat = false
        verify { mockMarker.isFlat = false }

        wrapper.showInfoWindow()
        verify { mockMarker.showInfoWindow() }

        wrapper.hideInfoWindow()
        verify { mockMarker.hideInfoWindow() }

        // Info window & drag listeners
        var dragStarted = false
        var dragging = false
        var dragEnded = false
        wrapper.onDrag(
            onStart = { dragStarted = true },
            onDrag = { dragging = true },
            onEnd = { dragEnded = true }
        )
        wrapper.dragStartListener?.invoke(wrapper)
        wrapper.dragListener?.invoke(wrapper)
        wrapper.dragEndListener?.invoke(wrapper)
        assertThat(dragStarted).isTrue()
        assertThat(dragging).isTrue()
        assertThat(dragEnded).isTrue()

        var infoClicked = false
        wrapper.onInfoWindowClick { infoClicked = true }
        wrapper.infoWindowClickListener?.invoke(wrapper)
        assertThat(infoClicked).isTrue()

        var infoLongClicked = false
        wrapper.onInfoWindowLongClick { infoLongClicked = true }
        wrapper.infoWindowLongClickListener?.invoke(wrapper)
        assertThat(infoLongClicked).isTrue()

        val mockView = mockk<View>()
        wrapper.setCustomInfoWindow(infoWindow = { mockView }, infoContents = { null })
        assertThat(wrapper.infoWindowProvider?.invoke(wrapper)).isEqualTo(mockView)
        assertThat(wrapper.infoContentsProvider?.invoke(wrapper)).isNull()
    }

    @Test
    fun testCircleOverlayProperties() {
        val mockCircle = mockk<Circle>(relaxed = true)
        every { mockCircle.center } returns LatLng(10.0, 20.0)
        every { mockCircle.radius } returns 50.0

        val wrapper = CircleOverlay(mockCircle) { true }
        assertThat(wrapper.position).isEqualTo(LatLng(10.0, 20.0))
        assertThat(wrapper.center).isEqualTo(LatLng(10.0, 20.0))
        assertThat(wrapper.radius).isEqualTo(50.0)

        wrapper.center = LatLng(15.0, 25.0)
        verify { mockCircle.center = LatLng(15.0, 25.0) }

        wrapper.radius = 100.0
        verify { mockCircle.radius = 100.0 }

        wrapper.fillColor = 0xFF0000
        verify { mockCircle.fillColor = 0xFF0000 }

        wrapper.strokeColor = 0x00FF00
        verify { mockCircle.strokeColor = 0x00FF00 }

        every { mockCircle.isClickable } returns true
        wrapper.isClickable = true
        verify { mockCircle.isClickable = true }
        assertThat(wrapper.isClickable).isTrue()

        every { mockCircle.strokePattern } returns null
        wrapper.strokePattern = null
        assertThat(wrapper.strokePattern).isNull()
        verify { mockCircle.strokePattern = null }

        every { mockCircle.zIndex } returns 3f
        wrapper.zIndex = 3f
        assertThat(wrapper.zIndex).isEqualTo(3f)
        verify { mockCircle.zIndex = 3f }

        every { mockCircle.tag } returns "circle_tag"
        wrapper.tag = "circle_tag"
        assertThat(wrapper.tag).isEqualTo("circle_tag")
        verify { mockCircle.tag = "circle_tag" }

        every { mockCircle.isVisible } returns false
        wrapper.isVisible = false
        assertThat(wrapper.isVisible).isFalse()
        verify { mockCircle.isVisible = false }

        assertThat(wrapper.isClickable).isTrue()
        assertThat(wrapper.remove()).isTrue()
    }

    @Test
    fun testPolygonOverlayProperties() {
        val mockPolygon = mockk<Polygon>(relaxed = true)
        val points = listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        every { mockPolygon.points } returns points
        every { mockPolygon.fillColor } returns 0x123456
        every { mockPolygon.strokeColor } returns 0x654321
        every { mockPolygon.strokeWidth } returns 5f
        every { mockPolygon.strokeJointType } returns 2
        every { mockPolygon.isGeodesic } returns true
        every { mockPolygon.isClickable } returns true
        every { mockPolygon.isVisible } returns true
        every { mockPolygon.zIndex } returns 4f
        every { mockPolygon.tag } returns "poly_tag"

        val wrapper = PolygonOverlay(mockPolygon) { true }
        assertThat(wrapper.points).isEqualTo(points)
        assertThat(wrapper.fillColor).isEqualTo(0x123456)
        assertThat(wrapper.strokeColor).isEqualTo(0x654321)
        assertThat(wrapper.strokeWidth).isEqualTo(5f)
        assertThat(wrapper.strokeJointType).isEqualTo(2)
        assertThat(wrapper.isGeodesic).isTrue()
        assertThat(wrapper.isClickable).isTrue()
        assertThat(wrapper.isVisible).isTrue()
        assertThat(wrapper.zIndex).isEqualTo(4f)
        assertThat(wrapper.tag).isEqualTo("poly_tag")

        wrapper.points = points
        verify { mockPolygon.points = points }

        val holes = listOf(points)
        wrapper.holes = holes
        verify { mockPolygon.holes = holes }
        every { mockPolygon.holes } returns holes
        assertThat(wrapper.holes).isEqualTo(holes)

        wrapper.fillColor = 0x123456
        verify { mockPolygon.fillColor = 0x123456 }

        wrapper.strokeColor = 0x654321
        verify { mockPolygon.strokeColor = 0x654321 }

        wrapper.strokeWidth = 5f
        verify { mockPolygon.strokeWidth = 5f }

        wrapper.strokeJointType = 2
        verify { mockPolygon.strokeJointType = 2 }

        wrapper.isGeodesic = true
        verify { mockPolygon.isGeodesic = true }

        every { mockPolygon.strokePattern } returns null
        wrapper.strokePattern = null
        assertThat(wrapper.strokePattern).isNull()
        verify { mockPolygon.strokePattern = null }

        wrapper.isClickable = false
        verify { mockPolygon.isClickable = false }

        wrapper.isVisible = false
        verify { mockPolygon.isVisible = false }

        wrapper.zIndex = 1f
        verify { mockPolygon.zIndex = 1f }

        wrapper.tag = "new_tag"
        verify { mockPolygon.tag = "new_tag" }

        assertThat(wrapper.remove()).isTrue()
    }

    @Test
    fun testPolylineOverlayProperties() {
        val mockPolyline = mockk<Polyline>(relaxed = true)
        val points = listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        every { mockPolyline.points } returns points
        every { mockPolyline.color } returns 0xFF00FF
        every { mockPolyline.width } returns 8f
        every { mockPolyline.jointType } returns 1
        every { mockPolyline.isGeodesic } returns true
        every { mockPolyline.isClickable } returns true
        every { mockPolyline.isVisible } returns true
        every { mockPolyline.zIndex } returns 2f
        every { mockPolyline.tag } returns "line_tag"

        val wrapper = PolylineOverlay(mockPolyline) { true }
        assertThat(wrapper.points).isEqualTo(points)
        assertThat(wrapper.color).isEqualTo(0xFF00FF)
        assertThat(wrapper.width).isEqualTo(8f)
        assertThat(wrapper.jointType).isEqualTo(1)
        assertThat(wrapper.isGeodesic).isTrue()
        assertThat(wrapper.isClickable).isTrue()
        assertThat(wrapper.isVisible).isTrue()
        assertThat(wrapper.zIndex).isEqualTo(2f)
        assertThat(wrapper.tag).isEqualTo("line_tag")

        wrapper.points = points
        verify { mockPolyline.points = points }

        wrapper.color = 0xFF00FF
        verify { mockPolyline.color = 0xFF00FF }

        wrapper.width = 8f
        verify { mockPolyline.width = 8f }

        wrapper.jointType = 1
        verify { mockPolyline.jointType = 1 }

        val cap = com.google.android.gms.maps.model.RoundCap()
        wrapper.startCap = cap
        verify { mockPolyline.startCap = cap }
        every { mockPolyline.startCap } returns cap
        assertThat(wrapper.startCap).isEqualTo(cap)

        wrapper.endCap = cap
        verify { mockPolyline.endCap = cap }
        every { mockPolyline.endCap } returns cap
        assertThat(wrapper.endCap).isEqualTo(cap)

        every { mockPolyline.pattern } returns null
        wrapper.pattern = null
        assertThat(wrapper.pattern).isNull()
        verify { mockPolyline.pattern = null }

        wrapper.isGeodesic = true
        verify { mockPolyline.isGeodesic = true }

        wrapper.isClickable = false
        verify { mockPolyline.isClickable = false }

        wrapper.isVisible = false
        verify { mockPolyline.isVisible = false }

        wrapper.zIndex = 7f
        verify { mockPolyline.zIndex = 7f }

        wrapper.tag = "line_new"
        verify { mockPolyline.tag = "line_new" }

        assertThat(wrapper.remove()).isTrue()
    }

    @Test
    fun testGroundOverlayOverlayProperties() {
        val mockOverlay = mockk<GroundOverlay>(relaxed = true)
        every { mockOverlay.position } returns LatLng(5.0, 5.0)
        every { mockOverlay.width } returns 200f
        every { mockOverlay.height } returns 100f
        every { mockOverlay.bearing } returns 45f
        every { mockOverlay.transparency } returns 0.5f
        every { mockOverlay.isClickable } returns true
        every { mockOverlay.isVisible } returns true
        every { mockOverlay.zIndex } returns 6f
        every { mockOverlay.tag } returns "ground_tag"

        val wrapper = GroundOverlayOverlay(mockOverlay) { true }
        assertThat(wrapper.position).isEqualTo(LatLng(5.0, 5.0))
        assertThat(wrapper.width).isEqualTo(200f)
        assertThat(wrapper.height).isEqualTo(100f)
        assertThat(wrapper.bearing).isEqualTo(45f)
        assertThat(wrapper.transparency).isEqualTo(0.5f)
        assertThat(wrapper.isClickable).isTrue()
        assertThat(wrapper.isVisible).isTrue()
        assertThat(wrapper.zIndex).isEqualTo(6f)
        assertThat(wrapper.tag).isEqualTo("ground_tag")

        wrapper.position = LatLng(6.0, 6.0)
        verify { mockOverlay.position = LatLng(6.0, 6.0) }

        wrapper.height = 150f
        verify { mockOverlay.setDimensions(200f, 150f) }

        val bounds = LatLngBounds(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        wrapper.bounds = bounds
        verify { mockOverlay.setPositionFromBounds(bounds) }
        every { mockOverlay.bounds } returns bounds
        assertThat(wrapper.bounds).isEqualTo(bounds)

        wrapper.bearing = 45f
        verify { mockOverlay.bearing = 45f }

        wrapper.transparency = 0.5f
        verify { mockOverlay.transparency = 0.5f }

        wrapper.isClickable = true
        verify { mockOverlay.isClickable = true }

        wrapper.isVisible = false
        verify { mockOverlay.isVisible = false }

        wrapper.zIndex = 9f
        verify { mockOverlay.zIndex = 9f }

        wrapper.tag = "new_ground"
        verify { mockOverlay.tag = "new_ground" }

        assertThat(wrapper.remove()).isTrue()
    }
}
