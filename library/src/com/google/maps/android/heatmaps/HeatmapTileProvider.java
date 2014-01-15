package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.quadtree.PointQuadTree;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Tile provider that creates heatmap tiles.
 */
public class HeatmapTileProvider implements TileProvider {
    /**
     * Tile dimension. Package access - LatLngWrapper
     */
    static final int TILE_DIM = 512;

    /**
     * Assumed screen size
     */
    private static final int SCREEN_SIZE = 1280;
    /**
     * Default radius for convolution
     */
    public static final int DEFAULT_HEATMAP_RADIUS = 20;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_HEATMAP_OPACITY = 0.7;

    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     * Array of colors, in int form.
     */
    public static final int[] DEFAULT_HEATMAP_GRADIENT = {
            //a, r, g, b / r, g, b
            Color.argb(0, 102, 255, 0),  // green (invisible)
            Color.argb(255 / 3 * 2, 102, 255, 0),  // 2/3rds invisible
            Color.rgb(147, 255, 0),
            Color.rgb(193, 255, 0),
            Color.rgb(238, 255, 0),  // yellow
            Color.rgb(244, 227, 0),
            Color.rgb(249, 198, 0),
            Color.rgb(255, 170, 0),  // orange
            Color.rgb(255, 113, 0),
            Color.rgb(255, 57, 0),
            Color.rgb(255, 0, 0)     // red
    };

    /**
     * Default (and minimum possible) minimum zoom level at which to calculate maximum intensities
     */
    private static final int DEFAULT_MIN_ZOOM = 5;

    /**
     * Default (and maximum possible) maximum zoom level at which to calculate maximum intensities
     */
    private static final int DEFAULT_MAX_ZOOM = 9;

    /**
     * Maximum zoom level possible on a map.
     */
    private static final int MAX_ZOOM_LEVEL = 22;

    /**
     * Minimum radius value.
     */
    private static final int MIN_RADIUS = 10;

    /**
     * Maximum radius value.
     */
    private static final int MAX_RADIUS = 50;

    /**
     * Blank tile
     */
    private static final Tile mBlankTile = TileProvider.NO_TILE;

    private static final String TAG = HeatmapTileProvider.class.getName();

    /**
     * Quad tree of all the points to display in the heatmap
     */
    private PointQuadTree mTree;

    /**
     * Collection of all the data.
     */
    private Collection<LatLngWrapper> mData;

    /**
     * Bounds of the quad tree
     */
    private Bounds mBounds;

    /**
     * Heatmap point radius.
     */
    private int mRadius;

    /**
     * Gradient of the color map
     */
    private int[] mGradient;

    /**
     * Color map to use to color tiles
     */
    private int[] mColorMap;

    /**
     * Kernel to use for convolution
     */
    private double[] mKernel;

    /**
     * Opacity of the overall heatmap overlay (0...1)
     */
    private double mOpacity;

    /**
     * Minimum zoom level to calculate custom intensity estimate for
     */
    private int mMinZoom;

    /**
     * Maximum zoom level to calculate custom intensity estimate for
     */
    private int mMaxZoom;

    /**
     * Maximum intensity estimates for heatmap
     */
    private double[] mMaxIntensity;

    /**
     * Builder class for the HeatmapTileProvider.
     */
    public static class Builder {
        // Required parameters
        private final Collection<LatLngWrapper> data;

        // Optional, initialised to default values
        private int radius = DEFAULT_HEATMAP_RADIUS;
        private int[] gradient = DEFAULT_HEATMAP_GRADIENT;
        private double opacity = DEFAULT_HEATMAP_OPACITY;
        // Only custom min/max if they differ so initialise both to 5
        private int minZoom = 5;
        private int maxZoom = 9;

        /**
         * Constructor for builder, which contains the required parameters for a heatmap.
         *
         * @param points Collection of all LatLngWrappers to put into quadtree.
         *               Should be non-empty.
         */
        public Builder(Collection<LatLngWrapper> points)
                throws IllegalArgumentException {
            this.data = points;

            // Check that points is non empty
            if (this.data.isEmpty()) {
                throw new IllegalArgumentException("No input points.");
            }
        }

