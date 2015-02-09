package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class KmlDemoActivity extends BaseDemoActivity {
    GoogleMap mMap;

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            Log.i("Demo", "Start");
            mMap = getMap();
            KmlLayer layer = new KmlLayer(getMap(), R.raw.egypt, getApplicationContext());
                for (KmlContainer container : layer.getNestedContainers()) {
                    for (Object property : ) {
                        container.get

                    }
                }
            Log.i("Demo", "End");
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }


}
