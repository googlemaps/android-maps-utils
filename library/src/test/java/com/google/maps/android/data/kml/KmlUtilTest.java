package com.google.maps.android.data.kml;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class KmlUtilTest {

    @Test
    public void testSubstituteProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("name", "Bruce Wayne");
        properties.put("description", "Batman");
        properties.put("Snippet", "I am the night");
        KmlPlacemark placemark = new KmlPlacemark(null, null, null, properties);

        String result1 = KmlUtil.substituteProperties("$[name] is my name", placemark);
        assertEquals("Bruce Wayne is my name", result1);

        String result2 = KmlUtil.substituteProperties("Also known as $[description]", placemark);
        assertEquals("Also known as Batman", result2);

        String result3 = KmlUtil.substituteProperties("I say \"$[Snippet]\" often", placemark);
        assertEquals("I say \"I am the night\" often", result3);

        String result4 = KmlUtil.substituteProperties("My address is $[address]", placemark);
        assertEquals("When property doesn't exist, placeholder is left in place", "My address is $[address]", result4);
    }
}
