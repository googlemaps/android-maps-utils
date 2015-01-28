package com.google.maps.android.kml;

import android.test.ActivityTestCase;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

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

    public void testCreateContainerProperty() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.basic_folder));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlProperties());
        assertEquals(kmlContainer.getKmlProperty("name"), "Basic Folder");
    }

    public void testCreateContainerPlacemark() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.basic_folder));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 1);
        parser = new KmlContainerParser(createParser(R.raw.multiple_placemarks));
        kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 2);

    }

    public void testCreateContainerGroundOverlay() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.basic_folder));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        //TODO: Test for Ground Overlay
    }

    public void testCreateContainerObjects() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser(R.raw.nested_folders));
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertNotNull(kmlContainer.getNestedKmlContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getNestedKmlContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(numberOfNestedContainers, 2);
    }



}
