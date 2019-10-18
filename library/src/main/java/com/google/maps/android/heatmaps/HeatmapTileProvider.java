/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.collection.LongSparseArray;

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
     * Default radius for convolution
     */
    public static final int DEFAULT_RADIUS = 20;

    /**
     * Default opacity of heatmap overlay
     */
    public static final double DEFAULT_OPACITY = 0.7;

    /**
     * Colors for default gradient.
     * Array of colors, represented by ints.
     */
    private static final int[] DEFAULT_GRADIENT_COLORS = {
            Color.rgb(102, 225, 0),
            Color.rgb(255, 0, 0)
    };

    /**
     * Starting fractions for default gradient.
     * This defines which percentages the above colors represent.
     * These should be a sorted array of floats in the interval [0, 1].
     */
    private static final float[] DEFAULT_GRADIENT_START_POINTS = {
            0.2f, 1f
    };

    /**
     * Default gradient for heatmap.
     */
    public static final Gradient DEFAULT_GRADIENT = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);

    /**
     * Size of the world (arbitrary).
     * Used to measure distances relative to the total world size.
     * Package access for WeightedLatLng.
     */
    static final double WORLD_WIDTH = 1;

    /**
     * Tile dimension, in pixels.
     */
    private static final int TILE_DIM = 512;

    /**
     * Assumed screen size (pixels)
     */
    private static final int SCREEN_SIZE = 1280;

    /**
     * Default (and minimum possible) minimum zoom level at which to calculate maximum intensities
     */
    private static final int DEFAULT_MIN_ZOOM = 5;

    /**
     * Default (and maximum possible) maximum zoom level at which to calculate maximum intensities
     */
    private static final int DEFAULT_MAX_ZOOM = 11;

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
     * Quad tree of all the points to display in the heatmap
     */
    private PointQuadTree<WeightedLatLng> mTree;

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
    private Gradient mGradient;

    /**
     * Color map to use to color tiles
     */
    private int[] mColorMap;

    /**
     * Kernel to use for convolution
     */
    private double[] mKernel;

    /**
     * Opacity of the overall heatmap overlay [0...1]
     */
    private double mOpacity;

    /**
     * Maximum intensity estimates for heatmap
     */
    private double[] mMaxIntensity;

    /**
     * Optional user defined maximum intensity for heatmap
     */
    private double mCustomMaxIntensity;

    /**
     * Builder class for the HeatmapTileProvider.
     */
    public static class Builder {
        // Required parameters - not final, as there are 2 ways to set it
        private Collection<WeightedLatLng> data;

        // Optional, initialised to default values
        private int radius = DEFAULT_RADIUS;
        private Gradient gradient = DEFAULT_GRADIENT;
        private double opacity = DEFAULT_OPACITY;
        private double intensity = 0;

        /**
         * Constructor for builder.
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
         * @return updated builder object
         */
        public Builder gradient(Gradient val) {
            gradient = val;
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
         * Setter for Max Intensity in builder
         *
         * @param val maximum intensity of pixel density
         * @return updated builder object
         */
        public Builder maxIntensity(double val) {
            intensity = val;
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
        mCustomMaxIntensity = builder.intensity;

        // Compute kernel density function (sd = 1/3rd of radius)
        mKernel = generateKernel(mRadius, mRadius / 3.0);

        // Generate color map
        setGradient(mGradient);

        // Set the data
        setWeightedData(mData);
    }

    /**
     * Changes the dataset the heatmap is portraying. Weighted.
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
     *
     * @param data Data set of points to use in the heatmap, as LatLngs.
     *             Note: Editing data without calling setWeightedData again will not update the data
     *             displayed on the map, but will impact calculation of max intensity values,
     *             as the collection you pass in is stored.
     *             Outside of changing the data, max intensity values are calculated only upon
     *             changing the radius.
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

        // Make the quad tree
        mBounds = getBounds(mData);

        mTree = new PointQuadTree<WeightedLatLng>(mBounds);

        // Add points to quad tree
        for (WeightedLatLng l : mData) {
            mTree.add(l);
        }

        // Calculate reasonable maximum intensity for color scale (user can also specify)
        // Get max intensities
        mMaxIntensity = getMaxIntensities(mRadius);
    }

    /**
     * Changes the dataset the heatmap is portraying. Unweighted.
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
     *
     * @param data Data set of points to use in the heatmap, as LatLngs.
     */
    public void setData(Collection<LatLng> data) {
        // Turn them into WeightedLatLngs and delegate.
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
        // Convert tile coordinates and zoom into Point/Bounds format
        // Know that at zoom level 0, there is one tile: (0, 0) (arbitrary width 512)
        // Each zoom level multiplies number of tiles by 2
        // Width of the world = WORLD_WIDTH = 1
        // x = [0, 1) corresponds to [-180, 180)

        // calculate width of one tile, given there are 2 ^ zoom tiles in that zoom level
        // In terms of world width units
        double tileWidth = WORLD_WIDTH / Math.pow(2, zoom);

        // how much padding to include in search
        // is to tileWidth as mRadius (padding in terms of pixels) is to TILE_DIM
        // In terms of world width units
        double padding = tileWidth * mRadius / TILE_DIM;

        // padded tile width
        // In terms of world width units
        double tileWidthPadded = tileWidth + 2 * padding;

        // padded bucket width - divided by number of buckets
        // In terms of world width units
        double bucketWidth = tileWidthPadded / (TILE_DIM + mRadius * 2);

        // Make bounds: minX, maxX, minY, maxY
        double minX = x * tileWidth - padding;
        double maxX = (x + 1) * tileWidth + padding;
        double minY = y * tileWidth - padding;
        double maxY = (y + 1) * tileWidth + padding;

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
            Bounds overlapBounds = new Bounds(minX + WORLD_WIDTH, WORLD_WIDTH, minY, maxY);
            xOffset = -WORLD_WIDTH;
            wrappedPoints = mTree.search(overlapBounds);
        } else if (maxX > WORLD_WIDTH) {
            // Cant both be true as then tile covers whole world
            // Need to consider "overflow" points
            // (512 to maxX) -> (0 to maxX-512) ie -512
            // subtract 512 from search bounds and add 512 to actual points
            Bounds overlapBounds = new Bounds(0, maxX - WORLD_WIDTH, minY, maxY);
            xOffset = WORLD_WIDTH;
            wrappedPoints = mTree.search(overlapBounds);
        }

        // Main tile bounds to search
        Bounds tileBounds = new Bounds(minX, maxX, minY, maxY);

        // If outside of *padded* quadtree bounds, return blank tile
        // This is comparing our bounds to the padded bounds of all points in the quadtree
        // ie tiles that don't touch the heatmap at all
        Bounds paddedBounds = new Bounds(mBounds.minX - padding, mBounds.maxX + padding,
                mBounds.minY - padding, mBounds.maxY + padding);
        if (!tileBounds.intersects(paddedBounds)) {
            return TileProvider.NO_TILE;
        }

        // Search for all points within tile bounds
        Collection<WeightedLatLng> points = mTree.search(tileBounds);

        // If no points, return blank tile
        if (points.isEmpty()) {
            return TileProvider.NO_TILE;
        }

        // Quantize points
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

        // Convolve it ("smoothen" it out)
        double[][] convolved = convolve(intensity, mKernel);

        // Color it into a bitmap
        Bitmap bitmap = colorize(convolved, mColorMap, mMaxIntensity[zoom]);

        // Convert bitmap to tile and return
        return convertBitmap(bitmap);
    }

    /**
     * Setter for gradient/color map.
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
     *
     * @param gradient Gradient to set
     */
    public void setGradient(Gradient gradient) {
        mGradient = gradient;
        mColorMap = gradient.generateColorMap(mOpacity);
    }

    /**
     * Setter for radius.
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
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
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
     *
     * @param opacity opacity to set
     */
    public void setOpacity(double opacity) {
        mOpacity = opacity;
        // need to recompute kernel color map
        setGradient(mGradient);
    }

    /**
     * Setter for max intensity
     * User should clear overlay's tile cache (using clearTileCache()) after calling this.
     *
     * @param intensity intensity to set
     */
    public void setMaxIntensity(double intensity) {
        mCustomMaxIntensity = intensity;
        // need to recompute data convolution
        setWeightedData(mData);
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

        // A custom max intensity has been specified by user
        // Set all zoom levels with intensity value
        if(mCustomMaxIntensity != 0.0) {
            for(int i = 0; i < MAX_ZOOM_LEVEL; i++) {
                maxIntensityArray[i] = mCustomMaxIntensity;
            }

            return maxIntensityArray;
        }

        // Calculate max intensity for each zoom level
        for (int i = DEFAULT_MIN_ZOOM; i < DEFAULT_MAX_ZOOM; i++) {
            // Each zoom level multiplies viewable size by 2
            maxIntensityArray[i] = getMaxValue(mData, mBounds, radius,
                    (int) (SCREEN_SIZE * Math.pow(2, i - 3)));
            if (i == DEFAULT_MIN_ZOOM) {
                for (int j = 0; j < i; j++) maxIntensityArray[j] = maxIntensityArray[i];
            }
        }
        for (int i = DEFAULT_MAX_ZOOM; i < MAX_ZOOM_LEVEL; i++) {
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

    /* Utility functions below */

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
        double maxX = first.getPoint().x;
        double minY = first.getPoint().y;
        double maxY = first.getPoint().y;

        while (iter.hasNext()) {
            WeightedLatLng l = iter.next();
            double x = l.getPoint().x;
            double y = l.getPoint().y;
            // Extend bounds if necessary
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        return new Bounds(minX, maxX, minY, maxY);
    }

    /**
     * Generates 1D Gaussian kernel density function, as a double array of size radius * 2  + 1
     * Normalised with central value of 1.
     *
     * @param radius radius of the kernel
     * @param sd     standard deviation of the Gaussian function
     * @return generated Gaussian kernel
     */
    static double[] generateKernel(int radius, double sd) {
        double[] kernel = new double[radius * 2 + 1];
        for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = (Math.exp(-i * i / (2 * sd * sd)));
        }
        return kernel;
    }

    /**
     * Applies a 2D Gaussian convolution to the input grid, returning a 2D grid cropped of padding.
     *
     * @param grid   Raw input grid to convolve: dimension (dim + 2 * radius) x (dim + 2 * radius)
     *               ie dim * dim with padding of size radius
     * @param kernel Pre-computed Gaussian kernel of size radius * 2 + 1
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
                    // Don't care about
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
        double colorMapScaling = (colorMap.length - 1) / max;
        // Dimension of the input grid (and dimension of output bitmap)
        int dim = grid.length;

        int i, j, index, col;
        double val;
        // Array of colors
        int colors[] = new int[dim * dim];
        for (i = 0; i < dim; i++) {
            for (j = 0; j < dim; j++) {
                // [x][y]
                // need to enter each row of x coordinates sequentially (x first)
                // -> [j][i]
                val = grid[j][i];
                index = i * dim + j;
                col = (int) (val * colorMapScaling);

                if (val != 0) {
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
        // Use a sparse array - use LongSparseArray just in case
        LongSparseArray<LongSparseArray<Double>> buckets = new LongSparseArray<LongSparseArray<Double>>();
        //double[][] buckets = new double[nBuckets][nBuckets];

        // Assign into buckets + find max value as we go along
        double x, y;
        double max = 0;
        for (WeightedLatLng l : points) {
            x = l.getPoint().x;
            y = l.getPoint().y;

            int xBucket = (int) ((x - minX) * scale);
            int yBucket = (int) ((y - minY) * scale);

            // Check if x bucket exists, if not make it
            LongSparseArray<Double> column = buckets.get(xBucket);
            if (column == null) {
                column = new LongSparseArray<Double>();
                buckets.put(xBucket, column);
            }
            // Check if there is already a y value there
            Double value = column.get(yBucket);
            if (value == null) {
                value = 0.0;
            }
            value += l.getIntensity();
            // Yes, do need to update it, despite it being a Double.
            column.put(yBucket, value);

            if (value > max) max = value;
        }

        return max;
    }
}
