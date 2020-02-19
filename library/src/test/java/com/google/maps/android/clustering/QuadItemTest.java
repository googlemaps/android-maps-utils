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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuadItemTest {

    @Test
    public void testRemoval() {
        TestingItem item_1_5 = new TestingItem(0.1, 0.5);
        TestingItem item_2_3 = new TestingItem(0.2, 0.3);

        NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo =
                new NonHierarchicalDistanceBasedAlgorithm<>();
        algo.addItem(item_1_5);
        algo.addItem(item_2_3);

        assertEquals(2, algo.getItems().size());

        algo.removeItem(item_1_5);

        assertEquals(1, algo.getItems().size());

        assertFalse(algo.getItems().contains(item_1_5));
        assertTrue(algo.getItems().contains(item_2_3));
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

    private class TestingItem implements ClusterItem {
        private final LatLng mPosition;
        private final String mTitle;

        TestingItem(String title, double lat, double lng) {
            mTitle = title;
            mPosition = new LatLng(lat, lng);
        }

        TestingItem(double lat, double lng) {
            mTitle = "";
            mPosition = new LatLng(lat, lng);
        }

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
    }
}
