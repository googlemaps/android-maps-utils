package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import com.google.maps.android.test.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

public class KmlContainerParserTest extends ActivityTestCase {

    public XmlPullParser createParser(int res) throws Exception {
        InputStream stream = getInstrumentation().getContext().getResources().openRawResource(res);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    public void testCDataEntity() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_cdata);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertEquals(kmlContainer.getProperty("description"), "TELEPORT");
    }

    public void testCreateContainerProperty() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperties());
        assertEquals(kmlContainer.getProperty("name"), "Basic Folder");
        xmlPullParser = createParser(R.raw.amu_unknwown_folder);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperty("name"));
    }

    public void testCreateContainerPlacemark() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(kmlContainer.getPlacemarksHashMap().size(), 1);
        xmlPullParser = createParser(R.raw.amu_multiple_placemarks);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(kmlContainer.getPlacemarksHashMap().size(), 2);
    }

    public void testCreateContainerGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_ground_overlay);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertEquals(kmlContainer.getGroundOverlayHashMap().size(), 2);
    }

    public void testCreateContainerObjects() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_nested_folders);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertNotNull(kmlContainer.getContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(numberOfNestedContainers, 2);
    }



}
