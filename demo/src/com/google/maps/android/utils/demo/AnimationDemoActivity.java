package com.google.maps.android.utils.demo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.animation.Animation;
import com.google.maps.android.animation.SphericalAnimation;

public class AnimationDemoActivity extends BaseDemoActivity implements GoogleMap.OnMapClickListener {
    private Marker mClickyMarker;

    @SuppressLint("NewApi")
    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 10));
        Marker marker = getMap().addMarker(new MarkerOptions().position(new LatLng(-33.9046, 151.155)).draggable(true));

        ObjectAnimator animator = Animation.ofMarkerPosition(marker, new LatLng(-33.8291, 151.248));
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();

        getMap().setOnMapClickListener(this);

        mClickyMarker = getMap().addMarker(new MarkerOptions().position(new LatLng(-34.012051, 151.2686)));
    }

    @SuppressLint("NewApi")
    @Override
    public void onMapClick(LatLng latLng) {
        SphericalAnimation.ofMarkerPosition(mClickyMarker, latLng).start();
    }
}
