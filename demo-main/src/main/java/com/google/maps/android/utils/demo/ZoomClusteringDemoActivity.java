/*
 * Copyright 2021 Google Inc.
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

package com.google.maps.android.utils.demo;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.utils.demo.model.MyItem;

import java.util.Set;

/**
 * Demonstrates how to force re-rendering of clusters even when the contents don't change. For
 * example, when changing zoom levels.
 */
public class ZoomClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterInfoWindowClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterItemInfoWindowClickListener<MyItem> {

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String title = cluster.getItems().iterator().next().getTitle();
        Toast.makeText(this, cluster.getSize() + " (including " + title + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MyItem> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(MyItem item) {
        // Does nothing, but you could go into a user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyItem item) {
        // Does nothing, but you could go into a user's profile page, for example.
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.528146, 73.797726), 9.5f));
        }

        ClusterManager<MyItem> clusterManager = new ClusterManager<>(this, getMap());
        getMap().setOnCameraIdleListener(clusterManager);

        // Initialize renderer
        ZoomBasedRenderer renderer = new ZoomBasedRenderer(this, getMap(), clusterManager);
        clusterManager.setRenderer(renderer);

        // Set click listeners
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);

        String snippet = "This item wouldn't have changed to a marker if we didn't override shouldRenderAsCluster() AND shouldRender()";

        // Add items
        clusterManager.addItem(new MyItem(18.528146, 73.797726, "Loc1", snippet));
        clusterManager.addItem(new MyItem(18.545723, 73.917202, "Loc2", snippet));
    }

    private class ZoomBasedRenderer extends DefaultClusterRenderer<MyItem> implements GoogleMap.OnCameraIdleListener {
        private Float zoom = 15f;
        private Float oldZoom;
        private static final float ZOOM_THRESHOLD = 12f;

        public ZoomBasedRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        /**
         * The {@link ClusterManager} will call the {@link this.onCameraIdle()} implementation of
         * any Renderer that implements {@link GoogleMap.OnCameraIdleListener} <i>before</i>
         * clustering and rendering takes place. This allows us to capture metrics that may be
         * useful for clustering, such as the zoom level.
         */
        @Override
        public void onCameraIdle() {
            // Remember the previous zoom level, capture the new zoom level.
            oldZoom = zoom;
            zoom = getMap().getCameraPosition().zoom;
        }

        /**
         * You can override this method to control when the cluster manager renders a group of
         * items as a cluster (vs. as a set of individual markers).
         * <p>
         * In this case, we want single markers to show up as a cluster when zoomed out, but
         * individual markers when zoomed in.
         *
         * @param cluster cluster to examine for rendering
         * @return true when zoom level is less than the threshold (show as cluster when zoomed out),
         * and false when the the zoom level is more than or equal to the threshold (show as marker
         * when zoomed in)
         */
        @Override
        protected boolean shouldRenderAsCluster(@NonNull Cluster<MyItem> cluster) {
            // Show as cluster when zoom is less than the threshold, otherwise show as marker
            return zoom < ZOOM_THRESHOLD;
        }

        /**
         * You can override this method to control optimizations surrounding rendering. The default
         * implementation in the library simply checks if the new clusters are equal to the old
         * clusters, and if so, it returns false to avoid re-rendering the same content.
         * <p>
         * However, in our case we need to change this behavior. As defined in
         * {@link this.shouldRenderAsCluster()}, we want an item to render as a cluster above a
         * certain zoom level and as a marker below a certain zoom level <i>even if the contents of
         * the clusters themselves did not change</i>. In this case, we need to override this method
         * to implement this new optimization behavior.
         *
         * Note that always returning true from this method could potentially have negative
         * performance implications as clusters will be re-rendered on each pass even if they don't
         * change.
         *
         * @param oldClusters The clusters from the previous iteration of the clustering algorithm
         * @param newClusters The clusters from the current iteration of the clustering algorithm
         * @return true if the new clusters should be rendered on the map, and false if they should
         * not.
         */
        @Override
        protected boolean shouldRender(@NonNull Set<? extends Cluster<MyItem>> oldClusters, @NonNull Set<? extends Cluster<MyItem>> newClusters) {
            if (crossedZoomThreshold(oldZoom, zoom)) {
                // Render when the zoom level crosses the threshold, even if the clusters don't change
                return true;
            } else {
                // If clusters didn't change, skip render for optimization using default super implementation
                return super.shouldRender(oldClusters, newClusters);
            }
        }

        /**
         * Returns true if the transition between the two zoom levels crossed a defined threshold,
         * false if it did not.
         *
         * @param oldZoom zoom level from the previous time the camera stopped moving
         * @param newZoom zoom level from the most recent time the camera stopped moving
         * @return true if the transition between the two zoom levels crossed a defined threshold,
         * false if it did not.
         */
        private boolean crossedZoomThreshold(Float oldZoom, Float newZoom) {
            if (oldZoom == null || newZoom == null) {
                return true;
            }
            return (oldZoom < ZOOM_THRESHOLD && newZoom > ZOOM_THRESHOLD) ||
                    (oldZoom > ZOOM_THRESHOLD && newZoom < ZOOM_THRESHOLD);
        }
    }
}
