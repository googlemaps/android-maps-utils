package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.geoJsonLayer.GeoJsonCollection;
import com.google.maps.android.geoJsonLayer.GeoJsonFeature;
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
        try {

            //TODO: Test for when geometry is a null value or true or false




            GeoJsonCollection geoJsonCollection = new GeoJsonCollection(getMap(), R.raw.geometrycollection_in_feature, getApplicationContext());
            geoJsonCollection.parseGeoJson();
            geoJsonCollection.addCollectionToMap();

            GeoJsonLineStringStyle line = new GeoJsonLineStringStyle();
            line.setColor(Color.CYAN);
            GeoJsonPolygonStyle poly = new GeoJsonPolygonStyle();
            poly.setFillColor(Color.MAGENTA);
            GeoJsonPointStyle point = new GeoJsonPointStyle();
            point.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            point.setRotation(60);




            for (GeoJsonFeature feature: geoJsonCollection.getFeatures()) {
                feature.setLineStringStyle(line);
                feature.setPointStyle(point);
                feature.setPolygonStyle(poly);
                ((GeoJsonPoint) feature.getGeometry()).
            }
            Log.i("MultiLineString", geoJsonCollection.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

