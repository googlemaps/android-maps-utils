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

import com.google.android.gms.maps.model.GroundOverlay;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses a given KML file into KmlStyle, KmlPlacemark, KmlGroundOverlay and KmlContainer objects
 */
/* package */ class KmlParser {

    private final static String STYLE = "Style";

    private final static String STYLE_MAP = "StyleMap";

    private final static String PLACEMARK = "Placemark";

    private final static String GROUND_OVERLAY = "GroundOverlay";

    private final static String CONTAINER_REGEX = "Folder|Document";

    private final XmlPullParser mParser;

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    private final ArrayList<KmlContainer> mContainers;

    private final HashMap<String, KmlStyle> mStyles;

    private final HashMap<String, String> mStyleMaps;

    private final HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private final static String UNSUPPORTED_REGEX = "altitude|altitudeModeGroup|altitudeMode|" +
            "begin|bottomFov|cookie|displayName|displayMode|end|expires|extrude|" +
            "flyToView|gridOrigin|httpQuery|leftFov|linkDescription|linkName|linkSnippet|" +
            "listItemType|maxSnippetLines|maxSessionLength|message|minAltitude|minFadeExtent|" +
            "minLodPixels|minRefreshPeriod|maxAltitude|maxFadeExtent|maxLodPixels|maxHeight|" +
            "maxWidth|near|NetworkLink|NetworkLinkControl|overlayXY|range|refreshMode|" +
            "refreshInterval|refreshVisibility|rightFov|roll|rotationXY|screenXY|shape|sourceHref|" +
            "state|targetHref|tessellate|tileSize|topFov|viewBoundScale|viewFormat|viewRefreshMode|" +
            "viewRefreshTime|when";

    /**
     * Creates a new KmlParser object
     *
     * @param parser parser containing the KML file to parse
     */
    /* package */ KmlParser(XmlPullParser parser) {
        mParser = parser;
        mPlacemarks = new HashMap<>();
        mContainers = new ArrayList<>();
        mStyles = new HashMap<>();
        mStyleMaps = new HashMap<>();
        mGroundOverlays = new HashMap<>();
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    /* package */ void parseKml() throws XmlPullParserException, IOException {
        int eventType = mParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (mParser.getName().matches(UNSUPPORTED_REGEX)) {
                    skip(mParser);
                }
                if (mParser.getName().matches(CONTAINER_REGEX)) {
                    mContainers.add(KmlContainerParser.createContainer(mParser));
                }
                if (mParser.getName().equals(STYLE)) {
                    KmlStyle style = KmlStyleParser.createStyle(mParser);
                    mStyles.put(style.getStyleId(), style);
                }
                if (mParser.getName().equals(STYLE_MAP)) {
                    mStyleMaps.putAll(KmlStyleParser.createStyleMap(mParser));
                }
                if (mParser.getName().equals(PLACEMARK)) {
                    mPlacemarks.put(KmlFeatureParser.createPlacemark(mParser), null);
                }
                if (mParser.getName().equals(GROUND_OVERLAY)) {
                    mGroundOverlays.put(KmlFeatureParser.createGroundOverlay(mParser), null);
                }
            }
            eventType = mParser.next();

        }
        //Need to put an empty new style
        mStyles.put(null, new KmlStyle());
    }

    /**
     * @return List of styles created by the parser
     */
    /* package */ HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * @return A list of Kml Placemark objects
     */
    /* package */ HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    /**
     * @return A list of Kml Style Maps
     */
    /* package */ HashMap<String, String> getStyleMaps() {
        return mStyleMaps;
    }

    /**
     * @return A list of Kml Folders
     */
    /* package */ ArrayList<KmlContainer> getContainers() {
        return mContainers;
    }

    /**
     * @return A list of Ground Overlays
     */
    /* package */ HashMap<KmlGroundOverlay, GroundOverlay> getGroundOverlays() {
        return mGroundOverlays;
    }

    /**
     * Skips tags from START TAG to END TAG
     *
     * @param parser XmlPullParser
     */
    /*package*/
    static void skip(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