        /**
         * Setter for radius in builder
         *
         * @param val Radius of convolution to use, in terms of pixels.
         *            Must be within minimum and maximum values of 10 to 50 inclusive.
         * @return updated builder object
         */
        public Builder radius(int val) {
            radius = val;
            // Check that radius is within bounds.
            if (radius < MIN_RADIUS || radius > MAX_RADIUS) {
                throw new IllegalArgumentException("Radius not within bounds.");
            }
            return this;
        }

        /**
         * Setter for gradient in builder
         *
         * @param val Gradient to color heatmap with.
         *            Ordered from least to highest corresponding intensity.
         *            A larger colour map is interpolated from these "colour stops".
         *            First color usually fully transparent, and should be at least 3 colors for
         *            best results.
         * @return updated builder object
         */
        public Builder gradient(int[] val) {
            gradient = val;
            // Check that gradient is not empty
            if (gradient.length == 0) {
                throw new IllegalArgumentException("Gradient is empty.");
            }
            return this;
        }

        /**
         * Setter for opacity in builder
         *
         * @param val Opacity of the entire heatmap in range [0, 1]
         * @return updated builder object
         */
        public Builder opacity(double val) {
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
         *            recommended/default is 5
         * @param max maximum zoom level to calculate max intensity for
         *            recommended/default is 8
         *            Must be greater than or equal to min
         * @return updated builder object
         */
        public Builder zoom(int min, int max) {
            minZoom = min;
            maxZoom = max;
            // Check min and max are OK
            if (min > max) {
                throw new IllegalArgumentException("Min must be smaller than or equal to max");
            }
            if (min < DEFAULT_MIN_ZOOM) {
                throw new IllegalArgumentException("Min smaller than allowed");
            }
            if (max > DEFAULT_MAX_ZOOM) {
                throw new IllegalArgumentException("Max larger than allowed");
            }
            return this;
        }

        /**
         * Call when all desired options have been set.
         *
         * @return HeatmapTileProvider created with desired options.
         */
        public HeatmapTileProvider build() {
            // Check
            return new HeatmapTileProvider(this);
        }
    }

    private HeatmapTileProvider(Builder builder) {
        // Get parameters from builder
        mData = builder.data;
        mMinZoom = builder.minZoom;
        mMaxZoom = builder.maxZoom;

        mRadius = builder.radius;
        mGradient = builder.gradient;
        mOpacity = builder.opacity;

        // Compute kernel density function (sigma = 1/3rd of radius)
        mKernel = HeatmapUtil.generateKernel(mRadius, mRadius / 3.0);

        // Generate color map
        setGradient(mGradient);

        // Set the data
        setData(mData);
    }

