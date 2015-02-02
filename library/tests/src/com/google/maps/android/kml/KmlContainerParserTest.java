package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

import com.google.maps.android.test.R;

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
        XmlPullParser xmlPullParser = createParser(R.raw.cdata);
        KmlContainerParser parser = new KmlContainerParser();
        KmlContainer kmlContainer = parser.assignPropertiesToContainer(xmlPullParser);
        assertEquals(kmlContainer.getKmlProperty("description"), "TELEPORT");
    }

    public void testCreateContainerProperty() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.basic_folder);
        KmlContainerParser parser = new KmlContainerParser();
        KmlContainer kmlContainer = parser.assignPropertiesToContainer(xmlPullParser);
        assertTrue(kmlContainer.hasKmlProperties());
        assertEquals(kmlContainer.getKmlProperty("name"), "Basic Folder");
    }

    public void testCreateContainerPlacemark() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.basic_folder);
        KmlContainerParser parser = new KmlContainerParser();
        KmlContainer kmlContainer = parser.assignPropertiesToContainer(xmlPullParser);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 1);
        xmlPullParser = createParser(R.raw.multiple_placemarks);
        parser = new KmlContainerParser();
        kmlContainer = parser.assignPropertiesToContainer(xmlPullParser);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 2);
    }

    public void testCreateContainerGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.ground_overlay);
        KmlContainerParser parser = new KmlContainerParser();
        KmlContainer kmlContainer = parser.assignPropertiesToContainer(xmlPullParser);
        assertEquals(kmlContainer.getGroundOverlayHashMap().size(), 2);
    }

    public void testCreateContainerObjects() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.nested_folders);
        KmlContainerParser parser = new KmlContainerParser();
        parser.createContainer(xmlPullParser);
        KmlContainer kmlContainer = parser.getContainer();
        assertNotNull(kmlContainer.getNestedKmlContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getNestedKmlContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(numberOfNestedContainers, 2);
    }



}
