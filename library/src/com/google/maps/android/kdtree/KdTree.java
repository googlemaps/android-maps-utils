package com.google.maps.android.kdtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
    private ArrayList<T> mItems;

    /**
     * Maximum depth.
     */
    private final static int MAX_DEPTH = 40;

    /**
     * Child quads.
     */
    private KdTree<T>[] mChildren = null;

    private static final Random r = new Random();

    public KdTree(ArrayList<T> items) {
        mItems = items;
        mDepth = 0;
        if (items == null) {
            mBounds = null;
        } else {
            mBounds = getBounds(items);
            if (mItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
                split();
            }
        }
    }

    private KdTree(ArrayList<T> items, int depth, Bounds bounds) {
        mItems = items;
        mDepth = depth;
        mBounds = bounds;
        if (mItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split();
        }
    }

    private void split() {
        Bounds lowBounds, highBounds;
        if (mDepth % 2 == 0) {
            selectX(mItems.size() / 2, 0, mItems.size() - 1);
            double boundary = (mItems.get(mItems.size() / 2).getPoint().x + mItems.get((mItems.size() / 2) + 1).getPoint().x) / 2;
            lowBounds = new Bounds(mBounds.minX, boundary, mBounds.minY, mBounds.maxY);
            highBounds = new Bounds(mBounds.minX, boundary, mBounds.minY, mBounds.maxY);
        } else {
            selectY(mItems.size() / 2, 0, mItems.size() - 1);
            double boundary = (mItems.get(mItems.size() / 2).getPoint().y + mItems.get((mItems.size() / 2) + 1).getPoint().y) / 2;
            lowBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary);
            highBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary);
        }
        // TODO what if multiple at same x / y value? How to do bounds?
        mChildren = new KdTree[]{
                new KdTree(Arrays.copyOfRange(mItems, 0, mItems.size() / 2), mDepth + 1, lowBounds),
                new KdTree(Arrays.copyOfRange(mItems, mItems.size() / 2, mItems.size()), mDepth + 1, highBounds)
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
                results.addAll(mItems);
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
    private Bounds getBounds(Collection<T> points) {

        // Use an iterator, need to access any one point of the collection for starting bounds
        Iterator<T> iter = points.iterator();

        T first = iter.next();

        double minX = first.getPoint().x;
        double maxX = first.getPoint().x + sigma;
        double minY = first.getPoint().y;
        double maxY = first.getPoint().y + sigma;

        while (iter.hasNext()) {
            T l = iter.next();
            double x = l.getPoint().x;
            double y = l.getPoint().y;
            // Extend bounds if necessary
            if (x < minX) minX = x;
            if (x + sigma > maxX) maxX = x + sigma;
            if (y < minY) minY = y;
            if (y + sigma > maxY) maxY = y + sigma;
        }

        return new Bounds(minX, maxX, minY, maxY);
    }

}
