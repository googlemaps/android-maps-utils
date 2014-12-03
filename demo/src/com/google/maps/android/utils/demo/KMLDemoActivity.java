package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.Document;
import com.google.maps.android.kml.Style;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.HashMap;

public class KMLDemoActivity extends BaseDemoActivity {

    private XmlPullParser readItems() throws JSONException, XmlPullParserException {
        InputStream stream = getResources().openRawResource(R.raw.cta);
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    public void startDemo () {
        System.out.println("Hello");
        try {
            XmlPullParser parser = readItems();
            Document d = new Document(parser);
            d.setStyles();
            HashMap<String, Style> styles = d.getStyles();
            for (Style s : styles.values()) {
                System.out.println(s.getStyleID() + " " + s.getColorMode());
            }
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
        System.out.println("Hello");
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 3));
    }
}
