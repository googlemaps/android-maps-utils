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

public class GeoJsonMultiPolygonTest {
    private GeoJsonMultiPolygon mp;

    @Test
    public void testGetType() {
        List<GeoJsonPolygon> polygons = new ArrayList<>();
        List<ArrayList<LatLng>> polygon = new ArrayList<>();
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        polygon = new ArrayList<>();
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(50, 80),
                                new LatLng(10, 15),
                                new LatLng(0, 0))));
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        mp = new GeoJsonMultiPolygon(polygons);
        assertEquals("MultiPolygon", mp.getType());
    }

    @Test
    public void testGetPolygons() {
        List<GeoJsonPolygon> polygons = new ArrayList<>();
        List<ArrayList<LatLng>> polygon = new ArrayList<>();
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        polygon = new ArrayList<>();
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(50, 80),
                                new LatLng(10, 15),
                                new LatLng(0, 0))));
        polygon.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        mp = new GeoJsonMultiPolygon(polygons);
        assertEquals(polygons, mp.getPolygons());

        polygons = new ArrayList<>();
        mp = new GeoJsonMultiPolygon(polygons);
        assertEquals(new ArrayList<GeoJsonPolygon>(), mp.getPolygons());

        try {
            mp = new GeoJsonMultiPolygon(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
