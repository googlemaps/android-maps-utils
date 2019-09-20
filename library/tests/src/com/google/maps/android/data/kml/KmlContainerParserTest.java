package com.google.maps.android.data.kml;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;
import org.junit.Assert;

import com.google.maps.android.test.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

public class KmlContainerParserTest {

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
    public void testCDataEntity() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_cdata);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertEquals(kmlContainer.getProperty("description"), "TELEPORT");
    }

    @Test
    public void testCreateContainerProperty() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertTrue(kmlContainer.hasProperties());
        Assert.assertEquals(kmlContainer.getProperty("name"), "Basic Folder");
        xmlPullParser = createParser(R.raw.amu_unknown_folder);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertTrue(kmlContainer.hasProperty("name"));
    }

    @Test
    public void testCreateContainerPlacemark() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertTrue(kmlContainer.hasPlacemarks());
        Assert.assertEquals(kmlContainer.getPlacemarksHashMap().size(), 1);
        xmlPullParser = createParser(R.raw.amu_multiple_placemarks);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertTrue(kmlContainer.hasPlacemarks());
        Assert.assertEquals(kmlContainer.getPlacemarksHashMap().size(), 2);
    }

    @Test
    public void testCreateContainerGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_ground_overlay);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertEquals(kmlContainer.getGroundOverlayHashMap().size(), 2);
    }

    @Test
    public void testCreateContainerObjects() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_nested_folders);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        Assert.assertNotNull(kmlContainer.getContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getContainers()) {
            numberOfNestedContainers++;
        }
        Assert.assertEquals(numberOfNestedContainers, 2);
    }


}
