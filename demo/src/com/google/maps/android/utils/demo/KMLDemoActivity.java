package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.Document;
import com.google.maps.android.kml.Placemark;
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
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }




    public void startDemo () {
        try {
            XmlPullParser parser;
            Document document = new Document();
            parser = readItems();
            document.setParser(parser);
            document.readKMLData();

            HashMap<String, Style> styles = document.getStyles();
            HashMap<String, Placemark> placemarks = document.getPlacemarks();

            for (Style s: styles.values()) {
                System.out.println(s.getStyleID());
            }

            for (Placemark p: placemarks.values()) {
                System.out.println(p.getName());
            }
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }
}
