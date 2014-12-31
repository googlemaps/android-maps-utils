package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/*
          .------.
         ( #-....'`\
          \ #      |
         _ )"====="| _
        (_`"======="`_)
         /`"""""""""`\
        |         o _o\
        |          (_>|
         \       '.___/--#
          '.     ;-._:'\
            )`===|  <)_/  __
      .---""`====`--'\__.'  `|
     /              ()\     /
     \___..--'         \_.-'
        |            () |
        ;               ;
         \           ()/
          \       '.  /
       _.'`\        `;
      (     `\        \_
       \    .-`\        `\
        \___)   `.______.'

    http://www.angelfire.com/ca/mathcool/christmas.html
 */

/**
 * Represents a series of coordinates in a placemark
 */
public class LineString implements Geometry{

    ArrayList<LatLng> mLineStringPoints;

    @Override
    public void createCoordinates(String text) {
        String[] lines = text.trim().split("(\\s+)");
        for (String points : lines) {
            String[] coordinate = points.split(",");
            LatLng latLng = Placemark.convertToLatLng(coordinate);
            setGeometry(latLng);
        }
    }

    public void setGeometry(Object geometry) {
        LatLng latLng = (LatLng) geometry;
        if (mLineStringPoints == null) mLineStringPoints = new ArrayList<LatLng>();
        mLineStringPoints.add(latLng);
    }

    @Override
    public Object getGeometry() {
        return mLineStringPoints;
    }
}
