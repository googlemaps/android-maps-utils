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

    private static final int UNINITIALIZED = -1;

    private static final int POLYSTYLE = 0;

    private static final int LINESTYLE = 1;

    private final HashMap<String, String> mValues;

    private int mStyle;

    public Style() {
        mValues = new HashMap<String, String>();
        mStyle = UNINITIALIZED;

    }

    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser reads input from designated source
     */
    public void styleProperties(XmlPullParser p) throws XmlPullParserException, IOException {
        // Style tag is always followed by the LineStyle or PolyStyle tag
        // Need 2 next calls to iterate over style tag and its id attribute
        p.next();
        p.next();
        // Only parse the style if it is a LineStyle or PolyStyle
        if (p.getName().matches("LineStyle|PolyStyle")) {
            mStyle = p.getName().equals("LineStyle") ? LINESTYLE : POLYSTYLE;
            p.next();
            int eventType = p.getEventType();
            // Iterate through document until closing style tag is reached
            while (!(eventType == XmlPullParser.END_TAG)) {
                String name = p.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    // In future we may want to save all the properties to allow the user to access
                    // additional data
                    if (name.matches("color|width|colorMode|fill|outline")) {
                        setValue(name, p.nextText());
                    }
                }
                eventType = p.next();
            }
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
    public void setValue(String key, String value) {
        mValues.put(key, value);
    }

    /**
     * Retrieves a value from a hash map using a given key
     *
     * @param key The name of the value which we want to retrieve
     * @return The value which was inserted using the key, otherwise null
     */
    public String getValue(String key) {
        return mValues.get(key);
    }

    /**
     * Used to check if there are any values in the mValues hashmap
     *
     * @return true if the mValues is empty, false otherwise
     */
    public boolean isEmpty() {
        return mValues.isEmpty();
    }

    /**
     * Gets the type of style this class defines
     *
     * @return type of style, -1 if not a LineStyle or PolyStyle
     */
    public int getStyleType() {
        return mStyle;
    }

}
