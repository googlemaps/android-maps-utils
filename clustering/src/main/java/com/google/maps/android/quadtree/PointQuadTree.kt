/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.quadtree

import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
public class PointQuadTree<T : PointQuadTree.Item>
@JvmOverloads
public constructor(
    private val mBounds: Bounds,
    private val mDepth: Int = 0,
) {
    public interface Item {
        public val point: Point
    }

    /**
     * The elements inside this quad, if any.
     */
    private var mItems: MutableSet<T>? = null

    /**
     * Child quads.
     */
    private var mChildren: MutableList<PointQuadTree<T>>? = null

    public constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) :
        this(Bounds(minX, maxX, minY, maxY))

    /**
     * Insert an item.
     */
    public fun add(item: T) {
        val point = item.point
        if (this.mBounds.contains(point.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        val children = this.mChildren
        if (children != null) {
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    children[0].insert(x, y, item)
                } else { // top right
                    children[1].insert(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    children[2].insert(x, y, item)
                } else {
                    children[3].insert(x, y, item)
                }
            }
            return
        }
        val items = mItems ?: LinkedHashSet<T>().also { mItems = it }
        items.add(item)
        if (items.size > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split()
        }
    }

    /**
     * Split this quad.
     */
    private fun split() {
        val children = ArrayList<PointQuadTree<T>>(4).apply {
            add(PointQuadTree(Bounds(mBounds.minX, mBounds.midX, mBounds.minY, mBounds.midY), mDepth + 1))
            add(PointQuadTree(Bounds(mBounds.midX, mBounds.maxX, mBounds.minY, mBounds.midY), mDepth + 1))
            add(PointQuadTree(Bounds(mBounds.minX, mBounds.midX, mBounds.midY, mBounds.maxY), mDepth + 1))
            add(PointQuadTree(Bounds(mBounds.midX, mBounds.maxX, mBounds.midY, mBounds.maxY), mDepth + 1))
        }
        mChildren = children

        val items = mItems
        mItems = null

        if (items != null) {
            for (item in items) {
                // re-insert items into child quads.
                insert(item.point.x, item.point.y, item)
            }
        }
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    public fun remove(item: T): Boolean {
        val point = item.point
        return if (this.mBounds.contains(point.x, point.y)) {
            remove(point.x, point.y, item)
        } else {
            false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        val children = this.mChildren
        return if (children != null) {
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    children[0].remove(x, y, item)
                } else { // top right
                    children[1].remove(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    children[2].remove(x, y, item)
                } else {
                    children[3].remove(x, y, item)
                }
            }
        } else {
            mItems?.remove(item) ?: false
        }
    }

    /**
     * Removes all points from the quadTree
     */
    public fun clear() {
        mChildren = null
        mItems?.clear()
    }

    /**
     * Search for all items within a given bounds.
     */
    public fun search(searchBounds: Bounds): Collection<T> {
        val results: MutableList<T> = ArrayList()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!mBounds.intersects(searchBounds)) {
            return
        }

        val children = this.mChildren
        val items = this.mItems
        when {
            children != null -> {
                for (quad in children) {
                    quad.search(searchBounds, results)
                }
            }

            items != null -> {
                if (searchBounds.contains(mBounds)) {
                    results.addAll(items)
                } else {
                    for (item in items) {
                        if (searchBounds.contains(item.point)) {
                            results.add(item)
                        }
                    }
                }
            }
        }
    }

    public companion object {
        /**
         * Maximum number of elements to store in a quad before splitting.
         */
        private const val MAX_ELEMENTS = 50

        /**
         * Maximum depth.
         */
        private const val MAX_DEPTH = 40
    }
}
