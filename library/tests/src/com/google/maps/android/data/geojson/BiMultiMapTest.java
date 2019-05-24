package com.google.maps.android.data.geojson;

import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

public class BiMultiMapTest {

    @Test
    public void testSingle() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        String value = "bar";
        map.put(key, value);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(value, map.get(key));
        Assert.assertEquals(key, map.getKey(value));
        map.remove(key);
        Assert.assertEquals(0, map.size());
        Assert.assertNull(map.get(key));
        Assert.assertNull(map.getKey(value));
    }

    @Test
    public void testMulti() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        map.put(key, values);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(values, map.get(key));
        for (String value : values) {
            Assert.assertEquals(key, map.getKey(value));
        }
        map.remove(key);
        Assert.assertEquals(0, map.size());
        Assert.assertNull(map.get(key));
        for (String value : values) {
            Assert.assertEquals(null, map.getKey(value));
        }
    }

    @Test
    public void testCollection() {
        BiMultiMap<String> map = new BiMultiMap<>();
        String key = "foo";
        List<String> values = Arrays.asList("bar", "baz");
        map.put(key, values);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(values, map.get(key));
        for (String value : values) {
            Assert.assertEquals(key, map.getKey(value));
        }
        map.remove(key);
        Assert.assertEquals(0, map.size());
        for (String value : values) {
            Assert.assertEquals(null, map.getKey(value));
        }
    }
}
