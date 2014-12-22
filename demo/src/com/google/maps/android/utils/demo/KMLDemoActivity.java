package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.ImportKML;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

public class KMLDemoActivity extends BaseDemoActivity {

    /**
     * TODO:
     * - Read data using that async thing
     */
    private XmlPullParser readItems() throws JSONException, XmlPullParserException {
        InputStream stream = getResources().openRawResource(R.raw.sample_kml);
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
            ImportKML importKML = new ImportKML(getMap(), parser);
            importKML.importKML();
            importKML.addKMLData();
        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }

}
