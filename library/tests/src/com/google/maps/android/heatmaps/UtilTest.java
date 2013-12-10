package com.google.maps.android.heatmaps;

import com.google.maps.android.heatmaps.HeatmapsUtil;
import junit.framework.TestCase;


/**
 * Tests for HeatmapsUtil
 */
public class UtilTest extends TestCase {

    public void testGenerateKernel() {
        double[] testKernel = HeatmapsUtil.generateKernel(5, 5.0/3.0);
        assertTrue(testKernel[5] == 1);
    }

}
