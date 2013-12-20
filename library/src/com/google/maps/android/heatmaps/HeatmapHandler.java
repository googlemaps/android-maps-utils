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

    public static final int MAX_ZOOM_LEVEL = 22;

    /** Quad tree of points */
    private PointQuadTree mTree;

    private TileOverlay mOverlay;

    private HeatmapTileProvider mTileProvider;

    private ArrayList<LatLngWrapper> mList;

    private Bounds mTreeBounds;

    private int mMinZoom;

    private int mMaxZoom;

    private double[] mMaxIntensity;

    /**
     * Constructor for the handler
     * @param list List of all LatLngWrappers to put into quadtree
     * @param radius Radius of convolution to use
     * @param gradient Gradient to color heatmap with
     * @param opacity Opacity of the entire heatmap
     * @param minZoom minimum zoom level to calculate max intensity for
     *                recommended/default is 5
     * @param maxZoom maximum zoom level to calculate max intensity for
     *                recommended/default is 8
     *                if minZoom >= maxZoom, only one default max intensity value is calculated
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, int radius, int[] gradient, double opacity,
                          int minZoom, int maxZoom, GoogleMap map) {
        // Assignments
        mList = list;
        mMinZoom = minZoom;
        mMaxZoom = maxZoom;

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
        mMaxIntensity = getMaxIntensities(radius, minZoom, maxZoom);
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
     * @param minZoom minimum zoom level to calculate max intensity for
     *                recommended/default is 5
     * @param maxZoom maximum zoom level to calculate max intensity for
     *                recommended/default is 8
     *                if minZoom >= maxZoom, only one default max intensity value is calculated
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, int minZoom, int maxZoom, GoogleMap map) {
        this(list, HeatmapConstants.DEFAULT_HEATMAP_RADIUS,
                HeatmapConstants.DEFAULT_HEATMAP_GRADIENT, HeatmapConstants.DEFAULT_HEATMAP_OPACITY,
                minZoom, maxZoom, map);
    }

    /**
     * Alternative constructor that uses default values for radius, gradient and opacity
     * and doesnt calculate custom max intensities for different zoom levels
     * @param list List of all LatLngWrappers to put into quadtree
     * @param map pass the map so we can draw the heatmap onto it
     */
    public HeatmapHandler(ArrayList<LatLngWrapper> list, GoogleMap map) {
        this(list, HeatmapConstants.DEFAULT_HEATMAP_RADIUS,
                HeatmapConstants.DEFAULT_HEATMAP_GRADIENT, HeatmapConstants.DEFAULT_HEATMAP_OPACITY,
                0, 0, map);
    }

    private double[] getMaxIntensities(int radius, int min_zoom, int max_zoom) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[MAX_ZOOM_LEVEL];

        if (min_zoom < max_zoom) {
            // Calculate max intensity for each zoom level
            for (int i = min_zoom; i < max_zoom; i ++) {
                // Each zoom level multiplies viewable size by 2
                maxIntensityArray[i] = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius,
                        (int)(HeatmapConstants.SCREEN_SIZE * Math.pow(2, i - 3)));
                if (i == min_zoom) {
                    for(int j = 0; j < i; j++) maxIntensityArray[j] = maxIntensityArray[i];
                }
            }
            for (int i = max_zoom; i < MAX_ZOOM_LEVEL; i ++) {
                maxIntensityArray[i] = maxIntensityArray[max_zoom - 1];
            }
        } else {
            // Just calculate one max intensity across whole map
            double maxIntensity = HeatmapUtil.getMaxVal(mList, mTreeBounds, radius,
                    HeatmapConstants.SCREEN_SIZE);
            for (int i = 0; i < MAX_ZOOM_LEVEL; i ++) {
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
        double[] maxIntensity = getMaxIntensities(radius, mMinZoom, mMaxZoom);
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
