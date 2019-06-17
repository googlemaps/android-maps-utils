package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
public class GeoJsonFeature extends Feature implements Observer {

    private final LatLngBounds mBoundingBox;

    private GeoJsonPointStyle mPointStyle;

    private GeoJsonLineStringStyle mLineStringStyle;

    private GeoJsonPolygonStyle mPolygonStyle;

    /**
     * Creates a new GeoJsonFeature object
     * @param geometry    type of geometry to assign to the feature
     * @param id          common identifier of the feature
     * @param properties  hashmap of containing properties related to the feature
     * @param boundingBox bounding box of the feature
     */
   public GeoJsonFeature(Geometry geometry, String id,
                         HashMap<String, String> properties, LatLngBounds boundingBox) {
        super(geometry, id, properties);
        mId = id;
        mBoundingBox = boundingBox;
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    public String setProperty(String property, String propertyValue) {
        return super.setProperty(property, propertyValue);
    }


    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    public String removeProperty(String property) {
        return super.removeProperty(property);
    }

    /**
     * Returns the style used to render GeoJsonPoints
     *
     * @return style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle getPointStyle() {
        return mPointStyle;
    }

    /**
     * Sets the style used to render GeoJsonPoints
     *
     * @param pointStyle style used to render GeoJsonPoints
     */
    public void setPointStyle(GeoJsonPointStyle pointStyle) {
        if (pointStyle == null) {
            throw new IllegalArgumentException("Point style cannot be null");
        }

        if (mPointStyle != null) {
            // Remove observer for previous style
            mPointStyle.deleteObserver(this);
        }
        mPointStyle = pointStyle;
        mPointStyle.addObserver(this);
        checkRedrawFeature(mPointStyle);
    }

    /**
     * Returns the style used to render GeoJsonLineStrings
     *
     * @return style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle getLineStringStyle() {
        return mLineStringStyle;
    }

    /**
     * Sets the style used to render GeoJsonLineStrings
     *
     * @param lineStringStyle style used to render GeoJsonLineStrings
     */
    public void setLineStringStyle(GeoJsonLineStringStyle lineStringStyle) {
        if (lineStringStyle == null) {
            throw new IllegalArgumentException("Line string style cannot be null");
        }

        if (mLineStringStyle != null) {
            // Remove observer for previous style
            mLineStringStyle.deleteObserver(this);
        }
        mLineStringStyle = lineStringStyle;
        mLineStringStyle.addObserver(this);
        checkRedrawFeature(mLineStringStyle);
    }

    /**
     * Returns the style used to render GeoJsonPolygons
     *
     * @return style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle getPolygonStyle() {
        return mPolygonStyle;
    }

    /**
     * Sets the style used to render GeoJsonPolygons
     *
     * @param polygonStyle style used to render GeoJsonPolygons
     */
    public void setPolygonStyle(GeoJsonPolygonStyle polygonStyle) {
        if (polygonStyle == null) {
            throw new IllegalArgumentException("Polygon style cannot be null");
        }

        if (mPolygonStyle != null) {
            // Remove observer for previous style
            mPolygonStyle.deleteObserver(this);
        }
        mPolygonStyle = polygonStyle;
        mPolygonStyle.addObserver(this);
        checkRedrawFeature(mPolygonStyle);

    }

    /**
     * Gets a PolygonOptions object from mPolygonStyle containing styles for the GeoJsonPolygon
     *
     * @return PolygonOptions object
     */
    public PolygonOptions getPolygonOptions() {
        return mPolygonStyle.toPolygonOptions();
    }

    /**
     * Gets a MarkerOptions object from mPointStyle containing styles for the GeoJsonPoint
     *
     * @return MarkerOptions object
     */
    public MarkerOptions getMarkerOptions(){
        return mPointStyle.toMarkerOptions();
    }

    /**
     * Gets a Polyline object from mLineStringStyle containing styles for the GeoJsonLineString
     *
     * @return Polyline object
     */
    public PolylineOptions getPolylineOptions(){
        return mLineStringStyle.toPolylineOptions();
    }


    /**
     * Checks whether the new style that was set requires the feature to be redrawn. If the
     * geometry and the style that was set match, then the feature is redrawn.
     *
     * @param style style to check if a redraw is needed
     */
    private void checkRedrawFeature(GeoJsonStyle style) {
        if (hasGeometry() && Arrays.asList(style.getGeometryType())
                .contains(getGeometry().getGeometryType())) {
            // Don't redraw objects that aren't on the map
            setChanged();
            notifyObservers();
        }
    }

   /**
     * Sets the stored Geometry and redraws it on the layer if it has already been added
     *
     * @param geometry Geometry to set
     */
    public void setGeometry(Geometry geometry) {
        super.setGeometry(geometry);
        setChanged();
        notifyObservers();
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
        sb.append(",\n geometry=").append(getGeometry());
        sb.append(",\n point style=").append(mPointStyle);
        sb.append(",\n line string style=").append(mLineStringStyle);
        sb.append(",\n polygon style=").append(mPolygonStyle);
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(getProperties());
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
