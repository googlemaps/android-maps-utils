package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.quadtree.PointQuadTree;

public class HeatmapsDemoActivity extends BaseDemoActivity {

    /** where sydney is */
    private final LatLng SYDNEY = new LatLng(-33.865955, 151.195891);

    /** Quad tree of points*/
    private PointQuadTree mTree;

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 16));

        mTree = new PointQuadTree(1, 2, 1, 2);

        TileProvider heatmapTileProvider = new HeatmapTileProvider(mTree,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, HeatmapConstants.DEFAULT_HEATMAP_GRADIENT,
                HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        getMap().addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
    }

}
