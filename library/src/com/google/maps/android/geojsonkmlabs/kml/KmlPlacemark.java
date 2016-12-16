package com.google.maps.android.geojsonkmlabs.kml;

import com.google.maps.android.geojsonkmlabs.Feature;
import com.google.maps.android.geojsonkmlabs.Geometry;

import java.util.HashMap;

/**
 * Represents a placemark which is either a {@link com.google.maps.android.geojsonkmlabs.kml.KmlPoint},
 * {@link
 * com.google.maps.android.geojsonkmlabs.kml.KmlLineString}, {@link com.google.maps.android.geojsonkmlabs.kml.KmlPolygon} or a
 * {@link com.google.maps.android.geojsonkmlabs.kml.KmlMultiGeometry}. Stores the properties and styles of the
 * place.
 */
public class KmlPlacemark extends Feature{

    private final String mStyle;

    private final KmlStyle mInlineStyle;

    /**
     * Creates a new KmlPlacemark object
     *
     * @param geometry   geometry object to store
     * @param style      style id to store
     * @param properties properties hashmap to store
     */
    public KmlPlacemark(KmlGeometry geometry, String style, KmlStyle inlineStyle,
            HashMap<String, String> properties) {
        super((Geometry) geometry, style, properties);
        mStyle = style;
        mInlineStyle = inlineStyle;

    }
//check if this method can or should be deleted
    /**
     * Gets the style id associated with the basic_placemark
     *
     * @return style id
     */
    public String getStyleId() {
        return mStyle;
    }

    /**
     * Gets the inline style that was found
     *
     * @return InlineStyle or null if not found
     */
    public KmlStyle getInlineStyle() {
        return mInlineStyle;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Placemark").append("{");
        sb.append("\n style id=").append(mStyle);
        sb.append(",\n inline style=").append(mInlineStyle);
        sb.append(",\n properties=").append(super.getProperties());
        sb.append(",\n geometry=").append(super.getGeometry());
        sb.append("\n}\n");
        return sb.toString();
    }
}
