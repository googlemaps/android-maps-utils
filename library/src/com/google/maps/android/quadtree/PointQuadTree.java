package com.google.maps.android.quadtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.Collection;

/**
 * Created by irisu on 12/9/13.
 */
public interface PointQuadTree<T extends PointQuadTree.Item> {

    /**
     * Insert an item.
     */
    void add(T item);

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    boolean remove(T item);

    /**
     * Removes all items from the quadTree
     */
    void clear();

    /**
     * Search for all items within a given bounds.
     */
    Collection<T> search(Bounds searchBounds);

    public interface Item {
        public Point getPoint();
    }
}
