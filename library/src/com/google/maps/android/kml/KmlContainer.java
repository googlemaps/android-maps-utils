package com.google.maps.android.kml;

import com.google.android.gms.maps.model.GroundOverlay;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a KML Document or Folder.
 */
public class KmlContainer {

    private final HashMap<String, String> mProperties;

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    private final ArrayList<KmlContainer> mContainers;

    private final HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private final HashMap<String, String> mStyleMap;

    private HashMap<String, KmlStyle> mStyles;

    public KmlContainer() {
        mProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyles = new HashMap<String, KmlStyle>();
        mStyleMap = new HashMap<String, String>();
        mContainers = new ArrayList<KmlContainer>();
        mGroundOverlays = new HashMap<KmlGroundOverlay, GroundOverlay>();
    }

    /*package*/ ArrayList<KmlContainer> getNestedContainers() {
        return mContainers;
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
     * @param object     Corresponding GoogleMap map object of the basic_placemark (if it has been
     *                   added
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
        mProperties.put(propertyName, propertyValue);
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
     * Gets the value of a property based on the given key
     *
     * @param propertyName property key to find
     * @return value of property found, null if key doesn't exist
     */
    public String getKmlProperty(String propertyName) {
        return mProperties.get(propertyName);
    }

    /**
     * Gets whether the container has any properties
     *
     * @return true if there are properties, false otherwise
     */
    public boolean hasKmlProperties() {
        return mProperties.size() > 0;
    }

    /**
     * Gets whether the given key exists in the properties
     *
     * @param keyValue property key to find
     * @return true if key was found, false otherwise
     */
    public boolean hasKmlProperty(String keyValue) {
        return mProperties.containsKey(keyValue);
    }

    /**
     * Gets whether the container has containers
     *
     * @return true if there are containers, false otherwise
     */
    public boolean hasNestedKmlContainers() {
        return mContainers.size() > 0;
    }

    /**
     * Gets an iterable of nested KmlContainers
     *
     * @return iterable of KmlContainers
     */
    public Iterable<KmlContainer> getNestedKmlContainers() {
        return mContainers;
    }

    /**
     * Gets an iterable of the properties hashmap entries
     *
     * @return iterable of the properties hashmap entries
     */
    public Iterable getKmlProperties() {
        return mProperties.entrySet();
    }

    /**
     * Gets an iterable of KmlPlacemarks
     *
     * @return iterable of KmlPlacemarks
     */
    public Iterable<KmlPlacemark> getKmlPlacemarks() {
        return mPlacemarks.keySet();
    }

    /**
     * Gets whether the container has any placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return mGroundOverlays.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Container").append("{");
        sb.append("\n properties=").append(mProperties);
        sb.append(",\n placemarks=").append(mPlacemarks);
        sb.append(",\n containers=").append(mContainers);
        sb.append(",\n ground overlays=").append(mGroundOverlays);
        sb.append(",\n style maps=").append(mStyleMap);
        sb.append(",\n styles=").append(mStyles);
        sb.append("\n}\n");
        return sb.toString();
    }
}
