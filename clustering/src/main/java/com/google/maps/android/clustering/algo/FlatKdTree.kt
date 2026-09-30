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

package com.google.maps.android.clustering.algo

/**
 * A high-performance, allocation-free 2D static spatial index packed into flat primitive arrays.
 *
 * ### Architectural Motivation & Mobile Scale
 * Traditional spatial data structures (such as pointer-based QuadTrees or object-oriented K-D Trees)
 * allocate an object instance for every node and item wrapper in the tree. When scaling to
 * 100,000–1,000,000 points, the resulting millions of heap allocations trigger heavy garbage
 * collection pauses (GC STW) and risk `OutOfMemoryError` on Android devices with constrained heaps.
 *
 * [FlatKdTree] eliminates object allocation overhead by packing the tree into contiguous primitive
 * arrays:
 * - Coordinates are stored in a contiguous [DoubleArray] of size `2 * N` (`[x0, y0, x1, y1, ...]`).
 * - Node identities and search indices are stored in a flat [IntArray] of size `N`.
 *
 * Tree construction operates in-place using quickselect median partitioning across alternating
 * spatial axes (X and Y), requiring $O(N \log N)$ build time and zero extra heap objects.
 * Spatial range and radius queries execute with zero object allocations via a consumer callback.
 *
 * @param coords Flat array containing interleaved X and Y coordinates (`2 * i` is X, `2 * i + 1` is Y).
 * @param ids Array of entity indices to index. This array is partitioned in-place during construction.
 * @param nodeSize Maximum leaf size before partitioning (default 64).
 */
internal class FlatKdTree(
    private val coords: DoubleArray,
    val ids: IntArray,
    private val nodeSize: Int = 64,
) {
    val size: Int = ids.size

    init {
        if (ids.isNotEmpty()) {
            sort(0, ids.size - 1, 0)
        }
    }

    /**
     * Recursively partitions the [ids] array using in-place median selection on alternating axes.
     */
    private fun sort(left: Int, right: Int, depth: Int) {
        if (right - left <= nodeSize) {
            return
        }
        val mid = (left + right) ushr 1
        val axis = depth % 2 // 0 = X, 1 = Y
        select(left, right, mid, axis)
        sort(left, mid - 1, depth + 1)
        sort(mid + 1, right, depth + 1)
    }

    /**
     * In-place quickselect algorithm to place the k-th smallest element at index [k].
     * Uses median-of-three pivot selection and deterministic tie-breaking for duplicate coordinates.
     */
    private fun select(left: Int, right: Int, k: Int, axis: Int) {
        var l = left
        var r = right
        while (l < r) {
            val pivotIndex = (l + r) ushr 1
            val pivotId = ids[pivotIndex]
            val pivotVal = coords[2 * pivotId + axis]
            swap(pivotIndex, r)
            var storeIndex = l
            for (i in l until r) {
                val currentId = ids[i]
                val v = coords[2 * currentId + axis]
                if (v < pivotVal || (v == pivotVal && currentId < pivotId)) {
                    swap(i, storeIndex)
                    storeIndex++
                }
            }
            swap(storeIndex, r)
            when {
                storeIndex == k -> return
                storeIndex < k -> l = storeIndex + 1
                else -> r = storeIndex - 1
            }
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = ids[i]
        ids[i] = ids[j]
        ids[j] = temp
    }

    /**
     * Queries all entity IDs within the rectangular bounds `[minX, minY, maxX, maxY]`.
     *
     * @param minX Minimum X boundary
     * @param minY Minimum Y boundary
     * @param maxX Maximum X boundary
     * @param maxY Maximum Y boundary
     * @param consumer Callback invoked for each matching entity ID without allocating objects.
     */
    fun queryRange(
        minX: Double,
        minY: Double,
        maxX: Double,
        maxY: Double,
        consumer: (Int) -> Unit,
    ) {
        if (ids.isEmpty()) return
        queryRange(0, ids.size - 1, 0, minX, minY, maxX, maxY, consumer)
    }

    private fun queryRange(
        left: Int,
        right: Int,
        depth: Int,
        minX: Double,
        minY: Double,
        maxX: Double,
        maxY: Double,
        consumer: (Int) -> Unit,
    ) {
        if (right - left <= nodeSize) {
            for (i in left..right) {
                val id = ids[i]
                val x = coords[2 * id]
                val y = coords[2 * id + 1]
                if (x in minX..maxX && y in minY..maxY) {
                    consumer(id)
                }
            }
            return
        }

        val mid = (left + right) ushr 1
        val midId = ids[mid]
        val midX = coords[2 * midId]
        val midY = coords[2 * midId + 1]

        if (midX in minX..maxX && midY in minY..maxY) {
            consumer(midId)
        }

        val axis = depth % 2
        val axisVal = if (axis == 0) midX else midY
        val minVal = if (axis == 0) minX else minY
        val maxVal = if (axis == 0) maxX else maxY

        if (minVal <= axisVal) {
            queryRange(left, mid - 1, depth + 1, minX, minY, maxX, maxY, consumer)
        }
        if (maxVal >= axisVal) {
            queryRange(mid + 1, right, depth + 1, minX, minY, maxX, maxY, consumer)
        }
    }

    /**
     * Queries all entity IDs within Euclidean distance [radius] from query point (`[qx]`, `[qy]`).
     *
     * @param qx Query point X
     * @param qy Query point Y
     * @param radius Search radius
     * @param consumer Callback invoked for each matching entity ID without allocating objects.
     */
    fun queryRadius(
        qx: Double,
        qy: Double,
        radius: Double,
        consumer: (Int) -> Unit,
    ) {
        if (ids.isEmpty()) return
        val r2 = radius * radius
        val minX = qx - radius
        val maxX = qx + radius
        val minY = qy - radius
        val maxY = qy + radius
        queryRadius(0, ids.size - 1, 0, qx, qy, r2, minX, minY, maxX, maxY, consumer)
    }

    private fun queryRadius(
        left: Int,
        right: Int,
        depth: Int,
        qx: Double,
        qy: Double,
        r2: Double,
        minX: Double,
        minY: Double,
        maxX: Double,
        maxY: Double,
        consumer: (Int) -> Unit,
    ) {
        if (right - left <= nodeSize) {
            for (i in left..right) {
                val id = ids[i]
                val x = coords[2 * id]
                val y = coords[2 * id + 1]
                val dx = x - qx
                val dy = y - qy
                if (dx * dx + dy * dy <= r2) {
                    consumer(id)
                }
            }
            return
        }

        val mid = (left + right) ushr 1
        val midId = ids[mid]
        val midX = coords[2 * midId]
        val midY = coords[2 * midId + 1]

        val dx = midX - qx
        val dy = midY - qy
        if (dx * dx + dy * dy <= r2) {
            consumer(midId)
        }

        val axis = depth % 2
        val axisVal = if (axis == 0) midX else midY
        val minVal = if (axis == 0) minX else minY
        val maxVal = if (axis == 0) maxX else maxY

        if (minVal <= axisVal) {
            queryRadius(left, mid - 1, depth + 1, qx, qy, r2, minX, minY, maxX, maxY, consumer)
        }
        if (maxVal >= axisVal) {
            queryRadius(mid + 1, right, depth + 1, qx, qy, r2, minX, minY, maxX, maxY, consumer)
        }
    }
}
