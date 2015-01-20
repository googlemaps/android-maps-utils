package com.google.maps.android.kml;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lavenderch on 1/20/15.
 */
public class KmlGroundOverlay {

    private LatLngBounds mBounds;
    private String mGroundOverlayImage;

    private HashMap<String, String> mProperties;

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

    public Iterator<Map.Entry<String, String>> getProperties() {
        return mProperties.entrySet().iterator();
    }

    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    public boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
    }
}
