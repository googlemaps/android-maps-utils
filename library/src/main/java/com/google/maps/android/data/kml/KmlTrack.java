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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thorin on 22/02/2017.
 */

public class KmlTrack extends KmlLineString {
    private final ArrayList<Long> mTimestamps;
    private final HashMap<String, String> mProperties;

    /**
     * Creates a new KmlTrack object
     *
     * @param coordinates array of coordinates
     * @param altitudes   array of altitudes
     * @param timestamps  array of timestamps
     * @param properties  hashmap of properties
     */
    public KmlTrack(ArrayList<LatLng> coordinates, ArrayList<Double> altitudes, ArrayList<Long> timestamps, HashMap<String, String> properties) {
        super(coordinates, altitudes);

        this.mTimestamps = timestamps;
        this.mProperties = properties;
    }

    public ArrayList<Long> getTimestamps() {
        return mTimestamps;
    }

    public HashMap<String, String> getProperties() {
        return mProperties;
    }
}
