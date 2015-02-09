package com.google.maps.android.utils.demo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by lavenderc on 12/1/14.
 */
public class KMLDemoActivity extends FragmentActivity {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setUpMapIfNeeded();
    }

    /**********************************
        Sets up KML layer if map layer is already initialised.
     **********************************/
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setKML();
    }

    /**********************************
        Sets up a map if it is not initialised, usually on start up.
        If the map is already initialised, then we do nothing.
     **********************************/

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    /**********************************
        Returns an XML Parser from a given KML or XML data set.
        To change the file destination, change R.raw.<Your file here>
        Raw data is currently being fetched from demo/res/raw folder.
     **********************************/

    private XmlPullParser getKMLFile () {
        XmlPullParser parser = null;
        InputStream stream;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            stream = getResources().openRawResource(R.raw.cta);
            parser.setInput(stream, null);
        } catch (Exception e) {
            System.out.println("File not found!");
        }
        return parser;
    }

    /**********************************
     Receives a list of coordinates from KML data and turns them into a polyline option
     **********************************/

    private PolylineOptions getPlotPoints(XmlPullParser parser) {
        String[] lines;
        PolylineOptions options = new PolylineOptions();
        Double lonDouble, latDouble;
        Integer lon = 0, lat = 1;
        LatLng latLng;
        try {
            lines = parser.nextText().split("\n");
            for (String point: lines) {
                String[] coordinate = point.split(",");
                if (coordinate.length > 2) {
                    latDouble = Double.parseDouble(coordinate[lat]);
                    lonDouble = Double.parseDouble(coordinate[lon]);
                    latLng = new LatLng(latDouble,lonDouble);
                    options.add(latLng).width(4).color(Color.BLACK);
                }
            }
        } catch (Exception e) {
            System.out.println ("Coordinate list not found! Try again");
        }
        return options;
    }


    public void setKML () {

        PolylineOptions options = new PolylineOptions();
        String name;

        try {
            XmlPullParser parser = getKMLFile();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    name = parser.getName();
                    if (name.equals("coordinates")) {
                        options = getPlotPoints(parser);
                    }
                }
                eventType = parser.next();
                mMap.addPolyline(options);
                options = new PolylineOptions();
            }
        } catch (Exception e) {
            System.out.println("Nope");
        }
    }

}
