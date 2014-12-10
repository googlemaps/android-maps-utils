package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/2/14.
 *
 * Represents the defined styles in the KML document
 */
public class Style {

    private HashMap<String, String> values = new HashMap<String, String>();

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser reads input from designated source
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void styleProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        // Iterate through document until closing style tag is reached
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("Style"))) {
            String name = p.getName();
            if (eventType == XmlPullParser.START_TAG) {
                // List of all the allowed values in our hashmap
                // TODO: Add more allowed values e.g. strokeWidth,
                if (name.equals("color") || name.equals("width") || name.equals("colorMode")) {
                    values.put(name, p.nextText());
                }
            }
            eventType = p.next();
        }
    }

    /**
     * Takes in two strings, key and value, whereby the key is the name of the tag and the value
     * is the text, and puts them into a hashmap.
     *
     * EXAMPLE USE: <name>Hello World</name>
     * key = name, value = Hello World.
     *
     * @param key   The string value which we use to access value
     * @param value The string value which we want to access
     */
    public void setValues(String key, String value) {
        values.put(key, value);
    }

    /**
     * Retrieves values from a hash map using a key
     *
     * @param key The name of the value which we want to retrieve
     * @return The value which was inserted using the key, otherwise null
     */
    public String getValues(String key) {
        return values.get(key);
    }

}
