/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.clustering.algo;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContinuousZoomEuclideanAlgorithmTest {

    static class TestClusterItem implements ClusterItem {
        private final LatLng position;

        TestClusterItem(double lat, double lng) {
            this.position = new LatLng(lat, lng);
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return position;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getSnippet() {
            return null;
        }

        @Override
        public Float getZIndex() {
            return 0f;
        }
    }

    @Test
    public void testContinuousZoomMergesClosePairAtLowZoomAndSeparatesAtHighZoom() {
        ContinuousZoomEuclideanAlgorithm<TestClusterItem> algo =
                new ContinuousZoomEuclideanAlgorithm<>();

        // Optional: customize threshold if your defaults differ
        // algo.setMaxDistanceBetweenClusteredItems(100);

        Collection<TestClusterItem> items = Arrays.asList(
                new TestClusterItem(10.0, 10.0),
                new TestClusterItem(10.0001, 10.0001), // very close to the first
                new TestClusterItem(20.0, 20.0)        // far away
        );

        algo.addItems(items);

        // At a high zoom, the close pair should be separate (small radius)
        Set<? extends Cluster<TestClusterItem>> highZoom = algo.getClusters(20.0f);
        assertEquals(3, highZoom.size());

        // At a lower zoom, the close pair should merge (larger radius)
        Set<? extends Cluster<TestClusterItem>> lowZoom = algo.getClusters(5.0f);
        assertTrue(lowZoom.size() < 3);

        // And specifically, we expect one cluster of size 2 and one singleton
        boolean hasClusterOfTwo = lowZoom.stream().anyMatch(c -> c.getItems().size() == 2);
        boolean hasClusterOfOne = lowZoom.stream().anyMatch(c -> c.getItems().size() == 1);
        assertTrue(hasClusterOfTwo);
        assertTrue(hasClusterOfOne);
    }
}
