package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.Collection;

/**
 * Created by irisu on 12/9/13.
 */
public interface QuadTree<T extends PointQuadTree.Item> {
    void add(T item);

    boolean remove(T item);

    void clear();

    Collection<T> search(Bounds searchBounds);

    public interface Item {
        Point getPoint();
    }
}
