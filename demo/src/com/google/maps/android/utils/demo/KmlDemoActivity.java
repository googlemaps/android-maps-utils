package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

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
            mMap = getMap();
            //postcode.kml working

            //new DownloadKmlFile("http://gmaps-samples.googlecode.com/svn/trunk/ggeoxml/cta.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/placemark.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/kmz/balloon/balloon-image-rel.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Document/doc-with-id.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Document/doc-without-id.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/BalloonStyle/displayMode.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/BalloonStyle/simpleBalloonStyles.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/ExtendedData/data-golf.kml").execute();
            new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/GroundOverlay/etna.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/MultiGeometry/multi-linestrings.kml").execute();


            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/MultiGeometry/multi-rollover.kml").execute();


            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/MultiGeometry/polygon-point.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/LineString/straight.kml").execute();

            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/LineString/styled.kml").execute();


            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/placemark.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/simple_placemark.kml").execute();
            //new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Polygon/polyInnerBoundaries.kml").execute();



            /*

            No images:
            new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/styled_placemark2.kml").execute();
            new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/styled_placemark.kml").execute();

            Didn't read the reference:
            new DownloadKmlFile("http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/LinearRing/linear-ring.kml").execute();



             */

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
                KmlLayer layer = new KmlLayer(mMap, new ByteArrayInputStream(byteArr),
                        getApplicationContext());
                layer.addLayerToMap();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
