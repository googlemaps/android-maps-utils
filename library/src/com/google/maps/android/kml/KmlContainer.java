package com.google.maps.android.kml;

import com.google.android.gms.maps.model.GroundOverlay;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a KML Document or Folder.
 */
public class KmlContainer implements KmlContainerInterface {

    private final HashMap<String, String> mContainerProperties;

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    private final ArrayList<KmlContainerInterface> mContainers;

    private final HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private final HashMap<String, String> mStyleMap;

    private HashMap<String, KmlStyle> mStyles;

    public KmlContainer() {
        mContainerProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyles = new HashMap<String, KmlStyle>();
        mStyleMap = new HashMap<String, String>();
        mContainers = new ArrayList<KmlContainerInterface>();
        mGroundOverlays = new HashMap<KmlGroundOverlay, GroundOverlay>();
    }

    /**
     * Gets a style based on an ID
     */
    public KmlStyle getStyle(String styleID) {
        return mStyles.get(styleID);
    }

    /**
     * @return HashMap of styles, with key values representing style name (ie, color) and
     * value representing style value (ie #FFFFFF)
     */
    /* package */ HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * Takes an ArrayList of styles and assigns these folders with the styles
     */
    /* package */ void setStyles(HashMap<String, KmlStyle> styles) {
        mStyles = styles;
    }

    /**
     * @param placemarks Placemark for the container to contain
     * @param object     Corresponding GoogleMap map object of the placemark (if it has been added
     *                   to the map)
     */
    /* package */ void setPlacemark(KmlPlacemark placemarks, Object object) {
        mPlacemarks.put(placemarks, object);
    }

    /**
     * Add a nested container
     *
     * @param container Container to nest within the current instance of the container
     */
    /* package */ void addChildContainer(KmlContainer container) {
        mContainers.add(container);
    }

    /**
     * @return HashMap of containers
     */
    public HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    /**
     * Sets a property to be contained by the container
     *
     * @param propertyName  Name of the property, ie "name"
     * @param propertyValue Value of the property, ie "Arizona"
     */
    public void setProperty(String propertyName, String propertyValue) {
        mContainerProperties.put(propertyName, propertyValue);
    }

    /**
     * Sets a style to be contained by the container
     *
     * @param styleId Name or ID of the style
     * @param style   KmlStyle object
     */
    public void setStyle(String styleId, KmlStyle style) {
        mStyles.put(styleId, style);
    }

    /**
     * @return A map of strings representing a style map, null if no style maps exist
     */
    /* package */ HashMap<String, String> getStyleMap() {
        return mStyleMap;
    }

    /**
     * @param styleMap Adds a map of strings representing a style map
     */
    /* package */ void setStyleMap(HashMap<String, String> styleMap) {
        mStyleMap.putAll(styleMap);
    }

    /**
     * Add a ground overlay for this container
     *
     * @param groundOverlay ground overlay to add
     */
    /* package */ void addGroundOverlay(KmlGroundOverlay groundOverlay) {
        mGroundOverlays.put(groundOverlay, null);
    }

    /* package */ HashMap<KmlGroundOverlay, GroundOverlay> getGroundOverlayHashMap() {
        return mGroundOverlays;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKmlProperty(String propertyName) {
        return mContainerProperties.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKmlProperties() {
        return mContainerProperties.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKmlProperty(String keyValue) {
        return mContainerProperties.containsKey(keyValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNestedKmlContainers() {
        return mContainers.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<KmlContainerInterface> getNestedKmlContainers() {
        return mContainers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable getKmlProperties() {
        return mContainerProperties.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<KmlPlacemark> getKmlPlacemarks() {
        return mPlacemarks.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return mGroundOverlays.keySet();
    }
}
