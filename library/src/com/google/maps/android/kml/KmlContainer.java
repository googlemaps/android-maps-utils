package com.google.maps.android.kml;

import java.util.HashMap;

/**
 * Created by lavenderch on 1/14/15.
 */
public class KmlContainer {

    private HashMap<String, String> mContainerProperties;

    private HashMap<KMLFeature, Object> mPlacemarks;

    private HashMap<String, KmlStyle> mStyles;

    public KmlContainer() {
        mContainerProperties = new HashMap<String, String>();
        mPlacemarks = new HashMap<KMLFeature, Object>();
        mStyles = new  HashMap<String, KmlStyle>();
    }

    public void setStyles( HashMap<String, KmlStyle> styles) {
        mStyles = styles;
    }

    public KmlStyle getStyle(String styleID) {
        return mStyles.get(styleID);
    }

    public  HashMap<String, KmlStyle>  getStyles() {
        return mStyles;
    }

    public void addPlacemarks(KMLFeature placemarks, Object object) {
       mPlacemarks.put(placemarks, object);
    }

    public HashMap<KMLFeature, Object> getPlacemarks() {
        return mPlacemarks;
    }

    public void setProperty(String propertyName, String propertyValue) {
        mContainerProperties.put(propertyName, propertyValue);
    }

    public String getProperty(String propertyName) {
        return mContainerProperties.get(propertyName);
    }

    public HashMap<String, String> getProperties() {
        return mContainerProperties;
    }






}
