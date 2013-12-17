package com.google.maps.android.quadtree;

import android.util.Log;

import com.google.android.gms.internal.cu;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by irisu on 12/9/13.
 */
public class LinearQuadTree<T extends QuadTree.Item> implements QuadTree<T> {

    private enum mQuadrant {
        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3);

        final int numQuad;

        private mQuadrant(int i) {
            this.numQuad = i;
        }

        public int getValue(){
            return this.numQuad;
        }
    }

    private class PointLocationComparator implements Comparator<Item> {
        public int compare(Item lhs, Item rhs) {
            return getLocation(lhs.getPoint()) - getLocation(rhs.getPoint());
        }
    }

    private class TempItem implements QuadTree.Item {
        Point p;

        public TempItem(int location) {
            this.p = getMiddlePoint(location);
        }

        @Override
        public Point getPoint() {
            return p;
        }

        private Point getMiddlePoint(int location) {
            Bounds currBounds = mBounds;
            for (int order = mPrecision-1; order >= 0; order--) {
                int divisor = (int) Math.pow(mBase, order);
                if (location / divisor == mQuadrant.TOP_LEFT.getValue()) {
                    currBounds = currBounds.getTopLeft();
                } else if (location / divisor == mQuadrant.TOP_RIGHT.getValue()) {
                    currBounds = currBounds.getTopRight();
                } else if (location / divisor == mQuadrant.BOTTOM_LEFT.getValue()) {
                    currBounds = currBounds.getBottomLeft();
                } else if (location / divisor == mQuadrant.BOTTOM_RIGHT.getValue()) {
                    currBounds = currBounds.getBottomRight();
                }
            }
            return new Point(currBounds.midX,currBounds.midY);
        }
    }

    /**
     * The bounds of this quad.
     */
    private final Bounds mBounds;

    private ArrayList<T> mPoints;

    public int mPrecision;

    public final int mBase = 10; // TODO change to binary?

    private final Comparator<Item> comparator = new PointLocationComparator();

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public LinearQuadTree(double minX, double maxX, double minY, double maxY, int precision) {
        this(new Bounds(minX, maxX, minY, maxY), precision);
    }

    public LinearQuadTree(Bounds bounds, int precision) {
        if (precision > 25) precision = 25; // arbitrary maximum precision
        if (precision < 3)  precision = 3;  // arbitrary minimum precision
        mPoints = new ArrayList<T>();
        mBounds = bounds;
        mPrecision = precision;
    }

    @Override
    public void add(T item) {
        int size = mPoints.size();
        int index = Collections.binarySearch(mPoints, item, comparator);
        if (index < 0) {
            if (-index-1 < mPoints.size())
                mPoints.add(-index-1, item);
            else
                mPoints.add(item);
        } else {
            mPoints.add(index+1, item);
        }
    }

    @Override
    public boolean remove(T item) {
        int index = Collections.binarySearch(mPoints, item, comparator);
        while(index < mPoints.size() && index > 0
                && getLocation(mPoints.get(index-1).getPoint()) == getLocation(item.getPoint())) {
            index--;
        }
        if (index >= 0) { // found
            while (index < mPoints.size()
                    && getLocation(mPoints.get(index).getPoint()) == getLocation(item.getPoint())
                    && mPoints.get(index) != item) {
                index++;
            }
            if (mPoints.get(index) == item) {
                mPoints.remove(index);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        mPoints.clear();
    }

    @Override
    public Collection<T> search(Bounds searchBounds) {
        Collection<T> collection = new ArrayList<T>();
        if (mPoints.size() > 0) {
            search(searchBounds, mBounds, 0, mPrecision, collection);
        }
        return collection;
    }

    private void search(Bounds searchBounds, Bounds currBounds,
                         int location, int depth, Collection<T> results) {
        if (searchBounds.contains(currBounds)) {
            // all the points in these bounds are in searchBounds
            Item item = new TempItem(location);
            int index = Collections.binarySearch(mPoints, item, comparator);
            if (index < 0) index = -1-index;
            while(index < mPoints.size() && index > 0
                    && getLocation(mPoints.get(index-1).getPoint()) == location) {
                index--;
            }
            for (; index < mPoints.size()
                    && getLocation(mPoints.get(index).getPoint())
                         < location + Math.pow(mBase, depth); index++) {
                results.add(mPoints.get(index));
            }
        } else if (searchBounds.intersects(currBounds) && depth > 0) {
            // some of the points in these bounds are in searchBounds, can be split into quads
            int multiplier = (int) Math.pow(mBase, depth-1);
            search(searchBounds,
                    new Bounds(currBounds.minX, currBounds.midX, currBounds.minY, currBounds.midY),
                    location + mQuadrant.TOP_LEFT.getValue() * multiplier, depth - 1, results);
            search(searchBounds,
                    new Bounds(currBounds.midX, currBounds.maxX, currBounds.minY, currBounds.midY),
                    location + mQuadrant.TOP_RIGHT.getValue() * multiplier, depth - 1, results);
            search(searchBounds,
                    new Bounds(currBounds.minX, currBounds.midX, currBounds.midY, currBounds.maxY),
                    location + mQuadrant.BOTTOM_LEFT.getValue() * multiplier, depth - 1, results);
            search(searchBounds,
                    new Bounds(currBounds.midX, currBounds.maxX, currBounds.midY, currBounds.maxY),
                    location + mQuadrant.BOTTOM_RIGHT.getValue() * multiplier, depth - 1, results);

        } else if (searchBounds.intersects(currBounds)) {
            // some of the points in bounds are in searchBounds, quads can't be split
            Item item = new TempItem(location);
            int index = Collections.binarySearch(mPoints, item, comparator);
            if (index < 0) index = -1-index;
            while(index < mPoints.size() && index > 0
                    && getLocation(mPoints.get(index-1).getPoint()) == location) {
                index--;
            }
            for (; index < mPoints.size()
                    && getLocation(mPoints.get(index).getPoint())
                         < location + Math.pow(mBase, depth); index++) {
                if (searchBounds.contains(mPoints.get(index).getPoint().x,
                                              mPoints.get(index).getPoint().y)) {
                    results.add(mPoints.get(index));
                }
            }
        }
    }

    private int getLocation(Point p) {
        int location = 0;
        Bounds currBounds = mBounds;
        for (int order = mPrecision-1; order >= 0; order--) {
            if (p.y < currBounds.midY) {       // top
                if (p.x < currBounds.midX) {   // left = 0
                    location += mQuadrant.TOP_LEFT.getValue() * Math.pow(mBase, order);
                    currBounds = new Bounds(currBounds.minX, currBounds.midX,
                            currBounds.minY, currBounds.midY);
                } else {                       // right = 1
                    location += mQuadrant.TOP_RIGHT.getValue() * Math.pow(mBase, order);
                    currBounds = new Bounds(currBounds.midX, currBounds.maxX,
                            currBounds.minY, currBounds.midY);
                }
            } else {                           // bottom
                if (p.x < currBounds.midX) {   // left = 2
                    location += mQuadrant.BOTTOM_LEFT.getValue() * Math.pow(mBase, order);
                    currBounds = new Bounds(currBounds.minX, currBounds.midX,
                            currBounds.midY, currBounds.maxY);
                } else {                       // right = 3
                    location += mQuadrant.BOTTOM_RIGHT.getValue() * Math.pow(mBase, order);
                    currBounds = new Bounds(currBounds.midX, currBounds.maxX,
                            currBounds.midY, currBounds.maxY);
                }
            }
        }
        return location;
    }
}
