package com.google.maps.android.data.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.Renderer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer extends Layer {


    /**
     * Same as {@link KmlLayer#KmlLayer(GoogleMap, int, Context, String, boolean)} but with a null directoryName
     * and and will use the default info window adapter.
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context)
            throws XmlPullParserException, IOException {
        this(map, resourceId, context, null, true);
    }


    /**
     * Same as {@link KmlLayer#KmlLayer(GoogleMap, InputStream, Context, String, boolean)} but with a null directoryName
     * and will use the default info window adapter.
     */
    public KmlLayer(GoogleMap map, InputStream stream, Context context)
            throws XmlPullParserException, IOException {
        this(map, stream, context, null, true);
    }



    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param context    Context object
     * @param directoryName the fully qualified directory name to look in (in the android file
     *                      system) for any relative-path images, or null to only look online.
     * @param setDefaultInfoWindowAdapter See {@link Renderer#Renderer(GoogleMap, Context, boolean)} docs.
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context, String directoryName, boolean setDefaultInfoWindowAdapter)
            throws XmlPullParserException, IOException {
        this(map, context.getResources().openRawResource(resourceId), context, directoryName, setDefaultInfoWindowAdapter);
    }

    /**
     * Creates a new KmlLayer object
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @param directoryName the fully qualified directory name to look in (in the android file
     *                      system) for any relative-path images, or null to only look online.
     * @param setDefaultInfoWindowAdapter See {@link Renderer#Renderer(GoogleMap, Context, boolean)} docs.
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, InputStream stream, Context context, String directoryName, boolean setDefaultInfoWindowAdapter)
            throws XmlPullParserException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("KML InputStream cannot be null");
        }
        KmlRenderer mRenderer = new KmlRenderer(map, context, directoryName, setDefaultInfoWindowAdapter);
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
    public void addLayerToMap() throws IOException, XmlPullParserException {
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
