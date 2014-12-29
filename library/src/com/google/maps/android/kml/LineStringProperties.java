package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;


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
public class LineStringProperties implements Geometry{


    ArrayList<LatLng> mLineStringPoints;

    @Override
    public void parseGeometry(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("coordinates"))) {
            if (eventType == XmlPullParser.START_TAG)
                if (parser.getName().equals("coordinates")) {
                    parser.next();
                    setGeometry(parser.getText());
                }
            eventType = parser.next();
        }
    }

    @Override
    public void setGeometry(String text) {
        String[] lines = text.trim().split("(\\s+)");
        for (String points : lines) {
            String[] coordinate = points.split(",");
            if (mLineStringPoints == null) mLineStringPoints = new ArrayList<LatLng>();
            mLineStringPoints.add(convertToLatLng(coordinate));
        }
    }

    @Override
    public LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LONGITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LATITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

    @Override
    public Object getGeometry() {
        return mLineStringPoints;
    }
}
