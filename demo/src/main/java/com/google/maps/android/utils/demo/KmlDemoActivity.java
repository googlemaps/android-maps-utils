package com.google.maps.android.utils.demo;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class KmlDemoActivity extends BaseDemoActivity {

    private GoogleMap mMap;
    private boolean mIsRestore;

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo (boolean isRestore) {
        mIsRestore = isRestore;
        try {
            mMap = getMap();
            //retrieveFileFromResource();
            retrieveFileFromUrl();
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }

    private void retrieveFileFromResource() {
        new LoadLocalKmlFile(R.raw.campus).execute();
    }

    private void retrieveFileFromUrl() {
        new DownloadKmlFile(getString(R.string.kml_url)).execute();
    }

    private void moveCameraToKml(KmlLayer kmlLayer) {
        if (mIsRestore) return;
        try {
            //Retrieve the first container in the KML layer
            KmlContainer container = kmlLayer.getContainers().iterator().next();
            //Retrieve a nested container within the first container
            container = container.getContainers().iterator().next();
            //Retrieve the first placemark in the nested container
            KmlPlacemark placemark = container.getPlacemarks().iterator().next();
            //Retrieve a polygon object in a placemark
            KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
            //Create LatLngBounds of the outer coordinates of the polygon
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
                builder.include(latLng);
            }

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 1));
        } catch (Exception e) {
            // may fail depending on the KML being shown
            e.printStackTrace();
        }
    }

    private class LoadLocalKmlFile extends AsyncTask<String, Void, KmlLayer> {
        private final int mResourceId;

        LoadLocalKmlFile(int resourceId) {
            mResourceId = resourceId;
        }

        @Override
        protected KmlLayer doInBackground(String... strings) {
            try {
                return new KmlLayer(mMap, mResourceId, KmlDemoActivity.this);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(KmlLayer kmlLayer) {
            addKmlToMap(kmlLayer);
        }
    }

    private class DownloadKmlFile extends AsyncTask<String, Void, KmlLayer> {
        private final String mUrl;

        DownloadKmlFile(String url) {
            mUrl = url;
        }

        protected KmlLayer doInBackground(String... params) {
            try {
                InputStream is =  new URL(mUrl).openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                try {
                    return new KmlLayer(mMap, new ByteArrayInputStream(buffer.toByteArray()),
                            KmlDemoActivity.this);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(KmlLayer kmlLayer) {
            addKmlToMap(kmlLayer);
        }
    }

    private void addKmlToMap(KmlLayer kmlLayer) {
        if (kmlLayer != null) {
            kmlLayer.addLayerToMap();
            kmlLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    Toast.makeText(KmlDemoActivity.this,
                            "Feature clicked: " + feature.getId(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            moveCameraToKml(kmlLayer);
        }
    }
}
