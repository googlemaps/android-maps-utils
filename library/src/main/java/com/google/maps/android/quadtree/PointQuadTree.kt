/*
 * Copyright 2013 Google Inc.
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
class PointQuadTree<T : PointQuadTree.Item> private constructor(
    /**
     * The bounds of this quad.
     */
    private val mBounds: Bounds,
    /**
     * The depth of this quad in the tree.
     */
    private val mDepth: Int
) {
    interface Item {
        val point: Point
    }

    /**
     * The elements inside this quad, if any.
     */
    private var mItems: MutableSet<T> = mutableSetOf()

    /**
     * Child quadrants.
     *
     * Note: to be backwards compatible, the SOUTH_EAST quadrant must be first and the NORTH_WEST
     * quadrant must be last.
     */
    private enum class QUADRANT(val index: Int) {
        SOUTH_EAST(0),
        SOUTH_WEST(1),
        NORTH_EAST(2),
        NORTH_WEST(3),
    }

    /**
     * Child quads.
     */
    private var mChildren: List<PointQuadTree<T>> = emptyList()

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) : this(
        Bounds(
            minX,
            maxX,
            minY,
            maxY
        )
    )

    constructor(bounds: Bounds) : this(bounds, 0)

    /**
     * Insert an item.
     */
    fun add(item: T) {
        val point = item.point
        if (mBounds.contains(point.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun applyToMatchingChild(
        point: Point,
        action: PointQuadTree<T>.(PointQuadTree<T>) -> Unit
    ) {
        matchingChild(point)?.apply {
            action(this)
        }
    }

    private fun matchingChild(point: Point): PointQuadTree<T>? = matchingChild(point.x, point.y)

    private fun matchingChild(x: Double, y: Double): PointQuadTree<T>? {
        return mChildren.firstOrNull {
            it.mBounds.contains(x, y)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        if (mChildren.isNotEmpty()) {
            applyToMatchingChild(item.point) { it.insert(x, y, item) }
        } else {
            mItems.add(item)
            if (mItems.size > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
                split()
            }
        }
    }

    /**
     * Split this quad.
     */
    private fun split() {
        require(mChildren.isEmpty()) { "Children already exist" }

        val childrenDepth = mDepth + 1

        mChildren = buildList {
            mBounds.quadrants().forEach {
                add(PointQuadTree(it, childrenDepth))
            }
        }

        val items = mItems.toList()
        mItems.clear()

        for (item in items) {
            // re-insert items into child quads.
            insert(item.point.x, item.point.y, item)
        }
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    fun remove(item: T): Boolean {
        return if (mBounds.contains(item.point.x, item.point.y)) {
            remove(item.point.x, item.point.y, item)
        } else {
            false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        return matchingChild(x, y)?.remove(x, y, item) ?: mItems.remove(item)
    }

    /**
     * Removes all points from the quadTree
     */
    fun clear() {
        mChildren = emptyList()
        mItems.clear()
    }

    /**
     * Search for all items within a given bounds.
     */
    fun search(searchBounds: Bounds): Collection<T> {
        return ArrayList<T>().apply {
            search(searchBounds, this)
        }
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!mBounds.intersects(searchBounds)) {
            return
        }

        if (mChildren.isNotEmpty()) {
            for (quad in mChildren) {
                quad.search(searchBounds, results)
            }
        } else {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mItems)
            } else {
                mItems.filterTo(results) { searchBounds.contains(it.point) }
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

private fun Bounds.quadrants(): List<Bounds> {
    /*
     * Note: to be backwards compatible, the southEast quadrant must be first and the northWest
     * quadrant must be last.
     */
    return buildList {
        add(southEast)
        add(southWest)
        add(northEast)
        add(northWest)
    }
}

private val Bounds.northWest: Bounds
    get() = Bounds(minX, midX, minY, midY)

private val Bounds.northEast: Bounds
    get() = Bounds(midX, maxX, minY, midY)

private val Bounds.southWest: Bounds
    get() = Bounds(minX, midX, midY, maxY)

private val Bounds.southEast: Bounds
    get() = Bounds(midX, maxX, midY, maxY)