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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.AnimationUtil;

import androidx.annotation.NonNull;

/**
 * Simple activity demonstrating the AnimationUtil.
 */
public class AnimationUtilDemoActivity extends BaseDemoActivity implements GoogleMap.OnMarkerClickListener {

    LatLng sydney = new LatLng(-33.852, 151.211);
    Marker currentMarker;

    @Override
    protected void startDemo(boolean isRestore) {
        getMap().setOnMarkerClickListener(this);

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        currentMarker = getMap().addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        getMap().moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public double getRandomNumber(int min, int max) {
        return ((Math.random() * (max - min)) + min);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        LatLng newRandomLat = new LatLng(getRandomNumber(-34, -33), getRandomNumber(150, 151));
        AnimationUtil.animateMarkerTo(currentMarker, newRandomLat);
        return false;
    }
}