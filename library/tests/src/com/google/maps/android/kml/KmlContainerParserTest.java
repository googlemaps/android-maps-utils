package com.google.maps.android.kml;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by lavenderch on 1/27/15.
 */
public class KmlContainerParserTest extends TestCase {

    KmlContainerParser parser;
    KmlContainer kmlContainer;

    public XmlPullParser createSimpleContainer() throws Exception {
        String folder =
                "<Folder>\n" +
                "   <name>Folder name</name>\n" +
                "   <Placemark>\n" +
                "    <name>Pin on a mountaintop</name>\n" +
                "    <styleUrl>#pushpin</styleUrl>\n" +
                "    <Point>\n" +
                "      <coordinates>170.1435558771009,-43.60505741890396,0</coordinates>\n" +
                "    </Point>\n" +
                "    </Placemark>\n" +
                 "</Folder>";
        InputStream stream = new ByteArrayInputStream( folder.getBytes() );
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    public XmlPullParser createNestedContainer() throws Exception {
        String folder =
                "<Folder>\n" +
                        "<Folder>\n" +
                        "</Folder>\n" +
                        "<Folder>\n" +
                        "</Folder>\n" +
                "</Folder>\n";
        InputStream stream = new ByteArrayInputStream( folder.getBytes() );
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        parser.next();
        return parser;
    }

    public void testCreateContainerProperty() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createSimpleContainer());
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlProperties());
        assertEquals(kmlContainer.getKmlProperty("name"), "Folder name");
    }

    public void testCreateContainerPlacemark() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createSimpleContainer());
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        assertTrue(kmlContainer.hasKmlPlacemarks());
        assertEquals(kmlContainer.getPlacemarks().size(), 1);
    }

    public void testCreateContainerGroundOverlay() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createSimpleContainer());
        KmlContainer kmlContainer = new KmlContainer();
        parser.assignFolderProperties(kmlContainer);
        //TODO: Test for Ground Overlay
    }

    public void testCreateContainerObjects() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createNestedContainer());
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
