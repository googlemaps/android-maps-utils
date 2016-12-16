package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class GeoJsonRendererTest extends TestCase {

    GoogleMap mMap1;
    Set<GeoJsonFeature> geoJsonFeaturesSet;
    GeoJsonRenderer mRenderer;
    GeoJsonLayer mLayer;
    GeoJsonFeature mGeoJsonFeature;
    Collection<Object> mValues;

    public void setUp() throws Exception {
        super.setUp();
        GeoJsonParser parser = new GeoJsonParser(createFeatureCollection());
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<GeoJsonFeature, Object>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            geoJsonFeatures.put(feature, null);
        }
        geoJsonFeaturesSet = geoJsonFeatures.keySet();
        mRenderer = new GeoJsonRenderer(mMap1, geoJsonFeatures);
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        GeoJsonLineString geoJsonLineString = new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        mGeoJsonFeature = new GeoJsonFeature(geoJsonLineString, null, null, null);
        mValues = geoJsonFeatures.values();
    }

    public void tearDown() throws Exception {

    }

    public void testGetMap() throws Exception {
        assertEquals(mMap1, mRenderer.getMap());
    }

    public void testSetMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mMap1 = mLayer.getMap();
        mRenderer.setMap(mMap1);
        assertEquals(mMap1, mRenderer.getMap());
        mRenderer.setMap(null);
        assertEquals(null, mRenderer.getMap());
    }

    public void testGetFeatures() throws Exception {
        assertEquals(geoJsonFeaturesSet, mRenderer.getFeatures());
    }

    public void testAddFeature() throws Exception {
        mRenderer.addFeature(mGeoJsonFeature);
        assertTrue(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    public void testGetValues() {
        assertEquals(mValues.size(), mRenderer.getValues().size());
    }

    public void testRemoveLayerFromMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mRenderer.removeLayerFromMap();
        assertEquals(mMap1, mRenderer.getMap());
    }

    public void testRemoveFeature() throws Exception {
        mRenderer.addFeature(mGeoJsonFeature);
        mRenderer.removeFeature(mGeoJsonFeature);
        assertFalse(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"MultiPoint\", \"coordinates\": [[102.0, 0.5], [100, 0.5]]},\n"
                        + "        \"properties\": {\"title\": \"Test MultiPoint\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"MultiLineString\",\n"
                        + "          \"coordinates\": [\n"
                        + "            [[100, 0],[101, 1]], [[102, 2], [103, 3]]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"title\": \"Test MultiLineString\"\n"
                        + "          }\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "         \"geometry\": {\n"
                        + "           \"type\": \"MultiPolygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],\n" +
                        "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],\n" +
                        "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]],\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"title\": \"Test MultiPolygon\"}\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"
        );
    }
}