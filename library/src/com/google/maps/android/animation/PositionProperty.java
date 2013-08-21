package com.google.maps.android.animation;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Property;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class PositionProperty extends Property<Marker, LatLng> {
    public PositionProperty() {
        super(LatLng.class, "position");
    }

    @Override
    public void set(Marker marker, LatLng latLng) {
        marker.setPosition(latLng);
    }

    @Override
    public LatLng get(Marker marker) {
        return marker.getPosition();
    }
}
