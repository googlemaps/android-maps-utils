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

import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;

import java.util.List;

/**
 * A GeoJsonGeometryCollection geometry contains a number of GeoJson Geometry objects.
 */
public class GeoJsonGeometryCollection extends MultiGeometry {
    /**
     * Creates a new GeoJsonGeometryCollection object
     *
     * @param geometries array of Geometry objects to add to the GeoJsonGeometryCollection
     */
    public GeoJsonGeometryCollection(List<Geometry> geometries) {
        super(geometries);
        setGeometryType("GeometryCollection");
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
     * Gets the stored Geometry objects
     *
     * @return stored Geometry objects
     */
    public List<Geometry> getGeometries() {
        return getGeometryObject();
    }
}
