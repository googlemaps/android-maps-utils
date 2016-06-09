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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.utils.demo.model.MyItem;

public class BigClusteringDemoActivity extends BaseDemoActivity {

    private ClusterManager<MyItem> mClusterManager;

    private final MenuItem.OnMenuItemClickListener menuAddMoreItemClickListener = new MenuItem.OnMenuItemClickListener() {

        final Random random = new Random();

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            // get a random item within the visible region
            final VisibleRegion visibleRegion = getMap().getProjection().getVisibleRegion();

            MyItem item;
            do {
                int randidx = random.nextInt(mClusterManager.getMarkerCollection().getItemSize());
                item = mClusterManager.getMarkerCollection().getItem(randidx);
            } while (!visibleRegion.latLngBounds.contains(item.getPosition()));

            List<MyItem> newItems = new ArrayList<>(10);

            for (int i = 1; i <= 10; i++) {
                double offset = i / 600d;

                LatLng position = item.getPosition();
                double lat = position.latitude + (offset * ((i % (random.nextInt(9) + 1) == 0) ? -1 : 1));
                double lng = position.longitude + (offset * ((i % (random.nextInt(9) + 1) == 0) ? -1 : 1));
                MyItem offsetItem = new MyItem(lat, lng);
                newItems.add(offsetItem);
            }

            mClusterManager.getMarkerCollection().addAllItems(newItems);
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add more").setOnMenuItemClickListener(menuAddMoreItemClickListener);
        return true;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new ClusterManager<MyItem>(this, getMap());

        getMap().setOnCameraChangeListener(mClusterManager);
        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        List<MyItem> items = new MyItemReader().read(inputStream);
        List<MyItem> offsetItems = new ArrayList<>(items.size() * 10);
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            for (MyItem item : items) {
                LatLng position = item.getPosition();
                double lat = position.latitude + offset;
                double lng = position.longitude + offset;
                MyItem offsetItem = new MyItem(lat, lng);
                offsetItems.add(offsetItem);
            }
        }

        mClusterManager.getMarkerCollection().setItems(offsetItems);
    }
}