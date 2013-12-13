package com.google.maps.android.utils.demo;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.LatLngWrapper;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

public class HeatmapsDemoActivity extends BaseDemoActivity {

    /** where sydney is */
    private final LatLng SYDNEY = new LatLng(-33.865955, 151.195891);

    /** Quad tree of points*/
    private PointQuadTree mTree;

    private SphericalMercatorProjection mProjection =
            new SphericalMercatorProjection(HeatmapConstants.HEATMAP_TILE_SIZE);

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 16));

        Bounds treeBounds = new Bounds(230, 240, 150, 160);
        mTree = new PointQuadTree(treeBounds);

        // E/sydneyPoint﹕ Point{x=235.51707804444442, y=153.62117985807495}

        Point sydneyPoint = mProjection.toPoint(SYDNEY);
        Log.e("sydneyPoint", sydneyPoint.toString());

        Log.e("mercator", mProjection.toPoint(new LatLng(-89.999999, 0)).toString());
        Log.e("mercator", mProjection.toPoint(new LatLng(89.999999, 0)).toString());
        Log.e("mercator", mProjection.toPoint(new LatLng(-89.99999999, 0)).toString());
        Log.e("mercator", mProjection.toPoint(new LatLng(89.99999999, 0)).toString());

        LatLngWrapper sydneyWrapped = new LatLngWrapper(SYDNEY, 10, mProjection);
        mTree.add(sydneyWrapped);

        // Create a heatmap tile provider, that will generate the overlay tiles
        TileProvider heatmapTileProvider = new HeatmapTileProvider(mTree, treeBounds,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, HeatmapConstants.DEFAULT_HEATMAP_GRADIENT,
                HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        // Add the tile overlay to the map
        getMap().addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));

        //draw marker where the stuff is supposed to be
        getMap().addMarker(new MarkerOptions()
                .position(SYDNEY));
    }

}
