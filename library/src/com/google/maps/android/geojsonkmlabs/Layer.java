package com.google.maps.android.geojsonkmlabs;


import com.google.android.gms.maps.GoogleMap;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface Layer {
    /**
     * Gets an iterable of all GeoJsonFeature elements that have been added to the layer
     *
     * @return iterable of GeoJsonFeature elements
     */
    Iterable<Feature> getFeatures();

    /**
     * Adds all the Feature objects parsed from the given GeoJSON data onto the map
     */
    void addLayerToMap() throws IOException, XmlPullParserException;

    GoogleMap getMap();

    void setMap(GoogleMap map);

    void removeLayerFromMap();

}
