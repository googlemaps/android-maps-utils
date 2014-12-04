package com.google.maps.android.kml;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by lavenderc on 12/3/14.
 */
public class Placemark {

    private String name;
    private String visibility;
    private String description;
    private String styleURL;
    private String address;
    private String phoneNumber;
    private String region;

    private int POLYGON_TYPE = 0;
    private int LINESTRING_TYPE = 1;
    private int POINT_TYPE = 2;

    private int INNER_BOUNDARY = 0;
    private int OUTER_BOUNDARY = 1;

    public Placemark() {
        name = null;
        visibility = null;
        description = null;
        styleURL = null;
        phoneNumber = null;
        address = null;
    }

    public void setName(String string) {
        name = string;
    }
    public void setVisibility(String string) {
        visibility = string;
    }
    public void setDescription(String string) {
        description = string;
    }
    public void setStyleURL(String string) {
        styleURL = string;
    }
    public void setPhoneNumber(String string) {
        phoneNumber = string;
    }
    public void setAddress(String string) {
        address = string;
    }
    public void setRegion(String string) {
        region = string;
    }

    public String getName() {
        return name;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getDescription() {
        return description;
    }

    public String getStyleURL() {
        return styleURL;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setPolygon(String coordinates, int type) {
        Coordinate coordinate = new Coordinate();
        coordinate.setType(POLYGON_TYPE);
        coordinate.setBoundary(type);
        coordinate.setCoordinateList(coordinates);
    }
    public void setLineString(String coordinates) {
        Coordinate coordinate = new Coordinate();
        coordinate.setType(LINESTRING_TYPE);
        coordinate.setCoordinateList(coordinates);
    }
    public void setPoint(String coordinates) {
        Coordinate coordinate = new Coordinate();
        coordinate.setType(LINESTRING_TYPE);
        coordinate.setCoordinateList(coordinates);
    }

}
