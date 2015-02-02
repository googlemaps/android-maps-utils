package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.maps.android.test.R;

public class KmlFeatureParserTest extends ActivityTestCase {

    public XmlPullParser createParser(int res) throws Exception {
        InputStream stream = getInstrumentation().getContext().getResources().openRawResource(res);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    public void testPolygon() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.basic_placemark);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull(placemark);
        assertEquals(placemark.getGeometry().getKmlGeometryType(), "Polygon");
        KmlPolygon polygon = ((KmlPolygon) placemark.getGeometry());
        assertEquals(polygon.getInnerBoundaryCoordinates().size(), 2);
        assertEquals(polygon.getOuterBoundaryCoordinates().size(), 5);
    }

    public void testMultiGeometry() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.multigeometry_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull( placemark );
        assertEquals( placemark .getGeometry().getKmlGeometryType(), "MultiGeometry");
        KmlMultiGeometry multiGeometry = ((KmlMultiGeometry) placemark .getGeometry());
        assertEquals(multiGeometry.getKmlGeometryObject().size(), 3);
    }

    public void testProperties() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.multigeometry_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertTrue( placemark.hasProperties());
        assertEquals( placemark .getProperty("name"), "Placemark Test");
        assertNull( placemark .getProperty("description"));
    }

    public void testExtendedData() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.multiple_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        assertNotNull(placemark.getProperty("holeNumber"));
    }

    public void testGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.ground_overlay);
        KmlFeatureParser parser = new KmlFeatureParser();
        parser.createGroundOverlay(xmlPullParser);
        KmlGroundOverlay groundOverlay = parser.getGroundOverlay();
        assertNotNull(groundOverlay);
        assertEquals(groundOverlay.getProperty("name"), "Sample Ground Overlay");
        assertNotNull(groundOverlay.getImageUrl());
        assertEquals(groundOverlay.getGroundOverlayOptions().getZIndex(), 99.0f);
        assertTrue(groundOverlay.getGroundOverlayOptions().isVisible());
        assertNotNull(groundOverlay.getLatLngBox());
        xmlPullParser = createParser(R.raw.ground_overlay_color);
        parser.createGroundOverlay(xmlPullParser);
        groundOverlay = parser.getGroundOverlay();
        assertNotNull(groundOverlay);
    }


}
