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

import junit.framework.TestCase;

public class QuadItemTest extends TestCase {

    public class TestingItem implements ClusterItem {
        private final LatLng mPosition;

        public TestingItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }

    public void setUp() {
        // nothing to setup
    }

    public void testRemoval() {
        TestingItem item_1_5 = new TestingItem(0.1, 0.5);
        TestingItem item_2_3 = new TestingItem(0.2, 0.3);

        NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo
                = new NonHierarchicalDistanceBasedAlgorithm<ClusterItem>();
        algo.addItem(item_1_5);
        algo.addItem(item_2_3);

        assertEquals(2, algo.getItems().size());

        algo.removeItem(item_1_5);

        assertEquals(1, algo.getItems().size());

        assertFalse(algo.getItems().contains(item_1_5));
        assertTrue(algo.getItems().contains(item_2_3));
    }
}
