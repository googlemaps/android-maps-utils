package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Marker and creates a
 * {@link com.google.android.gms.maps.model.MarkerOptions} object
 */
public class MarkerProperties {

    private final MarkerOptions mOptions;

    private boolean mVisibility = true;

    /**
     * Takes in a JSONObject containing properties for a Marker and parses the relevant properties
     * for mOptions
     *
     * @param geoJsonPointProperties contains style properties of a marker
     * @param coordinates            {@link com.google.android.gms.maps.model.LatLng} object
     *                               representing coordinates of the marker
     */
    public MarkerProperties(JSONObject geoJsonPointProperties, LatLng coordinates)
            throws JSONException {

        this.mOptions = new MarkerOptions();

        this.mOptions.position(coordinates);

        // Parse style properties relevant to a Marker
        if (geoJsonPointProperties != null) {

            if (geoJsonPointProperties.has("id")) {
                //TODO: To consider if returning the elements added to the map
            }
            if (geoJsonPointProperties.has("title")) {
                this.mOptions.title(geoJsonPointProperties.getString("title"));
            }
            if (geoJsonPointProperties.has("snippet")) {
                this.mOptions.snippet(geoJsonPointProperties.getString("snippet"));
            }
            if (geoJsonPointProperties.has("alpha")) {
                this.mOptions.alpha((float) geoJsonPointProperties.getDouble("alpha"));
            }
            if (geoJsonPointProperties.has("anchorU") && geoJsonPointProperties.has("anchorV")) {
                this.mOptions.anchor((float) geoJsonPointProperties.getDouble("anchorU"),
                        (float) geoJsonPointProperties.getDouble("anchorV"));
            }
            if (geoJsonPointProperties.has("draggable")) {
                this.mOptions.draggable(geoJsonPointProperties.getBoolean("draggable"));
            }
            if (geoJsonPointProperties.has("rotation")) {
                this.mOptions.rotation((float) geoJsonPointProperties.getDouble("rotation"));
            }
            if (geoJsonPointProperties.has("visible")) {
                this.mOptions.visible(geoJsonPointProperties.getBoolean("visible"));
                mVisibility = geoJsonPointProperties.getBoolean("visible");
            }
        }
    }

    /**
     * Creates a MarkerOptions object with all of the properties from the properties object passed
     * in
     *
     * @return MarkerOptions object with defined mOptions
     */
    public MarkerOptions getMarkerOptions() {
        return this.mOptions;
    }

    /**
     * Gets the visibility of the Marker from when it was imported
     * @return true if visible, false if invisible
     */
    public boolean getVisibility() {
        return this.mVisibility;
    }

    /**
     * Creates a string containing properties for the marker
     *
     * @return string containing properties for marker
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MarkerProperties{");
        sb.append("\n position=").append(mOptions.getPosition());
        sb.append(",\n title=").append(mOptions.getTitle());
        sb.append(",\n snippet=").append(mOptions.getSnippet());
        sb.append(",\n alpha=").append(mOptions.getAlpha());
        sb.append(",\n anchor(U,V)= (").append(mOptions.getAnchorU()).append(", ")
                .append(mOptions.getAnchorV()).append(")");
        sb.append(",\n draggable=").append(mOptions.isDraggable());
        sb.append(",\n rotation=").append(mOptions.getRotation());
        sb.append(",\n visible=").append(mOptions.isVisible());
        sb.append("\n}");
        return sb.toString();
    }
}
