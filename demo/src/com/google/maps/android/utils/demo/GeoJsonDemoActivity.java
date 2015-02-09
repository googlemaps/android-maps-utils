package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.json.JSONObject;

public class GeoJsonDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo()  {
        // TODO: make a demo
        try {
            JSONObject geoJson = new JSONObject(
                    "{ \"type\": \"FeatureCollection\",\n"
                            + "\"bbox\": [-150.0, -80.0, 150.0, 80.0],"
                            + "    \"features\": [\n"
                            + "      { \"type\": \"Feature\",\n"
                            + "        \"id\": \"point\", \n"
                            + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                            + "        \"properties\": {\"prop0\": \"value0\"}\n"
                            + "        },\n"
                            + "      { \"type\": \"Feature\",\n"
                            + "        \"geometry\": {\n"
                            + "          \"type\": \"LineString\",\n"
                            + "          \"coordinates\": [\n"
                            + "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                            + "            ]\n"
                            + "          },\n"
                            + "        \"properties\": {\n"
                            + "          \"prop0\": \"value0\",\n"
                            + "          \"prop1\": 0.0\n"
                            + "          }\n"
                            + "        },\n"
                            + "      { \"type\": \"Feature\",\n"
                            + "         \"geometry\": {\n"
                            + "           \"type\": \"Polygon\",\n"
                            + "           \"coordinates\": [\n"
                            + "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                            + "               [100.0, 1.0], [100.0, 0.0] ]\n"
                            + "             ]\n"
                            + "         },\n"
                            + "         \"properties\": {\n"
                            + "           \"prop0\": \"value0\",\n"
                            + "           \"prop1\": {\"this\": \"that\"}\n"
                            + "           }\n"
                            + "         }\n"
                            + "       ]\n"
                            + "     }"
            );
            GeoJsonLayer layer = new GeoJsonLayer(getMap(), geoJson);
            layer.addLayer();
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            layer.setDefaultPointStyle(pointStyle);
            layer.addFeature(
                    new GeoJsonFeature(new GeoJsonPoint(new LatLng(0, 0)), null, null, null));
            layer.addFeature(
                    new GeoJsonFeature(new GeoJsonPoint(new LatLng(10, 10)), null, null, null));
            layer.clearLayer();
            pointStyle = new GeoJsonPointStyle();
            pointStyle.setTitle("BANANANA!");
            layer.setDefaultPointStyle(pointStyle);
            layer.addLayer();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

