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

package com.google.maps.android.collections

import com.google.android.gms.maps.GoogleMap
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [MapObjectManager].
 */
@RunWith(RobolectricTestRunner::class)
class MapObjectManagerTest {

    private class TestObject(val id: String)

    private class ConcreteManager(map: GoogleMap) :
        MapObjectManager<TestObject, ConcreteManager.Collection>(map) {

        val removedObjects = mutableListOf<TestObject>()

        override fun setListenersOnUiThread() {}

        override fun newCollection(): Collection = Collection()

        override fun removeObjectFromMap(mapObject: TestObject) {
            removedObjects.add(mapObject)
        }

        inner class Collection : MapObjectManager<TestObject, Collection>.Collection() {
            fun addObject(obj: TestObject) {
                super.add(obj)
            }

            fun testGetObjects(): kotlin.collections.Collection<TestObject> = getObjects()
        }
    }

    private lateinit var map: GoogleMap
    private lateinit var manager: ConcreteManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        manager = ConcreteManager(map)
    }

    @Test
    fun testCollectionCreationAndRetrieval() {
        val col1 = manager.newCollection("col1")
        assertThat(manager.getCollection("col1")).isSameInstanceAs(col1)
        assertThat(manager.getCollection("nonExistent")).isNull()

        assertThrows(IllegalArgumentException::class.java) {
            manager.newCollection("col1")
        }
    }

    @Test
    fun testAddAndRemoveObjectLifecycle() {
        val col = manager.newCollection()
        val obj = TestObject("1")

        col.addObject(obj)
        assertThat(col.testGetObjects()).containsExactly(obj)

        // Base remove
        assertThat(manager.remove(obj)).isTrue()
        assertThat(col.testGetObjects()).isEmpty()
        assertThat(manager.removedObjects).containsExactly(obj)

        // Removing already removed object
        assertThat(manager.remove(obj)).isFalse()
        assertThat(manager.remove(null)).isFalse()
    }

    @Test
    fun testCollectionClear() {
        val col = manager.newCollection()
        val obj1 = TestObject("1")
        val obj2 = TestObject("2")

        col.addObject(obj1)
        col.addObject(obj2)
        assertThat(col.testGetObjects()).hasSize(2)

        col.clear()
        assertThat(col.testGetObjects()).isEmpty()
        assertThat(manager.removedObjects).containsExactly(obj1, obj2)
    }
}
