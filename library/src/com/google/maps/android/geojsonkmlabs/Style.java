package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by suvercha on 12/15/16.
 */

public class Style {

    private final static int HSV_VALUES = 3;

    private final static int HUE_VALUE = 0;

    private final static int INITIAL_SCALE = 1;

    private final MarkerOptions mMarkerOptions;

    private final PolylineOptions mPolylineOptions;

    private final PolygonOptions mPolygonOptions;

    private final HashMap<String, String> mBalloonOptions;

    private final HashSet<String> mStylesSet;

    private boolean mFill = true;

    private boolean mOutline = true;

    private String mIconUrl;

    private double mScale;

    private String mStyleId;

    private boolean mIconRandomColorMode;

    private boolean mLineRandomColorMode;

    private boolean mPolyRandomColorMode;

    private float mMarkerColor;

    private final static String[] GEOMETRY_TYPE = {"Polygon", "MultiPolygon", "GeometryCollection"};



    Style() {
        mStyleId = null;
        mMarkerOptions = new MarkerOptions();
        mPolylineOptions = new PolylineOptions();
        mPolygonOptions = new PolygonOptions();
        mBalloonOptions = new HashMap<>();
        mStylesSet = new HashSet<>();
        mScale = INITIAL_SCALE;
        mMarkerColor = 0;
        mIconRandomColorMode = false;
        mLineRandomColorMode = false;
        mPolyRandomColorMode = false;
    }

    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }





}
