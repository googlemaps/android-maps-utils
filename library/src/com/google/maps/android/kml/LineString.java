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

    /**
     * Recieves text, with each line representing a tuple coordinates seperated by a comma
     * (longitude, latitude, altitude) This method converts these tuples into LatLng points,
     * and ignores the altitude component
     * @param text
     */
    public void createCoordinates(String text) {
        String[] lines = text.trim().split("(\\s+)");
        for (String points : lines) {
            String[] coordinate = points.split(",");
            LatLng latLng = Placemark.convertToLatLng(coordinate);
            setGeometry(latLng);
        }
    }

    /**
     * Creates a new ArrayList of LatLng points if it has not been created already and adds LatLng
     * points to this ArrayList
     * @param geometry  An object which represents a LatLng point to add to an ArrayList
     */
    public void setGeometry(Object geometry) {
        LatLng latLng = (LatLng) geometry;
        if (mLineStringPoints == null) mLineStringPoints = new ArrayList<LatLng>();
        mLineStringPoints.add(latLng);
    }

    /**
     * Returns an ArrayList of LatLng points which represent coordinates in a Polyline object
     * @return
     */
    public Object getGeometry() {
        return mLineStringPoints;
    }
}
