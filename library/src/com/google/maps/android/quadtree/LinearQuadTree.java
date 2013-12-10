package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by irisu on 12/9/13.
 */
public class LinearQuadTree<T extends LinearQuadTree.Item> implements QuadTree<T> {

    private class Node implements Comparable<Node> {
        public final int base = 5;
        public int location;
        public T t;

        public Node(T item) {
            location = getLocation(item.getPoint());
            this.t = item;
        }

        private int getLocation(Point p) {
            int location = 0;
            Bounds currBounds = mBounds;
            for (int currPrecision = mPrecision-1; currPrecision >= 0; currPrecision--) {
                if (p.y < currBounds.midY) {       // top
                    if (p.x < currBounds.midX) {   // left = 0
                        currBounds = new Bounds(currBounds.minX, currBounds.midX,
                                                     currBounds.minY, currBounds.midY);
                    } else {                       // right = 1
                        location += 1 * base^currPrecision;
                        currBounds = new Bounds(currBounds.midX, currBounds.maxX,
                                                     currBounds.minY, currBounds.midY);
                    }
                } else {                           // bottom
                    if (p.x < currBounds.midX) {   // left = 2
                        location += 2 * base^currPrecision;
                        currBounds = new Bounds(currBounds.minX, currBounds.midX,
                                                     currBounds.midY, currBounds.maxY);
                    } else {                       // right = 3
                        location += 3 * base^currPrecision;
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

    private ArrayList<Node> mPoints;

    public int mPrecision;

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
        mPoints = new ArrayList<Node>();
        mBounds = bounds;
        mPrecision = precision;
    }

    @Override
    public void add(T item) {
        Node node = new Node(item);
        int index = Collections.binarySearch(mPoints, node);
        mPoints.add(index, node);
    }

    @Override
    public boolean remove(T item) {
        Node node = new Node(item);
        return mPoints.remove(node);
    }

    @Override
    public void clear() {
        mPoints.clear();
    }

    @Override
    public Collection<T> search(Bounds searchBounds) {
        //TODO
        return null;
    }
}
