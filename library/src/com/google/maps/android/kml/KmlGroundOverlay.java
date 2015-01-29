package com.google.maps.android.kml;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;

/**
 * Represents a KML Ground Overlay
 */
public class KmlGroundOverlay {

    private final HashMap<String, String> mProperties;

    private final GroundOverlayOptions mGroundOverlayOptions;

    private String mImageUrl;

    private LatLngBounds mLatLngBox;

    private final static int HSV_VALUES = 3;

    private final static int HUE_VALUE = 0;

    private String mGroundOverlayColor;


    public KmlGroundOverlay() {
        mImageUrl = null;
        mLatLngBox = null;
        mGroundOverlayColor = null;
        mProperties = new HashMap<String, String>();
        mGroundOverlayOptions = new GroundOverlayOptions();
    }

    public void setColor(String stringColor) {
        mGroundOverlayColor = stringColor;
    }

    public String getColor() {
        return mGroundOverlayColor;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    /* package */ void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public LatLngBounds getLatLngBox() {
        return mLatLngBox;
    }

    /* package */ void setLatLngBox(LatLngBounds latLngBox) {
        mLatLngBox = latLngBox;
        mGroundOverlayOptions.positionFromBounds(latLngBox);
    }

    public void setProperty(String propertyName, String propertyValue) {
        mProperties.put(propertyName, propertyValue);
    }

    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    /* package */ void setProperties(HashMap<String, String> properties) {
        mProperties.putAll(properties);
    }

    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    public boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
    }

    public void setDrawOrder(float zIndex) {
        mGroundOverlayOptions.zIndex(zIndex);
    }

    public void setVisibility(int visibility) {
        if (visibility == 0) {
            mGroundOverlayOptions.visible(false);
        }
    }

    public void setRotation(float rotation) {
        if (rotation > 0.0 && rotation <= 180.0) {
            mGroundOverlayOptions.bearing(rotation + 180);
        } else if (rotation < 0.0 && rotation >= -180.0) {
            mGroundOverlayOptions.bearing(Math.abs(rotation));
        } else {
            mGroundOverlayOptions.bearing(rotation);
        }
    }

    public GroundOverlayOptions getGroundOverlayOptions() {
        return mGroundOverlayOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GroundOverlay").append("{");
        sb.append("\n properties=").append(mProperties);
        sb.append(",\n image url=").append(mImageUrl);
        sb.append(",\n LatLngBox=").append(mLatLngBox);
        sb.append("\n}\n");
        return sb.toString();
    }
}
