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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GeoJsonLineStringTest {
    private GeoJsonLineString ls;

    @Test
    public void testGetType() {
        List<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        assertEquals("LineString", ls.getType());
    }

    @Test
    public void testGetCoordinates() {
        List<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        assertEquals(coordinates, ls.getCoordinates());

        try {
            ls = new GeoJsonLineString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAltitudes() {
        List<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        List<Double> altitudes = new ArrayList<>();
        altitudes.add(100d);
        altitudes.add(200d);
        altitudes.add(300d);
        ls = new GeoJsonLineString(coordinates, altitudes);
        assertEquals(altitudes, ls.getAltitudes());
        assertEquals(ls.getAltitudes().get(0), 100.0, 0);
        assertEquals(ls.getAltitudes().get(1), 200.0, 0);
        assertEquals(ls.getAltitudes().get(2), 300.0, 0);
    }
}
