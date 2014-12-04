package com.google.maps.android.utils.demo;

import com.google.maps.android.importGeoJson.ImportGeoJson;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJSONDemoActivity extends BaseDemoActivity {

    @Override
    protected void startDemo() {
        ImportGeoJson url_collection_test = new ImportGeoJson(getMap(),
                "http://thebeatsbrief.com/M_Features_21_Aug.geojson");
//        ImportGeoJson resource_collection_test = new ImportGeoJson(getMap(),
//                R.raw.google_letters_geojson, getApplicationContext());
        ImportGeoJson resource_feature_test = new ImportGeoJson(getMap(),
                R.raw.single_feature_geojson, getApplicationContext());
    }
}