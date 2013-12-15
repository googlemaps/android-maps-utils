package com.google.maps.android.utils.demo;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.LatLngWrapper;
import com.google.maps.android.quadtree.PointQuadTree;

public class HeatmapsDemoActivity extends BaseDemoActivity {

    /** where sydney is */
    private final LatLng SYDNEY = new LatLng(-33.865955, 151.195891);

    /** Quad tree of points*/
    private PointQuadTree mTree;

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 16));

        // TODO: move a lot of this into a nicer HeatmapLayer class?
        Bounds treeBounds = new Bounds(230, 240, 150, 160);
        mTree = new PointQuadTree(treeBounds);

        // E/sydneyPoint﹕ Point{x=235.51707804444442, y=153.62117985807495}

        LatLngWrapper[] list = {
                new LatLngWrapper(SYDNEY, 20),
                new LatLngWrapper(new LatLng(-33.865955, 151.195991)),
                new LatLngWrapper(new LatLng(-33.865955, 151.196891))
        };


        for(LatLngWrapper l: list) {
            mTree.add(l);
        }

        // Create a heatmap tile provider, that will generate the overlay tiles
        TileProvider heatmapTileProvider = new HeatmapTileProvider(mTree, treeBounds,
                HeatmapConstants.DEFAULT_HEATMAP_RADIUS, HeatmapConstants.DEFAULT_HEATMAP_GRADIENT,
                HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        // Add the tile overlay to the map
        getMap().addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));

        //draw marker where the stuff is supposed to be
        //getMap().addMarker(new MarkerOptions()
                //.position(SYDNEY));
    }

}
