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

import com.google.android.gms.maps.MapsInitializer;
import com.google.maps.android.TestUtil;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import android.graphics.Color;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KmlStyleTest {

    @Before
    public void setUp() {
        MapsInitializer.initialize(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void testStyleId() {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setStyleId("BlueLine");
        assertEquals("BlueLine", kmlStyle.getStyleId());
    }

    @Test
    public void testFill() {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setFill(true);
        assertTrue(kmlStyle.hasFill());
        kmlStyle.setFill(false);
        assertFalse(kmlStyle.hasFill());
    }

    @Test
    public void testFillColor() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor("000000");
        int fillColor = Color.parseColor("#000000");
        assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testFillColorLeadingSpace() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor(" 000000");
        int fillColor = Color.parseColor("#000000");
        assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testFillColorTrailingSpace() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor("000000 ");
        int fillColor = Color.parseColor("#000000");
        assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testColorFormatting() {
        KmlStyle kmlStyle = new KmlStyle();
        // AABBGGRR -> AARRGGBB.
        kmlStyle.setFillColor("ff579D00");
        assertEquals(Color.parseColor("#009D57"), kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testHeading() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
        assertEquals(0.0f, kmlStyle.getMarkerOptions().getRotation(), 0);
        kmlStyle.setHeading(3);
        assertEquals(3.0f, kmlStyle.getMarkerOptions().getRotation(), 0);
    }

    @Test
    public void testWidth() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(10.0f, kmlStyle.getPolylineOptions().getWidth(), 0);
        assertEquals(10.0f, kmlStyle.getPolygonOptions().getStrokeWidth(), 0);
        kmlStyle.setWidth(11.0f);
        assertEquals(11.0f, kmlStyle.getPolylineOptions().getWidth(), 0);
        assertEquals(11.0f, kmlStyle.getPolygonOptions().getStrokeWidth(), 0);
    }

    @Test
    public void testLineColor() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(Color.BLACK, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.BLACK, kmlStyle.getPolygonOptions().getStrokeColor());
        kmlStyle.setOutlineColor("FFFFFF");
        assertEquals(Color.WHITE, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.WHITE, kmlStyle.getPolygonOptions().getStrokeColor());
    }

    @Test
    public void testMarkerColorLeadingSpace() {
        if (TestUtil.isRunningOnTravis()) {
            Assume.assumeTrue("Skipping KmlStyleTest.testMarkerColorLeadingSpace() - this is expected behavior on Travis CI (#573)", false);
            return;
        }
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
        kmlStyle.setMarkerColor(" FFFFFF");
        assertEquals(Color.WHITE, kmlStyle.mMarkerColor, 1);
    }

    @Test
    public void testMarkerColorTrailingSpace() {
        if (TestUtil.isRunningOnTravis()) {
            Assume.assumeTrue("Skipping KmlStyleTest.testMarkerColorTrailingSpace() - this is expected behavior on Travis CI (#573)", false);
            return;
        }
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
        kmlStyle.setMarkerColor("FFFFFF ");
        assertEquals(Color.WHITE, kmlStyle.mMarkerColor, 1);
    }

    @Test
    public void testLineColorWithLeadingSpace() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(Color.BLACK, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.BLACK, kmlStyle.getPolygonOptions().getStrokeColor());
        kmlStyle.setOutlineColor(" FFFFFFFF");
        assertEquals(Color.WHITE, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.WHITE, kmlStyle.getPolygonOptions().getStrokeColor());
    }

    @Test
    public void testLineColorWithTrailingSpace() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(Color.BLACK, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.BLACK, kmlStyle.getPolygonOptions().getStrokeColor());
        kmlStyle.setOutlineColor("FFFFFFFF ");
        assertEquals(Color.WHITE, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.WHITE, kmlStyle.getPolygonOptions().getStrokeColor());
    }

    @Test
    public void testClickable() {
        KmlStyle kmlStyle = new KmlStyle();
        assertTrue(kmlStyle.getPolylineOptions().isClickable());
        assertTrue(kmlStyle.getPolygonOptions().isClickable());
    }
}
