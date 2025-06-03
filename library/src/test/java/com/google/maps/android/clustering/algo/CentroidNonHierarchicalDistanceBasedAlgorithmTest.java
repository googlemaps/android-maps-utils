package com.google.maps.android.clustering.algo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;

public class CentroidNonHierarchicalDistanceBasedAlgorithmTest {

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
    public void testComputeCentroid() {
        CentroidNonHierarchicalDistanceBasedAlgorithm<TestClusterItem> algo =
                new CentroidNonHierarchicalDistanceBasedAlgorithm<>();

        Collection<TestClusterItem> items = Arrays.asList(
                new TestClusterItem(10.0, 20.0),
                new TestClusterItem(20.0, 30.0),
                new TestClusterItem(30.0, 40.0)
        );

        LatLng centroid = algo.computeCentroid(items);

        assertEquals(20.0, centroid.latitude, 0.0001);
        assertEquals(30.0, centroid.longitude, 0.0001);
    }
}
