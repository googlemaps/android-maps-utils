package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.KmlLayer;

import android.util.Log;

import java.util.Iterator;

public class KmlDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            Log.i("Demo", "Start");
            KmlLayer kmlLayer = new KmlLayer(getMap(), R.raw.germany, getApplicationContext());
            kmlLayer.addKmlData();

            Iterator iterator = kmlLayer.getPlacemarks();








            Log.i("Demo", "End");


        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }
}
