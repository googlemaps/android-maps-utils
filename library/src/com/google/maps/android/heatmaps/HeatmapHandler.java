package com.google.maps.android.heatmaps;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * Handles the heatmap layer, creating the tile overlay, provider, and so on.
 */
public class HeatmapHandler {
    /** Quad tree of points*/
    private PointQuadTree mTree;

    private TileOverlay mOverlay;

    public HeatmapHandler(LatLngWrapper[] list, int radius, int[] gradient, double opacity,
                          Activity activity, GoogleMap map) {
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
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenDim = dm.widthPixels > dm.heightPixels ? dm.widthPixels : dm.heightPixels;

        double maxIntensity = HeatmapUtil.getMaxVal(list, treeBounds,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, screenDim);
        Log.e("MAX", "MaxIntensity = " + maxIntensity);

        // Create a heatmap tile provider, that will generate the overlay tiles
        TileProvider heatmapTileProvider = new HeatmapTileProvider(mTree, treeBounds, radius,
                gradient, opacity, maxIntensity);

        // Add the overlay to the map
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
    }

}
