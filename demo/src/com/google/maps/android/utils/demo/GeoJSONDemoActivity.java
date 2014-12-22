package com.google.maps.android.utils.demo;

import com.google.maps.android.importGeoJson.ImportGeoJson;

import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJSONDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo() {



//        ImportGeoJson url_collection_test = new ImportGeoJson(getMap(),
//                "http://thebeatsbrief.com/M_Features_21_Aug.geojson_demo");
//        ImportGeoJson resource_collection_test = new ImportGeoJson(getMap(),
//                R.raw.google_letters_geojson, getApplicationContext());
//        ImportGeoJson resource_feature_test = new ImportGeoJson(getMap(),
//                R.raw.single_feature_geojson, getApplicationContext());

//        This has 87 points
//        ImportGeoJson spain_collection_test = new ImportGeoJson(getMap(),
//                R.raw.markers_in_spain_geojson, getApplicationContext());
        try {
//           ImportGeoJson feature_collection_test = new ImportGeoJson(getMap(),
//                    R.raw.big_very_big, getApplicationContext());
            final ImportGeoJson feature_collection_test = new ImportGeoJson(getMap(),
                    R.raw.feature_collection_geojson_demo, getApplicationContext());
//
            feature_collection_test.addGeoJsonData();
//            feature_collection_test.toggleGeoJsonData();
//            feature_collection_test.invertVisibility();
//
//            ImportGeoJson feature_collection_test_1 = new ImportGeoJson(getMap(),
//                    R.raw.google_letters_geojson, getApplicationContext());
//
//            feature_collection_test_1.addGeoJsonData();
            final Button toggle_button = (Button) findViewById(R.id.toggle);
            toggle_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    feature_collection_test.toggleGeoJsonData();

                }
            });

            final Button invert_button = (Button) findViewById(R.id.invert);
            invert_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    feature_collection_test.invertVisibility();

                }
            });

            final Button show_all_button = (Button) findViewById(R.id.show_all);
            show_all_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    feature_collection_test.showGeoJsonData();
                }
            });

            final Button hide_all_button = (Button) findViewById(R.id.hide_all);
            hide_all_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    feature_collection_test.hideGeoJsonData();
                }
            });

            final Button remove_button = (Button) findViewById(R.id.remove);
            remove_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    feature_collection_test.removeGeoJsonData();
                }
            });



        } catch (Exception e) {
            Log.v("Exception", e.toString());
        }
    }
}

