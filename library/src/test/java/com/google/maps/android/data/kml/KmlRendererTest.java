/*
 * Copyright 2020 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.data.Feature;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;

import static com.google.maps.android.data.kml.KmlTestUtil.createParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class KmlRendererTest {
    private GoogleMap mMap1;
    private KmlRenderer mRenderer;
    KmlParser mParser;

    @Before
    public void setUp() throws Exception {
        XmlPullParser parser = createParser("amu_inline_style.kml");
        mParser = new KmlParser(parser);
        mParser.parseKml();

        mRenderer = new KmlRenderer(mMap1, null, null, null, null, null, null);
        mRenderer.storeKmlData(mParser.getStyles(), mParser.getStyleMaps(), mParser.getPlacemarks(),
                mParser.getContainers(), mParser.getGroundOverlays());
    }

    @Test
    public void testDefaultStyleClickable() {
        // TODO - we should call mRenderer.addLayerToMap() here for a complete end-to-end test, but
        // that requires an instantiated GoogleMap be passed into KmlRenderer()
        for (Feature f : mRenderer.getFeatures()) {
            assertTrue(((KmlPlacemark)f).getPolylineOptions().isClickable());
            assertTrue(((KmlPlacemark)f).getPolygonOptions().isClickable());
        }
    }

    @Test
    public void testAssignStyleMap() {
        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("BlueKey", "BlueValue");
        HashMap<String, KmlStyle> styles = new HashMap<>();
        KmlStyle blueStyle = new KmlStyle();
        KmlStyle redStyle = new KmlStyle();
        styles.put("BlueValue", blueStyle);
        styles.put("RedValue", redStyle);
        KmlRenderer renderer = new KmlRenderer(null, null, null, null, null, null, null);
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
