/*
 * Copyright 2026 Google LLC
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;

import androidx.annotation.NonNull;

public class DistanceDemoActivity extends BaseDemoActivity implements GoogleMap.OnMarkerDragListener {
    private TextView mTextView;
    private Marker mMarkerA;
    private Marker mMarkerB;
    private Polyline mPolyline;

    @Override
    protected int getLayoutId() {
        return R.layout.distance_demo;
    }

    @Override
    protected void startDemo(boolean isRestore) {
        mTextView = findViewById(R.id.textView);

        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 10));
        }
        getMap().setOnMarkerDragListener(this);

        mMarkerA = getMap().addMarker(new MarkerOptions().position(new LatLng(-33.9046, 151.155)).draggable(true));
        mMarkerB = getMap().addMarker(new MarkerOptions().position(new LatLng(-33.8291, 151.248)).draggable(true));
        mPolyline = getMap().addPolyline(new PolylineOptions().geodesic(true));

        Toast.makeText(this, getString(R.string.drag_the_markers), Toast.LENGTH_LONG).show();
        showDistance();
    }

    @SuppressLint("SetTextI18n")
    private void showDistance() {
        double distance = SphericalUtil.computeDistanceBetween(mMarkerA.getPosition(), mMarkerB.getPosition());
        mTextView.setText(getString(R.string.distance_format, formatNumber(distance)));
    }

    private void updatePolyline() {
        mPolyline.setPoints(Arrays.asList(mMarkerA.getPosition(), mMarkerB.getPosition()));
    }

    private String formatNumber(double distance) {
        String unit = getString(R.string.unit_m);
        if (distance < 1) {
            distance *= 1000;
            unit = getString(R.string.unit_mm);
        } else if (distance > 1000) {
            distance /= 1000;
            unit = getString(R.string.unit_km);
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        showDistance();
        updatePolyline();
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        showDistance();
        updatePolyline();
    }
}
