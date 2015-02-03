package com.google.maps.android.utils.demo;

import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONObject;

import android.graphics.Color;

/**
 * Created by juliawong on 12/1/14.
 */
public class GeoJsonDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.geojson_demo;
    }

    @Override
    protected void startDemo()  {
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
            layer.addDataToLayer();
            layer.removeFeature(layer.getFeatureById("point"));
            layer.addDataToLayer();
            GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
            polygonStyle.setFillColor(Color.BLUE);
            layer.setDefaultPolygonStyle(polygonStyle);
            layer.removeLayer();
            layer.addDataToLayer();

            for (GeoJsonFeature feature : layer.getFeatures()) {
                feature.getGeometry().getType();

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

