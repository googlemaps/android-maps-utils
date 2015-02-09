package com.google.maps.android.utils.demo;

import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import android.graphics.Color;
import android.util.Log;

public class GeoJsonDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo()  {
        // TODO: make a demo
        try {
            GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.google, getApplicationContext());
            layer.addLayer();
            for (GeoJsonFeature feature : layer.getFeatures()) {
                if (feature.hasProperty("color")) {
                    GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
                    polygonStyle.setFillColor(Color.parseColor(feature.getProperty("color")));
                    feature.setPolygonStyle(polygonStyle);
                }
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
    }
}

