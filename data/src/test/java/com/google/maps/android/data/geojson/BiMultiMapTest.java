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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BiMultiMapTest {
    @Test
    public void testSingle() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        String value = "bar";
        map.put(key, value);
        assertEquals(1, map.size());
        assertEquals(value, map.get(key));
        assertEquals(key, map.getKey(value));
        map.remove(key);
        assertEquals(0, map.size());
        assertNull(map.get(key));
        assertNull(map.getKey(value));
    }

    @Test
    public void testMulti() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        map.put(key, values);
        assertEquals(1, map.size());
        assertEquals(values, map.get(key));
        for (String value : values) {
            assertEquals(key, map.getKey(value));
        }
        map.remove(key);
        assertEquals(0, map.size());
        assertNull(map.get(key));
        for (String value : values) {
            assertNull(map.getKey(value));
        }
    }

    @Test
    public void testCollection() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        map.put(key, values);
        assertEquals(1, map.size());
        assertEquals(values, map.get(key));
        for (String value : values) {
            assertEquals(key, map.getKey(value));
        }
        map.remove(key);
        assertEquals(0, map.size());
        for (String value : values) {
            assertNull(map.getKey(value));
        }
    }
}
