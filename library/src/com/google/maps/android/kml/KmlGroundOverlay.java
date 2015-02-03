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

    /* package */ KmlGroundOverlay(String imageUrl, LatLngBounds latLonBox, float drawOrder,
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

    /**
     * Gets the color of the ground overlay
     *
     * @return String representation of a hexadecimal value
     */
    /* package */ String getColor() {
        return mColor;
    }

    /**
     * Gets the image url of the image used for the ground overlay
     *
     * @return  Image url of the ground overlay
     */
    /* package */ String getImageUrl() {
        return mImageUrl;
    }

    /**
     * Sets the image url of the image used for the ground overlay
     *
     * @param imageUrl  Image url for the ground overlay
     */
    /* package */ void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    /**
     * Returns a latlngbounds representing the outer boundaries of the ground overlay
     *
     * @return latlngbound representing the outer boundary of the ground overlay
     */
    /* package */ LatLngBounds getLatLngBox() {
        return mLatLngBox;
    }

    /**
     * Gets an iterable of the properties
     *
     * @return  Iterable of the properties of the ground overlay
     */
    /* package */ Iterable getProperties() {
        return mProperties.entrySet();
    }

    /**
     * Gets a property value based on key
     *
     * @param keyValue key value of the property
     * @return Value of property
     */
    /* package */ String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    /**
     * Returns a boolean value determining whether the ground overlay has a property
     * @param keyValue  Value to retrieve
     * @return  True if the property exists, false otherwise
     */
    /* package */ boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
    }

    /**
     * Gets the ground overlay option of the ground overlay on the map
     * @return  GroundOverlayOptions
     */
    /* package */ GroundOverlayOptions getGroundOverlayOptions() {
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
