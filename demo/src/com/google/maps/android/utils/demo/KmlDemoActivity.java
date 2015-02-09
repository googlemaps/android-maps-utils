package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class KmlDemoActivity extends BaseDemoActivity {
    GoogleMap mMap;

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            Log.i("Demo", "Start");
            mMap = getMap();
            new DownloadKmlFile("http://www.touroflimassol.com/TourOfLimassol.kml").execute();
            Log.i("Demo", "End");
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }

    private class DownloadKmlFile extends AsyncTask<String, Void, byte[]> {
        private final String mUrl;

        public DownloadKmlFile(String url) {
            mUrl = url;
        }

        @Override
        protected byte[] doInBackground(String... params) {

            try {
                InputStream is =  new URL(mUrl).openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();

                return buffer.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] byteArr) {
            try {
                KmlLayer layer = new KmlLayer(mMap, new ByteArrayInputStream(byteArr));
                layer.addLayer();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
