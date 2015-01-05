package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.KmlLayer;

public class KmlDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {

        try {
            KmlLayer kmlLayer = new KmlLayer(getMap(), R.raw.germany, getApplicationContext());
            kmlLayer.setKmlData();

        } catch (Exception e) {
            System.out.println("Unable to find file in res/raw, please try again!");
        }
    }

}