    /**
     * Changes the dataset the heatmap is portraying.
     *
     * @param points Points to use in the heatmap.
     */
    public void setData(Collection<LatLngWrapper> points) {
        // Change point set
        mData = points;

        // Check point set is OK
        if (mData.isEmpty()) {
            throw new IllegalArgumentException("No input points.");
        }

        // Because quadtree bounds are final once the quadtree is created, we cannot add
        // points outside of those bounds to the quadtree after creation.
        // As quadtree creation is actually quite lightweight/fast as compared to other functions
        // called in heatmap creation, re-creating the quadtree is an acceptable solution here.

        long start = System.currentTimeMillis();
        // Make the quad tree
        mBounds = HeatmapUtil.getBounds(mData);
        long end = System.currentTimeMillis();
        Log.d(TAG, "getBounds: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        mTree = new PointQuadTree(mBounds);

        // Add points to quad tree
        for (LatLngWrapper l : mData) {
            mTree.add(l);
        }
        end = System.currentTimeMillis();

        Log.d(TAG, "make quadtree: " + (end - start) + "ms");

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get max intensities
        start = System.currentTimeMillis();
        mMaxIntensity = getMaxIntensities(mRadius, mMinZoom, mMaxZoom);
        end = System.currentTimeMillis();
        Log.d(TAG, "getMaxIntensities: " + (end - start) + "ms");
    }

    /**
     * Creates tile.
     *
     * @param x    X coordinate of tile.
     * @param y    Y coordinate of tile.
     * @param zoom Zoom level.
     * @return image in Tile format
     */
    public Tile getTile(int x, int y, int zoom) {
        long startTime = System.currentTimeMillis();
        // Convert tile coordinates and zoom into Point/Bounds format
        // Know that at zoom level 0, there is one tile: (0, 0) (arbitrary width 512)
        // Each zoom level multiplies number of tiles by 2
        // Width of the world = 512 (Spherical Mercator Projection)
        // x = [0, 512) [-180, 180)

        //basically arbitrarily chosen scale (based off the demo)
        double worldWidth = TILE_DIM;

        // calculate width of one tile, given there are 2 ^ zoom tiles in that zoom level
        double tileWidth = worldWidth / Math.pow(2, zoom);

        // how much padding to include in search
        // Maths: padding = tileWidth * mRadius / TILE_DIM = TILE_DIM /(2^zoom) * mRadius / TILE_DIM
        double padding = mRadius / Math.pow(2, zoom);

        // padded tile width
        double tileWidthPadded = tileWidth + 2 * padding;

        // padded bucket width
        double bucketWidth = tileWidthPadded / (TILE_DIM + mRadius * 2);

        // Make bounds: minX, maxX, minY, maxY
        // Sigma because search is non inclusive
        double sigma = 0.00000001;
        double minX = x * tileWidth - padding;
        double maxX = (x + 1) * tileWidth + padding + sigma;
        double minY = y * tileWidth - padding;
        double maxY = (y + 1) * tileWidth + padding + sigma;

        // Deal with overlap across lat = 180
        // Need to make it wrap around both ways
        // However, maximum tile size is such that you wont ever have to deal with both, so
        // hence, the else
        // Note: Tile must remain square, so cant optimise by editing bounds
        double xOffset = 0;
        Collection<LatLngWrapper> wrappedPoints = new ArrayList<LatLngWrapper>();
        if (minX < 0) {
            // Need to consider "negative" points
            // (minX to 0) ->  (512+minX to 512) ie +512
            // add 512 to search bounds and subtract 512 from actual points
            Bounds overlapBounds = new Bounds(minX + worldWidth, worldWidth, minY, maxY);
            xOffset = -worldWidth;
            wrappedPoints = mTree.search(overlapBounds);
        } else if (maxX > worldWidth) {
            // Cant both be true as then tile covers whole world
            // Need to consider "overflow" points
            // (512 to maxX) -> (0 to maxX-512) ie -512
            // subtract 512 from search bounds and add 512 to actual points
            Bounds overlapBounds = new Bounds(0, maxX - worldWidth, minY, maxY);
            xOffset = worldWidth;
            wrappedPoints = mTree.search(overlapBounds);
        }

        // Main tile bounds to search
        Bounds tileBounds = new Bounds(minX, maxX, minY, maxY);

        // If outside of *padded* quadtree bounds, return blank tile
        // This is comparing our bounds to the padded bounds of all points in the quadtree
        Bounds paddedBounds = new Bounds(mBounds.minX - padding, mBounds.maxX + padding,
                mBounds.minY - padding, mBounds.maxY + padding);
        if (!tileBounds.intersects(paddedBounds)) {
            return mBlankTile;
        }

        // Search for all points within tile bounds
        long start = System.currentTimeMillis();
        Collection<LatLngWrapper> points = mTree.search(tileBounds);
        long end = System.currentTimeMillis();
        Log.d(TAG, "getTile Search (" + x + "," + y + ") : " + (end - start) + "ms");

        // Add wrapped (wraparound) points if necessary
        if (!wrappedPoints.isEmpty()) {
            for (LatLngWrapper l : wrappedPoints) {
                points.add(new LatLngWrapper(l, xOffset));
            }
        }

        // If no points, return blank tile
        if (points.isEmpty()) {
            return mBlankTile;
        }

        // Quantize points
        start = System.currentTimeMillis();
        double[][] intensity = new double[TILE_DIM + mRadius * 2][TILE_DIM + mRadius * 2];
        for (LatLngWrapper w : points) {
            Point p = w.getPoint();
            int bucketX = (int) ((p.x - minX) / bucketWidth);
            int bucketY = (int) ((p.y - minY) / bucketWidth);
            intensity[bucketX][bucketY] += w.getIntensity();
        }
        end = System.currentTimeMillis();
        Log.d(TAG, "getTile Bucketing (" + x + "," + y + ") : " + (end - start) + "ms");

        start = System.currentTimeMillis();
        // Convolve it ("smoothen" it out)
        double[][] convolved = HeatmapUtil.convolve(intensity, mKernel);
        end = System.currentTimeMillis();
        Log.d(TAG, "getTile Convolving (" + x + "," + y + ") : " + (end - start) + "ms");

        // Color it into a bitmap
        start = System.currentTimeMillis();
        Bitmap bitmap = HeatmapUtil.colorize(convolved, mColorMap, mMaxIntensity[zoom]);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "getTile Colorize (" + x + "," + y + ") : " + (endTime - start) + "ms");

        Log.d(TAG, "getTile Total (" + x + "," + y + ") : " + (endTime - startTime) + "ms, Points: " + points.size() + ", Zoom: " + zoom);

        return convertBitmap(bitmap);
    }

