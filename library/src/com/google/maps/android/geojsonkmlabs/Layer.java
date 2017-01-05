package com.google.maps.android.geojsonkmlabs;


import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPointStyle;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonRenderer;
import com.google.maps.android.geojsonkmlabs.kml.KmlContainer;
import com.google.maps.android.geojsonkmlabs.kml.KmlGroundOverlay;
import com.google.maps.android.geojsonkmlabs.kml.KmlRenderer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public abstract class Layer {

    private Renderer mRenderer;

    /**
     * Adds the KML data to the map
     */
    protected void addKMLToMap() throws IOException, XmlPullParserException {
        ((KmlRenderer)mRenderer).addLayerToMap();
    }

    /**
     * Adds the GeoJson data to the map
     */
    protected void addGeoJsonToMap() {
        ((GeoJsonRenderer)mRenderer).addLayerToMap();
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeLayerFromMap() {
        if (mRenderer instanceof GeoJsonRenderer) {
            ((GeoJsonRenderer)mRenderer).removeLayerFromMap();
        } else if (mRenderer instanceof KmlRenderer){
            ((KmlRenderer)mRenderer).removeLayerFromMap();
        }
    }

    /**
     * Sets a single click listener for the entire GoogleMap object, that will be called
     * with the corresponding GeoJsonFeature object when an object on the map (Polygon,
     * Marker, Polyline) is clicked.
     *
     * Note that if multiple GeoJsonLayer objects are bound to a GoogleMap object, calling
     * setOnFeatureClickListener on one will override the listener defined on the other. In
     * that case, you must define each of the GoogleMap click listeners manually
     * (OnPolygonClickListener, OnMarkerClickListener, OnPolylineClickListener), and then
     * use the GeoJsonLayer.getFeature(mapObject) method on each GeoJsonLayer instance to
     * determine if the given mapObject belongs to the layer.
     *
     * If getFeature() returns null this means that the object is a MultiPolygon, MultiLineString
     * or MultiPoint and must be handled differently.
     *
     * @param listener Listener providing the onFeatureClick method to call.
     */
    public void setOnFeatureClickListener(final Renderer.OnFeatureClickListener listener) {
        mRenderer.setOnFeatureClickListener(listener);
    }

    protected void storeRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    /**
     * Gets an iterable of all Feature elements that have been added to the layer
     *
     * @return iterable of Feature elements
     */
    public Iterable<Feature> getFeatures(){
        return mRenderer.getFeatures();
    }

    /**
     * Checks if there are features stored
     * @return true if features are stored, false otherwise
     */
    protected boolean hasFeatures() {
        return mRenderer.hasFeatures();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    protected boolean hasContainers() {
        if (mRenderer instanceof KmlRenderer) {
            return ((KmlRenderer)mRenderer).hasNestedContainers();
        }
        return false;
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    protected Iterable<KmlContainer> getContainers() {
        if (mRenderer instanceof KmlRenderer) {
            return ((KmlRenderer)mRenderer).getNestedContainers();
        }
        return null;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    protected Iterable<KmlGroundOverlay> getGroundOverlays() {
        if (mRenderer instanceof KmlRenderer) {
            return ((KmlRenderer)mRenderer).getGroundOverlays();
        }
        return null;
    }

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    public GoogleMap getMap(){
        return mRenderer.getMap();
    }

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    public void setMap(GoogleMap map){
        mRenderer.setMap(map);
    }

    /**
     * Checks if the layer is on the map
     *
     * @return true if the layer is on the map, false otherwise
     */
    public boolean isLayerOnMap() {
        return mRenderer.isLayerOnMap();
    }

    /**
     * Adds a provided feature to the map
     *
     * @param feature feature to add to map
     */
    protected void addFeature(Feature feature){
        mRenderer.addFeature(feature);

    }

    /**
     * Remove a specified feature from the map
     *
     * @param feature feature to be removed
     */
    protected void removeFeature(Feature feature) {
        mRenderer.removeFeature(feature);
    }

    /**
     * Gets the default style used to render GeoJsonPoints. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle getDefaultPointStyle() {
        return mRenderer.getDefaultPointStyle();
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mRenderer.getDefaultLineStringStyle();
    }

    /**
     * Gets the default style used to render GeoJsonPolygons. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mRenderer.getDefaultPolygonStyle();
    }
}
