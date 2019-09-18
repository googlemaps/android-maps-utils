package com.google.maps.android.data.kml;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;
import org.junit.Assert;

import com.google.maps.android.data.Geometry;
import com.google.maps.android.test.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;

public class KmlFeatureParserTest {

    public XmlPullParser createParser(int res) throws Exception {
        InputStream stream = InstrumentationRegistry.getTargetContext().getResources().openRawResource(res);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    @Test
    public void testPolygon() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_placemark);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        Assert.assertNotNull(placemark);
        Assert.assertEquals(placemark.getGeometry().getGeometryType(), "Polygon");
        KmlPolygon polygon = ((KmlPolygon) placemark.getGeometry());
        Assert.assertEquals(polygon.getInnerBoundaryCoordinates().size(), 2);
        Assert.assertEquals(polygon.getOuterBoundaryCoordinates().size(), 5);
    }

    @Test
    public void testMultiGeometry() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_multigeometry_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        Assert.assertNotNull(placemark);
        Assert.assertEquals(placemark.getGeometry().getGeometryType(), "MultiGeometry");
        KmlMultiGeometry multiGeometry = ((KmlMultiGeometry) placemark.getGeometry());
        Assert.assertEquals(multiGeometry.getGeometryObject().size(), 3);
    }

    @Test
    public void testProperties() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_multigeometry_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        Assert.assertTrue(placemark.hasProperties());
        Assert.assertEquals(placemark.getProperty("name"), "Placemark Test");
        Assert.assertNull(placemark.getProperty("description"));
    }

    @Test
    public void testExtendedData() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_multiple_placemarks);
        KmlPlacemark placemark = KmlFeatureParser.createPlacemark(xmlPullParser);
        Assert.assertNotNull(placemark.getProperty("holeNumber"));
    }

    @Test
    public void testGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_ground_overlay);
        KmlGroundOverlay groundOverlay = KmlFeatureParser.createGroundOverlay(xmlPullParser);
        Assert.assertNotNull(groundOverlay);
        Assert.assertEquals(groundOverlay.getProperty("name"), "Sample Ground Overlay");
        Assert.assertNotNull(groundOverlay.getImageUrl());
        Assert.assertEquals(groundOverlay.getGroundOverlayOptions().getZIndex(), 99.0f);
        Assert.assertTrue(groundOverlay.getGroundOverlayOptions().isVisible());
        Assert.assertNotNull(groundOverlay.getLatLngBox());
        xmlPullParser = createParser(R.raw.amu_ground_overlay_color);
        groundOverlay = KmlFeatureParser.createGroundOverlay(xmlPullParser);
        Assert.assertNotNull(groundOverlay);
    }

    @Test
    public void testMultiGeometries() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_nested_multigeometry);
        KmlPlacemark feature = KmlFeatureParser.createPlacemark(xmlPullParser);
        Assert.assertEquals(feature.getProperty("name"), "multiPointLine");
        Assert.assertEquals(feature.getProperty("description"), "Nested MultiGeometry structure");
        Assert.assertEquals(feature.getGeometry().getGeometryType(), "MultiGeometry");
        ArrayList<Geometry> objects = (ArrayList<Geometry>) feature.getGeometry().getGeometryObject();
        Assert.assertEquals(objects.get(0).getGeometryType(), "Point");
        Assert.assertEquals(objects.get(1).getGeometryType(), "LineString");
        Assert.assertEquals(objects.get(2).getGeometryType(), "MultiGeometry");
        ArrayList<Geometry> subObjects = (ArrayList<Geometry>) objects.get(2).getGeometryObject();
        Assert.assertEquals(subObjects.get(0).getGeometryType(), "Point");
        Assert.assertEquals(subObjects.get(1).getGeometryType(), "LineString");
    }


}
