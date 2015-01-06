package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by juliawong on 12/29/14.
 *
 * A feature has a geometry, id and set of properties. Styles are also stored in this class.
 */
public class Feature {

    private final Geometry mGeometry;

    private final String mId;

    private MultiPoint mBoundingBox;

    private final Map<String, String> mProperties;

    private PointStyle mPointStyle;

    private LineStringStyle mLineStringStyle;

    private PolygonStyle mPolygonStyle;

    // check if ID exists

    /**
     * Creates a new Feature object
     *
     * @param geometry   type of geometry to assign to the feature
     * @param id         id to refer to the feature by
     * @param properties map of data containing properties related to the feature
     */
    public Feature(Geometry geometry, String id, Map<String, String> properties,
                   MultiPoint boundingBox) {
        mGeometry = geometry;
        mId = id;
        mBoundingBox = boundingBox;
        mProperties = properties;
        mPointStyle = new PointStyle();
        mLineStringStyle = new LineStringStyle();
        mPolygonStyle = new PolygonStyle();
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
    public PointStyle getPointStyle() {
        return mPointStyle;
    }

    /**
     * Sets the PointStyle of the feature
     *
     * @param pointStyle PointStyle object
     */
    public void setPointStyle(PointStyle pointStyle) {
        mPointStyle = pointStyle;
    }

    /**
     * Gets the LineStringStyle of the feature
     *
     * @return LineStringStyle object
     */
    public LineStringStyle getLineStringStyle() {
        return mLineStringStyle;
    }

    /**
     * Sets the LineStringStyle of the feature
     *
     * @param lineStringStyle LineStringStyle object
     */
    public void setLineStringStyle(LineStringStyle lineStringStyle) {
        mLineStringStyle = lineStringStyle;
    }

    /**
     * Gets the PolygonStyle of the feature
     *
     * @return PolygonStyle object
     */
    public PolygonStyle getPolygonStyle() {
        return mPolygonStyle;
    }

    /**
     * Sets the PolygonStyle of the feature
     *
     * @param polygonStyle PolygonStyle object
     */
    public void setPolygonStyle(PolygonStyle polygonStyle) {
        mPolygonStyle = polygonStyle;
    }

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public Geometry getGeometry() {
        return mGeometry;
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
        return (mGeometry != null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feature{");
        sb.append("\n bounding box=").append(mBoundingBox);
        sb.append("\n geometry=").append(mGeometry);
        sb.append(",\n point style=").append(mPointStyle);
        sb.append(",\n line string style=").append(mLineStringStyle);
        sb.append(",\n polygon style=").append(mPolygonStyle);
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(mProperties);
        sb.append("\n}\n");
        return sb.toString();
    }
}
