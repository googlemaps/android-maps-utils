/*
 * Copyright 2013 Google Inc.
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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.utils.demo.model.Asset;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;





public class PieChartMarkerClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Asset>, ClusterManager.OnClusterInfoWindowClickListener<Asset>, ClusterManager.OnClusterItemClickListener<Asset>, ClusterManager.OnClusterItemInfoWindowClickListener<Asset> {
    private ClusterManager<Asset> mClusterManager;
    private Random mRandom = new Random(1984);
    private Random r = new Random(2000);
    private String[] names = {"Emma", "Noah", "Olivia", "Liam", "Sophia", "Mason", "Ava", "Jacob", "Isabella", "William"};
    private String[] status = {"active", "semiactive", "nonactive", "inactive"};
    int active;
    int semiActive;
    int inactive;
    int nonactive;
    float values[], realValues[];
    float total = 0;
    private static final Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    /**
     * Draws 3D pieChart inside markers (using IconGenerator).
     */
    private class AssetRenderer extends DefaultClusterRenderer<Asset> {
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mClusterImageView;

        AssetRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View PieChart = getLayoutInflater().inflate(R.layout.piechart_cluster, null);
            mClusterIconGenerator.setContentView(PieChart);
            mClusterImageView = (ImageView) PieChart.findViewById(R.id.image);
        }

        @Override
        protected void onBeforeClusterItemRendered(Asset Asset, MarkerOptions markerOptions) {
            // Draw a single Asset.
            // Set the info window to show their name.
            markerOptions.icon(BitmapDescriptorFactory.fromResource(Asset.marker)).title(Asset.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Asset> cluster, MarkerOptions markerOptions) {
            //Draw 3D PieChart on map
            active = 0;
            semiActive = 0;
            inactive = 0;
            nonactive = 0;
            mClusterIconGenerator.setBackground(TRANSPARENT_DRAWABLE);
            for (Asset p : cluster.getItems()) {
                if (p.marker == 2130837504) {
                    active++;
                } else if (p.marker == 2130837536) {
                    semiActive++;
                } else if (p.marker == 2130837534) {
                    nonactive++;
                } else {
                    inactive++;
                }
            }
            values = new float[]{active, semiActive, nonactive, inactive};
            realValues = Arrays.copyOf(values, values.length);
            values = calculateData(values);
            PieChart pieChart = new PieChart(values, realValues);
            mClusterImageView.setImageDrawable(pieChart);
            Bitmap icon = mClusterIconGenerator.makeIcon(cluster.getSize() + "");
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).anchor(.5f, .5f);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Asset> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Asset> cluster) {
        // Does nothing.
    }

    @Override
    public boolean onClusterItemClick(Asset item) {
        // Does nothing.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Asset item) {
        // Does nothing.
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.988612, 53.459411), 4.5f));

        mClusterManager = new ClusterManager<>(this, getMap());
        mClusterManager.setRenderer(new AssetRenderer());
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
    }

    private void addItems() {
        for (int i = 0; i < 300; i++) {
            mClusterManager.addItem(new Asset(position(), randomStatus(), randomImage()));
        }
    }

    private int randomImage() {
        int min = 0;
        int max = 4;
        return getDrawableId(status[r.nextInt(max - min) + min]);
    }

    private LatLng position() {
        return new LatLng(random(30.325877, 36.615905), random(60.648976, 47.465382));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }

    private String randomStatus() {
        int min = 0;
        int max = 9;
        return "Driver: " + names[r.nextInt(max - min) + min];
    }

    private int getDrawableId(String name) {
        try {
            Field field = R.drawable.class.getField(name);
            return field.getInt(null);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return -1;
    }

    private float[] calculateData(float[] data) {
        total = 0;
        for (float aData : data) {
            total += aData;
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = 360 * (data[i] / total);
        }
        return data;

    }

}

