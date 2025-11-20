/*
 * Copyright 2020 Google Inc.
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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.LineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML LineString. Contains a single array of coordinates.
 */
public class KmlLineString extends LineString {
    private final ArrayList<Double> mAltitudes;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     * @param altitudes   array of altitudes
     */
    public KmlLineString(ArrayList<LatLng> coordinates, ArrayList<Double> altitudes) {
        super(coordinates);

        this.mAltitudes = altitudes;
    }

    /**
     * Gets the altitudes
     *
     * @return ArrayList of Double
     */
    public ArrayList<Double> getAltitudes() {
        return mAltitudes;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public ArrayList<LatLng> getGeometryObject() {
        List<LatLng> coordinatesList = super.getGeometryObject();
        return new ArrayList<>(coordinatesList);
    }
}
