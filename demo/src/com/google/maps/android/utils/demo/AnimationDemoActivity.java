package com.google.maps.android.utils.demo;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.animation.Animation;
import com.google.maps.android.animation.SphericalAnimation;
import com.google.maps.android.ui.TextIconGenerator;

public class AnimationDemoActivity extends BaseDemoActivity implements GoogleMap.OnMapClickListener {
    private Marker mClickyMarker;

    @SuppressLint("NewApi")
    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, -180), 3));
        Marker marker = getMap().addMarker(new MarkerOptions().position(new LatLng(-30, -170)).draggable(true));

        ValueAnimator animator = Animation.ofMarkerPosition(marker, new LatLng(30, 170));
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();

        getMap().setOnMapClickListener(this);

        mClickyMarker = getMap().addMarker(new MarkerOptions().position(new LatLng(-20, 175)).
            icon(BitmapDescriptorFactory.fromBitmap(new TextIconGenerator(this).makeIcon("Click the map!"))));
    }

    @SuppressLint("NewApi")
    @Override
    public void onMapClick(LatLng latLng) {
        SphericalAnimation.ofMarkerPosition(mClickyMarker, latLng).start();
    }
}
