package com.google.maps.android.heatmaps;



import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapUtil;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.quadtree.PointQuadTree;

import java.io.ByteArrayOutputStream;


/**
 * Tile provider that creates heatmap tiles.
 */
public class HeatmapTileProvider implements TileProvider{
    /** Quad tree of all the points to display in the heatmap */
    private PointQuadTree mTree;

    /**  Heatmap point radius. */
    private int mRadius;

    /** Color map to use to color tiles */
    private int[] mColorMap;

    /** Kernel to use for convolution */
    private double[] mKernel;

    /** Opacity of the overall heatmap overlay (0...1) */
    private double mOpacity;

    // TODO: make radius, gradient, opacity etc changeable after creation?
    // TODO: have default that are optionally editable?
    public HeatmapTileProvider(PointQuadTree<PointQuadTree.Item> tree, int radius, int[] gradient,
                               double opacity) {
        // Assign function arguments to fields
        mTree = tree;
        mRadius = radius;
        mOpacity = opacity;

        // Compute kernel density function (sigma = 1/3rd of radius)
        mKernel = HeatmapUtil.generateKernel(mRadius, mRadius/3.0);

        // Generate color map from gradient
        // TODO: make size an option? is that needed?
        mColorMap = HeatmapUtil.generateColorMap(gradient, HeatmapConstants.HEATMAP_COLOR_MAP_SIZE,
                mOpacity);

    }

    public Tile getTile(int x, int y, int zoom) {
        // Get points from quadtree, bucket them into intensity array
        int tileDim = HeatmapConstants.HEATMAP_TILE_SIZE;
        double[][] intensity = new double[tileDim][tileDim];
        intensity[100][100] = 10;

        // Convolve it ("smoothen" it out)
        double[][] convolved = HeatmapUtil.convolve(intensity, mKernel);

        // Color it into a bitmap
        double max = HeatmapUtil.getMaxVal(convolved);
        Bitmap bitmap = HeatmapUtil.colorize(convolved, mColorMap, max);

        // Convert it into byte array (required for tile creation)
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        return new Tile(tileDim, tileDim, bitmapdata);
    }
}