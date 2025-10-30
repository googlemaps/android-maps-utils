/*
 * Copyright 2025 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
class PointQuadTree<T : PointQuadTree.Item> @JvmOverloads constructor(
    private val mBounds: Bounds,
    private val mDepth: Int = 0
) {
    interface Item {
        val point: Point
    }

    /**
     * The elements inside this quad, if any.
     */
    private var mItems: MutableSet<T>? = null

    /**
     * Child quads.
     */
    private var mChildren: MutableList<PointQuadTree<T>>? = null

    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) :
        this(Bounds(minX, maxX, minY, maxY))

    /**
     * Insert an item.
     */
    fun add(item: T) {
        val point = item.point
        if (this.mBounds.contains(point.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        if (this.mChildren != null) {
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    mChildren!![0].insert(x, y, item)
                } else { // top right
                    mChildren!![1].insert(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    mChildren!![2].insert(x, y, item)
                } else {
                    mChildren!![3].insert(x, y, item)
                }
            }
            return
        }
        if (mItems == null) {
            mItems = LinkedHashSet()
        }
        mItems!!.add(item)
        if (mItems!!.size > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split()
        }
    }

    /**
     * Split this quad.
     */
    private fun split() {
        mChildren = ArrayList(4)
        mChildren!!.add(PointQuadTree(Bounds(mBounds.minX, mBounds.midX, mBounds.minY, mBounds.midY), mDepth + 1))
        mChildren!!.add(PointQuadTree(Bounds(mBounds.midX, mBounds.maxX, mBounds.minY, mBounds.midY), mDepth + 1))
        mChildren!!.add(PointQuadTree(Bounds(mBounds.minX, mBounds.midX, mBounds.midY, mBounds.maxY), mDepth + 1))
        mChildren!!.add(PointQuadTree(Bounds(mBounds.midX, mBounds.maxX, mBounds.midY, mBounds.maxY), mDepth + 1))

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
    fun remove(item: T): Boolean {
        val point = item.point
        return if (this.mBounds.contains(point.x, point.y)) {
            remove(point.x, point.y, item)
        } else {
            false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        return if (this.mChildren != null) {
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    mChildren!![0].remove(x, y, item)
                } else { // top right
                    mChildren!![1].remove(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    mChildren!![2].remove(x, y, item)
                } else {
                    mChildren!![3].remove(x, y, item)
                }
            }
        } else {
            if (mItems == null) {
                false
            } else {
                mItems!!.remove(item)
            }
        }
    }

    /**
     * Removes all points from the quadTree
     */
    fun clear() {
        mChildren = null
        if (mItems != null) {
            mItems!!.clear()
        }
    }

    /**
     * Search for all items within a given bounds.
     */
    fun search(searchBounds: Bounds): Collection<T> {
        val results: MutableList<T> = ArrayList()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!mBounds.intersects(searchBounds)) {
            return
        }

        if (this.mChildren != null) {
            for (quad in mChildren!!) {
                quad.search(searchBounds, results)
            }
        } else if (mItems != null) {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mItems!!)
            } else {
                for (item in mItems!!) {
                    if (searchBounds.contains(item.point)) {
                        results.add(item)
                    }
                }
            }
        }
    }

    companion object {
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