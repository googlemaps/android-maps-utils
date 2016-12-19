package com.google.maps.android.geojsonkmlabs.geojson;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class BiMultiMapTest extends TestCase {

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
            assertEquals(null, map.getKey(value));
        }
    }

}
