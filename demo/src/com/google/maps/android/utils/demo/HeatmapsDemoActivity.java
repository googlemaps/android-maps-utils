package com.google.maps.android.utils.demo;

import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.HeatmapUtil;
import com.google.maps.android.heatmaps.LatLngWrapper;
import com.google.maps.android.quadtree.PointQuadTree;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

public class HeatmapsDemoActivity extends BaseDemoActivity {

    /** Quad tree of points*/
    private PointQuadTree mTree;

    @Override
    // TODO: move a lot of this into a nicer HeatmapLayer class (handler)?
    protected void startDemo() {
        //getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 16));

        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        LatLngWrapper[] list = new LatLngWrapper[10];

        try {
            list = readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        // Make the quad tree
        Bounds treeBounds = HeatmapUtil.getBounds(list);
        mTree = new PointQuadTree<LatLngWrapper>(treeBounds);

        // Add points to quad tree
        for (LatLngWrapper l: list) {
            mTree.add(l);
        }

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get screen dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenDim = dm.widthPixels > dm.heightPixels ? dm.widthPixels : dm.heightPixels;

        double maxIntensity = HeatmapUtil.getMaxVal(list, treeBounds,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, screenDim);
        Log.e("MAX", "MaxIntensity = " + maxIntensity);

        // Create a heatmap tile provider, that will generate the overlay tiles
        TileProvider heatmapTileProvider = new HeatmapTileProvider(mTree, treeBounds,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, HeatmapConstants.DEFAULT_HEATMAP_GRADIENT,
                HeatmapConstants.DEFAULT_HEATMAP_OPACITY, maxIntensity);
        // Add the tile overlay to the map
        getMap().addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
    }


    // Copied from ClusteringDemoActivity
    private LatLngWrapper[] readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        List<MyItem> items = new MyItemReader().read(inputStream);

        LatLngWrapper[] list = new LatLngWrapper[items.size() * 11];
        int i, j;
        for (i = 0; i < items.size(); i++) {
            MyItem temp = items.get(i);
            list[i] = new LatLngWrapper(temp.getPosition());
        }

        for (j = 0; j < 10; j++) {
            double offset = j / 60d;
            for (MyItem item : items) {
                LatLng position = item.getPosition();
                double lat = position.latitude + offset;
                double lng = position.longitude + offset;
                list[i] = new LatLngWrapper(new LatLng(lat, lng));
                i++;
            }
        }

        return list;
    }

}
