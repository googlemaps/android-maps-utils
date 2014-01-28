package com.google.maps.android.kdtree;

import android.util.Log;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KdTree<T extends KdTree.Item> {
    public interface Item {
        public Point getPoint();
    }

    /**
     * For use in getBounds.
     * Sigma is used to ensure search is inclusive of upper bounds (eg if a point is on exactly the
     * upper bound, it should be returned)
     */
    static double sigma = 0.0000001;

    /**
     * The bounds of this quad.
     */
    private final Bounds mBounds;

    /**
     * The depth of this quad in the tree.
     */
    private final int mDepth;

    /**
     * Maximum number of elements to store in a quad before splitting.
     */
    private final static int MAX_ELEMENTS = 50;

    /**
     * The elements inside this quad, if any.
     */
    private T[] mItems;

    /**
     * Maximum depth.
     */
    private final static int MAX_DEPTH = 40;

    /**
     * Child quads.
     */
    private KdTree<T>[] mChildren = null;

    private static final Random r = new Random();

    public KdTree(T[] items) {
        mItems = items;
        mDepth = 0;
        if (items == null) {
            mBounds = null;
        } else {
            mBounds = getBounds(items);
            if (mItems.length > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
                split();
            }
        }
    }

    private KdTree(T[] items, int depth, Bounds bounds) {
        mItems = items;
        mDepth = depth;
        mBounds = bounds;
        if (mItems.length > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split();
        }
    }

    private void split() {
        Bounds lowBounds, highBounds;
        if (mDepth % 2 == 0) {
            selectX(mItems.length / 2, 0, mItems.length - 1);
            double boundary = (mItems[mItems.length / 2].getPoint().x + mItems[(mItems.length / 2) + 1].getPoint().x) / 2;
            lowBounds = new Bounds(mBounds.minX, boundary, mBounds.minY, mBounds.maxY);
            highBounds = new Bounds(mBounds.minX, boundary, mBounds.minY, mBounds.maxY);
        } else {
            selectY(mItems.length / 2, 0, mItems.length - 1);
            double boundary = (mItems[mItems.length / 2].getPoint().y + mItems[(mItems.length / 2) + 1].getPoint().y) / 2;
            lowBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary);
            highBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary);
        }
        // TODO what if multiple at same x / y value? How to do bounds?
        //TODO : copyOfRange uses API level 9 - change min in Android Manifest?
        mChildren = new KdTree[]{
                new KdTree(Arrays.copyOfRange(mItems, 0, mItems.length / 2), mDepth + 1, lowBounds),
                new KdTree(Arrays.copyOfRange(mItems, mItems.length / 2, mItems.length), mDepth + 1, highBounds)
        };
        mItems = null;
    }

    private void selectX(int i, int min, int max) {
        if (min < max) {
            int j = r.nextInt(max - min) + min;
            swapItems(max, j);
            double pivotVal = mItems[j].getPoint().x;
            int store = min;
            for (int k = min; k < max; k++) {
                if (mItems[k].getPoint().x <= pivotVal) {
                    swapItems(k, store);
                    store++;
                }
            }
            swapItems(store, max);
            if (store < i) {
                selectX(i - store - 1, store + 1, max);
            } else {
                selectX(i, min, store - 1);
            }
        }
    }

    private void selectY(int i, int min, int max) {
        if (min < max) {
            int j = r.nextInt(max - min) + min;
            swapItems(max, j);
            double pivotVal = mItems[j].getPoint().y;
            int store = min;
            for (int k = min; k < max; k++) {
                if (mItems[k].getPoint().y <= pivotVal) {
                    swapItems(k, store);
                    store++;
                }
            }
            swapItems(store, max);
            if (store < i) {
                selectY(i - store - 1, store + 1, max);
            } else {
                selectY(i, min, store - 1);
            }
        }
    }

    private void swapItems(int a, int b) {
        T temp = mItems[a];
        mItems[a] = mItems[b];
        mItems[b] = mItems[a];
    }

    /**
     * Search for all items within a given bounds.
     */
    public Collection<T> search(Bounds searchBounds) {
        final List<T> results = new ArrayList<T>();
        if (mBounds != null) {
            search(searchBounds, results);
        }
        return results;
    }

    private void search(Bounds searchBounds, Collection<T> results) {
        if (!mBounds.intersects(searchBounds)) {
            return;
        }

        if (this.mChildren != null) {
            for (KdTree<T> quad : mChildren) {
                quad.search(searchBounds, results);
            }
        } else if (mItems != null) {
            if (searchBounds.contains(mBounds)) {
                Collections.addAll(results, mItems);
            } else {
                for (T item : mItems) {
                    if (searchBounds.contains(item.getPoint())) {
                        results.add(item);
                    }
                }
            }
        }
    }

    /**
     * Helper function for quadtree creation
     *
     * @param points Collection of WeightedLatLng to calculate bounds for
     * @return Bounds that enclose the listed WeightedLatLng points
     */
    private Bounds getBounds(T[] points) {

        double minX = points[0].getPoint().x;
        double maxX = points[0].getPoint().x + sigma;
        double minY = points[0].getPoint().y;
        double maxY = points[0].getPoint().y + sigma;

        for (int i = 1; i < points.length; i++) {
            double x = points[i].getPoint().x;
            double y = points[i].getPoint().y;
            // Extend bounds if necessary
            if (x < minX) minX = x;
            if (x + sigma > maxX) maxX = x + sigma;
            if (y < minY) minY = y;
            if (y + sigma > maxY) maxY = y + sigma;
        }

        return new Bounds(minX, maxX, minY, maxY);
    }

}
