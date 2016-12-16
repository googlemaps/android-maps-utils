package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class KmlRendererTest extends ActivityTestCase {

    public void testAssignStyleMap() {
        HashMap<String, String> styleMap = new HashMap<String, String>();
        styleMap.put("BlueKey", "BlueValue");
        HashMap<String, KmlStyle> styles = new HashMap<String, KmlStyle>();
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
        assertEquals(styles.get("BlueKey"), null);
        styleMap.put("BlueKey", "RedValue");
        renderer.assignStyleMap(styleMap, styles);
        assertNotNull(styleMap.get("BlueKey"));
        assertEquals(styles.get("BlueKey"), redStyle);
    }
}
