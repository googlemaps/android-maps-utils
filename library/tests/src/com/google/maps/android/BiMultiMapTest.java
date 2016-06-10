package com.google.maps.android;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class BiMultiMapTest extends TestCase {

    public void testSingle() {
        BiMultiMap<String> mMap = new BiMultiMap<>();
        String key = "foo";
        String value = "bar";
        mMap.clear();
        mMap.put(key, value);
        assertEquals(1, mMap.size());
        assertEquals(value, mMap.get(key));
        assertEquals(key, mMap.getKey(value));
        mMap.remove(key);
        assertEquals(0, mMap.size());
        assertNull(mMap.get(key));
        assertNull(mMap.getKey(value));
    }

    public void testMulti() {
        BiMultiMap<String> mMap = new BiMultiMap<>();
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        mMap.clear();
        mMap.put(key, values);
        assertEquals(1, mMap.size());
        assertEquals(values, mMap.get(key));
        for (String value : values) {
            assertEquals(key, mMap.getKey(value));
        }
        mMap.remove(key);
        assertEquals(0, mMap.size());
        assertNull(mMap.get(key));
        for (String value : values) {
            assertEquals(null, mMap.getKey(value));
        }
    }

}
