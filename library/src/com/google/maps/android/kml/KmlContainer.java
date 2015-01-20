package com.google.maps.android.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlContainer implements KmlContainerInterface {

    private HashMap<String, String> mContainerProperties;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private HashMap<String, KmlStyle> mStyles;

    private ArrayList<KmlContainerInterface> mContainers;

    private HashMap<String, String> mStyleMap;

    public KmlContainer() {
        mContainerProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyles = new  HashMap<String, KmlStyle>();
        mStyleMap = new HashMap<String, String>();
        mContainers = new ArrayList<KmlContainerInterface>();
    }

    /**
     * Takes an ArrayList of styles and assings these folders with the styles
     * @param styles
     */
    public void setStyles( HashMap<String, KmlStyle> styles) {
        mStyles = styles;
    }

    /**
     * Gets a style based on an ID
     * @param styleID
     * @return
     */
    public KmlStyle getStyle(String styleID) {
        return mStyles.get(styleID);
    }

    /**
     * @return HashMap of styles, with key values representing style name (ie, color) and
     * value representing style value (ie #FFFFFF)
     */
    public  HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * @param placemarks Placemark for the container to contain
     * @param object    Corresponding GoogleMap map object of the placemark (if it has been added
     *                  to the map)
     */
    public void setPlacemark(KmlPlacemark placemarks, Object object) {
       mPlacemarks.put(placemarks, object);
    }

    /**
     * Add a nested container
     * @param container Container to nest within the current instance of the container
     */
    public void addChildContainer(KmlContainer container) {
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
     * @param propertyName  Name of the property, ie "name"
     * @param propertyValue Value of the property, ie "Arizona"
     */
    public void setProperty(String propertyName, String propertyValue) {
        mContainerProperties.put(propertyName, propertyValue);
    }

    /**
     * Sets a style to be contained by the container
     * @param styleId   Name or ID of the style
     * @param style     KmlStyle object
     */
    public void setStyle(String styleId, KmlStyle style) {
        mStyles.put(styleId, style);
    }


    /**
     * @return A map of strings representing a style map, null if no style maps exist
     */
    public HashMap<String, String> getStyleMap() {
        return mStyleMap;
    }

    /**
     * @param styleMap Adds a map of strings representing a style map
     */
    public void setStyleMap(HashMap<String, String> styleMap) {
        mStyleMap.putAll(styleMap);
    }

    /**
     * @param propertyName Name of the property, ie "name"
     * @return  Value of the property, ie "Arizona"
     */
    public String getKmlProperty(String propertyName) {
        return mContainerProperties.get(propertyName);
    }

    /**
     * @return True if container has properties, false otherwise
     */
    public boolean hasKmlProperties() {
        return mContainerProperties.size() > 0;
    }


    public boolean hasKmlProperty(String keyValue) {
        return mContainerProperties.containsKey(keyValue);
    }

    /**
     * @return True is container has nested containers, false otherwise
     */
    public boolean hasNestedKmlContainers() {
        return mContainers.size() > 0;
    }

    /**
     *@return Iterator of containers within this instance of a container, null if it does not exist
     */
    public Iterator getNestedKmlContainers() { return mContainers.iterator(); }

    /**
     * @return Iterator of Kml properties, null if it does not exist
     */
    public Iterator<Map.Entry<String, String>> getKmlProperties() {
        return mContainerProperties.entrySet().iterator();
    }

    /**
     * @return KML placemarks, null if they do not exist
     */
    public Iterator getKmlPlacemarks()  {
        return mPlacemarks.entrySet().iterator();
    }

    /**
     * @return True is KML Placemarks exist
     */
    public boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

}
