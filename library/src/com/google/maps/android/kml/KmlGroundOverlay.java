package com.google.maps.android.kml;

import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a KML Ground Overlay
 */
public class KmlGroundOverlay {

    private final Map<String, String> mProperties;

    private final GroundOverlayOptions mGroundOverlayOptions;

    private String mImageUrl;

    private LatLngBounds mLatLngBox;

    private String mColor;

    public KmlGroundOverlay(String imageUrl, LatLngBounds latLonBox, float drawOrder,
            int visibility, String color, HashMap<String, String> properties, float rotation) {
        mGroundOverlayOptions = new GroundOverlayOptions();
        mImageUrl = imageUrl;
        mProperties = properties;
        mColor = color;
        if (latLonBox == null) {
            throw new IllegalArgumentException("No LatLonBox given");
        }
        mLatLngBox = latLonBox;
        mGroundOverlayOptions.positionFromBounds(latLonBox);
        mGroundOverlayOptions.bearing(rotation);
        mGroundOverlayOptions.zIndex(drawOrder);
        mGroundOverlayOptions.visible(visibility != 0);
    }

    public String getColor() {
        return mColor;
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
    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    public boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
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
