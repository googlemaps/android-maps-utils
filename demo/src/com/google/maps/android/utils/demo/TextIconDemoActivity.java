package com.google.maps.android.utils.demo;

import android.graphics.Bitmap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.TextIconGenerator;

public class TextIconDemoActivity extends BaseDemoActivity {

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8696, 151.2094), 10));

        TextIconGenerator iconFactory = new TextIconGenerator(this);
        addIcon(iconFactory.makeIcon("Default"), new LatLng(-33.8696, 151.2094));

        iconFactory.setStyle(TextIconGenerator.STYLE_BLUE);
        addIcon(iconFactory.makeIcon("Blue style"), new LatLng(-33.9360, 151.2070));

        iconFactory.setRotation(90);
        iconFactory.setStyle(TextIconGenerator.STYLE_RED);
        addIcon(iconFactory.makeIcon("Rotated 90 degrees"), new LatLng(-33.8858, 151.096));

        iconFactory.setContentRotation(-90);
        iconFactory.setStyle(TextIconGenerator.STYLE_PURPLE);
        addIcon(iconFactory.makeIcon("Rotate=90, ContentRotate=-90"), new LatLng(-33.9992, 151.098));

        iconFactory.setRotation(0);
        iconFactory.setContentRotation(90);
        iconFactory.setStyle(TextIconGenerator.STYLE_GREEN);
        addIcon(iconFactory.makeIcon("ContentRotate=90"), new LatLng(-33.7677, 151.244));
    }

    private void addIcon(Bitmap icon, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(icon)).
                position(position);

        getMap().addMarker(markerOptions);
    }
}
