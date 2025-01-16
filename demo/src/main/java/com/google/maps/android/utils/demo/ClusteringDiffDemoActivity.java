/*
 * Copyright 2025 Google LLC
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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRendererMultipleItems;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.utils.demo.model.Person;

import java.util.ArrayList;
import java.util.List;

enum City {
    ENFIELD(new LatLng(51.6524, -0.0838), "Enfield"),
    ILFORD(new LatLng(51.5590, -0.0815), "Ilford"),
    LONDON(new LatLng(51.5074, -0.1278), "London");

    public final LatLng latLng;
    public final String label;

    City(LatLng latLng, String label) {
        this.latLng = latLng;
        this.label = label;
    }
}

/**
 * Demonstrates how to apply a diff to the current Cluster
 */
public class ClusteringDiffDemoActivity extends BaseDemoActivity
        implements ClusterManager.OnClusterClickListener<Person>,
        ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterItemClickListener<Person>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Person> {

    private final LatLng midpoint = getMidpoint();
    private ClusterManager<Person> mClusterManager;
    private Person itemToUpdate = new Person(City.ENFIELD.latLng, "Teach", R.drawable.teacher);
    private int currentLocationIndex = 0;

    protected int getLayoutId() {
        return R.layout.map_with_floating_button;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        super.onMapReady(map);
        findViewById(R.id.fab_rotate_location).setOnClickListener(v -> rotateLocation());
        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 12));
    }

    private LatLng getMidpoint() {
        double latitude = 0.0;
        double longitude = 0.0;

        for (City city: City.values()) {
            latitude += city.latLng.latitude;
            longitude += city.latLng.longitude;
        }

        int numCities = City.values().length;

        return new LatLng(latitude / numCities, longitude / numCities);
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
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
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
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 6));
        }

        mClusterManager = new ClusterManager<>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
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
        // Marker in Enfield
        mClusterManager.addItem(new Person(City.ENFIELD.latLng, "John", R.drawable.john));

        // Marker in the center of London
        itemToUpdate = new Person(City.LONDON.latLng, "Teach", R.drawable.teacher);
        mClusterManager.addItem(itemToUpdate);
    }

    private void rotateLocation() {
        // Update the current index to cycle through locations (0 = Enfield, 1 = Olford, 2 = London)
        currentLocationIndex = (currentLocationIndex + 1) % City.values().length;

        City nextCity = City.values()[currentLocationIndex];

        LatLng newLocation = nextCity.latLng;
        String cityName = nextCity.label;

        Log.d("ClusterTest", "Item rotated to: " + newLocation.toString() + ", City: " + cityName);

        if (itemToUpdate != null) {
            itemToUpdate = new Person(newLocation, "Teach", R.drawable.teacher);
            mClusterManager.updateItem(itemToUpdate); // Update the marker
            mClusterManager.cluster();
        }
    }

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    @SuppressLint("InflateParams")
    private class PersonRenderer extends ClusterRendererMultipleItems<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull Person person, @NonNull MarkerOptions markerOptions) {
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions.icon(getItemIcon(person)).title(person.name);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull Person person, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            marker.setIcon(getItemIcon(person));
            marker.setTitle(person.name);
        }

        /**
         * Get a descriptor for a single person (i.e., a marker outside a cluster) from their
         * profile photo to be used for a marker icon
         *
         * @param person person to return an BitmapDescriptor for
         * @return the person's profile photo as a BitmapDescriptor
         */
        private BitmapDescriptor getItemIcon(Person person) {
            mImageView.setImageResource(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<Person> cluster, @NonNull MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.icon(getClusterIcon(cluster));
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<Person> cluster, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterRendered() (to update cached markers)
            marker.setIcon(getClusterIcon(cluster));
        }

        /**
         * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
         * method runs on the UI thread. Don't spend too much time in here (like in this example).
         *
         * @param cluster cluster to draw a BitmapDescriptor for
         * @return a BitmapDescriptor representing a cluster
         */
        private BitmapDescriptor getClusterIcon(Cluster<Person> cluster) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = ResourcesCompat.getDrawable(getBaseContext().getResources(), p.profilePhoto, null);
                if (drawable != null) {
                    drawable.setBounds(0, 0, width, height);
                }
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected boolean shouldRenderAsCluster(@NonNull Cluster<Person> cluster) {
            return cluster.getSize() >= 2;
        }
    }
}