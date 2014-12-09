package com.google.maps.android.kml;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by lavenderc on 12/2/14.
 */
public class Style {

    private HashMap<String, String> values;

    public Style() {
        values = new HashMap<String, String>();
    }

    public void setValues (String key, String value) {
        values.put(key, value);
    }

    public String getValues (String key) {
        return values.get(key);
    }

}
