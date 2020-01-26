/*
 * Copyright 2008, 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.google.maps.android.SphericalUtil.*;
import static java.lang.Math.*;
import static com.google.maps.android.MathUtil.*;

public class PolyUtil {

    private PolyUtil() {
    }

    /**
     * Returns tan(latitude-at-lng3) on the great circle (lat1, lng1) to (lat2, lng2). lng1==0.
     * See http://williams.best.vwh.net/avform.htm .
     */
    private static double tanLatGC(double lat1, double lat2, double lng2, double lng3) {
        return (tan(lat1) * sin(lng2 - lng3) + tan(lat2) * sin(lng3)) / sin(lng2);
    }

    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to (lat2, lng2). lng1==0.
     */
    private static double mercatorLatRhumb(double lat1, double lat2, double lng2, double lng3) {
        return (mercator(lat1) * (lng2 - lng3) + mercator(lat2) * lng3) / lng2;
    }

    /**
     * Computes whether the vertical segment (lat3, lng3) to South Pole intersects the segment
     * (lat1, lng1) to (lat2, lng2).
     * Longitudes are offset by -lng1; the implicit lng1 becomes 0.
     */
    private static boolean intersects(double lat1, double lat2, double lng2,
                                      double lat3, double lng3, boolean geodesic) {
        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false;
        }
        // Point is South Pole.
        if (lat3 <= -PI / 2) {
            return false;
        }
        // Any segment end is a pole.
        if (lat1 <= -PI / 2 || lat2 <= -PI / 2 || lat1 >= PI / 2 || lat2 >= PI / 2) {
            return false;
        }
        if (lng2 <= -PI) {
            return false;
        }
        double linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2;
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false;
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true;
        }
        // North Pole.
        if (lat3 >= PI / 2) {
            return true;
        }
        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to lng3.
        // Compare through a strictly-increasing function (tan() or mercator()) as convenient.
        return geodesic ?
                tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3) :
                mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3);
    }

    public static boolean containsLocation(LatLng point, List<LatLng> polygon, boolean geodesic) {
        return containsLocation(point.latitude, point.longitude, polygon, geodesic);
    }

    /**
     * Computes whether the given point lies inside the specified polygon.
     * The polygon is always considered closed, regardless of whether the last point equals
     * the first or not.
     * Inside is defined as not containing the South Pole -- the South Pole is always outside.
     * The polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     */
    public static boolean containsLocation(double latitude, double longitude, List<LatLng> polygon, boolean geodesic) {
        final int size = polygon.size();
        if (size == 0) {
            return false;
        }
        double lat3 = toRadians(latitude);
        double lng3 = toRadians(longitude);
        LatLng prev = polygon.get(size - 1);
        double lat1 = toRadians(prev.latitude);
        double lng1 = toRadians(prev.longitude);
        int nIntersect = 0;
        for (LatLng point2 : polygon) {
            double dLng3 = wrap(lng3 - lng1, -PI, PI);
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0) {
                return true;
            }
            double lat2 = toRadians(point2.latitude);
            double lng2 = toRadians(point2.longitude);
            // Offset longitudes by -lng1.
            if (intersects(lat1, lat2, wrap(lng2 - lng1, -PI, PI), lat3, dLng3, geodesic)) {
                ++nIntersect;
            }
            lat1 = lat2;
            lng1 = lng2;
        }
        return (nIntersect & 1) != 0;
    }

    private static final double DEFAULT_TOLERANCE = 0.1;  // meters.

    /**
     * Computes whether the given point lies on or near the edge of a polygon, within a specified
     * tolerance in meters. The polygon edge is composed of great circle segments if geodesic
     * is true, and of Rhumb segments otherwise. The polygon edge is implicitly closed -- the
     * closing segment between the first point and the last point is included.
     */
    public static boolean isLocationOnEdge(LatLng point, List<LatLng> polygon, boolean geodesic,
                                           double tolerance) {
        return isLocationOnEdgeOrPath(point, polygon, true, geodesic, tolerance);
    }

    /**
     * Same as {@link #isLocationOnEdge(LatLng, List, boolean, double)}
     * with a default tolerance of 0.1 meters.
     */
    public static boolean isLocationOnEdge(LatLng point, List<LatLng> polygon, boolean geodesic) {
        return isLocationOnEdge(point, polygon, geodesic, DEFAULT_TOLERANCE);
    }

    /**
     * Computes whether the given point lies on or near a polyline, within a specified
     * tolerance in meters. The polyline is composed of great circle segments if geodesic
     * is true, and of Rhumb segments otherwise. The polyline is not closed -- the closing
     * segment between the first point and the last point is not included.
     */
    public static boolean isLocationOnPath(LatLng point, List<LatLng> polyline,
                                           boolean geodesic, double tolerance) {
        return isLocationOnEdgeOrPath(point, polyline, false, geodesic, tolerance);
    }

    /**
     * Same as {@link #isLocationOnPath(LatLng, List, boolean, double)}
     * <p>
     * with a default tolerance of 0.1 meters.
     */
    public static boolean isLocationOnPath(LatLng point, List<LatLng> polyline,
                                           boolean geodesic) {
        return isLocationOnPath(point, polyline, geodesic, DEFAULT_TOLERANCE);
    }

    private static boolean isLocationOnEdgeOrPath(LatLng point, List<LatLng> poly, boolean closed,
                                                  boolean geodesic, double toleranceEarth) {
        int idx = locationIndexOnEdgeOrPath(point, poly, closed, geodesic, toleranceEarth);

        return (idx >= 0);
    }

    /**
     * Computes whether (and where) a given point lies on or near a polyline, within a specified tolerance.
     * The polyline is not closed -- the closing segment between the first point and the last point is not included.
     *
     * @param point     our needle
     * @param poly      our haystack
     * @param geodesic  the polyline is composed of great circle segments if geodesic
     *                  is true, and of Rhumb segments otherwise
     * @param tolerance tolerance (in meters)
     * @return -1 if point does not lie on or near the polyline.
     * 0 if point is between poly[0] and poly[1] (inclusive),
     * 1 if between poly[1] and poly[2],
     * ...,
     * poly.size()-2 if between poly[poly.size() - 2] and poly[poly.size() - 1]
     */
    public static int locationIndexOnPath(LatLng point, List<LatLng> poly,
                                          boolean geodesic, double tolerance) {
        return locationIndexOnEdgeOrPath(point, poly, false, geodesic, tolerance);
    }

    /**
     * Same as {@link #locationIndexOnPath(LatLng, List, boolean, double)}
     * <p>
     * with a default tolerance of 0.1 meters.
     */
    public static int locationIndexOnPath(LatLng point, List<LatLng> polyline,
                                          boolean geodesic) {
        return locationIndexOnPath(point, polyline, geodesic, DEFAULT_TOLERANCE);
    }

    /**
     * Computes whether (and where) a given point lies on or near a polyline, within a specified tolerance.
     * If closed, the closing segment between the last and first points of the polyline is not considered.
     *
     * @param point          our needle
     * @param poly           our haystack
     * @param closed         whether the polyline should be considered closed by a segment connecting the last point back to the first one
     * @param geodesic       the polyline is composed of great circle segments if geodesic
     *                       is true, and of Rhumb segments otherwise
     * @param toleranceEarth tolerance (in meters)
     * @return -1 if point does not lie on or near the polyline.
     * 0 if point is between poly[0] and poly[1] (inclusive),
     * 1 if between poly[1] and poly[2],
     * ...,
     * poly.size()-2 if between poly[poly.size() - 2] and poly[poly.size() - 1]
     */
    public static int locationIndexOnEdgeOrPath(LatLng point, List<LatLng> poly, boolean closed,
                                                boolean geodesic, double toleranceEarth) {
        int size = poly.size();
        if (size == 0) {
            return -1;
        }
        double tolerance = toleranceEarth / EARTH_RADIUS;
        double havTolerance = hav(tolerance);
        double lat3 = toRadians(point.latitude);
        double lng3 = toRadians(point.longitude);
        LatLng prev = poly.get(closed ? size - 1 : 0);
        double lat1 = toRadians(prev.latitude);
        double lng1 = toRadians(prev.longitude);
        int idx = 0;
        if (geodesic) {
            for (LatLng point2 : poly) {
                double lat2 = toRadians(point2.latitude);
                double lng2 = toRadians(point2.longitude);
                if (isOnSegmentGC(lat1, lng1, lat2, lng2, lat3, lng3, havTolerance)) {
                    return Math.max(0, idx - 1);
                }
                lat1 = lat2;
                lng1 = lng2;
                idx++;
            }
        } else {
            // We project the points to mercator space, where the Rhumb segment is a straight line,
            // and compute the geodesic distance between point3 and the closest point on the
            // segment. This method is an approximation, because it uses "closest" in mercator
            // space which is not "closest" on the sphere -- but the error is small because
            // "tolerance" is small.
            double minAcceptable = lat3 - tolerance;
            double maxAcceptable = lat3 + tolerance;
            double y1 = mercator(lat1);
            double y3 = mercator(lat3);
            double[] xTry = new double[3];
            for (LatLng point2 : poly) {
                double lat2 = toRadians(point2.latitude);
                double y2 = mercator(lat2);
                double lng2 = toRadians(point2.longitude);
                if (max(lat1, lat2) >= minAcceptable && min(lat1, lat2) <= maxAcceptable) {
                    // We offset longitudes by -lng1; the implicit x1 is 0.
                    double x2 = wrap(lng2 - lng1, -PI, PI);
                    double x3Base = wrap(lng3 - lng1, -PI, PI);
                    xTry[0] = x3Base;
                    // Also explore wrapping of x3Base around the world in both directions.
                    xTry[1] = x3Base + 2 * PI;
                    xTry[2] = x3Base - 2 * PI;
                    for (double x3 : xTry) {
                        double dy = y2 - y1;
                        double len2 = x2 * x2 + dy * dy;
                        double t = len2 <= 0 ? 0 : clamp((x3 * x2 + (y3 - y1) * dy) / len2, 0, 1);
                        double xClosest = t * x2;
                        double yClosest = y1 + t * dy;
                        double latClosest = inverseMercator(yClosest);
                        double havDist = havDistance(lat3, latClosest, x3 - xClosest);
                        if (havDist < havTolerance) {
                            return Math.max(0, idx - 1);
                        }
                    }
                }
                lat1 = lat2;
                lng1 = lng2;
                y1 = y2;
                idx++;
            }
        }
        return -1;
    }

    /**
     * Returns sin(initial bearing from (lat1,lng1) to (lat3,lng3) minus initial bearing
     * from (lat1, lng1) to (lat2,lng2)).
     */
    private static double sinDeltaBearing(double lat1, double lng1, double lat2, double lng2,
                                          double lat3, double lng3) {
        double sinLat1 = sin(lat1);
        double cosLat2 = cos(lat2);
        double cosLat3 = cos(lat3);
        double lat31 = lat3 - lat1;
        double lng31 = lng3 - lng1;
        double lat21 = lat2 - lat1;
        double lng21 = lng2 - lng1;
        double a = sin(lng31) * cosLat3;
        double c = sin(lng21) * cosLat2;
        double b = sin(lat31) + 2 * sinLat1 * cosLat3 * hav(lng31);
        double d = sin(lat21) + 2 * sinLat1 * cosLat2 * hav(lng21);
        double denom = (a * a + b * b) * (c * c + d * d);
        return denom <= 0 ? 1 : (a * d - b * c) / sqrt(denom);
    }

    private static boolean isOnSegmentGC(double lat1, double lng1, double lat2, double lng2,
                                         double lat3, double lng3, double havTolerance) {
        double havDist13 = havDistance(lat1, lat3, lng1 - lng3);
        if (havDist13 <= havTolerance) {
            return true;
        }
        double havDist23 = havDistance(lat2, lat3, lng2 - lng3);
        if (havDist23 <= havTolerance) {
            return true;
        }
        double sinBearing = sinDeltaBearing(lat1, lng1, lat2, lng2, lat3, lng3);
        double sinDist13 = sinFromHav(havDist13);
        double havCrossTrack = havFromSin(sinDist13 * sinBearing);
        if (havCrossTrack > havTolerance) {
            return false;
        }
        double havDist12 = havDistance(lat1, lat2, lng1 - lng2);
        double term = havDist12 + havCrossTrack * (1 - 2 * havDist12);
        if (havDist13 > term || havDist23 > term) {
            return false;
        }
        if (havDist12 < 0.74) {
            return true;
        }
        double cosCrossTrack = 1 - 2 * havCrossTrack;
        double havAlongTrack13 = (havDist13 - havCrossTrack) / cosCrossTrack;
        double havAlongTrack23 = (havDist23 - havCrossTrack) / cosCrossTrack;
        double sinSumAlongTrack = sinSumFromHav(havAlongTrack13, havAlongTrack23);
        return sinSumAlongTrack > 0;  // Compare with half-circle == PI using sign of sin().
    }

    /**
     * Simplifies the given poly (polyline or polygon) using the Douglas-Peucker decimation
     * algorithm.  Increasing the tolerance will result in fewer points in the simplified polyline
     * or polygon.
     * <p>
     * When the providing a polygon as input, the first and last point of the list MUST have the
     * same latitude and longitude (i.e., the polygon must be closed).  If the input polygon is not
     * closed, the resulting polygon may not be fully simplified.
     * <p>
     * The time complexity of Douglas-Peucker is O(n^2), so take care that you do not call this
     * algorithm too frequently in your code.
     *
     * @param poly      polyline or polygon to be simplified.  Polygon should be closed (i.e.,
     *                  first and last points should have the same latitude and longitude).
     * @param tolerance in meters.  Increasing the tolerance will result in fewer points in the
     *                  simplified poly.
     * @return a simplified poly produced by the Douglas-Peucker algorithm
     */
    public static List<LatLng> simplify(List<LatLng> poly, double tolerance) {
        final int n = poly.size();
        if (n < 1) {
            throw new IllegalArgumentException("Polyline must have at least 1 point");
        }
        if (tolerance <= 0) {
            throw new IllegalArgumentException("Tolerance must be greater than zero");
        }

        boolean closedPolygon = isClosedPolygon(poly);
        LatLng lastPoint = null;

        // Check if the provided poly is a closed polygon
        if (closedPolygon) {
            // Add a small offset to the last point for Douglas-Peucker on polygons (see #201)
            final double OFFSET = 0.00000000001;
            lastPoint = poly.get(poly.size() - 1);
            // LatLng.latitude and .longitude are immutable, so replace the last point
            poly.remove(poly.size() - 1);
            poly.add(new LatLng(lastPoint.latitude + OFFSET, lastPoint.longitude + OFFSET));
        }

        int idx;
        int maxIdx = 0;
        Stack<int[]> stack = new Stack<>();
        double[] dists = new double[n];
        dists[0] = 1;
        dists[n - 1] = 1;
        double maxDist;
        double dist = 0.0;
        int[] current;

        if (n > 2) {
            int[] stackVal = new int[]{0, (n - 1)};
            stack.push(stackVal);
            while (stack.size() > 0) {
                current = stack.pop();
                maxDist = 0;
                for (idx = current[0] + 1; idx < current[1]; ++idx) {
                    dist = distanceToLine(poly.get(idx), poly.get(current[0]),
                            poly.get(current[1]));
                    if (dist > maxDist) {
                        maxDist = dist;
                        maxIdx = idx;
                    }
                }
                if (maxDist > tolerance) {
                    dists[maxIdx] = maxDist;
                    int[] stackValCurMax = {current[0], maxIdx};
                    stack.push(stackValCurMax);
                    int[] stackValMaxCur = {maxIdx, current[1]};
                    stack.push(stackValMaxCur);
                }
            }
        }

        if (closedPolygon) {
            // Replace last point w/ offset with the original last point to re-close the polygon
            poly.remove(poly.size() - 1);
            poly.add(lastPoint);
        }

        // Generate the simplified line
        idx = 0;
        ArrayList<LatLng> simplifiedLine = new ArrayList<>();
        for (LatLng l : poly) {
            if (dists[idx] != 0) {
                simplifiedLine.add(l);
            }
            idx++;
        }

        return simplifiedLine;
    }

    /**
     * Returns true if the provided list of points is a closed polygon (i.e., the first and last
     * points are the same), and false if it is not
     *
     * @param poly polyline or polygon
     * @return true if the provided list of points is a closed polygon (i.e., the first and last
     * points are the same), and false if it is not
     */
    public static boolean isClosedPolygon(List<LatLng> poly) {
        LatLng firstPoint = poly.get(0);
        LatLng lastPoint = poly.get(poly.size() - 1);
        return firstPoint.equals(lastPoint);
    }

    /**
     * Computes the distance on the sphere between the point p and the line segment start to end.
     *
     * @param p     the point to be measured
     * @param start the beginning of the line segment
     * @param end   the end of the line segment
     * @return the distance in meters (assuming spherical earth)
     */
    public static double distanceToLine(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return computeDistanceBetween(end, p);
        }

        final double s0lat = toRadians(p.latitude);
        final double s0lng = toRadians(p.longitude);
        final double s1lat = toRadians(start.latitude);
        final double s1lng = toRadians(start.longitude);
        final double s2lat = toRadians(end.latitude);
        final double s2lng = toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return computeDistanceBetween(p, start);
        }
        if (u >= 1) {
            return computeDistanceBetween(p, end);
        }
        LatLng su = new LatLng(start.latitude + u * (end.latitude - start.latitude), start.longitude + u * (end.longitude - start.longitude));
        return computeDistanceBetween(p, su);
    }

    /**
     * Decodes an encoded path string into a sequence of LatLngs.
     */
    public static List<LatLng> decode(final String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        final List<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    public static String encode(final List<LatLng> path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuffer result = new StringBuffer();

        for (final LatLng point : path) {
            long lat = Math.round(point.latitude * 1e5);
            long lng = Math.round(point.longitude * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    private static void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }
}
