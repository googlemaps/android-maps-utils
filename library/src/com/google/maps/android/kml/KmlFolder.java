package com.google.maps.android.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlFolder implements KmlContainer {

    private HashMap<String, String> mContainerProperties;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private HashMap<String, KmlStyle> mStyles;

    private ArrayList<KmlContainer> mContainers;

    public KmlFolder() {
        mContainerProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyles = new  HashMap<String, KmlStyle>();
        mContainers = new ArrayList<KmlContainer>();
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

    public  HashMap<String, KmlStyle> getStyles() {
        return mStyles;
    }

    public void setPlacemark(KmlPlacemark placemarks, Object object) {
       mPlacemarks.put(placemarks, object);
    }

    public void addChildContainer(KmlFolder container) {
        mContainers.add(container);
    }

    public HashMap<KmlPlacemark, Object> getPlacemarks() {
        return mPlacemarks;
    }

    public void setProperty(String propertyName, String propertyValue) {
        mContainerProperties.put(propertyName, propertyValue);
    }

    public void setStyle(String styleId, KmlStyle style) {
        mStyles.put(styleId, style);
    }

    public String getProperty(String propertyName) {
        return mContainerProperties.get(propertyName);
    }

    public boolean hasChildren() {
        return mContainers.size() > 0;
    }

    public ArrayList<KmlContainer> getChildren() {
        return mContainers;
    }

    public Iterator getProperties() {
        return mContainerProperties.entrySet().iterator();
    }

}
