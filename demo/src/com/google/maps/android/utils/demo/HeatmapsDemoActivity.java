package com.google.maps.android.utils.demo;

import android.util.Log;

import com.google.maps.android.heatmaps.HeatmapUtil;

import java.util.Arrays;

public class HeatmapsDemoActivity extends BaseDemoActivity {
    @Override
    protected void startDemo() {
        double[] kernel = HeatmapUtil.generateKernel(5, 1.5);
        Log.e("kernel", Arrays.toString(kernel));

        // test with radius 2
        double[][] grid = new double[5][5];
        grid[2][2] = 2;
        grid[2][1] = 1;
        grid[1][2] = 1;
        grid[2][3] = 1;
        grid[3][2] = 1;
        double[] testKernel = {0.5, 1, 0.5};
        double[][] convolved = HeatmapUtil.convolve(grid, testKernel);
        printGrid(convolved);
    }




    /**
     * Helper function for testing - print grid to Log.e
     * @param grid Grid to print
     */
    public static void printGrid(double[][] grid) {
        int i;
        for (i = 0; i < grid.length; i ++) {
            Log.e("grid"+i, Arrays.toString(grid[i]));
        }
    }
}
