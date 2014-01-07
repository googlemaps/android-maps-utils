package com.google.maps.android.heatmaps;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.quadtree.PointQuadTree;
import com.google.maps.android.quadtree.PointQuadTreeImpl;

import java.util.Collection;

/**
 * Handles the heatmap layer, creating the tile overlay, provider, and so on.
 */
public class HeatmapHelper {

    public static final int MAX_ZOOM_LEVEL = 22;

    /** Quad tree of points */
    private PointQuadTree mTree;

    private TileOverlay mOverlay;

    private HeatmapTileProvider mProvider;

    private Collection<LatLngWrapper> mPoints;

    private Bounds mTreeBounds;

    private int mRadius;

    private int[] mGradient;

    private double mOpacity;

    private int mMinZoom;

    private int mMaxZoom;

    private GoogleMap mMap;

    private double[] mMaxIntensity;

    /**
     * Builder class for the Handler.
     *
     */
    public static class Builder {
        // Required parameters
        private final Collection<LatLngWrapper> points;
        private final GoogleMap map;

        // Optional, initialised to default values
        private int radius = HeatmapConstants.DEFAULT_HEATMAP_RADIUS;
        private int[] gradient = HeatmapConstants.DEFAULT_HEATMAP_GRADIENT;
        private double opacity = HeatmapConstants.DEFAULT_HEATMAP_OPACITY;
        // Only custom min/max if they differ so initialise both to 5
        private int minZoom = 5;
        private int maxZoom = 5;

        /**
         * Constructor for builder, which contains the required parameters for a heatmap.
         * @param points Collection of all LatLngWrappers to put into quadtree.
         *               Should be non-empty.
         * @param map Map on which heatmap will be drawn.
         */
        public Builder(Collection<LatLngWrapper> points, GoogleMap map)
                throws IllegalArgumentException{
            this.points = points;
            this.map = map;

            // Check that points is non empty
            if (this.points.isEmpty()) {
                throw new IllegalArgumentException("No input points.");
            }
        }

        /**
         * Setter for radius in builder
         * @param val Radius of convolution to use, in terms of pixels.
         *            Must be within minimum and maximum values as found in HeatmapConstants.
         * @return updated builder object
         */
        public Builder radius(int val) throws IllegalArgumentException {
            radius = val;
            // Check that radius is within bounds.
            if (radius < HeatmapConstants.MIN_RADIUS || radius > HeatmapConstants.MAX_RADIUS) {
                throw new IllegalArgumentException("Radius not within bounds.");
            }
            return this;
        }

        /**
         * Setter for gradient in builder
         * @param val Gradient to color heatmap with. This is usually about 10 different colours.
         *                 Ordered from least to highest corresponding intensity.
         *                 A larger colour map is interpolated from these "colour stops".
         * @return updated builder object
         */
        public Builder gradient(int[] val) throws IllegalArgumentException {
            gradient = val;
            // Check that gradient is not empty
            if (gradient.length == 0) {
                throw new IllegalArgumentException("Gradient is empty.");
            }
            return this;
        }

        /**
         * Setter for opacity in builder
         * @param val Opacity of the entire heatmap in range [0, 1]
         * @return updated builder object
         */
        public Builder opacity(double val) throws IllegalArgumentException {
            opacity = val;
            // Check that opacity is in range
            if (opacity < 0 || opacity > 1) {
                throw new IllegalArgumentException("Opacity must be in range [0, 1]");
            }
            return this;
        }

        /**
         * Setter for which zoom levels to calculate max intensity for
         * Cannot have custom outside of defaults (too slow)
         * Max intensity will be set to that of min at zoom levels lower than min,
         * and similarly for those above max.
         * These should be the zoom levels at which your heatmap is intended to be viewed at.
         * @param min minimum zoom level to calculate max intensity for
         *                recommended/default is 5
         * @param max maximum zoom level to calculate max intensity for
         *                recommended/default is 8
         *                Must be greater than or equal to min
         * @return updated builder object
         */
        public Builder zoom(int min, int max) throws IllegalArgumentException {
            minZoom = min;
            maxZoom = max;
            // Check min and max are OK
            if (min > max) {
                throw new IllegalArgumentException("Min must be smaller than or equal to max");
            }
            if (min < HeatmapConstants.DEFAULT_MIN_ZOOM) {
                throw new IllegalArgumentException("Min smaller than allowed");
            }
            if (max > HeatmapConstants.DEFAULT_MAX_ZOOM) {
                throw new IllegalArgumentException("Max larger than allowed");
            }
            return this;
        }

