package com.google.maps.android.utils.demo;

import com.google.maps.android.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJsonDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo()  {
        try {
            GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.basic, getApplicationContext());
            layer.addGeoJsonDataToLayer();
        } catch (Exception e ) {

        }
    }
}

