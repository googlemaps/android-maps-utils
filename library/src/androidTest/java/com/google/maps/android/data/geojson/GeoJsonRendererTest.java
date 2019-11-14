package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.*;

public class GeoJsonRendererTest {
    private GoogleMap mMap1;
    private Set<GeoJsonFeature> geoJsonFeaturesSet;
    private GeoJsonRenderer mRenderer;
    private GeoJsonLayer mLayer;
    private GeoJsonFeature mGeoJsonFeature;
    private Collection<Object> mValues;

    @Before
    public void setUp() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createFeatureCollection());
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            geoJsonFeatures.put(feature, null);
        }
        geoJsonFeaturesSet = geoJsonFeatures.keySet();
        mRenderer = new GeoJsonRenderer(mMap1, geoJsonFeatures);
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        GeoJsonLineString geoJsonLineString =
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        mGeoJsonFeature = new GeoJsonFeature(geoJsonLineString, null, null, null);
        mValues = geoJsonFeatures.values();
    }

    @Test
    public void testGetMap() {
        assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testSetMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mMap1 = mLayer.getMap();
        mRenderer.setMap(mMap1);
        assertEquals(mMap1, mRenderer.getMap());
        mRenderer.setMap(null);
        assertNull(mRenderer.getMap());
    }

    @Test
    public void testGetFeatures() {
        assertEquals(geoJsonFeaturesSet, mRenderer.getFeatures());
    }

    @Test
    public void testAddFeature() {
        mRenderer.addFeature(mGeoJsonFeature);
        assertTrue(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    @Test
    public void testGetValues() {
        assertEquals(mValues.size(), mRenderer.getValues().size());
    }

    @Test
    public void testRemoveLayerFromMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mRenderer.removeLayerFromMap();
        assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testRemoveFeature() {
        mRenderer.addFeature(mGeoJsonFeature);
        mRenderer.removeFeature(mGeoJsonFeature);
        assertFalse(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"MultiPoint\", \"coordinates\": [[102.0,"
                        + " 0.5], [100, 0.5]]},\n"
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
                        + "             [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0],"
                        + " [102.0, 2.0]]],\n"
                        + "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0,"
                        + " 0.0]],\n"
                        + "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2,"
                        + " 0.2]]],\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"title\": \"Test MultiPolygon\"}\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }");
    }
}
