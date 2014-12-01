package com.google.maps.android.utils.demo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.HashMap;

/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class KMLDemoActivity extends FragmentActivity {

    private GoogleMap mMap;

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
     Reads a KML file and retrieves data based on the tag and the content of the tag.
     Currently reads input from an input stream and stops when an end of document tag,
     </kml>,is reached. Currently method only supports <coordinate></coordinate>
     **********************************/

    public void setKML () {
        PolylineOptions options = new PolylineOptions();

        try {
            XmlPullParser parser = getKMLFile();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("coordinates")) {
                        options = getPlotPoints(parser);
                    }
                }
                eventType = parser.next();
                mMap.addPolyline(options);
                options = new PolylineOptions();
            }
        } catch (Exception e) {
            String error = "Unable to retrieve KML file. " +
                    "Make sure that the file is in the correct destination folder.";
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    /**********************************
        Returns an XML Parser from a given KML or XML data set and returns as an input stream.
        To change the file destination, change R.raw.<Your file here>
        Raw data is currently being fetched from demo/res/raw folder.
        To retrieve a file from a URL, uncomment and replace YOUR_URL_HERE with your own.
     **********************************/

    private XmlPullParser getKMLFile () {
        XmlPullParser parser = null;
        InputStream stream;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            stream = getResources().openRawResource(R.raw.cta);
            //stream = new URL("YOUR_URL_HERE").openStream();
            parser.setInput(stream, null);
        } catch (Exception e) {
            String error = "Unable to retrieve KML file. " +
                    "Make sure that the file is in the correct destination folder.";
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
        return parser;
    }

    /**********************************
        Receives a list of coordinates from KML data and returns a PolylineOption class.
        The method receives plain text coordinates from an input steam, then splits by line
        to get a coordinate set, then splits by comma to get latitude and longitude values.
     **********************************/

    private PolylineOptions getPlotPoints(XmlPullParser parser) {
        String[] lines;
        PolylineOptions options = new PolylineOptions();
        LatLng latLng;
        try {
            lines = parser.nextText().split("\n");
            for (String point: lines) {
                String[] coordinate = point.split(",");
                if (coordinate.length > 2) {
                    Double latDouble = Double.parseDouble(coordinate[1]);
                    Double lonDouble = Double.parseDouble(coordinate[0]);
                    if (lonDouble > 120 || lonDouble < -120 || latDouble > 90 || latDouble < 90) {
                        String error = "KML data provided is in the incorrect format. " +
                                "Expected: lon, lat. Given: lat, lon";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        break;
                    }
                    latLng = new LatLng(latDouble,lonDouble);
                    options.add(latLng).width(4).color(Color.BLACK);
                }
            }
        } catch (Exception e) {
            String error = "Unable to retrieve list of coordinates. " +
                    "Make sure that there is content between the coordinate tags.";
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
        return options;
    }



}
