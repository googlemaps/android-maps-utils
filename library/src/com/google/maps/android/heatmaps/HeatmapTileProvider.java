package com.google.maps.android.heatmaps;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.quadtree.PointQuadTree;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


/**
 * Tile provider that creates heatmap tiles.
 */
public class HeatmapTileProvider implements TileProvider{
    /** Tile dimension */
    private static final int TILE_DIM = HeatmapConstants.HEATMAP_TILE_SIZE;

    /** Quad tree of all the points to display in the heatmap */
    private PointQuadTree mTree;

    /** Bounds of the quad tree */
    private Bounds mBounds;

    /**  Heatmap point radius. */
    private int mRadius;

    /** Gradient of the color map */
    private int[] mGradient;

    /** Color map to use to color tiles */
    private int[] mColorMap;

    /** Kernel to use for convolution */
    private double[] mKernel;

    /** Opacity of the overall heatmap overlay (0...1) */
    private double mOpacity;

    /** Maximum intensity estimate for heatmap */
    private double mMaxIntensity;

    /** Blank tile */
    private Tile mBlankTile;

    /**
     * Constuctor for the heatmap with all options.
     * @param tree The quadtree of heatmap points
     * @param bounds bounds of the quadtree
     * @param radius Radius of convolution to use
     * @param gradient Gradient to color heatmap with
     * @param opacity Opacity of the entire heatmap
     * @param maxIntensity Intensity value that maps to maximum gradient color
     */
    public HeatmapTileProvider(PointQuadTree<PointQuadTree.Item> tree, Bounds bounds,
                               int radius, int[] gradient, double opacity, double maxIntensity) {
        // Assign function arguments to fields
        mTree = tree;
        mBounds = bounds;
        mRadius = radius;
        mGradient = gradient;
        mOpacity = opacity;
        mMaxIntensity = maxIntensity;

        // Compute kernel density function (sigma = 1/3rd of radius)
        mKernel = HeatmapUtil.generateKernel(mRadius, mRadius/3.0);

        // Generate color map from gradient
        setColorMap(gradient);

        // Set up blank tile
        Bitmap blank = Bitmap.createBitmap(TILE_DIM, TILE_DIM, Bitmap.Config.ARGB_8888);
        mBlankTile = convertBitmap(blank);

    }

    public Tile getTile(int x, int y, int zoom) {
        long startTime = getTime();
        // Convert tile coordinates and zoom into Point/Bounds format
        // Know that at zoom level 0, there is one tile: (0, 0) (arbitrary width 256)
        // Each zoom level multiplies number of tiles by 2
        // Width of the world = 256 (Spherical Mercator Projection)
        // x ranges from 0 to 1 * world width

        //basically arbitrarily chosen scale (based off the demo)
        double worldWidth = HeatmapConstants.HEATMAP_TILE_SIZE;

        // calculate width of one tile, given there are 2 ^ zoom tiles in that zoom level
        double tileWidth = worldWidth / Math.pow(2, zoom);

        // how much padding to include in search
        double padding = tileWidth * mRadius/TILE_DIM;

        // padded tile width
        double tileWidthPadded = tileWidth + 2 * padding;

        // padded bucket width
        double bucketWidth = tileWidthPadded / (TILE_DIM + mRadius * 2);

        // Make bounds: minX, maxX, minY, maxY
        double minX = x * tileWidth - padding;
        double maxX = (x + 1) * tileWidth + padding;
        double minY = y * tileWidth - padding;
        double maxY = (y + 1) * tileWidth + padding;

        Bounds tileBounds = new Bounds(minX, maxX, minY, maxY);


        // If outside of quadtree bounds, return blank tile
        if (!tileBounds.intersects(mBounds)) {
            return mBlankTile;
        }

        // Search for all points within tile bounds
        ArrayList<LatLngWrapper> points = (ArrayList<LatLngWrapper>)mTree.search(tileBounds);

        // Bucket points into buckets
        double[][] intensity = new double[TILE_DIM + mRadius * 2][TILE_DIM + mRadius * 2];
        for(LatLngWrapper w: points) {
            Point p= w.getPoint();
            int bucketX = (int)((p.x - minX) / bucketWidth);
            int bucketY = (int)((p.y - minY) / bucketWidth);
            intensity[bucketX][bucketY] += w.getIntensity();
        }

        // Convolve it ("smoothen" it out)
        double[][] convolved = HeatmapUtil.convolve(intensity, mKernel);

        // Color it into a bitmap
        Bitmap bitmap = HeatmapUtil.colorize(convolved, mColorMap, mMaxIntensity);

        long endTime = getTime();

        Log.d("getTile", "Time: "+(endTime-startTime)+" Points: "+points.size()+" Zoom: "+zoom);

        return convertBitmap(bitmap);
    }

    /**
     * Setter for color map.
     * Important: tile overlay cache must be cleared after this for it to be effective
     * outside of initialisation
     * @param gradient Gradient to set
     */
    public void setColorMap(int[] gradient) {
        mGradient = gradient;
        mColorMap = HeatmapUtil.generateColorMap(gradient, HeatmapConstants.HEATMAP_COLOR_MAP_SIZE,
                mOpacity);
    }

    /**
     * Setter for radius
     * @param radius Radius to set
     */
    public void setRadius(int radius) {
        mRadius = radius;
        // need to recompute kernel
        mKernel = HeatmapUtil.generateKernel(mRadius, mRadius/3.0);
    }

    /**
     * Setter for opacity
     * @param opacity opacity to set
     */
    public void setOpacity(double opacity) {
        mOpacity = opacity;
        // need to recompute kernel color map
        setColorMap(mGradient);
    }

    public void setMaxIntensity(double intensity) {
        mMaxIntensity = intensity;
    }

    /**
     * helper function - convert a bitmap into a tile
     * @param bitmap bitmap to convert into a tile
     * @return the tile
     */
    private Tile convertBitmap(Bitmap bitmap) {
        // Convert it into byte array (required for tile creation)
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return new Tile(TILE_DIM, TILE_DIM, bitmapdata);
    }


    private long getTime() {
        return System.currentTimeMillis();
    }
}