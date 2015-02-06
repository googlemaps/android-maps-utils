package com.google.maps.android.utils.demo;

import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class KmlDemoActivity extends BaseDemoActivity {

    protected int getLayoutId() {
        return R.layout.kml_demo;
    }

    public void startDemo () {
        try {
            Log.i("Demo", "Start");
            KmlLayer kmlLayer = new KmlLayer(getMap(), R.raw.egypt, getApplicationContext());
            kmlLayer.addDataToLayer();

            TextView text = (TextView)findViewById(R.id.textView);

            text.append("Pictures in Egypt\n");
            text.setMovementMethod(new ScrollingMovementMethod());

            getPlacemarksInFolders(kmlLayer.getNestedContainers(), text, 1);



            Log.i("Demo", "End");
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }

    public void getPlacemarksInFolders(Iterable<KmlContainer> containers, TextView text, int deep) {


        for (KmlContainer kmlContainer :containers) {
            if (kmlContainer.hasKmlProperty("name")) {
                text.append(Html.fromHtml("<b>" + kmlContainer.getKmlProperty("name") + "</b><br>"));
            }
            for (KmlPlacemark kmlPlacemark : kmlContainer.getKmlPlacemarks()) {
                for( int i = 0; i < deep; ++i) {
                    text.append(Html.fromHtml("├──"));
                }
                if (kmlPlacemark.hasProperty("name")) {
                    text.append(Html.fromHtml(kmlPlacemark.getProperty("name") + "<br>"));
                }
            }

            if (kmlContainer.hasNestedKmlContainers()) {
                getPlacemarksInFolders(kmlContainer.getNestedKmlContainers(), text, deep++);
                deep--;
            }

        }
    }
}