    /**
     * Setter for gradient/color map.
     * Important: tile overlay cache must be cleared after this for it to be effective
     * outside of initialisation
     *
     * @param gradient Gradient to set
     */
    public void setGradient(int[] gradient) {
        mGradient = gradient;
        mColorMap = HeatmapUtil.generateColorMap(gradient, mOpacity);
    }

    /**
     * Setter for radius.
     * User should clear overlay's tile cache after calling this.
     *
     * @param radius Radius to set
     */
    public void setRadius(int radius) {
        mRadius = radius;
        // need to recompute kernel
        mKernel = HeatmapUtil.generateKernel(mRadius, mRadius / 3.0);
        // need to recalculate max intensity
        mMaxIntensity = getMaxIntensities(mRadius, mMinZoom, mMaxZoom);
    }

    /**
     * Setter for opacity
     * User should clear overlay's tile cache after calling this.
     *
     * @param opacity opacity to set
     */
    public void setOpacity(double opacity) {
        mOpacity = opacity;
        // need to recompute kernel color map
        setGradient(mGradient);
    }

    private double[] getMaxIntensities(int radius, int min_zoom, int max_zoom) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[MAX_ZOOM_LEVEL];

        if (min_zoom < max_zoom) {
            // Calculate max intensity for each zoom level
            for (int i = min_zoom; i < max_zoom; i++) {
                // Each zoom level multiplies viewable size by 2
                maxIntensityArray[i] = HeatmapUtil.getMaxValue(mData, mBounds, radius,
                        (int) (SCREEN_SIZE * Math.pow(2, i - 3)));
                if (i == min_zoom) {
                    for (int j = 0; j < i; j++) maxIntensityArray[j] = maxIntensityArray[i];
                }
            }
            for (int i = max_zoom; i < MAX_ZOOM_LEVEL; i++) {
                maxIntensityArray[i] = maxIntensityArray[max_zoom - 1];
            }
        } else {
            // Just calculate one max intensity across whole map
            double maxIntensity = HeatmapUtil.getMaxValue(mData, mBounds, radius, SCREEN_SIZE);
            for (int i = 0; i < MAX_ZOOM_LEVEL; i++) {
                maxIntensityArray[i] = maxIntensity;
            }
        }
        return maxIntensityArray;
    }

    /**
     * helper function - convert a bitmap into a tile
     *
     * @param bitmap bitmap to convert into a tile
     * @return the tile
     */
    private static Tile convertBitmap(Bitmap bitmap) {
        // Convert it into byte array (required for tile creation)
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return new Tile(TILE_DIM, TILE_DIM, bitmapdata);
    }

}