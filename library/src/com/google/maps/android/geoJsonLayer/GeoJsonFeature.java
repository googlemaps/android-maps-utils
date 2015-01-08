package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

/**
 * Created by juliawong on 12/29/14.
 *
 * A feature has a geometry, id and set of properties. Styles are also stored in this class.
 */
public class GeoJsonFeature extends Observable {

    private final GeoJsonGeometry mGeoJsonGeometry;

    private final String mId;

    private ArrayList<LatLng> mBoundingBox;

    private final Map<String, String> mProperties;

    private GeoJsonPointStyle mGeoJsonPointStyle;

    private GeoJsonLineStringStyle mGeoJsonLineStringStyle;

    private GeoJsonPolygonStyle mGeoJsonPolygonStyle;

    private GoogleMap mMap;

    /**
     * Creates a new Feature object
     *
     * @param GeoJsonGeometry   type of geometry to assign to the feature
     * @param id         id to refer to the feature by
     * @param properties map of data containing properties related to the feature
     */
    public GeoJsonFeature( GeoJsonGeometry GeoJsonGeometry, String id,
                          Map<String, String> properties, ArrayList<LatLng> boundingBox) {
        mGeoJsonGeometry = GeoJsonGeometry;
        mId = id;
        mBoundingBox = boundingBox;
        mProperties = properties;
        mGeoJsonPointStyle = new GeoJsonPointStyle();
        mGeoJsonLineStringStyle = new GeoJsonLineStringStyle();
        mGeoJsonPolygonStyle = new GeoJsonPolygonStyle();

    }

    /**
     * Gets the iterator of the property keys. Order of keys is undefined.
     *
     * @return iterator of property keys
     */
    public Iterator getProperties() {
        return mProperties.keySet().iterator();
    }

    // TODO: Redraw with new style when setters are used

    /**
     * Gets the PointStyle of the feature
     *
     * @return PointStyle object
     */
    public GeoJsonPointStyle getPointStyle() {
        return mGeoJsonPointStyle;
    }

    /**
     * Sets the PointStyle of the feature
     *
     * @param geoJsonPointStyle PointStyle object
     */
    public void setPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mGeoJsonPointStyle = geoJsonPointStyle;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the LineStringStyle of the feature
     *
     * @return LineStringStyle object
     */
    public GeoJsonLineStringStyle getLineStringStyle() {
        return mGeoJsonLineStringStyle;
    }

    /**
     * Sets the LineStringStyle of the feature
     *
     * @param geoJsonLineStringStyle LineStringStyle object
     */
    public void setLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mGeoJsonLineStringStyle = geoJsonLineStringStyle;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the PolygonStyle of the feature
     *
     * @return PolygonStyle object
     */
    public GeoJsonPolygonStyle getPolygonStyle() {
        return mGeoJsonPolygonStyle;
    }

    /**
     * Sets the PolygonStyle of the feature
     *
     * @param geoJsonPolygonStyle PolygonStyle object
     */
    public void setPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mGeoJsonPolygonStyle = geoJsonPolygonStyle;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public GeoJsonGeometry getGeometry() {
        return mGeoJsonGeometry;
    }

    /**
     * Gets the ID of the feature
     *
     * @return ID of the feature
     */
    public String getId() {
        return mId;
    }

    public boolean hasGeometry() {
        return (mGeoJsonGeometry != null);
    }

    public ArrayList<LatLng> getBoundingBox () {
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
