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

public class GeoJsonMultiLineStringTest {
    private GeoJsonMultiLineString mls;

    @Test
    public void testGetType() {
        List<GeoJsonLineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals("MultiLineString", mls.getType());
    }

    @Test
    public void testGetLineStrings() {
        List<GeoJsonLineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(lineStrings, mls.getLineStrings());

        lineStrings = new ArrayList<>();
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(new ArrayList<GeoJsonLineString>(), mls.getLineStrings());

        try {
            mls = new GeoJsonMultiLineString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
