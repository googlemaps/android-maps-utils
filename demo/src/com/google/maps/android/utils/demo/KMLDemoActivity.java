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
        InputStream stream = getResources().openRawResource(R.raw.linear_ring_example);
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
            styles = document.getStyles();
            placemarks = document.getPlacemarks();
            draw(styles, placemarks);
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }

    /**
     * TODO:
     * Convert data into information on the map, at the moment it is just outputting text
     * to a textview, just to see if it is working.
     */
    public void draw(ArrayList<Style> styles, ArrayList<Placemark> placemarks) {
        TextView t = (TextView)findViewById(R.id.attribution);
        t.setMovementMethod(new ScrollingMovementMethod());
        t.append(Html.fromHtml("<b>Styles</b><br>"));
        for (Style s: styles) {
            String text = ("ID: " + s.getValues("styleID") + ", COLOR: " + s.getValues("color") + "\n");
            t.append(text);
        }
        for (Placemark p: placemarks) {
            System.out.println(p.getValues("name"));
            t.append(Html.fromHtml("<b>PLACEMARK: " +p.getValues("name") + " </b><br>"));
            for (Coordinate c: p.getLine()) {
                t.append(Html.fromHtml("<b>COORDINATES: " + c.getType() + " " + c.getBoundary() + "</b><br>"));
                for (LatLng l: c.getCoordinateList()) {
                    String text = l.latitude + " " + l.longitude + "\n";
                    t.append(text);
                }
            }
        }
    }
}
