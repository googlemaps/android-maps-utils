package com.google.maps.android.utils.demo;

import android.util.Log;

import com.google.maps.android.heatmaps.HeatmapsUtil;

import java.util.Arrays;

public class HeatmapsDemoActivity extends BaseDemoActivity {
    @Override
    protected void startDemo() {
        double[] kernel = HeatmapsUtil.generateKernel(5, 5.0/3.0);
        Log.e("kernel", Arrays.toString(kernel));

        // test with radius 2
        double[][] grid = new double[10][10];
        grid[5][5] = 1;
        grid[5][6] = 1;
        double[] testKernel = {0, 0.5, 1, 0.5, 0};
        double[][] convolved = HeatmapsUtil.convolve(grid, testKernel);
        HeatmapsUtil.printGrid(convolved);
    }
}
