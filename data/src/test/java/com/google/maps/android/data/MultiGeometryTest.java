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
package com.google.maps.android.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.geojson.GeoJsonPolygon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiGeometryTest {
    private MultiGeometry mg;

    @Test
    public void testGetGeometryType() {
        List<LineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(56, 65), new LatLng(23, 23)))));
        mg = new MultiGeometry(lineStrings);
        assertEquals("MultiGeometry", mg.getGeometryType());
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
        mg = new MultiGeometry(polygons);
        assertEquals("MultiGeometry", mg.getGeometryType());
    }

    @Test
    public void testSetGeometryType() {
        List<LineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(56, 65), new LatLng(23, 23)))));
        mg = new MultiGeometry(lineStrings);
        assertEquals("MultiGeometry", mg.getGeometryType());
        mg.setGeometryType("MultiLineString");
        assertEquals("MultiLineString", mg.getGeometryType());
    }

    @Test
    public void testGetGeometryObject() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(new LatLng(0, 0)));
        points.add(new Point(new LatLng(5, 5)));
        points.add(new Point(new LatLng(10, 10)));
        mg = new MultiGeometry(points);
        assertEquals(points, mg.getGeometryObject());
        points = new ArrayList<>();
        mg = new MultiGeometry(points);
        assertEquals(new ArrayList<Point>(), mg.getGeometryObject());

        try {
            mg = new MultiGeometry(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
