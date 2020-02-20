/*
 * Copyright 2020 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.maps.android.test.R;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

import static org.junit.Assert.*;

public class KmlContainerParserTest {
    private XmlPullParser createParser(int res) throws Exception {
        InputStream stream =
                InstrumentationRegistry.getInstrumentation()
                        .getTargetContext()
                        .getResources()
                        .openRawResource(res);
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
        assertEquals("TELEPORT", kmlContainer.getProperty("description"));
    }

    @Test
    public void testCreateContainerProperty() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperties());
        assertEquals("Basic Folder", kmlContainer.getProperty("name"));
        xmlPullParser = createParser(R.raw.amu_unknown_folder);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperty("name"));
    }

    @Test
    public void testCreateContainerPlacemark() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_basic_folder);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(1, kmlContainer.getPlacemarksHashMap().size());
        xmlPullParser = createParser(R.raw.amu_multiple_placemarks);
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(2, kmlContainer.getPlacemarksHashMap().size());
    }

    @Test
    public void testCreateContainerGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_ground_overlay);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertEquals(2, kmlContainer.getGroundOverlayHashMap().size());
    }

    @Test
    public void testCreateContainerObjects() throws Exception {
        XmlPullParser xmlPullParser = createParser(R.raw.amu_nested_folders);
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertNotNull(kmlContainer.getContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(2, numberOfNestedContainers);
    }
}
