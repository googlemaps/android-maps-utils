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

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Observable
import java.util.Observer

class FeatureTest {

    // Test subclass to access protected members
    private class TestFeature(
        geometry: Geometry<*>?,
        id: String?,
        properties: Map<String, String>?
    ) : Feature(geometry, id, properties) {
        public override var id: String?
            get() = super.id
            set(value) {
                super.id = value
            }

        public override var geometry: Geometry<*>?
            get() = super.geometry
            set(value) {
                super.geometry = value
            }

        public override fun setProperty(property: String, propertyValue: String): String? {
            return super.setProperty(property, propertyValue)
        }

        public override fun removeProperty(property: String): String? {
            return super.removeProperty(property)
        }
    }

    @Test
    fun `getId returns correct id`() {
        var feature: Feature = Feature(null, "Pirate", null)
        assertThat(feature.id).isEqualTo("Pirate")
        feature = Feature(null, null, null)
        assertThat(feature.id).isNull()
    }

    @Test
    fun `properties work as expected`() {
        val properties = mapOf("Color" to "Red", "Width" to "3")
        val feature = Feature(null, null, properties)

        assertThat(feature.hasProperty("llama")).isFalse()
        assertThat(feature.hasProperty("Color")).isTrue()
        assertThat(feature.getProperty("Color")).isEqualTo("Red")
        assertThat(feature.hasProperties()).isTrue()
        assertThat(feature.propertyKeys).containsExactly("Color", "Width")
    }

    @Test
    fun `protected property methods work as expected`() {
        val testFeature = TestFeature(null, null, mutableMapOf("Color" to "Red", "Width" to "3"))

        assertThat(testFeature.removeProperty("Width")).isEqualTo("3")
        assertThat(testFeature.hasProperty("Width")).isFalse()

        assertThat(testFeature.setProperty("Width", "10")).isNull()
        assertThat(testFeature.getProperty("Width")).isEqualTo("10")

        assertThat(testFeature.setProperty("Width", "500")).isEqualTo("10")
        assertThat(testFeature.getProperty("Width")).isEqualTo("500")
    }

    @Test
    fun `geometry works as expected`() {
        val feature = Feature(null, null, null)
        assertThat(feature.hasGeometry()).isFalse()
        assertThat(feature.geometry).isNull()

        val point = Point(LatLng(0.0, 0.0))
        val featureWithPoint = Feature(point, null, null)
        assertThat(featureWithPoint.hasGeometry()).isTrue()
        assertThat(featureWithPoint.geometry).isEqualTo(point)
    }

    @Test
    fun `protected geometry setter works`() {
        val testFeature = TestFeature(null, null, null)
        val point = Point(LatLng(0.0, 0.0))
        testFeature.geometry = point
        assertThat(testFeature.geometry).isEqualTo(point)
    }

    @Test
    fun `observable notifies on change`() {
        val feature = TestFeature(null, null, null)
        val observer = TestObserver()
        feature.addObserver(observer)

        feature.setProperty("key", "value")
        assertThat(observer.wasUpdated).isTrue()
        observer.wasUpdated = false // reset

        feature.removeProperty("key")
        assertThat(observer.wasUpdated).isTrue()
        observer.wasUpdated = false // reset

        feature.geometry = Point(LatLng(1.0, 1.0))
        assertThat(observer.wasUpdated).isTrue()
    }

    class TestObserver : Observer {
        var wasUpdated = false
        override fun update(o: Observable?, arg: Any?) {
            wasUpdated = true
        }
    }
}