        /**
         * Call when all desired options have been set.
         * @return HeatmapHandler created with desired options.
         */
        public HeatmapHelper build() {
            // Check
            return new HeatmapHelper(this);
        }
    }

    private HeatmapHelper(Builder builder) {
        //Get parameters from builder
        mPoints = builder.points;
        mMinZoom = builder.minZoom;
        mMaxZoom = builder.maxZoom;

        mRadius = builder.radius;
        mGradient = builder.gradient;
        mOpacity = builder.opacity;

        mMap = builder.map;

        setData(mPoints);
    }

    /**
     * Changes the dataset the heatmap is portraying.
     * @param points Points to use in the heatmap.
     */
    public void setData(Collection<LatLngWrapper> points) throws IllegalArgumentException {
        // Change point set
        mPoints = points;

        // Check point set is OK
        if (mPoints.isEmpty()) {
            throw new IllegalArgumentException("No input points.");
        }

        // Remove old overlay if required
        if (mOverlay != null) {
            mOverlay.remove();
        }

        // Because quadtree bounds are final once the quadtree is created, we cannot add
        // points outside of those bounds to the quadtree after creation.
        // As quadtree creation is actually quite lightweight/fast as compared to other functions
        // called in heatmap creation, re-creating the quadtree is an acceptable solution here.

        long start = getTime();
        // Make the quad tree
        mTreeBounds = HeatmapUtil.getBounds(mPoints);
        long end = getTime();
        Log.e("Time getBounds", (end - start) + "ms");

        start = getTime();
        mTree = new PointQuadTreeImpl(mTreeBounds);

        // Add points to quad tree
        for (LatLngWrapper l: mPoints) {
            mTree.add(l);
        }
        end = getTime();

        Log.e("Time Make Quadtree", (end - start) + "ms");

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get max intensities
        start = getTime();
        mMaxIntensity = getMaxIntensities(mRadius, mMinZoom, mMaxZoom);
        end = getTime();
        Log.e("Time getMaxIntensities", (end - start) + "ms");

        // Create a heatmap tile provider, that will generate the overlay tiles
        start = getTime();
        mProvider = new HeatmapTileProvider(mTree, mTreeBounds, mRadius, mGradient, mOpacity,
                mMaxIntensity);
        end = getTime();
        Log.e("Time new HeatmapTileProvider", (end - start) + "ms");

        // Add the overlay to the map
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    /**
     * Used to change the radius of the heatmap
     * @param radius radius to change to (in pixels)
     */
    public void setRadius(int radius) {
        mProvider.setRadius(radius);
        // need to re calculate max intensity and change in provider
        double[] maxIntensity = getMaxIntensities(radius, mMinZoom, mMaxZoom);
        mProvider.setMaxIntensity(maxIntensity);
        repaint();
    }

    /**
     * Used to change gradient of the heatmap
     * @param gradient gradient to change to
     */
    public void setGradient(int[] gradient) {
        mProvider.setColorMap(gradient);
        repaint();
    }

    /**
     * Used to change the opacity of the heatmap
     * @param opacity opacity to change to (0...1)
     */
    public void setOpacity(double opacity) {
        mProvider.setOpacity(opacity);
        repaint();
    }

    /**
     * To be used when an option has been changed that requires tiles to be repainted
     */
    private void repaint() {
        mOverlay.clearTileCache();
    }

    /**
     * Removes the tile overlay from the map.
     */
    public void remove() {
        mOverlay.remove();
    }

    /**
     * Use to change visibility of overlay without changing opacity.
     * @param visibility Boolean: true for visible, false for not
     */
    public void setVisible(boolean visibility) {
        mOverlay.setVisible(visibility);
    }

    private double[] getMaxIntensities(int radius, int min_zoom, int max_zoom) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[MAX_ZOOM_LEVEL];

        if (min_zoom < max_zoom) {
            // Calculate max intensity for each zoom level
            for (int i = min_zoom; i < max_zoom; i ++) {
                // Each zoom level multiplies viewable size by 2
                maxIntensityArray[i] = HeatmapUtil.getMaxVal(mPoints, mTreeBounds, radius,
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
            double maxIntensity = HeatmapUtil.getMaxVal(mPoints, mTreeBounds, radius,
                    HeatmapConstants.SCREEN_SIZE);
            for (int i = 0; i < MAX_ZOOM_LEVEL; i ++) {
                maxIntensityArray[i] = maxIntensity;
            }
        }
        return maxIntensityArray;
    }

    private long getTime() {
        return System.currentTimeMillis();
    }
}
