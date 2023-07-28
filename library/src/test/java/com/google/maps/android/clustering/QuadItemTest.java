/*
 * Copyright 2015 Google Inc.
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

package com.google.maps.android.clustering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuadItemTest {

    @Test
    public void testAddRemoveUpdateClear() {
        ClusterItem item_1_5 = new TestingItem("title1", 0.1, 0.5);
        TestingItem item_2_3 = new TestingItem("title2", 0.2, 0.3);

        NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo =
                new NonHierarchicalDistanceBasedAlgorithm<>();
        assertTrue(algo.addItem(item_1_5));
        assertTrue(algo.addItem(item_2_3));

        assertEquals(2, algo.getItems().size());

        assertTrue(algo.removeItem(item_1_5));

        assertEquals(1, algo.getItems().size());

        assertFalse(algo.getItems().contains(item_1_5));
        assertTrue(algo.getItems().contains(item_2_3));

        // Update the item still in the algorithm
        item_2_3.setTitle("newTitle");
        assertTrue(algo.updateItem(item_2_3));

        // Try to remove the item that was already removed
        assertFalse(algo.removeItem(item_1_5));

        // Try to update the item that was already removed
        assertFalse(algo.updateItem(item_1_5));

        algo.clearItems();
        assertEquals(0, algo.getItems().size());

        // Test bulk operations
        List<ClusterItem> items = Arrays.asList(item_1_5, item_2_3);
        assertTrue(algo.addItems(items));

        // Try to bulk add items that were already added
        assertFalse(algo.addItems(items));

        assertTrue(algo.removeItems(items));

        // Try to bulk remove items that were already removed
        assertFalse(algo.removeItems(items));
    }

    /**
     * Test if insertion order into the algorithm is the same as returned item order. This matters
     * because we want repeatable clustering behavior when updating model values and re-clustering.
     */
    @Test
    public void testInsertionOrder() {
        NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo =
                new NonHierarchicalDistanceBasedAlgorithm<>();
        for (int i = 0; i < 100; i++) {
            algo.addItem(new TestingItem(Integer.toString(i), 0.0, 0.0));
        }

        assertEquals(100, algo.getItems().size());

        Collection<ClusterItem> items = algo.getItems();
        int counter = 0;
        for (ClusterItem item : items) {
            assertEquals(Integer.toString(counter), item.getTitle());
            counter++;
        }
    }

    private static class TestingItem implements ClusterItem {
        private final LatLng mPosition;
        private String mTitle;

        TestingItem(String title, double lat, double lng) {
            mTitle = title;
            mPosition = new LatLng(lat, lng);
        }

        TestingItem(double lat, double lng) {
            mTitle = "";
            mPosition = new LatLng(lat, lng);
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getSnippet() {
            return null;
        }

        @Nullable
        @Override
        public Float getZIndex() {
            return null;
        }

        public void setTitle(String title) {
            mTitle = title;
        }
    }
}
