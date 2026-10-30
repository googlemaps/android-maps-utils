/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.utils.demo;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.Units;
import com.google.maps.android.UnitsConverter;
import java.util.Arrays;
import java.util.Locale;

public class DistanceDemoActivity extends BaseDemoActivity
    implements GoogleMap.OnMarkerDragListener {
  static final LatLng SYDNEY_SOUTH = new LatLng(-33.9046, 151.155);
  static final LatLng SYDNEY_NORTH = new LatLng(-33.8291, 151.248);

  private TextView mTextView;
  private Marker mMarkerA;
  private Marker mMarkerB;
  private Polyline mPolyline;
  private UnitsConverter mUnitsConverter;

  @Override
  protected int getLayoutId() {
    return R.layout.distance_demo;
  }

  @Override
  protected void startDemo(boolean isRestore) {
    mTextView = findViewById(R.id.textView);
    mUnitsConverter = Units.getUnitsConverter(getResources());

    if (!isRestore) {
      getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 10));
    }
    getMap().setOnMarkerDragListener(this);

    mMarkerA = getMap().addMarker(new MarkerOptions().position(SYDNEY_SOUTH).draggable(true));
    mMarkerB = getMap().addMarker(new MarkerOptions().position(SYDNEY_NORTH).draggable(true));
    mPolyline = getMap().addPolyline(new PolylineOptions().geodesic(true));

    Toast.makeText(this, getString(R.string.drag_the_markers), Toast.LENGTH_LONG).show();
    showDistance();
  }

  // [START maps_android_utils_units_java]
  @SuppressLint("SetTextI18n")
  private void showDistance() {
    String formattedDistance =
        formatDistanceBetween(
            mMarkerA.getPosition(), mMarkerB.getPosition(), getResources(), mUnitsConverter);
    mTextView.setText(getString(R.string.distance_format, formattedDistance));
  }

  static String formatDistanceBetween(
      @NonNull LatLng pointA,
      @NonNull LatLng pointB,
      @NonNull Resources resources,
      @NonNull UnitsConverter converter) {
    double distanceMeters = SphericalUtil.computeDistanceBetween(pointA, pointB);
    return converter.toDistanceString(distanceMeters, resources);
  }

  static String formatDistanceBetween(
      @NonNull LatLng pointA,
      @NonNull LatLng pointB,
      @NonNull Locale locale,
      @NonNull UnitsConverter converter) {
    double distanceMeters = SphericalUtil.computeDistanceBetween(pointA, pointB);
    return converter.toDistanceString(distanceMeters, locale);
  }

  // [END maps_android_utils_units_java]

  private void updatePolyline() {
    mPolyline.setPoints(Arrays.asList(mMarkerA.getPosition(), mMarkerB.getPosition()));
  }

  @Override
  public void onMarkerDragEnd(@NonNull Marker marker) {
    showDistance();
    updatePolyline();
  }

  @Override
  public void onMarkerDragStart(@NonNull Marker marker) {}

  @Override
  public void onMarkerDrag(@NonNull Marker marker) {
    showDistance();
    updatePolyline();
  }
}
