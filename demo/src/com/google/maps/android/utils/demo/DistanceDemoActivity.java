package com.google.maps.android.utils.demo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;

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
    protected void startDemo() {
        mTextView = (TextView) findViewById(R.id.textView);

        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 10));
        getMap().setOnMarkerDragListener(this);

        mMarkerA = getMap().addMarker(new MarkerOptions().position(new LatLng(-33.9046, 151.155)).draggable(true));
        mMarkerB = getMap().addMarker(new MarkerOptions().position(new LatLng(-33.8291, 151.248)).draggable(true));
        mPolyline = getMap().addPolyline(new PolylineOptions().geodesic(true));

        Toast.makeText(this, "Drag the markers!", Toast.LENGTH_LONG).show();
        showDistance();
    }

    private void showDistance() {
        double distance = SphericalUtil.computeDistanceBetween(mMarkerA.getPosition(), mMarkerB.getPosition());
        mTextView.setText("The markers are " + formatNumber(distance) + " apart.");
    }

    private void updatePolyline() {
        mPolyline.setPoints(Arrays.asList(mMarkerA.getPosition(), mMarkerB.getPosition()));
    }

    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        showDistance();
        updatePolyline();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        showDistance();
        updatePolyline();
    }
}
