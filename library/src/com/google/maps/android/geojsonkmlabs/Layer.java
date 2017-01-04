package com.google.maps.android.geojsonkmlabs;


import com.google.android.gms.maps.GoogleMap;

public class Layer {

    private Renderer mRenderer;

    public Layer() {
        mRenderer = null;
    }

    protected void storeRenderer(Renderer renderer) {
        mRenderer = renderer;
    }
    /**
     * Gets an iterable of all Feature elements that have been added to the layer
     *
     * @return iterable of Feature elements
     */
    public Iterable<Feature> getFeatures(){
        return mRenderer.getFeatures();
    }

    protected boolean hasFeatures() {
        return mRenderer.hasFeatures();
    }


    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    public GoogleMap getMap(){
        return mRenderer.getMap();
    }

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    public void setMap(GoogleMap map){
        mRenderer.setMap(map);
    }

    public boolean isLayerOnMap() {
        return mRenderer.isLayerOnMap();
    }


}
