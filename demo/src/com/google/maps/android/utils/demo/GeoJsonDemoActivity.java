package com.google.maps.android.utils.demo;

import com.google.maps.android.geoJsonLayer.Collection;

import org.json.JSONException;

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
        try {
            Collection collection = new Collection(getMap(), R.raw.markers_in_spain_geojson, getApplicationContext());
            collection.parseGeoJson();
            Log.i("Features", collection.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

