package com.google.maps.android.data.kml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for KML
 */
public class KmlUtil {

    /**
     * Substitute property values in BalloonStyle text template
     *
     * @param template text template
     * @param placemark placemark to get property values from
     * @return string with property values substituted
     */
    public static String substituteProperties(String template, KmlPlacemark placemark) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("\\$\\[(.+?)]");
        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String property = matcher.group(1);
            String value = placemark.getProperty(property);
            if (value != null) {
                matcher.appendReplacement(sb, value);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
