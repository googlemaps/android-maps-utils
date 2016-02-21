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
import com.google.maps.android.clustering.algo.StaticCluster;

import junit.framework.TestCase;

public class StaticClusterTest extends TestCase {

    private StaticCluster<ClusterItem> mCluster;

    public void setUp() {
        mCluster = new StaticCluster<ClusterItem>(new LatLng(0.1, 0.5));
    }

    public void testEquality() {
        StaticCluster<ClusterItem> cluster_1_5 = new StaticCluster<ClusterItem>(
                new LatLng(0.1, 0.5));

        assertEquals(cluster_1_5, mCluster);
        assertNotSame(cluster_1_5, mCluster);
        assertEquals(cluster_1_5.hashCode(), mCluster.hashCode());
    }

    public void testUnequality() {
        StaticCluster<ClusterItem> cluster_2_3 = new StaticCluster<ClusterItem>(
                new LatLng(0.2, 0.3));

        assertFalse(mCluster.equals(cluster_2_3));
        assertFalse(cluster_2_3.hashCode() == mCluster.hashCode());
    }
}
