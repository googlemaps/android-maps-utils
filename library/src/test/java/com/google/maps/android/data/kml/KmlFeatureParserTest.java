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

import com.google.maps.android.data.Geometry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

import static com.google.maps.android.data.kml.KmlTestUtil.createParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class KmlFeatureParserTest {

    @Test
    public void testPolygon() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_basic_placemark.kml");
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull(placemark);
        assertEquals(placemark.getGeometry().getGeometryType(), "Polygon");
        KmlPolygon polygon = ((KmlPolygon) placemark.getGeometry());
        assertEquals(polygon.getInnerBoundaryCoordinates().size(), 2);
        assertEquals(polygon.getOuterBoundaryCoordinates().size(), 5);
    }

    @Test
    public void testMultiGeometry() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_multigeometry_placemarks.kml");
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull(placemark);
        assertEquals(placemark.getGeometry().getGeometryType(), "MultiGeometry");
        KmlMultiGeometry multiGeometry = ((KmlMultiGeometry) placemark.getGeometry());
        assertEquals(multiGeometry.getGeometryObject().size(), 3);
    }

    @Test
    public void testProperties() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_multigeometry_placemarks.kml");
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertTrue(placemark.hasProperties());
        assertEquals(placemark.getProperty("name"), "Placemark Test");
        assertNull(placemark.getProperty("description"));
    }

    @Test
    public void testExtendedData() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_multiple_placemarks.kml");
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull(placemark.getProperty("holeNumber"));
    }

    @Test
    public void testGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_ground_overlay.kml");
        KmlGroundOverlay groundOverlay = KmlFeatureParser.createGroundOverlay(xmlPullParser);
        assertNotNull(groundOverlay);
        assertEquals(groundOverlay.getProperty("name"), "Sample Ground Overlay");
        assertNotNull(groundOverlay.getImageUrl());
        assertEquals(groundOverlay.getGroundOverlayOptions().getZIndex(), 99.0f, 0);
        assertTrue(groundOverlay.getGroundOverlayOptions().isVisible());
        assertNotNull(groundOverlay.getLatLngBox());
        xmlPullParser = createParser("amu_ground_overlay_color.kml");
        groundOverlay = KmlFeatureParser.createGroundOverlay(xmlPullParser);
        assertNotNull(groundOverlay);
    }

    @Test
    public void testMultiGeometries() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_nested_multigeometry.kml");
        KmlPlacemark feature = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertEquals(feature.getProperty("name"), "multiPointLine");
        assertEquals(feature.getProperty("description"), "Nested MultiGeometry structure");
        assertEquals(feature.getGeometry().getGeometryType(), "MultiGeometry");
        List<Geometry> objects = (ArrayList<Geometry>) feature.getGeometry().getGeometryObject();
        assertEquals(objects.get(0).getGeometryType(), "Point");
        assertEquals(objects.get(1).getGeometryType(), "LineString");
        assertEquals(objects.get(2).getGeometryType(), "MultiGeometry");
        List<Geometry> subObjects = (ArrayList<Geometry>) objects.get(2).getGeometryObject();
        assertEquals(subObjects.get(0).getGeometryType(), "Point");
        assertEquals(subObjects.get(1).getGeometryType(), "LineString");
    }
}
