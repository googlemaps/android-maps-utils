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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GeoJsonPolygonTest {
    private GeoJsonPolygon p;

    @Test
    public void testGetType() {
        List<List<LatLng>> coordinates = new ArrayList<>();
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals("Polygon", p.getType());
    }

    @Test
    public void testGetCoordinates() {
        // No holes
        List<List<LatLng>> coordinates = new ArrayList<>();
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals(coordinates, p.getCoordinates());

        // Holes
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);

        try {
            p = new GeoJsonPolygon(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }
}
