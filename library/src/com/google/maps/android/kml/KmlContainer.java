package com.google.maps.android.kml;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by lavenderch on 1/16/15.
 */
public interface KmlContainer {

    public Iterator getKmlPlacemarks();

    public String getKmlProperty(String propertyName);

    public Iterator getKmlProperties();

    public boolean hasNestedKmlFolders();

    public Iterator getNestedKmlFolders();

}
