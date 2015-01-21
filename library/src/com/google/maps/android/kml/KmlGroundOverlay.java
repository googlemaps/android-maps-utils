package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;

/**
 * Represents a KML Ground Overlay
 */
public class KmlGroundOverlay {

    private LatLngBounds mBounds;

    private String mGroundOverlayImage;

    private final HashMap<String, String> mProperties;

    public KmlGroundOverlay() {
        mBounds = null;
        mGroundOverlayImage = null;
        mProperties = new HashMap<String, String>();
    }

    public String getImageUrl() {
        return mGroundOverlayImage;
    }

    public void setImage(String imageUrl) {
        mGroundOverlayImage = imageUrl;
    }

    public LatLngBounds getBounds() {
        return mBounds;
    }

    public void setLatLngBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    public void setProperty(String propertyName, String propertyValue) {
        mProperties.put(propertyName, propertyValue);
    }

    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    public boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
    }
}
