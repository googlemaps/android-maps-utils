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
            XmlPullParser parser = readItems();
            Document document = new Document(parser);
            document.readKMLData();

        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }
}
