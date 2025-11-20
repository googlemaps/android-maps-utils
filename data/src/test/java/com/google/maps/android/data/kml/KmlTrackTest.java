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
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class KmlTrackTest {
    private KmlTrack createSimpleTrack() {
        ArrayList<LatLng> coordinates = new ArrayList<>();
        ArrayList<Double> altitudes = new ArrayList<>();
        ArrayList<Long> timestamps = new ArrayList<>();
        HashMap<String, String> properties = new HashMap<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(90, 90));
        altitudes.add(100d);
        altitudes.add(200d);
        altitudes.add(300d);
        timestamps.add(1000L);
        timestamps.add(2000L);
        timestamps.add(3000L);
        properties.put("key", "value");
        return new KmlTrack(coordinates, altitudes, timestamps, properties);
    }

    @Test
    public void testGetType() {
        KmlTrack kmlTrack = createSimpleTrack();
        assertNotNull(kmlTrack);
        assertNotNull(kmlTrack.getGeometryType());
        assertEquals("LineString", kmlTrack.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() {
        KmlTrack kmlTrack = createSimpleTrack();
        assertNotNull(kmlTrack);
        assertNotNull(kmlTrack.getGeometryObject());
        assertEquals(kmlTrack.getGeometryObject().size(), 3);
        assertEquals(kmlTrack.getGeometryObject().get(0).latitude, 0.0, 0);
        assertEquals(kmlTrack.getGeometryObject().get(1).latitude, 50.0, 0);
        assertEquals(kmlTrack.getGeometryObject().get(2).latitude, 90.0, 0);
    }

    @Test
    public void testAltitudes() {
        KmlTrack kmlTrack = createSimpleTrack();
        assertNotNull(kmlTrack);
        assertNotNull(kmlTrack.getAltitudes());
        assertEquals(kmlTrack.getAltitudes().size(), 3);
        assertEquals(kmlTrack.getAltitudes().get(0), 100.0, 0);
        assertEquals(kmlTrack.getAltitudes().get(1), 200.0, 0);
        assertEquals(kmlTrack.getAltitudes().get(2), 300.0, 0);
    }

    @Test
    public void testTimestamps() {
        KmlTrack kmlTrack = createSimpleTrack();
        assertNotNull(kmlTrack);
        assertNotNull(kmlTrack.getTimestamps());
        assertEquals(kmlTrack.getTimestamps().size(), 3);
        assertEquals(kmlTrack.getTimestamps().get(0), Long.valueOf(1000L));
        assertEquals(kmlTrack.getTimestamps().get(1), Long.valueOf(2000L));
        assertEquals(kmlTrack.getTimestamps().get(2), Long.valueOf(3000L));
    }

    @Test
    public void testProperties() {
        KmlTrack kmlTrack = createSimpleTrack();
        assertNotNull(kmlTrack);
        assertNotNull(kmlTrack.getProperties());
        assertEquals(kmlTrack.getProperties().size(), 1);
        assertEquals(kmlTrack.getProperties().get("key"), "value");
        assertNull(kmlTrack.getProperties().get("missingKey"));
    }
}
