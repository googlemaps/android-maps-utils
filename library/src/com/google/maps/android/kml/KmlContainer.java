package com.google.maps.android.kml;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by lavenderch on 1/16/15.
 */
public interface KmlContainer {

    public HashMap<KmlPlacemark, Object> getPlacemarks();

    public String getProperty(String propertyName);

    public Iterator getProperties();


}
