package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by irisu on 12/9/13.
 */
public class LinearQuadTree<T extends LinearQuadTree.Item> implements QuadTree<T> {

    /**
     * The bounds of this quad.
     */
    private final Bounds mBounds;

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public LinearQuadTree(double minX, double maxX, double minY, double maxY) {
        this(new Bounds(minX, maxX, minY, maxY));
    }

    public LinearQuadTree(Bounds bounds) {
        mBounds = bounds;
    }

    @Override
    public void add(T item) {

    }

    @Override
    public boolean remove(T item) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Collection<T> search(Bounds searchBounds) {
        return null;
    }
}
