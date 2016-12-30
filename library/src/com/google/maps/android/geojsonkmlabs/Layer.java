package com.google.maps.android.geojsonkmlabs;


import com.google.android.gms.maps.GoogleMap;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface Layer {
    /**
     * Gets an iterable of all Feature elements that have been added to the layer
     *
     * @return iterable of Feature elements
     */
    Iterable<Feature> getFeatures();

    /**
     * Adds all the Feature objects parsed from the given GeoJSON or KML data onto the map
     */
    void addLayerToMap() throws IOException, XmlPullParserException;

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    GoogleMap getMap();

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    void setMap(GoogleMap map);

    /**
     * Removes all Features on the layer from the map
     */
    void removeLayerFromMap();

}
