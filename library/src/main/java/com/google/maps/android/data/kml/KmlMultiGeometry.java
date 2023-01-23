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

import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Represents a KML MultiGeometry. Contains an array of Geometry objects.
 */
public class KmlMultiGeometry extends MultiGeometry {
    /**
     * Creates a new MultiGeometry object
     *
     * @param geometries array of Geometry objects contained in the MultiGeometry
     */
    public KmlMultiGeometry(ArrayList<Geometry> geometries) {
        super(geometries);
    }

    /**
     * Gets an ArrayList of Geometry objects
     *
     * @return ArrayList of Geometry objects
     */
    public ArrayList<Geometry> getGeometryObject() {
        List<Geometry> geometriesList = super.getGeometryObject();
        return new ArrayList<>(geometriesList);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getGeometryType()).append("{");
        sb.append("\n geometries=").append(getGeometryObject());
        sb.append("\n}\n");
        return sb.toString();
    }
}
