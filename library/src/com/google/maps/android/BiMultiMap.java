package com.google.maps.android;

import java.util.Collection;
import java.util.HashMap;

/**
 * Extension of HashMap that provides two main features. Firstly it allows reverse lookup
 * for a key given a value, by storing a second HashMap internally which maps values to keys.
 * Secondly, it supports Collection values, in which case, each item in the collection is
 * used as a key in the internal reverse HashMap. It's therefore up to the caller to ensure
 * the overall set of values, and collection values, are unique.
 *
 * Used by GeoJsonRenderer to store GeoJsonFeature instances mapped to corresponding Marker,
 * Polyline, and Polygon map objects. We want to look these up in reverse to provide access
 * to GeoJsonFeature instances when map objects are clicked.
 */
public class BiMultiMap<K> extends HashMap<K, Object> {

    private final HashMap<Object, K> mValuesToKeys = new HashMap<>();

    public void putAll(HashMap<K, Object> map) {
        // put() manages the reverse map, so call it on each entry.
        for (Entry<K, Object> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object put(K key, Object value) {
        // Store value/key in the reverse map, and store each item in the value
        // if the value is a collection.
        if (value instanceof Collection) {
            for (Object valueItem: (Collection) value) {
                mValuesToKeys.put(valueItem, key);
            }
        } else {
            mValuesToKeys.put(value, key);
        }
        return super.put(key, value);
    }

    public Object remove(Object key) {
        // Also remove the value/key from the reverse map.
        mValuesToKeys.remove(get(key));
        return super.remove(key);
    }

    /**
     * Reverse lookup of key by value.
     *
     * @param value Object value to lookup
     * @return K the key for the given value
     */
    public K getKey(Object value) {
        return mValuesToKeys.get(value);
    }

}
