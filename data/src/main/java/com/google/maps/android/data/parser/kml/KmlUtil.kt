package com.google.maps.android.data.parser.kml

/**
 * Utility functions for parsing KML files.
 */
internal object KmlUtil {
    /**
     * Coerces a string value to a boolean, accepting "true", "false", "1", and "0".
     *
     * @param value The string to coerce.
     * @return `true` if the string is "true" or "1", `false` otherwise.
     */
    fun toBoolean(value: String?): Boolean {
        return value != null && (value.equals("true", ignoreCase = true) || value == "1")
    }
}