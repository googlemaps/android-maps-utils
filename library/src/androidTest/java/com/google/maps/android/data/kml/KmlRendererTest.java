package com.google.maps.android.data.kml;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class KmlRendererTest {
    @Test
    public void testAssignStyleMap() {
        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("BlueKey", "BlueValue");
        HashMap<String, KmlStyle> styles = new HashMap<>();
        KmlStyle blueStyle = new KmlStyle();
        KmlStyle redStyle = new KmlStyle();
        styles.put("BlueValue", blueStyle);
        styles.put("RedValue", redStyle);
        KmlRenderer renderer = new KmlRenderer(null, null);
        renderer.assignStyleMap(styleMap, styles);
        assertNotNull(styles.get("BlueKey"));
        assertEquals(styles.get("BlueKey"), styles.get("BlueValue"));
        styles.put("BlueValue", null);
        renderer.assignStyleMap(styleMap, styles);
        assertNull(styles.get("BlueKey"));
        styleMap.put("BlueKey", "RedValue");
        renderer.assignStyleMap(styleMap, styles);
        assertNotNull(styleMap.get("BlueKey"));
        assertEquals(styles.get("BlueKey"), redStyle);
    }
}
