package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlLayer;

import java.io.InputStream;

public class KMLDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            InputStream stream = getResources().openRawResource(R.raw.sample_kml);
            KmlLayer kmlLayer = new KmlLayer(getMap(), stream);

        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }

}
