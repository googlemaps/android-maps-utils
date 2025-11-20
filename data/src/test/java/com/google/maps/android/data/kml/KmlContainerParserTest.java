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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParser;

import static com.google.maps.android.data.kml.KmlTestUtil.createParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Ignore("I should run via Robolectric - I currently freeze") // FIXME
public class KmlContainerParserTest {

    @Test
    public void testCDataEntity() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_cdata.kml");
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertEquals("TELEPORT", kmlContainer.getProperty("description"));
    }

    @Test
    public void testCreateContainerProperty() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_basic_folder.kml");
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperties());
        assertEquals("Basic Folder", kmlContainer.getProperty("name"));
        xmlPullParser = createParser("amu_unknown_folder.kml");
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasProperty("name"));
    }

    @Test
    public void testCreateContainerPlacemark() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_basic_folder.kml");
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(1, kmlContainer.getPlacemarksHashMap().size());
        xmlPullParser = createParser("amu_multiple_placemarks.kml");
        kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertTrue(kmlContainer.hasPlacemarks());
        assertEquals(2, kmlContainer.getPlacemarksHashMap().size());
    }

    @Test
    public void testCreateContainerGroundOverlay() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_ground_overlay.kml");
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertEquals(2, kmlContainer.getGroundOverlayHashMap().size());
    }

    @Test
    public void testCreateContainerObjects() throws Exception {
        XmlPullParser xmlPullParser = createParser("amu_nested_folders.kml");
        KmlContainer kmlContainer = KmlContainerParser.createContainer(xmlPullParser);
        assertNotNull(kmlContainer.getContainers());
        int numberOfNestedContainers = 0;
        for (KmlContainer container : kmlContainer.getContainers()) {
            numberOfNestedContainers++;
        }
        assertEquals(2, numberOfNestedContainers);
    }
}
