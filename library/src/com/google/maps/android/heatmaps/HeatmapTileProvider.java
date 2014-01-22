package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.quadtree.PointQuadTree;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * Tile provider that creates heatmap tiles.
 */
public class HeatmapTileProvider implements TileProvider {
    /**
     * Tile dimension. Package access - WeightedLatLng
     */
    static final int TILE_DIM = 512;

    /**
     * Assumed screen size
     */
    private static final int SCREEN_SIZE = 1280;
    /**
     * Default radius for convolution
     */
    public static final int DEFAULT_RADIUS = 20;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_OPACITY = 0.7;

    /**
     * Default gradient for heatmap.
     * Copied from Javascript version.
     * Array of colors, in int form.
     */
    public static final int[] DEFAULT_GRADIENT = {
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

    /**
     * Tag, for logging
     */
    private static final String TAG = HeatmapTileProvider.class.getName();

    /**
     * Default size of a color map for the heatmap
     */
    private static final int COLOR_MAP_SIZE = 1001;

    /**
     * For use in getBounds.
     * Sigma is used to ensure search is inclusive of upper bounds (eg if a point is on exactly the
     * upper bound, it should be returned)
     */
    static double sigma = 0.0000001;

    /**
     * Quad tree of all the points to display in the heatmap
     */
    private PointQuadTree mTree;

    /**
     * Collection of all the data.
     */
    private Collection<WeightedLatLng> mData;

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
     * Maximum intensity estimates for heatmap
     */
    private double[] mMaxIntensity;

    /**
     * Builder class for the HeatmapTileProvider.
     */
    public static class Builder {
        // Required parameters - not final, as there are 2 ways to set it
        private Collection<WeightedLatLng> data;

        // Optional, initialised to default values
        private int radius = DEFAULT_RADIUS;
        private int[] gradient = DEFAULT_GRADIENT;
        private double opacity = DEFAULT_OPACITY;

        /**
         * Constructor for builder.
         * <p/>
         * No required parameters here, but user must call either data() or weightedData().
         */
        public Builder() {

        }

        /**
         * Setter for data in builder. Must call this or weightedData
         *
         * @param val Collection of LatLngs to put into quadtree.
         *            Should be non-empty.
         * @return updated builder object
         */
        public Builder data(Collection<LatLng> val) {
            return weightedData(wrapData(val));
        }


        /**
         * Setter for data in builder. Must call this or data
         *
         * @param val Collection of WeightedLatLngs to put into quadtree.
         *            Should be non-empty.
         * @return updated builder object
         */
        public Builder weightedData(Collection<WeightedLatLng> val) {
            this.data = val;

            // Check that points is non empty
            if (this.data.isEmpty()) {
                throw new IllegalArgumentException("No input points.");
            }
            return this;
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
         * Call when all desired options have been set.
         * Note: you must set data using data or weightedData before this!
         *
         * @return HeatmapTileProvider created with desired options.
         */
        public HeatmapTileProvider build() {
            // Check if data or weightedData has been called
            if (data == null) {
                throw new IllegalStateException("No input data: you must use either .data or " +
                        ".weightedData before building");
            }

            return new HeatmapTileProvider(this);
        }
    }

    private HeatmapTileProvider(Builder builder) {
        // Get parameters from builder
        mData = builder.data;

        mRadius = builder.radius;
        mGradient = builder.gradient;
        mOpacity = builder.opacity;

        // Compute kernel density function (sigma = 1/3rd of radius)
        mKernel = generateKernel(mRadius, mRadius / 3.0);

        // Generate color map
        setGradient(mGradient);

        // Set the data
        setWeightedData(mData);
    }

    /**
     * Changes the dataset the heatmap is portraying. Weighted.
     *
     * @param data Data set of points to use in the heatmap, as LatLngs.
     *             Note: Editing data without calling setWeightedData again will potentially cause
     *             problems (it is used in calculate max intensity values, which are recalculated
     *             upon changing radius). Either pass in a copy if you want to edit the data
     *             set without changing the data displayed in the heatmap, or call setWeightedData
     *             again afterwards.
     */
    public void setWeightedData(Collection<WeightedLatLng> data) {
        // Change point set
        mData = data;

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
        mBounds = getBounds(mData);
        long end = System.currentTimeMillis();
        Log.d(TAG, "getBounds: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        mTree = new PointQuadTree(mBounds);

        // Add points to quad tree
        for (WeightedLatLng l : mData) {
            mTree.add(l);
        }
        end = System.currentTimeMillis();

        Log.d(TAG, "make quadtree: " + (end - start) + "ms");

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get max intensities
        start = System.currentTimeMillis();
        mMaxIntensity = getMaxIntensities(mRadius);
        end = System.currentTimeMillis();
        Log.d(TAG, "getMaxIntensities: " + (end - start) + "ms");
    }

    /**
     * Changes the dataset the heatmap is portraying. Unweighted.
     *
     * @param data Data set of points to use in the heatmap, as LatLngs.
     */

    public void setData(Collection<LatLng> data) {
        // Turn them into LatLngs and delegate.
        setWeightedData(wrapData(data));
    }

    /**
     * Helper function - wraps LatLngs into WeightedLatLngs.
     *
     * @param data Data to wrap (LatLng)
     * @return Data, in WeightedLatLng form
     */
    private static Collection<WeightedLatLng> wrapData(Collection<LatLng> data) {
        // Use an ArrayList as it is a nice collection
        ArrayList<WeightedLatLng> weightedData = new ArrayList<WeightedLatLng>();

        for (LatLng l : data) {
            weightedData.add(new WeightedLatLng(l));
        }

        return weightedData;
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
        Collection<WeightedLatLng> wrappedPoints = new ArrayList<WeightedLatLng>();
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
        Collection<WeightedLatLng> points = mTree.search(tileBounds);
        long end = System.currentTimeMillis();
        Log.d(TAG, "getTile Search (" + x + "," + y + ") : " + (end - start) + "ms");

        // If no points, return blank tile
        if (points.isEmpty()) {
            return mBlankTile;
        }

        // Quantize points
        start = System.currentTimeMillis();
        double[][] intensity = new double[TILE_DIM + mRadius * 2][TILE_DIM + mRadius * 2];
        for (WeightedLatLng w : points) {
            Point p = w.getPoint();
            int bucketX = (int) ((p.x - minX) / bucketWidth);
            int bucketY = (int) ((p.y - minY) / bucketWidth);
            intensity[bucketX][bucketY] += w.getIntensity();
        }
        // Quantize wraparound points (taking xOffset into account)
        for (WeightedLatLng w : wrappedPoints) {
            Point p = w.getPoint();
            int bucketX = (int) ((p.x + xOffset - minX) / bucketWidth);
            int bucketY = (int) ((p.y - minY) / bucketWidth);
            intensity[bucketX][bucketY] += w.getIntensity();
        }

        end = System.currentTimeMillis();
        Log.d(TAG, "getTile Bucketing (" + x + "," + y + ") : " + (end - start) + "ms");

        start = System.currentTimeMillis();
        // Convolve it ("smoothen" it out)
        double[][] convolved = convolve(intensity, mKernel);
        end = System.currentTimeMillis();
        Log.d(TAG, "getTile Convolving (" + x + "," + y + ") : " + (end - start) + "ms");

        // Color it into a bitmap
        start = System.currentTimeMillis();
        Bitmap bitmap = colorize(convolved, mColorMap, mMaxIntensity[zoom]);
        end = System.currentTimeMillis();
        Log.d(TAG, "getTile Colorize (" + x + "," + y + ") : " + (end - start) + "ms");

        // Convert bitmap to tile
        start = System.currentTimeMillis();
        Tile tile = convertBitmap(bitmap);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "getTile convertBitmap (" + x + "," + y + ") : " + (endTime - start) + "ms");
        Log.d(TAG, "getTile Total (" + x + "," + y + ") : " + (endTime - startTime) + "ms, Points: " + points.size() + ", Zoom: " + zoom);

        return tile;
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
        mColorMap = generateColorMap(gradient, mOpacity);
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
        mKernel = generateKernel(mRadius, mRadius / 3.0);
        // need to recalculate max intensity
        mMaxIntensity = getMaxIntensities(mRadius);
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

    /**
     * Gets array of maximum intensity values to use with the heatmap for each zoom level
     * This is the value that the highest color on the color map corresponds to
     *
     * @param radius radius of the heatmap
     * @return array of maximum intensities
     */
    private double[] getMaxIntensities(int radius) {
        // Can go from zoom level 3 to zoom level 22
        double[] maxIntensityArray = new double[MAX_ZOOM_LEVEL];

        // Calculate max intensity for each zoom level
        for (int i = DEFAULT_MIN_ZOOM; i < DEFAULT_MAX_ZOOM; i++) {
            // Each zoom level multiplies viewable size by 2
            maxIntensityArray[i] = getMaxValue(mData, mBounds, radius,
                    (int) (SCREEN_SIZE * Math.pow(2, i - 3)));
            if (i == DEFAULT_MIN_ZOOM) {
                for (int j = 0; j < i; j++) maxIntensityArray[j] = maxIntensityArray[i];
            }
        }
        for (int i = DEFAULT_MIN_ZOOM; i < MAX_ZOOM_LEVEL; i++) {
            maxIntensityArray[i] = maxIntensityArray[DEFAULT_MAX_ZOOM - 1];
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

    /** Utility functions below */

    /**
     * Helper function for quadtree creation
     *
     * @param points Collection of WeightedLatLng to calculate bounds for
     * @return Bounds that enclose the listed WeightedLatLng points
     */
    static Bounds getBounds(Collection<WeightedLatLng> points) {

        // Use an iterator, need to access any one point of the collection for starting bounds
        Iterator<WeightedLatLng> iter = points.iterator();

        WeightedLatLng first = iter.next();

        double minX = first.getPoint().x;
        double maxX = first.getPoint().x + sigma;
        double minY = first.getPoint().y;
        double maxY = first.getPoint().y + sigma;

        while (iter.hasNext()) {
            WeightedLatLng l = iter.next();
            double x = l.getPoint().x;
            double y = l.getPoint().y;
            // Extend bounds if necessary
            if (x < minX) minX = x;
            if (x + sigma > maxX) maxX = x + sigma;
            if (y < minY) minY = y;
            if (y + sigma > maxY) maxY = y + sigma;
        }

        return new Bounds(minX, maxX, minY, maxY);
    }

    /**
     * Generates 1D Gaussian kernel density function, as a double array of size radius * 2  + 1
     * Normalised with central value of 1.
     *
     * @param radius radius of the kernel
     * @param sigma  standard deviation of the Gaussian function
     * @return generated Gaussian kernel
     */
    static double[] generateKernel(int radius, double sigma) {
        double[] kernel = new double[radius * 2 + 1];
        for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = (Math.exp(-i * i / (2 * sigma * sigma)));
        }
        return kernel;
    }

    /**
     * Applies a 2D Gaussian convolution to the input grid, returning a 2D grid cropped of padding.
     *
     * @param grid   Raw input grid to convolve: dimension dim+2*radius x dim + 2*radius
     *               ie dim * dim with padding of size radius
     * @param kernel Pre-computed Gaussian kernel of size radius*2+1
     * @return the smoothened grid
     */
    static double[][] convolve(double[][] grid, double[] kernel) {
        // Calculate radius size
        int radius = (int) Math.floor((double) kernel.length / 2.0);
        // Padded dimension
        int dimOld = grid.length;
        // Calculate final (non padded) dimension
        int dim = dimOld - 2 * radius;

        // Upper and lower limits of non padded (inclusive)
        int lowerLimit = radius;
        int upperLimit = radius + dim - 1;

        // Convolve horizontally
        double[][] intermediate = new double[dimOld][dimOld];

        // Need to convolve every point (including those outside of non-padded area)
        // but only need to add to points within non-padded area
        int x, y, x2, xUpperLimit, initial;
        double val;
        for (x = 0; x < dimOld; x++) {
            for (y = 0; y < dimOld; y++) {
                // for each point (x, y)
                val = grid[x][y];
                // only bother if something there
                if (val != 0) {
                    // need to "apply" convolution from that point to every point in
                    // (max(lowerLimit, x - radius), y) to (min(upperLimit, x + radius), y)
                    xUpperLimit = ((upperLimit < x + radius) ? upperLimit : x + radius) + 1;
                    // Replace Math.max
                    initial = (lowerLimit > x - radius) ? lowerLimit : x - radius;
                    for (x2 = initial; x2 < xUpperLimit; x2++) {
                        // multiplier for x2 = x - radius is kernel[0]
                        // x2 = x + radius is kernel[radius * 2]
                        // so multiplier for x2 in general is kernel[x2 - (x - radius)]
                        intermediate[x2][y] += val * kernel[x2 - (x - radius)];
                    }
                }
            }
        }

        // Convolve vertically
        double[][] outputGrid = new double[dim][dim];

        // Similarly, need to convolve every point, but only add to points within non-padded area
        // However, we are adding to a smaller grid here (previously, was to a grid of same size)
        int y2, yUpperLimit;

        // Don't care about convolving parts in horizontal padding - wont impact inner
        for (x = lowerLimit; x < upperLimit + 1; x++) {
            for (y = 0; y < dimOld; y++) {
                // for each point (x, y)
                val = intermediate[x][y];
                // only bother if something there
                if (val != 0) {
                    // need to "apply" convolution from that point to every point in
                    // (x, max(lowerLimit, y - radius) to (x, min(upperLimit, y + radius))
                    // Dont care about
                    yUpperLimit = ((upperLimit < y + radius) ? upperLimit : y + radius) + 1;
                    // replace math.max
                    initial = (lowerLimit > y - radius) ? lowerLimit : y - radius;
                    for (y2 = initial; y2 < yUpperLimit; y2++) {
                        // Similar logic to above
                        // subtract, as adding to a smaller grid
                        outputGrid[x - radius][y2 - radius] += val * kernel[y2 - (y - radius)];
                    }
                }
            }
        }

        return outputGrid;
    }

    /**
     * Converts a grid of intensity values to a colored Bitmap, using a given color map
     *
     * @param grid     the input grid (assumed to be square)
     * @param colorMap color map (created by generateColorMap)
     * @param max      Maximum intensity value: maps to 100% on gradient
     * @return the colorized grid in Bitmap form, with same dimensions as grid
     */
    static Bitmap colorize(double[][] grid, int[] colorMap, double max) {
        // Maximum color value
        int maxColor = colorMap[colorMap.length - 1];
        // Multiplier to "scale" intensity values with, to map to appropriate color
        // TODO: is this change (-1 to length) ok? Reasoning: otherwise max will break it
        double colorMapScaling = (colorMap.length - 1) / max;
        // Dimension of the input grid (and dimension of output bitmap)
        int dim = grid.length;


        int i, j, index, col;
        double val;
        // Array of colours
        int colors[] = new int[dim * dim];
        for (i = 0; i < dim; i++) {
            for (j = 0; j < dim; j++) {
                // [x][y]
                // need to enter each row of x coordinates sequentally (x first)
                // -> [j][i]
                val = grid[j][i];
                index = i * dim + j;
                col = (int) (val * colorMapScaling);

                if ((int) val != 0) {
                    // Make it more resilient: cant go outside colorMap
                    if (col < colorMap.length) colors[index] = colorMap[col];
                    else colors[index] = maxColor;
                } else {
                    colors[index] = Color.TRANSPARENT;
                }
            }
        }

        // Now turn these colors into a bitmap
        Bitmap tile = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        // (int[] pixels, int offset, int stride, int x, int y, int width, int height)
        tile.setPixels(colors, 0, dim, 0, 0, dim, dim);
        return tile;
    }

    /**
     * Calculate a reasonable maximum intensity value to map to maximum color intensity
     *
     * @param points    Collection of LatLngs to put into buckets
     * @param bounds    Bucket boundaries
     * @param radius    radius of convolution
     * @param screenDim larger dimension of screen in pixels (for scale)
     * @return Approximate max value
     */
    static double getMaxValue(Collection<WeightedLatLng> points, Bounds bounds, int radius,
                              int screenDim) {
        // Approximate scale as if entire heatmap is on the screen
        // ie scale dimensions to larger of width or height (screenDim)
        double minX = bounds.minX;
        double maxX = bounds.maxX;
        double minY = bounds.minY;
        double maxY = bounds.maxY;
        double boundsDim = (maxX - minX > maxY - minY) ? maxX - minX : maxY - minY;

        // Number of buckets: have diameter sized buckets
        int nBuckets = (int) (screenDim / (2 * radius) + 0.5);
        // Scaling factor to convert width in terms of point distance, to which bucket
        double scale = nBuckets / boundsDim;

        // Make buckets
        double[][] buckets = new double[nBuckets][nBuckets];

        // Assign into buckets + find max value as we go along
        double x, y;
        double max = 0;
        for (WeightedLatLng l : points) {
            x = l.getPoint().x;
            y = l.getPoint().y;

            int xBucket = (int) ((x - minX) * scale);
            int yBucket = (int) ((y - minY) * scale);

            buckets[xBucket][yBucket] += l.getIntensity();
            if (buckets[xBucket][yBucket] > max) max = buckets[xBucket][yBucket];
        }

        return max;
    }

    /**
     * Generates the color map to use with a provided gradient.
     *
     * @param gradient Array of colors (int format)
     * @param opacity  Overall opacity of entire image: every individual alpha value will be
     *                 multiplied by this opacity.
     * @return the generated color map based on the gradient
     */
    static int[] generateColorMap(int[] gradient, double opacity) {
        // Convert gradient into parallel arrays
        int[] values = new int[gradient.length];
        int[] colors = new int[gradient.length];

        // Evenly space out gradient colors with a constant interval (interval = "space" between
        // colors given in the gradient)
        // With defaults, this is 1000/10 = 100
        int interval = (COLOR_MAP_SIZE - 1) / (gradient.length - 1);

        // Go through gradient and insert into values/colors
        for (int i = 0; i < gradient.length; i++) {
            values[i] = i * interval;
            colors[i] = gradient[i];
        }

        int[] colorMap = new int[COLOR_MAP_SIZE];
        // lowColorStop = closest color stop (value from gradient) below current position
        int lowColorStop = 0;
        for (int i = 0; i < COLOR_MAP_SIZE; i++) {
            // if i is larger than next color stop value, increment to next color stop
            // Check that it is safe to access lowColorStop + 1 first!
            // TODO: This fixes previous problem of breaking upon no even divide, but isnt nice
            if (lowColorStop + 1 < values.length) {
                if (i > values[lowColorStop + 1]) lowColorStop++;
            }
            // In between two color stops: interpolate
            if (lowColorStop < values.length - 1) {
                // Check that it is safe to access lowColorStop + 1
                if (i > values[lowColorStop + 1]) lowColorStop++;

                float ratio = (i - interval * lowColorStop) / ((float) interval);
                colorMap[i] = interpolateColor(colors[lowColorStop], colors[lowColorStop + 1],
                        ratio);
            }
            // above highest color stop: use that
            else {
                colorMap[i] = colors[colors.length - 1];
            }
            // Deal with changing the opacity if required
            if (opacity != 1) {
                int c = colorMap[i];
                // TODO: make this better later?
                colorMap[i] = Color.argb((int) (Color.alpha(c) * opacity),
                        Color.red(c), Color.green(c), Color.blue(c));
            }
        }


        return colorMap;
    }

    /**
     * Helper function for creation of color map - interpolates between given colors
     *
     * @param color1 First color
     * @param color2 Second color
     * @param ratio  Between 0 to 1. Fraction of the distance between color1 and color2
     * @return Color associated with x2
     */
    static int interpolateColor(int color1, int color2, float ratio) {

        int alpha = (int) ((Color.alpha(color2) - Color.alpha(color1)) * ratio + Color.alpha(color1));

        float[] hsv1 = new float[3];
        Color.RGBToHSV(Color.red(color1), Color.green(color1), Color.blue(color1), hsv1);
        float[] hsv2 = new float[3];
        Color.RGBToHSV(Color.red(color2), Color.green(color2), Color.blue(color2), hsv2);

        // adjust so that the shortest path on the color wheel will be taken
        if (hsv1[0] - hsv2[0] > 180) {
            hsv2[0] += 360;
        } else if (hsv2[0] - hsv1[0] > 180) {
            hsv1[0] += 360;
        }

        // Interpolate using calculated ratio
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = (hsv2[i] - hsv1[i]) * (ratio) + hsv1[i];
        }

        return Color.HSVToColor(alpha, result);
    }

}