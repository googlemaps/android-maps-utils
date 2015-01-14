package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this
 * class.
 */
public class GeoJsonFeature extends Observable {

    private final String mId;

    private final ArrayList<LatLng> mBoundingBox;

    private final HashMap<String, String> mProperties;

    private GeoJsonGeometry mGeoJsonGeometry;

    private GeoJsonPointStyle mGeoJsonPointStyle;

    private GeoJsonLineStringStyle mGeoJsonLineStringStyle;

    private GeoJsonPolygonStyle mGeoJsonPolygonStyle;

    /**
     * Creates a new GeoJsonFeature object
     *
     * @param GeoJsonGeometry type of geometry to assign to the feature
     * @param id              id to refer to the feature by
     * @param properties      hashmap of data containing properties related to the feature
     * @param boundingBox     array defining the bounding box of the feature
     */
    public GeoJsonFeature(GeoJsonGeometry GeoJsonGeometry, String id,
            HashMap<String, String> properties, ArrayList<LatLng> boundingBox) {
        mGeoJsonGeometry = GeoJsonGeometry;
        mId = id;
        mBoundingBox = boundingBox;
        mProperties = properties;
        mGeoJsonPointStyle = new GeoJsonPointStyle();
        mGeoJsonLineStringStyle = new GeoJsonLineStringStyle();
        mGeoJsonPolygonStyle = new GeoJsonPolygonStyle();

    }

    /**
     * Gets the hashmap of the properties
     *
     * @return hashmap of properties
     */
    public HashMap<String, String> getProperties() {
        return mProperties;
    }

    /**
     * Gets the GeoJsonPointStyle of the feature
     *
     * @return GeoJsonPointStyle object
     */
    public GeoJsonPointStyle getPointStyle() {
        return mGeoJsonPointStyle;
    }

    /**
     * Sets the GeoJsonPointStyle of the feature
     *
     * @param geoJsonPointStyle GeoJsonPointStyle object
     */
    public void setPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mGeoJsonPointStyle = geoJsonPointStyle;
        checkRedrawFeature(mGeoJsonPointStyle);
    }

    /**
     * Gets the GeoJsonLineStringStyle of the feature
     *
     * @return GeoJsonLineStringStyle object
     */
    public GeoJsonLineStringStyle getLineStringStyle() {
        return mGeoJsonLineStringStyle;
    }

    /**
     * Sets the GeoJsonLineStringStyle of the feature
     *
     * @param geoJsonLineStringStyle GeoJsonLineStringStyle object
     */
    public void setLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mGeoJsonLineStringStyle = geoJsonLineStringStyle;
        checkRedrawFeature(mGeoJsonLineStringStyle);
    }

    /**
     * Gets the GeoJsonPolygonStyle of the feature
     *
     * @return GeoJsonPolygonStyle object
     */
    public GeoJsonPolygonStyle getPolygonStyle() {
        return mGeoJsonPolygonStyle;
    }

    /**
     * Sets the GeoJsonPolygonStyle of the feature
     *
     * @param geoJsonPolygonStyle GeoJsonPolygonStyle object
     */
    public void setPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mGeoJsonPolygonStyle = geoJsonPolygonStyle;
        checkRedrawFeature(mGeoJsonPolygonStyle);
    }

    /**
     * Checks whether the new style that was set requires the feature to be redrawn. If the geometry
     * and the style that was set match, then the feature is redrawn.
     *
     * @param style style to check if a redraw is needed
     */
    private void checkRedrawFeature(GeoJsonStyle style) {
        if (mGeoJsonGeometry != null && mGeoJsonGeometry.getType()
                .matches(style.getGeometryType())) {
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Gets the stored GeoJsonGeometry object
     *
     * @return GeoJsonGeometry object
     */
    public GeoJsonGeometry getGeometry() {
        return mGeoJsonGeometry;
    }

    public void setGeometry(GeoJsonGeometry geometry) {
        mGeoJsonGeometry = geometry;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the ID of the feature
     *
     * @return ID of the feature
     */
    public String getId() {
        return mId;
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, false if geometry is currently null
     */
    public boolean hasGeometry() {
        return (mGeoJsonGeometry != null);
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return array containing bounding box of FeatureCollection, null if no bounding box
     */
    public ArrayList<LatLng> getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feature{");
        sb.append("\n bounding box=").append(mBoundingBox);
        sb.append("\n geometry=").append(mGeoJsonGeometry);
        sb.append(",\n point style=").append(mGeoJsonPointStyle);
        sb.append(",\n line string style=").append(mGeoJsonLineStringStyle);
        sb.append(",\n polygon style=").append(mGeoJsonPolygonStyle);
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(mProperties);
        sb.append("\n}\n");
        return sb.toString();
    }
}
