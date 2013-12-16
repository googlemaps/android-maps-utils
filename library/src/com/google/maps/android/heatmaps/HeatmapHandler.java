package com.google.maps.android.heatmaps;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * Handles the heatmap layer, creating the tile overlay, provider, and so on.
 */
public class HeatmapHandler {
    /** Quad tree of points*/
    private PointQuadTree mTree;

    private TileOverlay mOverlay;

    private HeatmapTileProvider mTileProvider;

    private LatLngWrapper[] mList;

    private Bounds mTreeBounds;

    private int mScreenDim;

    public HeatmapHandler(LatLngWrapper[] list, int radius, int[] gradient, double opacity,
                          Activity activity, GoogleMap map) {
        // Assignments
        mList = list;

        // Make the quad tree
        mTreeBounds = HeatmapUtil.getBounds(list);
        mTree = new PointQuadTree<LatLngWrapper>(mTreeBounds);

        // Add points to quad tree
        for (LatLngWrapper l: list) {
            mTree.add(l);
        }

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get screen dimensions
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenDim = dm.widthPixels > dm.heightPixels ? dm.widthPixels : dm.heightPixels;

        double maxIntensity = HeatmapUtil.getMaxVal(list, mTreeBounds, radius, mScreenDim);
        Log.e("MAX", "MaxIntensity = " + maxIntensity);

        // Create a heatmap tile provider, that will generate the overlay tiles
        mTileProvider = new HeatmapTileProvider(mTree, mTreeBounds, radius,
                gradient, opacity, maxIntensity);

        // Add the overlay to the map
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider));
    }

    /**
     * Used to change the radius of the heatmap
     * @param radius radius to change to (in pixels)
     */
    public void setRadius(int radius) {
        mTileProvider.setRadius(radius);
        // need to re calculate max intensity and change in provider
        double maxIntensity = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius, mScreenDim);
        mTileProvider.setMaxIntensity(maxIntensity);
        repaint();
    }

    /**
     * Used to change gradient of the heatmap
     * @param gradient gradient to change to
     */
    public void setGradient(int[] gradient) {
        mTileProvider.setColorMap(gradient);
        repaint();
    }

    /**
     * Used to change the opacity of the heatmap
     * @param opacity opacity to change to (0...1)
     */
    public void setOpacity(double opacity) {
        mTileProvider.setOpacity(opacity);
        repaint();
    }

    /**
     * To be used when an option has been changed that requires tiles to be repainted
     */
    private void repaint() {
        mOverlay.clearTileCache();
    }

}
