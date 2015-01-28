package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.maps.android.test.R;

/**
 * Created by lavenderch on 1/28/15.
 */
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
        KmlFeatureParser parser = new KmlFeatureParser(createParser(R.raw.basic_placemark));
        parser.createPlacemark();
        assertNotNull(parser.getPlacemark());
        assertEquals(parser.getPlacemark().getGeometry().getKmlGeometryType(), "Polygon");
        KmlPolygon polygon = ((KmlPolygon) parser.getPlacemark().getGeometry());
        assertEquals(polygon.getInnerBoundaryCoordinates().size(), 2);
        assertEquals(polygon.getOuterBoundaryCoordinates().size(), 5);
    }

    public void testMultiGeometry() throws Exception {
        KmlFeatureParser parser = new KmlFeatureParser(createParser(R.raw.multigeometry_placemarks));
        parser.createPlacemark();
        assertNotNull(parser.getPlacemark());
        assertEquals(parser.getPlacemark().getGeometry().getKmlGeometryType(), "MultiGeometry");
        KmlMultiGeometry multiGeometry = ((KmlMultiGeometry)parser.getPlacemark().getGeometry());
        assertEquals(multiGeometry.getKmlGeometryObject().size(), 3);
    }

    public void testProperties() throws Exception {
        KmlFeatureParser parser = new KmlFeatureParser(createParser(R.raw.multigeometry_placemarks));
        parser.createPlacemark();s
        assertTrue(parser.getPlacemark().hasProperties());
        assertEquals(parser.getPlacemark().getProperty("name"), "Placemark Test");
        assertNull(parser.getPlacemark().getProperty("description"));
    }

    public void testExtendedData() throws Exception {
        KmlFeatureParser parser = new KmlFeatureParser(createParser(R.raw.multiple_placemarks));
        parser.createPlacemark();
        assertNotNull(parser.getPlacemark().getProperty("holeNumber"));
    }


}
