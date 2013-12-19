package com.google.maps.android.heatmaps;

import android.util.Log;

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
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, int radius, int[] gradient, double opacity,
                          boolean intensityFlag, GoogleMap map) {
        // Assignments
        mList = list;
        mIntensityFlag = intensityFlag;

        long start = getTime();
        // Make the quad tree
        mTreeBounds = HeatmapUtil.getBounds(list);
        long end = getTime();
        Log.e("Time getBounds", (end - start) + "ms");

        start = getTime();
        mTree = new PointQuadTree<LatLngWrapper>(mTreeBounds);

        // Add points to quad tree
        for (LatLngWrapper l: list) {
            mTree.add(l);
        }
        end = getTime();

        Log.e("Time Make Quadtree", (end - start) + "ms");

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get max intensities
        start = getTime();
        mMaxIntensity = getMaxIntensities(radius);
        end = getTime();
        Log.e("Time getMaxIntensities", (end - start) + "ms");

        // Create a heatmap tile provider, that will generate the overlay tiles
        start = getTime();
        mTileProvider = new HeatmapTileProvider(mTree, mTreeBounds, radius,
                gradient, opacity, mMaxIntensity);
        end = getTime();
        Log.e("Time new HeatmapTileProvider", (end - start) + "ms");

        // Add the overlay to the map
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider));
    }

    /**
     * Alternative constructor that uses default values for radius, gradient and opacity
     * @param list List of all LatLngWrappers to put into quadtree
     * @param intensityFlag whether max intensity changes at zoom level or not
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, boolean intensityFlag, GoogleMap map) {
        this(list, HeatmapConstants.DEFAULT_HEATMAP_RADIUS,
                HeatmapConstants.DEFAULT_HEATMAP_GRADIENT, HeatmapConstants.DEFAULT_HEATMAP_OPACITY,
                intensityFlag, map);
    }


    private double[] getMaxIntensities(int radius) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[22];
        int bestZoomLevel = 8;

        if (mIntensityFlag) {
            // Calculate max intensity for each zoom level
            for (int i = 3; i < bestZoomLevel; i ++) {
                maxIntensityArray[i] = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius,
                        (int)(HeatmapConstants.SCREEN_SIZE * Math.pow(2, i - 3)));
                if (i == 3) {
                    maxIntensityArray[2] = maxIntensityArray[3];
                    maxIntensityArray[1] = maxIntensityArray[3];
                    maxIntensityArray[0] = maxIntensityArray[3];
                }
            }
            for (int i = bestZoomLevel; i < 22; i ++) {
                maxIntensityArray[i] = maxIntensityArray[bestZoomLevel - 1];
            }
        } else {
            double maxIntensity = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius,
                    HeatmapConstants.SCREEN_SIZE);
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


    private long getTime() {
        return System.currentTimeMillis();
    }

}
