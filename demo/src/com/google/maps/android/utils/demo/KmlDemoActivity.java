package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.KmlLayer;

import android.util.Log;

public class KmlDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            Log.i("Demo", "Start");
            KmlLayer kmlLayer = new KmlLayer(getMap(), R.raw.japan_earthquake_tsunami, getApplicationContext());
            kmlLayer.addKmlData();
            Log.i("Demo", "End");

        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }
}
