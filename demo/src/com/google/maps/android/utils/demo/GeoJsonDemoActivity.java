package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.geoJsonLayer.Collection;
import com.google.maps.android.geoJsonLayer.Feature;
import com.google.maps.android.geoJsonLayer.LineStringStyle;
import com.google.maps.android.geoJsonLayer.PointStyle;
import com.google.maps.android.geoJsonLayer.PolygonStyle;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

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

            //TODO: Test for when geometry is a null value or true or false



            Collection collection = new Collection(getMap(), R.raw.overlapping, getApplicationContext());
            collection.parseGeoJson();
            collection.addCollectionToMap();

            Set<Feature> features = collection.getFeatures();
            for (Feature feature: features) {
                if (feature.getId().equals("google")) {
                    //Do something
                }
            }






            Log.i("MultiLineString", collection.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

