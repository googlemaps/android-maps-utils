/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.data;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Observable;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlStyle KmlStyle},
 * {@link com.google.maps.android.data.geojson.GeoJsonPointStyle GeoJsonPointStyle},
 * {@link com.google.maps.android.data.geojson.GeoJsonLineStringStyle GeoJsonLineStringStyle}
 * and {@link com.google.maps.android.data.geojson.GeoJsonPolygonStyle GeoJsonPolygonStyle}
 */
public abstract class Style extends Observable {

    protected MarkerOptions mMarkerOptions;

    protected PolylineOptions mPolylineOptions;

    protected PolygonOptions mPolygonOptions;

    /**
     * Creates a new Style object
     */
    public Style() {
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.clickable(true);
        mPolygonOptions = new PolygonOptions();
        mPolygonOptions.clickable(true);
    }

    /**
     * Gets the rotation of a marker in degrees clockwise about the marker's anchor
     *
     * @return rotation of the Point
     */
    public float getRotation() {
        return mMarkerOptions.getRotation();
    }

    /**
     * Sets the rotation / heading of the Point in degrees clockwise about the marker's anchor
     *
     * @param rotation Decimal representation of the rotation value of the Point
     */
    public void setMarkerRotation(float rotation) {
        mMarkerOptions.rotation(rotation);
    }

    /**
     * Sets the hotspot / anchor point of a marker
     *
     * @param x      x point of a marker position
     * @param y      y point of a marker position
     * @param xUnits units in which the x value is specified
     * @param yUnits units in which the y value is specified
     */
    public void setMarkerHotSpot(float x, float y, String xUnits, String yUnits) {
        float xAnchor = 0.5f;
        float yAnchor = 1.0f;

        // Set x coordinate
        if (xUnits.equals("fraction")) {
            xAnchor = x;
        }
        if (yUnits.equals("fraction")) {
            yAnchor = y;
        }

        mMarkerOptions.anchor(xAnchor, yAnchor);
    }

    /**
     * Sets the width of the LineString in screen pixels
     *
     * @param width width value of the LineString
     */
    public void setLineStringWidth(float width) {
        mPolylineOptions.width(width);
    }

    /**
     * Sets the stroke width of the Polygon in screen pixels
     *
     * @param strokeWidth stroke width value of the Polygon
     */
    public void setPolygonStrokeWidth(float strokeWidth) {
        mPolygonOptions.strokeWidth(strokeWidth);
    }

    /**
     * Sets the fill color of the Polygon as a 32-bit ARGB color
     *
     * @param fillColor fill color value of the Polygon
     */
    public void setPolygonFillColor(int fillColor) {
        mPolygonOptions.fillColor(fillColor);
    }
}
