package com.google.maps.android.heatmaps;

import android.app.Activity;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.quadtree.PointQuadTree;

import java.util.ArrayList;

/**
 * Handles the heatmap layer, creating the tile overlay, provider, and so on.
 */
public class HeatmapHandler {

    /** Quad tree of points */
    private PointQuadTree mTree;

    private TileOverlay mOverlay;

    private HeatmapTileProvider mTileProvider;

    private ArrayList<LatLngWrapper> mList;

    private Bounds mTreeBounds;

    private int mScreenDim;

    /** True if max intensity changes at different zoom levels: false otherwise */
    private boolean mIntensityFlag;

    private double[] mMaxIntensity;

    /**
     * Constructor for the handler
     * @param list List of all LatLngWrappers to put into quadtree
     * @param radius Radius of convolution to use
     * @param gradient Gradient to color heatmap with
     * @param opacity Opacity of the entire heatmap
     * @param intensityFlag whether max intensity changes at zoom level or not
     * @param activity pass the activity to obtain dimensions
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, int radius, int[] gradient, double opacity,
                          boolean intensityFlag, Activity activity, GoogleMap map) {
        // Assignments
        mList = list;
        mIntensityFlag = intensityFlag;

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

        // Get max intensities
        mMaxIntensity = getMaxIntensities(radius);

        // Create a heatmap tile provider, that will generate the overlay tiles
        mTileProvider = new HeatmapTileProvider(mTree, mTreeBounds, radius,
                gradient, opacity, mMaxIntensity);

        // Add the overlay to the map
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider));
    }

    /**
     * Alternative constructor that uses default values for radius, gradient and opacity
     * @param list List of all LatLngWrappers to put into quadtree
     * @param intensityFlag whether max intensity changes at zoom level or not
     * @param activity pass the activity to obtain dimensions
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, boolean intensityFlag, Activity activity,
                          GoogleMap map) {
        this(list, HeatmapConstants.DEFAULT_HEATMAP_RADIUS,
                HeatmapConstants.DEFAULT_HEATMAP_GRADIENT, HeatmapConstants.DEFAULT_HEATMAP_OPACITY,
                intensityFlag, activity, map);
    }


    private double[] getMaxIntensities(int radius) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[22];
        if (mIntensityFlag) {
            // Calculate max intensity for each zoom level
            for (int i = 3; i < 8; i ++) {
                maxIntensityArray[i] = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius,
                        (int)(mScreenDim * Math.pow(2, i - 3)));
            }
            for (int i = 8; i < 22; i ++) {
                maxIntensityArray[i] = maxIntensityArray[7];
            }
        } else {
            double maxIntensity = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius, mScreenDim);
            for (int i = 3; i < 22; i ++) {
                maxIntensityArray[i] = maxIntensity;
            }
        }
        return maxIntensityArray;
    }

    /**
     * Used to change the radius of the heatmap
     * @param radius radius to change to (in pixels)
     */
    public void setRadius(int radius) {
        mTileProvider.setRadius(radius);
        // need to re calculate max intensity and change in provider
        double[] maxIntensity = getMaxIntensities(radius);
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
