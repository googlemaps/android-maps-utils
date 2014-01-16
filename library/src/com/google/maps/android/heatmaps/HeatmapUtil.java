package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.maps.android.geometry.Bounds;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utility functions for heatmaps.
 * Based off the javascript heatmaps code
 */
public class HeatmapUtil {

    /**
     * Default size of a color map for the heatmap
     */
    private static final int COLOR_MAP_SIZE = 1001;


    // Sigma is used to ensure search is inclusive of upper bounds (eg if a point
    // is on exactly the upper bound, it should be returned)
    static double sigma = 0.0000001;

    /**
     * Helper function for quadtree creation
     *
     * @param points Collection of WeightedLatLng to calculate bounds for
     * @return Bounds that enclose the listed WeightedLatLng points
     */
    public static Bounds getBounds(Collection<WeightedLatLng> points) {

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
    public static double[] generateKernel(int radius, double sigma) {
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
    public static double[][] convolve(double[][] grid, double[] kernel) {
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
        int x, y, x2;
        double val;
        for (x = 0; x < dimOld; x++) {
            for (y = 0; y < dimOld; y++) {
                // for each point (x, y)
                val = grid[x][y];
                // only bother if something there
                if (val != 0) {
                    // need to "apply" convolution from that point to every point in
                    // (max(lowerLimit, x - radius), y) to (min(upperLimit, x + radius), y)
                    for (x2 = Math.max(lowerLimit, x - radius);
                         x2 < Math.min(upperLimit, x + radius) + 1; x2++) {
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
        int y2;

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
                    for (y2 = Math.max(lowerLimit, y - radius);
                         y2 < Math.min(upperLimit, y + radius) + 1; y2++) {
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
    public static Bitmap colorize(double[][] grid, int[] colorMap, double max) {
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
    public static double getMaxValue(Collection<WeightedLatLng> points, Bounds bounds, int radius,
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
    public static int[] generateColorMap(int[] gradient, double opacity) {
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
     * @param color1 First color
     * @param color2 Second color
     * @param ratio Between 0 to 1. Fraction of the distance between color1 and color2
     * @return Color associated with x2
     */
    private static int interpolateColor(int color1, int color2, float ratio) {

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
