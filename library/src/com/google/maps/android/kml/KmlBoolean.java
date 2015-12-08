package com.google.maps.android.kml;

/**
 * Utility class to help parse Kml boolean entities.
 */
public class KmlBoolean {
    public static boolean parseBoolean(String text) {
        if ("1".equals(text) || "true".equals(text)) {
            return true;
        }
        return false;
    }
}
