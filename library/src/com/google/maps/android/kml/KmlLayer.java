package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private final KmlRenderer mRenderer;

    private XmlPullParser mParser;

    /**
     * Creates a new KmlLayer object
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param context    Context object
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context)
            throws XmlPullParserException {
        this(map, context.getResources().openRawResource(resourceId));
    }

    /**
     * Creates a new KmlLayer object
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, InputStream stream)
            throws XmlPullParserException {
        mRenderer = new KmlRenderer(map);
        mParser = createXmlParser(stream);
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
     *
     * @throws XmlPullParserException if KML file cannot be parsed
     * @throws IOException            if KML file cannot be opened
     */
    public void addKmlData() throws IOException, XmlPullParserException {
        KmlParser parser = new KmlParser(mParser);
        parser.parseKml();
        mRenderer.addKmlData(parser.getStyles(), parser.getStyleMaps(), parser.getPlacemarks(),
                parser.getFolders(), parser.getGroundOverlays());
        // TODO: rethink this method, do we need to reparse?
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeKmlData() {
        // TODO: remove all ground overlays
        mRenderer.removeKmlData();
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */

    public Boolean hasKmlPlacemarks() {
        return mRenderer.hasKmlPlacemarks();
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    public Iterable<KmlPlacemark> getKmlPlacemarks() {
        return mRenderer.getKmlPlacemarks();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    public boolean hasNestedContainers() {
        return mRenderer.hasNestedContainers();
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    public Iterable<KmlContainer> getNestedContainers() {
        return mRenderer.getNestedContainers();
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return mRenderer.getGroundOverlays();
    }

    /**
     * Gets the map that objects are being placed on
     *
     * @return map
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Sets the map that objects are being placed on
     *
     * @param map map to place placemark, container, style and ground overlays on
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }
}
