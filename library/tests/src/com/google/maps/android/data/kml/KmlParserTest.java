package com.google.maps.android.data.kml;

import android.support.test.InstrumentationRegistry;
import android.graphics.Color;

import org.junit.Test;
import org.junit.Assert;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.HashMap;

public class KmlParserTest {

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
    public void testInlineStyle() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.amu_inline_style);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        Assert.assertNotNull(mParser.getPlacemarks());
        Assert.assertEquals(mParser.getPlacemarks().size(), 1);
        for (KmlPlacemark placemark : mParser.getPlacemarks().keySet()) {
            KmlStyle inlineStyle = placemark.getInlineStyle();
            Assert.assertNotNull(inlineStyle);
            Assert.assertEquals(inlineStyle.getPolylineOptions().getColor(),
                    Color.parseColor("#000000"));
            Assert.assertEquals(inlineStyle.getPolygonOptions().getFillColor(),
                    Color.parseColor("#ffffff"));
            Assert.assertEquals(inlineStyle.getPolylineOptions().getColor(),
                    inlineStyle.getPolygonOptions().getStrokeColor());
            Assert.assertEquals(placemark.getGeometry().getGeometryType(), "MultiGeometry");
        }
    }

    @Test
    public void testPolyStyleBooleanNumeric() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.amu_poly_style_boolean_numeric);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        Assert.assertNotNull(mParser.getPlacemarks());
        Assert.assertEquals(1, mParser.getContainers().size());
        KmlContainer kmlContainer = mParser.getContainers().get(0);
        Assert.assertEquals(true, kmlContainer.hasPlacemarks());

        HashMap<String, KmlStyle> styles = kmlContainer.getStyles();
        KmlStyle kmlStyle = styles.get("#fireadvisory");
        Assert.assertNotNull(kmlStyle);
        Assert.assertEquals(true, kmlStyle.hasFill());
        Assert.assertEquals(false, kmlStyle.hasOutline());
    }

    @Test
    public void testPolyStyleBooleanAlpha() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.amu_poly_style_boolean_alpha);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        Assert.assertNotNull(mParser.getPlacemarks());
        Assert.assertEquals(1, mParser.getContainers().size());
        KmlContainer kmlContainer = mParser.getContainers().get(0);
        Assert.assertEquals(true, kmlContainer.hasPlacemarks());

        HashMap<String, KmlStyle> styles = kmlContainer.getStyles();
        KmlStyle kmlStyle = styles.get("#fireadvisory");
        Assert.assertNotNull(kmlStyle);
        Assert.assertEquals(true, kmlStyle.hasFill());
        Assert.assertEquals(false, kmlStyle.hasOutline());
    }

    @Test
    public void testContainerHeirarchy() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.amu_document_nest);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        Assert.assertEquals(mParser.getContainers().get(0).getContainerId(), "hasId");
        Assert.assertEquals(mParser.getContainers().size(), 1);
        Assert.assertTrue(mParser.getContainers().get(0).hasContainers());
    }

    @Test
    public void testPlacemarkParsing() throws Exception {
        XmlPullParser parser = createParser(com.google.maps.android.test.R.raw.amu_unsupported);
        KmlParser mParser = new KmlParser(parser);
        mParser.parseKml();
        Assert.assertTrue(mParser.getPlacemarks().size() == 1);
    }
}
