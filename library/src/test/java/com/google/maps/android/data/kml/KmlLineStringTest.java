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

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class KmlLineStringTest {
    private KmlLineString createSimpleLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        return new KmlLineString(coordinates);
    }

    private KmlLineString createSimpleLineStringWithAltitudes() {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        ArrayList<Double> altitudes = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        altitudes.add(100d);
        altitudes.add(200d);
        altitudes.add(300d);
        return new KmlLineString(coordinates, altitudes);
    }

    private KmlLineString createLoopedLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(0, 0));
        return new KmlLineString(coordinates);
    }

    @Test
    public void testGetType() {
        KmlLineString kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryType());
        assertEquals("LineString", kmlLineString.getGeometryType());

        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryType());
        assertEquals("LineString", kmlLineString.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() {
        KmlLineString kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryObject());
        assertEquals(3, kmlLineString.getGeometryObject().size());
        assertEquals(0.0, kmlLineString.getGeometryObject().get(0).latitude, 0);
        assertEquals(50.0, kmlLineString.getGeometryObject().get(1).latitude, 0);
        assertEquals(90.0, kmlLineString.getGeometryObject().get(2).latitude, 0);

        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryObject());
        assertEquals(3, kmlLineString.getGeometryObject().size());
        assertEquals(0.0, kmlLineString.getGeometryObject().get(0).latitude, 0);
        assertEquals(50.0, kmlLineString.getGeometryObject().get(1).latitude, 0);
        assertEquals(0.0, kmlLineString.getGeometryObject().get(2).latitude, 0);
    }

    @Test
    public void testLineStringAltitudes() {
        // test linestring without altitudes
        KmlLineString kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNull(kmlLineString.getAltitudes());

        // test linestring with altitudes
        kmlLineString = createSimpleLineStringWithAltitudes();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getAltitudes());
        assertEquals(100.0, kmlLineString.getAltitudes().get(0), 0);
        assertEquals(200.0, kmlLineString.getAltitudes().get(1), 0);
        assertEquals(300.0, kmlLineString.getAltitudes().get(2), 0);
    }
}
