/*
 * Copyright 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.data;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.data.geojson.GeoJsonRenderer;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlGroundOverlay;
import com.google.maps.android.data.kml.KmlRenderer;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlLayer KmlLayer} and
 * {@link com.google.maps.android.data.geojson.GeoJsonLayer GeoJsonLayer}
 */
public abstract class Layer {

    private Renderer mRenderer;

    /**
     * Adds the KML data to the map
     */
    protected void addKMLToMap() {
        if (mRenderer instanceof KmlRenderer) {
            ((KmlRenderer) mRenderer).addLayerToMap();
        } else {
            throw new UnsupportedOperationException("Stored renderer is not a KmlRenderer");
        }
    }

    /**
     * Adds GeoJson data to the map
     */
    protected void addGeoJsonToMap() {
        if (mRenderer instanceof GeoJsonRenderer) {
            ((GeoJsonRenderer) mRenderer).addLayerToMap();
        } else {
            throw new UnsupportedOperationException("Stored renderer is not a GeoJsonRenderer");
        }
    }

    public abstract void addLayerToMap();

    /**
     * Removes all the data from the map and clears all the stored placemarks
     */
    public void removeLayerFromMap() {
        if (mRenderer instanceof GeoJsonRenderer) {
            ((GeoJsonRenderer) mRenderer).removeLayerFromMap();
        } else if (mRenderer instanceof KmlRenderer) {
            ((KmlRenderer) mRenderer).removeLayerFromMap();
        }
    }

    /**
     * Sets a single click listener for the entire GoogleMap object, that will be called
     * with the corresponding Feature object when an object on the map (Polygon,
     * Marker, Polyline) is clicked.
     * <p>
     * If getFeature() returns null this means that either the object is inside a KMLContainer,
     * or the object is a MultiPolygon, MultiLineString or MultiPoint and must
     * be handled differently.
     *
     * @param listener Listener providing the onFeatureClick method to call.
     */
    public void setOnFeatureClickListener(final OnFeatureClickListener listener) {
        mRenderer.setOnFeatureClickListener(listener);
    }

    /**
     * Callback interface for when a map object is clicked.
     */
    public interface OnFeatureClickListener {
        void onFeatureClick(Feature feature);
    }

    /**
     * Stores a new Renderer object into mRenderer
     *
     * @param renderer the new Renderer object that belongs to this Layer
     */
    protected void storeRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    /**
     * Gets an iterable of all Feature elements that have been added to the layer
     *
     * @return iterable of Feature elements
     */
    public Iterable<? extends Feature> getFeatures() {
        return mRenderer.getFeatures();
    }

    /**
     * Retrieves a corresponding Feature instance for the given Object
     * Allows maps with multiple layers to determine which layer the Object
     * belongs to.
     *
     * @param mapObject Object
     * @return Feature for the given object
     */
    public Feature getFeature(Object mapObject) {
        return mRenderer.getFeature(mapObject);
    }

    public Feature getContainerFeature(Object mapObject) {
        return mRenderer.getContainerFeature(mapObject);
    }

    /**
     * Checks if there are any features currently on the layer
     *
     * @return true if there are features on the layer, false otherwise
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
            return ((KmlRenderer) mRenderer).hasNestedContainers();
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
            return ((KmlRenderer) mRenderer).getNestedContainers();
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
            return ((KmlRenderer) mRenderer).getGroundOverlays();
        }
        return null;
    }

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }

    /**
     * Checks if the current layer has been added to the map
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
    protected void addFeature(Feature feature) {
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
