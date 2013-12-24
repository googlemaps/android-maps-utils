package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by irisu on 12/9/13.
 */
public class LinearQuadTree<T extends PointQuadTree.Item> implements PointQuadTree<T> {

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

    /**
     * Wrapper for Item that gives it a 'location' in the quadTree
     */
    private class Node implements Comparable<Node> {
        public int location;
        public T t;

        public Node(T item) {
            location = getLocation(item.getPoint());
            this.t = item;
        }

        public Node(int location) {
            this.location = location;
            this.t = null;
        }

        private int getLocation(Point p) {
            int location = 0;
            Bounds currBounds = mBounds;
            for (int order = DEPTH -1; order >= 0; order--) {
                if (p.y < currBounds.midY) {       // top
                    if (p.x < currBounds.midX) {   // left = 0
                        location += mQuadrant.TOP_LEFT.getValue() * Math.pow(BASE, order);
                        currBounds = new Bounds(currBounds.minX, currBounds.midX,
                                currBounds.minY, currBounds.midY);
                    } else {                       // right = 1
                        location += mQuadrant.TOP_RIGHT.getValue() * Math.pow(BASE, order);
                        currBounds = new Bounds(currBounds.midX, currBounds.maxX,
                                currBounds.minY, currBounds.midY);
                    }
                } else {                           // bottom
                    if (p.x < currBounds.midX) {   // left = 2
                        location += mQuadrant.BOTTOM_LEFT.getValue() * Math.pow(BASE, order);
                        currBounds = new Bounds(currBounds.minX, currBounds.midX,
                                currBounds.midY, currBounds.maxY);
                    } else {                       // right = 3
                        location += mQuadrant.BOTTOM_RIGHT.getValue() * Math.pow(BASE, order);
                        currBounds = new Bounds(currBounds.midX, currBounds.maxX,
                                currBounds.midY, currBounds.maxY);
                    }
                }
            }
            return location;
        }

        public int compareTo(Node node) {
            return this.location - node.location;
        }

    }

    /**
     * The bounds of this quad.
     */
    private final Bounds mBounds;

    /**
     * Lists all points in the tree
     */
    private List<Node> mPoints;

    /**
     * Defines the depth of the tree
     */
    public int DEPTH;

    /**
     * Base for the integers signifying the location of points in the quadTree
     */
    private final int BASE = 4;

    /**
     * True iff mPoints is sorted
     */
    private boolean mSorted = false;

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @param depth the maximum depth of the quadTree
     */
    public LinearQuadTree(double minX, double maxX, double minY, double maxY, int depth) {
        this(new Bounds(minX, maxX, minY, maxY), depth);
    }

    public LinearQuadTree(Bounds bounds, int depth) {
        if (depth > 25) depth = 25; // arbitrary maximum precision
        if (depth < 3)  depth = 3;  // arbitrary minimum precision
        mPoints = new ArrayList<Node>();
        mBounds = bounds;
        DEPTH = depth;
    }

    /**
     * Insert an item.
     */
    @Override
    public void add(T item) {
        mPoints.add(new Node(item));
        mSorted = false;
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    @Override
    public boolean remove(T item) {
        if (!mSorted) {
            Collections.sort(mPoints);
            mSorted = true;
        }
        Node node = new Node(item);
        int index = Collections.binarySearch(mPoints, node);
        while(index < mPoints.size() && index > 0
                && mPoints.get(index-1).location == node.location) {
            index--;
        }
        if (index >= 0) { // found
            while (index < mPoints.size() && mPoints.get(index).location == node.location
                    && mPoints.get(index).t != item) {
                index++;
            }
            if (mPoints.get(index).t == item) {
                mPoints.remove(index);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all points from the quadTree
     */
    @Override
    public void clear() {
        mPoints.clear();
    }

    /**
     * Search for all items within a given bounds.
     */
    @Override
    public Collection<T> search(Bounds searchBounds) {
        Collection<T> collection = new ArrayList<T>();
        if (mPoints.size() > 0) {
            if (!mSorted) {
                Collections.sort(mPoints);
                mSorted = true;
            }
            search(searchBounds, mBounds, 0, DEPTH, collection);
        }
        return collection;
    }

    private void search(Bounds searchBounds, Bounds currBounds,
                         int location, int depth, Collection<T> results) {
        if (searchBounds.contains(currBounds)) {
            // all the points in these bounds are in searchBounds
            Node node = new Node(location);
            int index = Collections.binarySearch(mPoints, node);
            if (index < 0) index = -1-index;
            while(index < mPoints.size() && index > 0
                    && mPoints.get(index-1).location == location) {
                index--;
            }
            for (; index < mPoints.size()
                    && mPoints.get(index).location < location + Math.pow(BASE, depth); index++) {
                results.add(mPoints.get(index).t);
            }
        } else if (searchBounds.intersects(currBounds) && depth > 0) {
            // some of the points in these bounds are in searchBounds, can be split into quads
            int multiplier = (int) Math.pow(BASE, depth-1);
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
            Node node = new Node(location);
            int index = Collections.binarySearch(mPoints, node);
            if (index < 0) index = -1-index;
            while(index < mPoints.size() && index > 0
                    && mPoints.get(index-1).location == location) {
                index--;
            }
            for (; index < mPoints.size()
                    && mPoints.get(index).location
                         < location + Math.pow(BASE, depth); index++) {
                if (searchBounds.contains(mPoints.get(index).t.getPoint().x,
                                              mPoints.get(index).t.getPoint().y)) {
                    results.add(mPoints.get(index).t);
                }
            }
        }
    }
}
