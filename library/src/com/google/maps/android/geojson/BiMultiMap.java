package com.google.maps.android.geojson;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
/* package */ class BiMultiMap<K> extends HashMap<K, Object> {

    private final Map<Object, K> mValuesToKeys = new HashMap<>();

    @Override
    public void putAll(Map<? extends K, ?> map) {
        // put() manages the reverse map, so call it on each entry.
        for (Entry<? extends K, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object put(K key, Object value) {
        // Store value/key in the reverse map.
        mValuesToKeys.put(value, key);
        return super.put(key, value);
    }

    public Object put(K key, Collection values) {
        // Store values/key in the reverse map.
        for (Object value : values) {
            mValuesToKeys.put(value, key);
        }
        return super.put(key, values);
    }

    @Override
    public Object remove(Object key) {
        Object value = super.remove(key);
        // Also remove the value(s) and key from the reverse map.
        if (value instanceof Collection) {
            for (Object valueItem : (Collection) value) {
                mValuesToKeys.remove(valueItem);
            }
        } else {
            mValuesToKeys.remove(value);
        }
        return value;
    }

    @Override
    public void clear() {
        super.clear();
        mValuesToKeys.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public BiMultiMap<K> clone() {
        BiMultiMap<K> cloned = new BiMultiMap<>();
        cloned.putAll((Map<K, Object>) super.clone());
        return cloned;
    }

    /**
     * Reverse lookup of key by value.
     *
     * @param value Value to lookup
     * @return Key for the given value
     */
    public K getKey(Object value) {
        return mValuesToKeys.get(value);
    }

}
