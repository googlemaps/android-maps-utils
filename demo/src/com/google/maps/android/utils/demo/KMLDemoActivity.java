package com.google.maps.android.utils.demo;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

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

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }


    public void startDemo () {

        TextView t = (TextView)findViewById(R.id.attribution);
       t.setMovementMethod(new ScrollingMovementMethod());

        try {
            XmlPullParser parser;
            Document document = new Document();
            parser = readItems();
            document.setParser(parser);
            document.readKMLData();

            HashMap<String, Style> styles = document.getStyles();
            HashMap<String, Placemark> placemarks = document.getPlacemarks();


            t.append(Html.fromHtml("<b>Styles</b><br>"));

            for (Style s: styles.values()) {
                String text = ("ID: " + s.getStyleID() + ", COLOR: " + s.getLineColor() + "\n");
                t.append(text);
            }

            for (Placemark p: placemarks.values()) {
                String text = ("Placemark Name: " + p.getName() + "\n");
                t.append(text);
            }
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }
}
