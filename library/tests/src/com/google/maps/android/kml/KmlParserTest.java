package com.google.maps.android.kml;

import android.graphics.Color;
import android.test.ActivityTestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

public class KmlParserTest extends ActivityTestCase {

    public XmlPullParser createParser(int res) throws Exception {
        InputStream stream = getInstrumentation().getContext().getResources().openRawResource(res);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    public void testInlineStyle() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.inline_style);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        assertNotNull(mParser.getPlacemarks());
        assertEquals(mParser.getPlacemarks().size(), 1);
        for (KmlPlacemark placemark : mParser.getPlacemarks().keySet()) {
            KmlStyle inlineStyle = placemark.getInlineStyle();
            assertNotNull(inlineStyle);
            assertEquals(inlineStyle.getPolylineOptions().getColor(),
                    Color.parseColor("#000000"));
            assertEquals(inlineStyle.getPolygonOptions().getFillColor(),
                    Color.parseColor("#ffffff"));
            assertEquals(inlineStyle.getPolylineOptions().getColor(),
                    inlineStyle.getPolygonOptions().getStrokeColor());
            assertEquals(placemark.getGeometry().getGeometryType(), "MultiGeometry");
        }
    }
    
    public void testContainerHeirarchy() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.document_nest);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        assertEquals(mParser.getContainers().get(0).getContainerId(), "hasId");
        assertEquals(mParser.getContainers().size(), 1);
        assertTrue(mParser.getContainers().get(0).hasContainers());
    }

    public void testPlacemarkParsing() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.unsupported);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        assertTrue(mParser.getPlacemarks().size() == 1);
    }
}
