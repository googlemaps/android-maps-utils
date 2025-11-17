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

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GeoJsonPointTest {
    @Test
    public void testGetType() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals("Point", p.getType());
    }

    @Test
    public void testGetCoordinates() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals(new LatLng(0, 0), p.getCoordinates());
        try {
            new GeoJsonPoint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAltitude() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0), 100d);
        assertEquals(new Double(100), p.getAltitude());
    }
}
