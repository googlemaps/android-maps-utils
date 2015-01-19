package com.google.maps.android.kml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lavenderch on 1/16/15.
 */
public interface KmlContainerInterface {

    public Iterator getKmlPlacemarks();

    public boolean hasKmlPlacemarks();

    public String getKmlProperty(String propertyName);

    public boolean hasKmlProperties();

    public boolean hasKmlProperty(String keyValue);

    public Iterator<Map.Entry<String, String>> getKmlProperties();

    public boolean hasNestedKmlContainers();

    public Iterator getNestedKmlContainers();

}
