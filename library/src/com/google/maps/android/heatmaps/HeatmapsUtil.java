package com.google.maps.android.heatmaps;

import android.util.Log;

import java.util.Arrays;

/**
 * Utility functions for heatmaps.
 */
public class HeatmapsUtil {

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
     * Helper function for testing - print grid to Log.i
     * @param grid Grid to print
     */
    public static void printGrid(double[][] grid) {
        int i;
        for (i = 0; i < grid.length; i ++) {
            Log.e("grid"+i, Arrays.toString(grid[i]));
        }
    }

}
