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

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.data.Layer;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer extends Layer {

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param activity   Activity object
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, int resourceId, FragmentActivity activity)
            throws XmlPullParserException, IOException {
        this(map, activity.getResources().openRawResource(resourceId), activity, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map));
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @param activity   Activity object
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, InputStream stream, FragmentActivity activity)
            throws XmlPullParserException, IOException {
        this(map, stream, activity, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map));
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     *
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param activity   Activity object
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, int resourceId, FragmentActivity activity, MarkerManager markerManager, PolygonManager polygonManager, PolylineManager polylineManager, GroundOverlayManager groundOverlayManager)
            throws XmlPullParserException, IOException {
        this(map, activity.getResources().openRawResource(resourceId), activity, markerManager, polygonManager, polylineManager, groundOverlayManager);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     *
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @param activity   Activity object
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, InputStream stream, FragmentActivity activity, MarkerManager markerManager, PolygonManager polygonManager, PolylineManager polylineManager, GroundOverlayManager groundOverlayManager)
            throws XmlPullParserException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("KML InputStream cannot be null");
        }
        KmlRenderer mRenderer = new KmlRenderer(map, activity, markerManager, polygonManager, polylineManager, groundOverlayManager);
        XmlPullParser xmlPullParser = createXmlParser(stream);
        KmlParser parser = new KmlParser(xmlPullParser);
        parser.parseKml();
        stream.close();
        mRenderer.storeKmlData(parser.getStyles(), parser.getStyleMaps(), parser.getPlacemarks(),
                parser.getContainers(), parser.getGroundOverlays());
        storeRenderer(mRenderer);
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    private static XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Adds the KML data to the map
     */
    @Override
    public void addLayerToMap() {
        super.addKMLToMap();
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasPlacemarks() {
        return hasFeatures();
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    public Iterable<KmlPlacemark> getPlacemarks() {
        return (Iterable<KmlPlacemark>) getFeatures();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    public boolean hasContainers() {
        return super.hasContainers();
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    public Iterable<KmlContainer> getContainers() {
        return super.getContainers();
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return super.getGroundOverlays();
    }

}
