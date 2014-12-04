package com.google.maps.android.utils.demo;

import com.google.maps.android.importGeoJson.ImportGeoJson;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJSONDemoActivity extends BaseDemoActivity {

    @Override
    protected void startDemo() {
        ImportGeoJson test = new ImportGeoJson(getMap(),
                "http://thebeatsbrief.com/M_Features_21_Aug.geojson");
        ImportGeoJson test_again = new ImportGeoJson(getMap(),
                R.raw.google_letters_geojson, getApplicationContext());
    }
}