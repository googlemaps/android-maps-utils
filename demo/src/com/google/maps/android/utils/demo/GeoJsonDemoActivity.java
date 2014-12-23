package com.google.maps.android.utils.demo;

import com.google.maps.android.geoJsonImport.GeoJsonImport;

import android.util.Log;
import android.view.View;
import android.widget.Button;

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

            final GeoJsonImport featureCollectionTest = new GeoJsonImport(getMap(),
                    R.raw.feature_collection_geojson_demo, getApplicationContext());

            featureCollectionTest.addGeoJsonData();

            final Button add_button = (Button) findViewById(R.id.add);
            add_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.addGeoJsonData();

                }
            });

            final Button toggle_button = (Button) findViewById(R.id.toggle);
            toggle_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.toggleVisibility();

                }
            });

            final Button invert_button = (Button) findViewById(R.id.invert);
            invert_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.invertVisibility();

                }
            });

            final Button show_all_button = (Button) findViewById(R.id.show_all);
            show_all_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.showAllGeoJsonData();
                }
            });

            final Button hide_all_button = (Button) findViewById(R.id.hide_all);
            hide_all_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.hideAllGeoJsonData();
                }
            });

            final Button remove_button = (Button) findViewById(R.id.remove);
            remove_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    featureCollectionTest.removeGeoJsonData();
                }
            });



        } catch (Exception e) {
            Log.v("Exception", e.toString());
        }
    }
}

