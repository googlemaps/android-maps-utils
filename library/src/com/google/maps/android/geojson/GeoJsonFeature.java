package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
public class GeoJsonFeature extends Observable implements Observer {

    private final String mId;

    private final LatLngBounds mBoundingBox;

    private final HashMap<String, String> mProperties;

    private GeoJsonGeometry mGeoJsonGeometry;

    private GeoJsonPointStyle mGeoJsonPointStyle;

    private GeoJsonLineStringStyle mGeoJsonLineStringStyle;

    private GeoJsonPolygonStyle mGeoJsonPolygonStyle;

    /**
     * Creates a new GeoJsonFeature object
     *
     * @param GeoJsonGeometry type of geometry to assign to the feature
     * @param id              common identifier of the feature
     * @param properties      hashmap of containing properties related to the feature
     * @param boundingBox     bounding box of the feature
     */
    public GeoJsonFeature(GeoJsonGeometry GeoJsonGeometry, String id,
            HashMap<String, String> properties, LatLngBounds boundingBox) {
        mGeoJsonGeometry = GeoJsonGeometry;
        mId = id;
        mBoundingBox = boundingBox;
        mProperties = properties;
        mGeoJsonPointStyle = new GeoJsonPointStyle();
        mGeoJsonLineStringStyle = new GeoJsonLineStringStyle();
        mGeoJsonPolygonStyle = new GeoJsonPolygonStyle();
    }

    /**
     * Returns all the stored property keys
     *
     * @return iterable of property keys
     */
    public Iterable<String> getPropertyKeys() {
        return mProperties.keySet();
    }

    /**
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    public String setProperty(String property, String propertyValue) {
        return mProperties.put(property, propertyValue);
    }

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    public boolean hasProperty(String property) {
        return mProperties.containsKey(property);
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    public String removeProperty(String property) {
        return mProperties.remove(property);
    }

    /**
     * Returns the style used to render GeoJsonPoints
     *
     * @return style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle getPointStyle() {
        return mGeoJsonPointStyle;
    }

    /**
     * Sets the style used to render GeoJsonPoints
     *
     * @param geoJsonPointStyle style used to render GeoJsonPoints
     */
    public void setPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mGeoJsonPointStyle.deleteObserver(this);
        mGeoJsonPointStyle = geoJsonPointStyle;
        mGeoJsonPointStyle.addObserver(this);
        checkRedrawFeature(mGeoJsonPointStyle);
    }

    /**
     * Returns the style used to render GeoJsonLineStrings
     *
     * @return style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle getLineStringStyle() {
        return mGeoJsonLineStringStyle;
    }

    /**
     * Sets the style used to render GeoJsonLineStrings
     *
     * @param geoJsonLineStringStyle style used to render GeoJsonLineStrings
     */
    public void setLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mGeoJsonLineStringStyle.deleteObserver(this);
        mGeoJsonLineStringStyle = geoJsonLineStringStyle;
        mGeoJsonLineStringStyle.addObserver(this);
        checkRedrawFeature(mGeoJsonLineStringStyle);
    }

    /**
     * Returns the style used to render GeoJsonPolygons
     *
     * @return style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle getPolygonStyle() {
        return mGeoJsonPolygonStyle;
    }

    /**
     * Sets the style used to render GeoJsonPolygons
     *
     * @param geoJsonPolygonStyle style used to render GeoJsonPolygons
     */
    public void setPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mGeoJsonPolygonStyle.deleteObserver(this);
        mGeoJsonPolygonStyle = geoJsonPolygonStyle;
        mGeoJsonPolygonStyle.addObserver(this);
        checkRedrawFeature(mGeoJsonPolygonStyle);
    }

    /**
     * Checks whether the new style that was set requires the feature to be redrawn. If the
     * geometry
     * and the style that was set match, then the feature is redrawn.
     *
     * @param style style to check if a redraw is needed
     */
    private void checkRedrawFeature(GeoJsonStyle style) {
        if (mGeoJsonGeometry != null && mGeoJsonGeometry.getType()
                .matches(style.getGeometryType())) {
            // Don't redraw objects that aren't on the map
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Gets the stored GeoJsonGeometry
     *
     * @return GeoJsonGeometry
     */
    public GeoJsonGeometry getGeometry() {
        return mGeoJsonGeometry;
    }

    /**
     * Sets the stored GeoJsonGeometry and redraws it on the layer if it has already been added
     *
     * @param geometry GeoJsonGeometry to set
     */
    public void setGeometry(GeoJsonGeometry geometry) {
        mGeoJsonGeometry = geometry;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the ID of the feature
     *
     * @return ID of the feature, if there is no ID then null will be returned
     */
    public String getId() {
        return mId;
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    public boolean hasGeometry() {
        return (mGeoJsonGeometry != null);
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the feature. If
     * the feature did not have a bounding box then null will be returned.
     *
     * @return LatLngBounds containing bounding box of the feature, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feature{");
        sb.append("\n bounding box=").append(mBoundingBox);
        sb.append(",\n geometry=").append(mGeoJsonGeometry);
        sb.append(",\n point style=").append(mGeoJsonPointStyle);
        sb.append(",\n line string style=").append(mGeoJsonLineStringStyle);
        sb.append(",\n polygon style=").append(mGeoJsonPolygonStyle);
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(mProperties);
        sb.append("\n}\n");
        return sb.toString();
    }

    /**
     * Update is called if the developer modifies a style that is stored in this feature
     *
     * @param observable GeoJsonStyle object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonStyle) {
            checkRedrawFeature((GeoJsonStyle) observable);
        }
    }
}
