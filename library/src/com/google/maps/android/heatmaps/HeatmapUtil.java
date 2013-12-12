package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.graphics.Color;

import android.util.Log;

import java.util.Arrays;

/**
 * Utility functions for heatmaps.
 * Based off the javascript heatmaps code
 */
public class HeatmapUtil {

    /**
     * Generates 1D Gaussian kernel density function, as a double array of size radius * 2  + 1
     * Normalised with central value of 1.
     * @param radius radius of the kernel
     * @param sigma standard deviation of the Gaussian function
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
     * @param grid Raw input grid to convolve: dimension dim+2*radius x dim + 2*radius
     *             ie dim * dim with padding of size radius
     * @param kernel Pre-computed Gaussian kernel of size radius*2+1
     * @return the smoothened grid
     */
    public static double[][] convolve(double[][] grid, double[] kernel) {
        // Calculate radius size
        int radius = (int)Math.floor((double)kernel.length/2.0);
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
                    for(x2 = Math.max(lowerLimit, x - radius);
                            x2 < Math.min(upperLimit, x + radius) + 1; x2 ++) {
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
                    for(y2 = Math.max(lowerLimit, y - radius);
                            y2 < Math.min(upperLimit, y + radius) + 1; y2 ++) {
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
     * @param grid the input grid (assumed to be square)
     * @param colorMap color map (created by generateColorMap)
     * @param max Maximum intensity value: maps to 100$ on gradient
     * @return the colorized grid in Bitmap form, with same dimensions as grid
     */
    public static Bitmap colorize(double[][] grid, int[] colorMap, double max) {
        // Maximum color value
        //int maxColor = colorMap[colorMap.length - 1];
        // Multiplier to "scale" intensity values with, to map to appropriate color
        // TODO: is this change (-1 to length) ok? Reasoning: otherwise max will break it
        double colorMapScaling = (colorMap.length - 1) / max;
        // Dimension of the input grid (and dimension of output bitmap)
        int dim = grid.length;


        int i, j, index;
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

                if ((int)val != 0) {
                    colors[index] = colorMap[(int)(grid[j][i] * colorMapScaling)];
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

    /** Given a grid, find max val (for colorize, if full colour spectrum desired)
     * @param grid grid to find max for. Assumed to be square
     * @return max value in grid
     */
    public static double getMaxVal(double[][] grid) {
        int i, j;
        double max = 0;
        for (i = 0; i < grid.length; i++) {
            for (j = 0; j < grid.length; j++) {
                if (grid[i][j] > max) max = grid[i][j];
            }
        }
        return max;
    }

    /**
     * Generates the color map to use with a provided gradient.
     * @param gradient Array of colors (int format)
     * @param size Number of elements in the color map
     * @param opacity Overall opacity of entire image: every individual alpha value will be
     *                multiplied by this opacity.
     * @return the generated color map based on the gradient
     */
    public static int[] generateColorMap(int[] gradient, int size, double opacity) {
        // Convert gradient into parallel arrays
        int[] values = new int[gradient.length];
        int[] colors = new int[gradient.length];

        // Evenly space out gradient colors with a constant interval (interval = "space" between
        // colors given in the gradient)
        // With defaults, this is 1000/10 = 100
        int interval = (size - 1) / (gradient.length - 1);

        // Go through gradient and insert into values/colors
        int i;
        for(i = 0; i < gradient.length; i++) {
            values[i] = i * interval;
            colors[i] = gradient[i];
        }

        //Log.e("values", Arrays.toString(values));

        int[] colorMap = new int[size];
        // lowColorStop = closest color stop (value from gradient) below current position
        int lowColorStop = 0;
        for (i = 0; i < size; i++) {
            //Log.e("i", "i = "+i+" lCS = "+lowColorStop);
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
               colorMap[i] = interpolateColor(interval * lowColorStop, i,
                       interval * (lowColorStop + 1),
                       colors[lowColorStop], colors[lowColorStop + 1]);
            }
            // above highest color stop: use that
            else {
                colorMap[i] = colors[colors.length - 1];
            }
            // Deal with changing the opacity if required
            if (opacity != 1) {
                int c = colorMap[i];
                // TODO: make this better later?
                colorMap[i] = Color.argb((int)(Color.alpha(c) * opacity),
                        Color.red(c), Color.green(c), Color.blue(c));
            }
        }



        return colorMap;
    }



    /**
     * Helper function for creation of color map - interpolates between given colors
     * @param x1 First color "coordinate"
     * @param x2 Middle color "coordinate" - interpolating to this point
     * @param x3 Last color "coordinate"
     * @param color1 Color associated with x1
     * @param color2 Color associated with x3
     * @return Color associated with x2
     */
    private static int interpolateColor(double x1, double x2, double x3, int color1, int color2) {
        // no need to interpolate
        if (x1 == x3) return color1;
        // interpolate on R, G, B and A
        double ratio = (x2 - x1)/(double)(x3 - x1);

        // Interpolate using calculated ratio
        double red = (Color.red(color2) - Color.red(color1)) * ratio + Color.red(color1);
        double green = (Color.green(color2) - Color.green(color1)) * ratio + Color.green(color1);
        double blue = (Color.blue(color2) - Color.blue(color1)) * ratio + Color.blue(color1);
        double alpha = (Color.alpha(color2) - Color.alpha(color1)) * ratio + Color.alpha(color1);

        return Color.argb((int) alpha, (int) red, (int) green, (int) blue);
    }
}
