/*
 * Copyright 2023 Google Inc.
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

import java.util.Random;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PinConfig;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultAdvancedMarkersClusterRenderer;
import com.google.maps.android.utils.demo.model.Person;

import androidx.annotation.NonNull;

/**
 * This sample demonstrates how to make use of the new Advanced Markers.
 */
public class CustomAdvancedMarkerClusteringDemoActivity extends BaseDemoActivity implements
        ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person>,
        OnMapsSdkInitializedCallback {
    private ClusterManager<Person> mClusterManager;
    private final Random mRandom = new Random(1984);

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
            default:
                break;
        }
    }

    private class AdvancedMarkerRenderer extends DefaultAdvancedMarkersClusterRenderer<Person> {

        public AdvancedMarkerRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull Person person,
                                                   @NonNull AdvancedMarkerOptions markerOptions) {
            markerOptions
                    .icon(BitmapDescriptorFactory.fromPinConfig(getPinConfig().build()))
                    .title(person.name);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull Person person, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            marker.setIcon(BitmapDescriptorFactory.fromPinConfig(getPinConfig().build()));
            marker.setTitle(person.name);
        }

        private PinConfig.Builder getPinConfig() {
            PinConfig.Builder pinConfigBuilder = PinConfig.builder();
            pinConfigBuilder.setBackgroundColor(Color.MAGENTA);
            pinConfigBuilder.setBorderColor(getResources().getColor(R.color.colorPrimaryDark));
            return pinConfigBuilder;
        }

        private View addTextAsMarker(int size) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText("I am a cluster of size " + size);
            textView.setBackgroundColor(Color.BLACK);
            textView.setTextColor(Color.YELLOW);
            return textView;
        }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<Person> cluster,
                                               @NonNull AdvancedMarkerOptions markerOptions) {
            markerOptions.iconView(addTextAsMarker(cluster.getSize()));
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<Person> cluster, AdvancedMarker marker) {
            marker.setIconView(addTextAsMarker(cluster.getSize()));
        }


        @Override
        protected boolean shouldRenderAsCluster(@NonNull Cluster<Person> cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
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
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 9.5f));
        }

        // This line is extremely important to initialise the advanced markers. Without it, advanced markers will not work.
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);

        mClusterManager = new ClusterManager<>(this, getMap());
        mClusterManager.setRenderer(new AdvancedMarkerRenderer());
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
        // http://www.flickr.com/photos/sdasmarchives/5036248203/
        mClusterManager.addItem(new Person(position(), "Walter", R.drawable.walter));

        // http://www.flickr.com/photos/usnationalarchives/4726917149/
        mClusterManager.addItem(new Person(position(), "Gran", R.drawable.gran));

        // http://www.flickr.com/photos/nypl/3111525394/
        mClusterManager.addItem(new Person(position(), "Ruth", R.drawable.ruth));

        // http://www.flickr.com/photos/smithsonian/2887433330/
        mClusterManager.addItem(new Person(position(), "Stefan", R.drawable.stefan));

        // http://www.flickr.com/photos/library_of_congress/2179915182/
        mClusterManager.addItem(new Person(position(), "Mechanic", R.drawable.mechanic));

        // http://www.flickr.com/photos/nationalmediamuseum/7893552556/
        mClusterManager.addItem(new Person(position(), "Yeats", R.drawable.yeats));

        // http://www.flickr.com/photos/sdasmarchives/5036231225/
        mClusterManager.addItem(new Person(position(), "John", R.drawable.john));

        // http://www.flickr.com/photos/anmm_thecommons/7694202096/
        mClusterManager.addItem(new Person(position(), "Trevor the Turtle", R.drawable.turtle));

        // http://www.flickr.com/photos/usnationalarchives/4726892651/
        mClusterManager.addItem(new Person(position(), "Teach", R.drawable.teacher));
    }

    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }
}