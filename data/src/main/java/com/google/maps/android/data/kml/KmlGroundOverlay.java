/*
 * Copyright 2023 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Represents a KML Ground Overlay
 */
public class KmlGroundOverlay {

    private final Map<String, String> mProperties;

    private final GroundOverlayOptions mGroundOverlayOptions;

    private String mImageUrl;

    private LatLngBounds mLatLngBox;

    /**
     * Creates a new Ground Overlay
     *
     * @param imageUrl   url of the ground overlay image
     * @param latLonBox  bounds of the image
     * @param drawOrder  z index of the image
     * @param visibility true if visible, false otherwise
     * @param properties properties hashmap
     * @param rotation   rotation of image
     */
    /* package */ KmlGroundOverlay(String imageUrl, LatLngBounds latLonBox, float drawOrder,
                                   int visibility, HashMap<String, String> properties, float rotation) {
        mGroundOverlayOptions = new GroundOverlayOptions();
        mImageUrl = imageUrl;
        mProperties = properties;
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
     * Gets an image url
     *
     * @return An image url
     */
    public String getImageUrl() {
        return mImageUrl;
    }

    /**
     * Returns boundaries of the ground overlay
     *
     * @return Boundaries of the ground overlay
     */
    public LatLngBounds getLatLngBox() {
        return mLatLngBox;
    }

    /**
     * Gets an iterable of the properties
     *
     * @return Iterable of the properties
     */
    public Iterable<String> getProperties() {
        return mProperties.keySet();
    }

    /**
     * Gets a property value
     *
     * @param keyValue key value of the property
     * @return Value of property
     */
    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    /**
     * Returns a boolean value determining whether the ground overlay has a property
     *
     * @param keyValue Value to retrieve
     * @return True if the property exists, false otherwise
     */
    public boolean hasProperty(String keyValue) {
        return mProperties.get(keyValue) != null;
    }

    /**
     * Gets the ground overlay option of the ground overlay on the map
     *
     * @return GroundOverlayOptions
     */
    /* package */ GroundOverlayOptions getGroundOverlayOptions() {
        return mGroundOverlayOptions;
    }

    @NonNull
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
