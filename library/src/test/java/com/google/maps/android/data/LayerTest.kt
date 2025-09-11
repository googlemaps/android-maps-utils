/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data

import com.google.android.gms.maps.GoogleMap
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlGroundOverlay
import com.google.maps.android.data.kml.KmlPlacemark
import com.google.maps.android.data.kml.KmlRenderer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LayerTest {

    private class TestLayer<T : Feature> : Layer<T>() {
        override fun addLayerToMap() {}

        // Expose protected members for testing
        fun setRenderer(renderer: Renderer<T>) {
            this.renderer = renderer
        }

        public override fun hasFeatures(): Boolean = super.hasFeatures()

        public override fun hasContainers(): Boolean = super.hasContainers()

        public override fun getContainers(): Iterable<KmlContainer>? = super.getContainers()

        public override fun getGroundOverlays(): Iterable<KmlGroundOverlay>? = super.getGroundOverlays()

        public override fun addFeature(feature: T) {
            super.addFeature(feature)
        }

        public override fun removeFeature(feature: T) {
            super.removeFeature(feature)
        }
    }

    @Test
    fun `removeLayerFromMap calls renderer`() {
        val renderer = mockk<KmlRenderer>(relaxed = true)
        val layer = TestLayer<KmlPlacemark>()
        layer.setRenderer(renderer)
        layer.removeLayerFromMap()
        verify { renderer.removeLayerFromMap() }
    }

    @Test
    fun `setOnFeatureClickListener calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val listener = Layer.OnFeatureClickListener { }
        layer.setOnFeatureClickListener(listener)
        verify { renderer.setOnFeatureClickListener(listener) }
    }

    @Test
    fun `getFeatures calls renderer`() {
        val renderer = mockk<Renderer<Feature>>()
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val features = emptySet<Feature>()
        every { renderer.getFeatures() } returns features
        assertThat(layer.getFeatures()).isSameInstanceAs(features)
    }

    @Test
    fun `getFeature calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val feature = mockk<Feature>()
        every { renderer.getFeature(any()) } returns feature
        assertThat(layer.getFeature(Any())).isSameInstanceAs(feature)
    }

    @Test
    fun `getContainerFeature calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val feature = mockk<Feature>()
        every { renderer.getContainerFeature(any()) } returns feature
        assertThat(layer.getContainerFeature(Any())).isSameInstanceAs(feature)
    }

    @Test
    fun `hasFeatures calls renderer`() {
        val renderer = mockk<Renderer<Feature>>()
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        every { renderer.hasFeatures() } returns true
        assertThat(layer.hasFeatures()).isTrue()
    }

    @Test
    fun `hasContainers calls renderer`() {
        val renderer = mockk<KmlRenderer>()
        val layer = TestLayer<KmlPlacemark>()
        layer.setRenderer(renderer)
        every { renderer.hasNestedContainers() } returns true
        assertThat(layer.hasContainers()).isTrue()
    }

    @Test
    fun `getContainers calls renderer`() {
        val renderer = mockk<KmlRenderer>()
        val layer = TestLayer<KmlPlacemark>()
        layer.setRenderer(renderer)
        val containers = emptyList<KmlContainer>()
        every { renderer.getNestedContainers() } returns containers
        assertThat(layer.getContainers()).isSameInstanceAs(containers)
    }

    @Test
    fun `getGroundOverlays calls renderer`() {
        val renderer = mockk<KmlRenderer>()
        val layer = TestLayer<KmlPlacemark>()
        layer.setRenderer(renderer)
        val overlays = emptySet<KmlGroundOverlay>()
        every { renderer.getGroundOverlays() } returns overlays
        assertThat(layer.getGroundOverlays()).isSameInstanceAs(overlays)
    }

    @Test
    fun `map property calls renderer`() {
        val renderer = mockk<Renderer<Feature>>()
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val map = mockk<GoogleMap>()
        every { renderer.map } returns map
        assertThat(layer.map).isSameInstanceAs(map)
    }

    @Test
    fun `setMap calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val map = mockk<GoogleMap>()
        layer.setMap(map)
        verify { renderer.map = map }
    }

    @Test
    fun `isLayerOnMap property calls renderer`() {
        val renderer = mockk<Renderer<Feature>>()
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        every { renderer.isLayerOnMap } returns true
        assertThat(layer.isLayerOnMap).isTrue()
    }

    @Test
    fun `addFeature calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val feature = mockk<Feature>()
        layer.addFeature(feature)
        verify { renderer.addFeature(feature) }
    }

    @Test
    fun `removeFeature calls renderer`() {
        val renderer = mockk<Renderer<Feature>>(relaxed = true)
        val layer = TestLayer<Feature>()
        layer.setRenderer(renderer)
        val feature = mockk<Feature>()
        layer.removeFeature(feature)
        verify { renderer.removeFeature(feature) }
    }
}