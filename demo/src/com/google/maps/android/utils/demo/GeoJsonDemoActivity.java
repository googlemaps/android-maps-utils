package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;

import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class GeoJsonDemoActivity extends BaseDemoActivity {

    private final static String mLogTag = "GeoJsonDemo";

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo()  {
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
            GeoJsonPoint point = new GeoJsonPoint(new LatLng(-33.866670, 151.195826));
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("country", "Australia");
            properties.put("city", "Sydney");
            GeoJsonFeature feature = new GeoJsonFeature(point, "Google Sydney", properties, null);
            layer.addFeature(feature);

        } catch (JSONException e) {
            Log.e(mLogTag, "JSON Exception caught: " + e.toString());
        } catch (IOException e) {
            Log.e(mLogTag, "IO Exception caught: " + e.toString());
        }
    }
}

