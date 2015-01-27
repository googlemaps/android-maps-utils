package com.google.maps.android.kml;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by lavenderch on 1/27/15.
 */
public class KmlContainerParserTest extends TestCase {


    public XmlPullParser createParser() throws Exception {
        String folder =
                "  <Placemark>\n" +
                "    <name>Pin on a mountaintop</name>\n" +
                "    <styleUrl>#pushpin</styleUrl>\n" +
                "    <Point>\n" +
                "      <coordinates>170.1435558771009,-43.60505741890396,0</coordinates>\n" +
                "    </Point>\n" +
                "  </Placemark>\n";
        InputStream stream = new ByteArrayInputStream( folder.getBytes() );
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }


    public void testAssignFolderProperties() throws Exception {
        KmlContainerParser parser = new KmlContainerParser(createParser());
        KmlContainer kmlContainer = new KmlContainer();
        parser.createContainerPlacemark(kmlContainer);
        assertEquals(kmlContainer.getPlacemarks().size(), 1);

    }
}
