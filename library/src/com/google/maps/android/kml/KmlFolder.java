package com.google.maps.android.kml;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlFolder {

    private HashMap<String, String> mContainerProperties;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private HashMap<String, KmlStyle> mStyles;

    private ArrayList<KmlFolder> mContainers;

    public KmlFolder() {
        mContainerProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyles = new  HashMap<String, KmlStyle>();
        mContainers = new ArrayList<KmlFolder>();
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

    public void setPlacemarks(HashMap<KmlPlacemark, Object> placemarks) {
        mPlacemarks = placemarks;
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

    public ArrayList<KmlFolder> getChildren() {
        return mContainers;
    }

    public HashMap<String, String> getProperties() {
        return mContainerProperties;
    }

}
