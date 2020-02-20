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

import org.junit.Before;
import org.junit.Test;

import android.graphics.Color;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeoJsonLineStringStyleTest {
    private GeoJsonLineStringStyle lineStringStyle;

    @Before
    public void setUp() {
        lineStringStyle = new GeoJsonLineStringStyle();
    }

    @Test
    public void testGetGeometryType() {
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("LineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("MultiLineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, lineStringStyle.getGeometryType().length);
    }

    @Test
    public void testColor() {
        lineStringStyle.setColor(Color.YELLOW);
        assertEquals(Color.YELLOW, lineStringStyle.getColor());
        assertEquals(Color.YELLOW, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(0x76543210);
        assertEquals(0x76543210, lineStringStyle.getColor());
        assertEquals(0x76543210, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(Color.parseColor("#000000"));
        assertEquals(Color.parseColor("#000000"), lineStringStyle.getColor());
        assertEquals(Color.parseColor("#000000"), lineStringStyle.toPolylineOptions().getColor());
    }

    @Test
    public void testGeodesic() {
        lineStringStyle.setGeodesic(true);
        assertTrue(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isGeodesic());
    }

    @Test
    public void testVisible() {
        lineStringStyle.setVisible(false);
        assertFalse(lineStringStyle.isVisible());
        assertFalse(lineStringStyle.toPolylineOptions().isVisible());
    }

    @Test
    public void testWidth() {
        lineStringStyle.setWidth(20.2f);
        assertEquals(20.2f, lineStringStyle.getWidth(), 0);
        assertEquals(20.2f, lineStringStyle.toPolylineOptions().getWidth(), 0);
    }

    @Test
    public void testZIndex() {
        lineStringStyle.setZIndex(50.78f);
        assertEquals(50.78f, lineStringStyle.getZIndex(), 0);
        assertEquals(50.78f, lineStringStyle.toPolylineOptions().getZIndex(), 0);
    }

    @Test
    public void testDefaultLineStringStyle() {
        assertEquals(Color.BLACK, lineStringStyle.getColor());
        assertFalse(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.isVisible());
        assertEquals(10.0f, lineStringStyle.getWidth(), 0);
        assertEquals(0.0f, lineStringStyle.getZIndex(), 0);
        assertTrue(lineStringStyle.isClickable());
    }

    @Test
    public void testDefaultGetPolylineOptions() {
        assertEquals(Color.BLACK, lineStringStyle.toPolylineOptions().getColor());
        assertFalse(lineStringStyle.toPolylineOptions().isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isVisible());
        assertEquals(10.0f, lineStringStyle.toPolylineOptions().getWidth(), 0);
        assertEquals(0.0f, lineStringStyle.toPolylineOptions().getZIndex(), 0);
        assertTrue(lineStringStyle.toPolylineOptions().isClickable());
    }
}
