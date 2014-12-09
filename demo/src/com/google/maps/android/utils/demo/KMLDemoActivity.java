package com.google.maps.android.utils.demo;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.Coordinate;
import com.google.maps.android.kml.Document;
import com.google.maps.android.kml.Placemark;
import com.google.maps.android.kml.Style;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;

public class KMLDemoActivity extends BaseDemoActivity {

    private ArrayList<Style> styles;
    private ArrayList<Placemark> placemarks;

    /**
     * TODO:
     * - Read data using that async thing
     */
    private XmlPullParser readItems() throws JSONException, XmlPullParserException {
        InputStream stream = getResources().openRawResource(R.raw.cta);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }


    public void startDemo () {
        try {
            XmlPullParser parser = readItems();
            Document document = new Document(parser);
            document.readKMLData();
            document.printKMLData();
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }


}
