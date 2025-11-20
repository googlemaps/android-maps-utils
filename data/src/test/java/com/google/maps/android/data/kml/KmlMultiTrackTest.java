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
import static org.junit.Assert.fail;

public class KmlMultiTrackTest {
    private KmlMultiTrack createMultiTrack() {
        ArrayList<KmlTrack> kmlTracks = new ArrayList<>();
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
        KmlTrack kmlTrack = new KmlTrack(coordinates, altitudes, timestamps, properties);
        kmlTracks.add(kmlTrack);
        return new KmlMultiTrack(kmlTracks);
    }

    @Test
    public void testGetKmlGeometryType() {
        KmlMultiTrack kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertNotNull(kmlMultiTrack.getGeometryType());
        assertEquals("MultiGeometry", kmlMultiTrack.getGeometryType());
    }

    @Test
    public void testGetGeometry() {
        KmlMultiTrack kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertEquals(1, kmlMultiTrack.getGeometryObject().size());
        KmlTrack lineString = ((KmlTrack) kmlMultiTrack.getGeometryObject().get(0));
        assertNotNull(lineString);
    }

    @Test
    public void testNullGeometry() {
        try {
            new KmlMultiTrack(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Tracks cannot be null", e.getMessage());
        }
    }
}
