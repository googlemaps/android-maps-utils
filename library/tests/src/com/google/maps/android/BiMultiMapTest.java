package com.google.maps.android;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class BiMultiMapTest extends TestCase {

    private BiMultiMap<String> mMap = new BiMultiMap<>();

    public void testSingle() {
        String key = "foo";
        String value = "bar";
        mMap.clear();
        mMap.put(key, value);
        assertEquals(mMap.size(), 1);
        assertEquals(mMap.get(key), value);
        assertEquals(mMap.getKey(value), key);
        mMap.remove(key);
        assertEquals(mMap.size(), 0);
        assertNull(mMap.get(key));
        assertNull(mMap.getKey(value));
    }

    public void testMulti() {
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        mMap.clear();
        mMap.put(key, values);
        assertEquals(mMap.size(), 1);
        assertEquals(mMap.get(key), values);
        for (String value : values) {
            assertEquals(mMap.getKey(value), key);
        }
        mMap.remove(key);
        assertEquals(mMap.size(), 0);
        assertNull(mMap.get(key));
        for (String value : values) {
            assertEquals(mMap.getKey(value), key);
        }
    }

}
