package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lavenderch on 12/22/14.
 */
public class PointProperties implements Geometry {

    private LatLng pointCoordinate;

    @Override
    public void parseGeometry(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG &&  parser.getName().equals("coordinates"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if ( parser.getName().equals("coordinates")) {
                    parser.next();
                    setGeometry(parser.getText());
                }
            }
            eventType =  parser.next();
        }
    }

    @Override
    public void setGeometry(String text) {

            String[] coordinate = text.split(",");

                createGeometry(convertToLatLng(coordinate));


    }

    public void createGeometry(Object geometry) {
        pointCoordinate = ((LatLng) geometry);
    }

    @Override
    public LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LONGITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LATITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

    @Override
    public Object getGeometry() {
       return pointCoordinate;
    }
}
