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
package com.google.maps.android.data.geojson;

import com.google.android.libraries.maps.model.LatLng;
import com.google.maps.android.data.LineString;

import java.util.List;

/**
 * A GeoJsonLineString geometry represents a number of connected {@link
 * com.google.android.libraries.maps.model.LatLng}s.
 */
public class GeoJsonLineString extends LineString {
    private final List<Double> mAltitudes;

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates list of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString(List<LatLng> coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates array of coordinates
     * @param altitudes   array of altitudes
     */
    public GeoJsonLineString(List<LatLng> coordinates, List<Double> altitudes) {
        super(coordinates);

        this.mAltitudes = altitudes;
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType() {
        return getGeometryType();
    }

    /**
     * Gets the coordinates of the GeoJsonLineString
     *
     * @return list of coordinates of the GeoJsonLineString
     */
    public List<LatLng> getCoordinates() {
        return getGeometryObject();
    }

    /**
     * Gets the altitudes of the GeoJsonLineString
     *
     * @return list of altitudes of the GeoJsonLineString
     */
    public List<Double> getAltitudes() {
        return mAltitudes;
    }
}
