package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;

import java.util.Collection;

/**
 * Created by irisu on 12/9/13.
 */
public class LinearQuadTree<T extends QuadTree.Item> implements QuadTree<T> {
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
