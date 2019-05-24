package com.google.maps.android.data.kml;

import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;

public class KmlRendererTest {

    @Test
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
        Assert.assertNotNull(styles.get("BlueKey"));
        Assert.assertEquals(styles.get("BlueKey"), styles.get("BlueValue"));
        styles.put("BlueValue", null);
        renderer.assignStyleMap(styleMap, styles);
        Assert.assertEquals(styles.get("BlueKey"), null);
        styleMap.put("BlueKey", "RedValue");
        renderer.assignStyleMap(styleMap, styles);
        Assert.assertNotNull(styleMap.get("BlueKey"));
        Assert.assertEquals(styles.get("BlueKey"), redStyle);
    }
}
