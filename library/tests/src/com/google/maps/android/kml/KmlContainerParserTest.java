package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

import com.google.maps.android.test.R;

/**
 * Created by lavenderch on 1/27/15.
 */
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
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.cdata));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertEquals(kmlContainer.getKmlProperty("description"), "TELEPORT");
    }

    public void testCreateContainerProperty() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.basic_folder));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlProperties());
        assertEquals(kmlContainer.getKmlProperty("name"), "Basic Folder");
    }

    public void testCreateContainerPlacemark() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.basic_folder));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 1);
        parser = new KmlContainerParser(createParser(R.raw.multiple_placemarks));
        kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 2);
    }

    public void testCreateContainerGroundOverlay() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.ground_overlay));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertEquals(kmlContainer.getGroundOverlayHashMap().size(), 2);
    }

    public void testCreateContainerObjects() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.nested_folders));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignContainerProperties(kmlContainer);
        assertNotNull(kmlContainer.getNestedKmlContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getNestedKmlContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(numberOfNestedContainers, 2);
    }



}
