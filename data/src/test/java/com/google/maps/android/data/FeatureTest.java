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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FeatureTest {
    @Test
    public void testGetId() {
        Feature feature = new Feature(null, "Pirate", null);
        assertNotNull(feature.getId());
        assertEquals("Pirate", feature.getId());
        feature = new Feature(null, null, null);
        assertNull(feature.getId());
    }

    @Test
    public void testProperty() {
        Map<String, String> properties = new HashMap<>();
        properties.put("Color", "Red");
        properties.put("Width", "3");
        Feature feature = new Feature(null, null, properties);
        assertFalse(feature.hasProperty("llama"));
        assertTrue(feature.hasProperty("Color"));
        assertEquals("Red", feature.getProperty("Color"));
        assertTrue(feature.hasProperty("Width"));
        assertEquals("3", feature.getProperty("Width"));
        assertNull(feature.removeProperty("banana"));
        assertEquals("3", feature.removeProperty("Width"));
        assertNull(feature.setProperty("Width", "10"));
        assertEquals("10", feature.setProperty("Width", "500"));
    }

    @Test
    public void testGeometry() {
        Feature feature = new Feature(null, null, null);
        assertNull(feature.getGeometry());
        Point point = new Point(new LatLng(0, 0));
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());

        LineString lineString =
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new Feature(lineString, null, null);
        assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        assertEquals(lineString, feature.getGeometry());
    }
}
