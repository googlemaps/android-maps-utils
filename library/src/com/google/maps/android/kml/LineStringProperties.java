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
public class LineStringProperties {

    //TODO: Still some magic numbers floating around, change them to something like
    //MINIMUM_POINT_LIST_SIZE so when we go back to this code, we can remember what that integer is

    private static final int MINIMUM_POINT_LIST_SIZE = 1;

    private static final int LATITUDE = 0;

    private static final int LONGITUDE = 1;

    ArrayList<LatLng> mLineStringPoints;

    private static Logger LOGGER = Logger.getLogger("InfoLogging");
    /**
     * Takes in a XMLPullParser containing properties for a parser and saves relevant properties
     *
     * @param p XMLPullParser which reads input from designated source
     */
    public void pointProperties (XmlPullParser p) throws XmlPullParserException, IOException {
        int eventType = p.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && p.getName().equals("coordinates"))) {
            if (eventType == XmlPullParser.START_TAG)
                if (p.getName().equals("coordinates")) {
                    p.next();
                    addToLineStringPoints(p.getText());
                }
            eventType = p.next();
        }
    }

    /**
     * Converts text to latlng points to add to an array
     * @param pointsAsText a list of latlng points represented as a string
     */
    public void addToLineStringPoints(String pointsAsText) {
        String[] lines = pointsAsText.trim().split("(\\s+)");
        if (!isCoordinateEmpty(lines)) {
            for (String points : lines) {
                String[] point = points.split(",");
                if (isValidCoordinatePair(point) && isValidCoordinateType(point)) {
                    if (mLineStringPoints == null) mLineStringPoints = new ArrayList<LatLng>();
                    mLineStringPoints.add(convertToLatLng(point));
                } else {
                    LOGGER.info("Found coordinate in incorrect format. Not added to map");
                }
            }
        }
    }

    /**
     * Receives a pair of coordinate values which are separated by a comma and assigns a latLng
     * value to it. The method then returns this latLng value. The method ignores any integers
     * which are found after the second element of the array. If lon or lat values are greater
     * than their respective geographic boundaries, then it is set to the maximum possible value.
     *
     * @param coordinate An array of integer values, individually representing coordinates
     * @return lat     LatLng value or null if param is a null string or isn't an integer
     */
    public LatLng convertToLatLng(String[] coordinate) {
        Double latDouble = Double.parseDouble(coordinate[LONGITUDE]);
        Double lonDouble = Double.parseDouble(coordinate[LATITUDE]);
        return new LatLng(latDouble, lonDouble);
    }

    /**
     * Determines whether a list of strings given converts to the correct format
     * @param coordinate The tuple or pair of coordinates given
     * @return  true if it can be converted in a double, false otherwise
     */
    public boolean isValidCoordinateType (String[] coordinate) {
        try {
            Double.parseDouble(coordinate[LONGITUDE]);
            Double.parseDouble(coordinate[LATITUDE]);
            return true;
        } catch (NumberFormatException e) {
            LOGGER.info("Coordinate not in valid format!");
            return false;
        }
    }

    public boolean isCoordinateEmpty (String[] lines) {
        if (lines.length == 0) return true;
        return false;
    }


    /**
     * Determines whether a list of strings given have at least two points
     * @param coordinate A list of strings given
     * @return true if it contains a lat or long point, false otherwise
     */
    public boolean isValidCoordinatePair(String[] coordinate) {
        if (coordinate.length > 1) return true;
        return false;
    }


    /**
     * Determines whether the line string is valid in conjunction with the KML reference. At least
     * two coordinate tuples must be specified.
     *
     * @return true if there are two or more coordinate tuples, false otherwise
     */
    public boolean isValidLineString (ArrayList<LatLng> lineStringPoints) {
        if (lineStringPoints.size() > 1) return true;
        return false;
    }

    /**
     * Retrieves an Arraylist of LatLng points
     *
     * @return an Arraylist of LatLng points, null if not set.
     */
    public ArrayList<LatLng> getLineStringPoints() {
        return mLineStringPoints;
    }


}
