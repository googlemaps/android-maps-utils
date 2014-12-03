package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

public class KMLDemoActivity extends BaseDemoActivity {

    private XmlPullParser readItems() throws JSONException, XmlPullParserException {
        InputStream stream = getResources().openRawResource(R.raw.cta);
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    protected void startDemo() {
            System.out.println("Hello");
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 3));
    }
}
