package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.geoJsonLayer.GeoJsonFeature;
import com.google.maps.android.geoJsonLayer.GeoJsonLayer;
import com.google.maps.android.geoJsonLayer.GeoJsonLineStringStyle;
import com.google.maps.android.geoJsonLayer.GeoJsonPoint;
import com.google.maps.android.geoJsonLayer.GeoJsonPointStyle;
import com.google.maps.android.geoJsonLayer.GeoJsonPolygon;
import com.google.maps.android.geoJsonLayer.GeoJsonPolygonStyle;

import org.json.JSONException;

import android.graphics.Color;
import android.util.Log;

import java.io.IOException;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJsonDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo() {

    }
}

