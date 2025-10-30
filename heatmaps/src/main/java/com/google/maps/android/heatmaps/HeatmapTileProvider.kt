/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.heatmaps

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.quadtree.PointQuadTree
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow

/**
 * Tile provider that creates heatmap tiles.
 */
class HeatmapTileProvider private constructor(builder: Builder) : TileProvider {

    private var data: Collection<WeightedLatLng>
    private var radius: Int
    private var gradient: Gradient
    private var opacity: Double
    private var customMaxIntensity: Double

    private lateinit var tree: PointQuadTree<WeightedLatLng>
    private lateinit var bounds: Bounds
    private lateinit var colorMap: IntArray
    private var kernel: DoubleArray
    private lateinit var maxIntensity: DoubleArray

    init {
        data = builder.weightedData!!
        radius = builder.radius
        gradient = builder.gradient
        opacity = builder.opacity
        customMaxIntensity = builder.intensity

        // Don't compute anything till data is set
        kernel = generateKernel(radius, radius / 3.0)
        setGradient(gradient)
        updateData(data)
    }

    /**
     * Builder class for the HeatmapTileProvider.
     */
    class Builder {
        internal var weightedData: Collection<WeightedLatLng>? = null
        internal var radius = DEFAULT_RADIUS
        internal var gradient = DEFAULT_GRADIENT
        internal var opacity = DEFAULT_OPACITY
        internal var intensity = 0.0

        /**
         * Specifies the dataset to use for the heatmap, accepting unweighted LatLngs.
         *
         * @param latLngs A collection of LatLngs.
         * @return This builder.
         */
        fun data(latLngs: Collection<LatLng>): Builder = apply {
            this.weightedData(wrapData(latLngs))
            require(this.weightedData?.isNotEmpty() == true) { "No input points." }
        }

        /**
         * Specifies the dataset to use for the heatmap, accepting WeightedLatLngs.
         *
         * @param weightedData A collection of WeightedLatLngs.
         * @return This builder.
         */
        fun weightedData(weightedData: Collection<WeightedLatLng>): Builder = apply {
            this.weightedData = weightedData
            require(this.weightedData?.isNotEmpty() == true) { "No input points." }
        }

        /**
         * Specifies the radius of the heatmap blur, in pixels.
         *
         * @param radius The radius. Must be between 10 and 50, inclusive.
         * @return This builder.
         */
        fun radius(radius: Int): Builder = apply {
            this.radius = radius
            require(this.radius in MIN_RADIUS..MAX_RADIUS) { "Radius not within bounds." }
        }

        /**
         * Specifies the color gradient of the heatmap.
         *
         * @param gradient The gradient to use.
         * @return This builder.
         */
        fun gradient(gradient: Gradient): Builder = apply {
            this.gradient = gradient
        }

        /**
         * Specifies the opacity of the heatmap layer.
         *
         * @param opacity The opacity. Must be between 0 and 1, inclusive.
         * @return This builder.
         */
        fun opacity(opacity: Double): Builder = apply {
            this.opacity = opacity
            require(this.opacity in 0.0..1.0) { "Opacity must be in range [0, 1]" }
        }

        /**
         * Specifies a custom maximum intensity value for the heatmap.
         *
         * @param intensity The maximum intensity.
         * @return This builder.
         */
        fun maxIntensity(intensity: Double): Builder = apply {
            this.intensity = intensity
        }

        /**
         * Creates a new HeatmapTileProvider instance from the builder's properties.
         *
         * @return A new HeatmapTileProvider.
         */
        fun build(): HeatmapTileProvider {
            require(this.weightedData?.isNotEmpty() == true) { "No input data: you must use either .data or .weightedData before building." }
            return HeatmapTileProvider(this)
        }
    }

    @Deprecated("Use updateData(Collection<WeightedLatLng>) instead.", ReplaceWith("updateData(data)"))
    fun setWeightedData(data: Collection<WeightedLatLng>) {
        updateData(data)
    }

    /**
     * Refreshes the heatmap with a new collection of weighted data points.
     *
     * This is an expensive operation. It involves rebuilding the quadtree index and recalculating
     * the bounds and maximum intensity values for the new dataset. This method should be used when
     * the underlying data for the heatmap has changed.
     *
     * @param data The new collection of [WeightedLatLng] points.
     */
    fun updateData(data: Collection<WeightedLatLng>) {
        this.data = data
        require(this.data.isNotEmpty()) { "No input points." }
        this.bounds = getBounds(this.data)
        this.tree = PointQuadTree(this.bounds)
        for (l in this.data) {
            this.tree.add(l)
        }
        this.maxIntensity = getMaxIntensities(this.radius)
    }

    @Deprecated("Use updateLatLngs(Collection<LatLng>) instead.", ReplaceWith("updateLatLngs(latLngs)"))
    fun setData(latLngs: Collection<LatLng>) {
        updateLatLngs(latLngs)
    }

    /**
     * Refreshes the heatmap with a new collection of unweighted data points.
     * Each point is assigned a default weight of 1.0.
     *
     * This is a convenience method that wraps the data in [WeightedLatLng] objects before
     * calling [updateData].
     *
     * @param latLngs The new collection of [LatLng] points.
     */
    fun updateLatLngs(latLngs: Collection<LatLng>) {
        updateData(wrapData(latLngs))
    }

    fun setGradient(gradient: Gradient) {
        this.gradient = gradient
        this.colorMap = gradient.generateColorMap(this.opacity)
    }

    fun setRadius(radius: Int) {
        this.radius = radius
        this.kernel = generateKernel(this.radius, this.radius / 3.0)
        this.maxIntensity = getMaxIntensities(this.radius)
    }

    fun setOpacity(opacity: Double) {
        this.opacity = opacity
        setGradient(this.gradient)
    }

    fun setMaxIntensity(intensity: Double) {
        this.customMaxIntensity = intensity
        updateData(this.data)
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val tileWidth = WORLD_WIDTH / 2.0.pow(zoom.toDouble())
        val padding = tileWidth * radius / TILE_DIM
        val tileWidthPadded = tileWidth + 2 * padding
        val bucketWidth = tileWidthPadded / (TILE_DIM + radius * 2)
        val minX = x * tileWidth - padding
        val maxX = (x + 1) * tileWidth + padding
        val minY = y * tileWidth - padding
        val maxY = (y + 1) * tileWidth + padding

        var xOffset = 0.0
        var wrappedPoints: Collection<WeightedLatLng> = emptyList()

        if (minX < 0) {
            val overlapBounds = Bounds(minX + WORLD_WIDTH, WORLD_WIDTH, minY, maxY)
            xOffset = -WORLD_WIDTH
            wrappedPoints = tree.search(overlapBounds)
        } else if (maxX > WORLD_WIDTH) {
            val overlapBounds = Bounds(0.0, maxX - WORLD_WIDTH, minY, maxY)
            xOffset = WORLD_WIDTH
            wrappedPoints = tree.search(overlapBounds)
        }

        val tileBounds = Bounds(minX, maxX, minY, maxY)
        val paddedBounds = Bounds(
            bounds.minX - padding, bounds.maxX + padding,
            bounds.minY - padding, bounds.maxY + padding
        )
        if (!tileBounds.intersects(paddedBounds)) {
            return TileProvider.NO_TILE
        }

        val points = tree.search(tileBounds)
        if (points.isEmpty()) {
            return TileProvider.NO_TILE
        }

        val intensity = Array(TILE_DIM + radius * 2) { DoubleArray(TILE_DIM + radius * 2) }
        points.forEach { w ->
            val p = w.point
            val bucketX = ((p.x - minX) / bucketWidth).toInt()
            val bucketY = ((p.y - minY) / bucketWidth).toInt()
            intensity[bucketX][bucketY] += w.intensity
        }
        wrappedPoints.forEach { w ->
            val p = w.point
            val bucketX = ((p.x + xOffset - minX) / bucketWidth).toInt()
            val bucketY = ((p.y - minY) / bucketWidth).toInt()
            intensity[bucketX][bucketY] += w.intensity
        }

        val convolved = convolve(intensity, kernel)
        val bitmap = colorize(convolved, colorMap, maxIntensity[zoom])
        return convertBitmap(bitmap)
    }

    private fun getMaxIntensities(radius: Int): DoubleArray {
        val maxIntensityArray = DoubleArray(MAX_ZOOM_LEVEL)
        if (customMaxIntensity != 0.0) {
            for (i in 0 until MAX_ZOOM_LEVEL) {
                maxIntensityArray[i] = customMaxIntensity
            }
            return maxIntensityArray
        }
        for (i in DEFAULT_MIN_ZOOM until DEFAULT_MAX_ZOOM) {
            maxIntensityArray[i] = getMaxValue(data, bounds, radius, (SCREEN_SIZE * Math.pow(2.0, (i - 3).toDouble())).toInt())
            if (i == DEFAULT_MIN_ZOOM) {
                for (j in 0 until i) maxIntensityArray[j] = maxIntensityArray[i]
            }
        }
        for (i in DEFAULT_MAX_ZOOM until MAX_ZOOM_LEVEL) {
            maxIntensityArray[i] = maxIntensityArray[DEFAULT_MAX_ZOOM - 1]
        }
        return maxIntensityArray
    }

    companion object {
        const val DEFAULT_RADIUS = 20
        const val DEFAULT_OPACITY = 0.7
        private val DEFAULT_GRADIENT_COLORS = intArrayOf(Color.rgb(102, 225, 0), Color.rgb(255, 0, 0))
        private val DEFAULT_GRADIENT_START_POINTS = floatArrayOf(0.2f, 1f)
        @JvmField
        val DEFAULT_GRADIENT = Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS)
        internal const val WORLD_WIDTH = 1.0
        private const val TILE_DIM = 512
        private const val SCREEN_SIZE = 1280
        private const val DEFAULT_MIN_ZOOM = 5
        private const val DEFAULT_MAX_ZOOM = 11
        private const val MAX_ZOOM_LEVEL = 22
        const val MIN_RADIUS = 10
        const val MAX_RADIUS = 50

        private data class Vector(val x: Int, val y: Int)

        private fun wrapData(data: Collection<LatLng>): Collection<WeightedLatLng> = data.map { WeightedLatLng(it) }

        private fun convertBitmap(bitmap: Bitmap): Tile {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitmapData = stream.toByteArray()
            return Tile(TILE_DIM, TILE_DIM, bitmapData)
        }

        @JvmStatic
        fun getBounds(points: Collection<WeightedLatLng>): Bounds {
            val firstPoint = points.first().point
            var minX = firstPoint.x
            var maxX = firstPoint.x
            var minY = firstPoint.y
            var maxY = firstPoint.y

            points.drop(1).forEach {
                val x = it.point.x
                val y = it.point.y
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
            return Bounds(minX, maxX, minY, maxY)
        }

        @JvmStatic
        fun generateKernel(radius: Int, sd: Double): DoubleArray {
            val kernel = DoubleArray(radius * 2 + 1)
            for (i in -radius..radius) {
                kernel[i + radius] = exp(-i * i / (2 * sd * sd))
            }
            return kernel
        }

        @JvmStatic
        fun convolve(grid: Array<DoubleArray>, kernel: DoubleArray): Array<DoubleArray> {
            val radius = floor(kernel.size / 2.0).toInt()
            val dimOld = grid.size
            val dim = dimOld - 2 * radius
            val lowerLimit = radius
            val upperLimit = radius + dim - 1
            val intermediate = Array(dimOld) { DoubleArray(dimOld) }

            for (x in 0 until dimOld) {
                for (y in 0 until dimOld) {
                    val value = grid[x][y]
                    if (value != 0.0) {
                        val xUpperLimit = (x + radius).coerceAtMost(upperLimit)
                        for (x2 in (x - radius).coerceAtLeast(lowerLimit)..xUpperLimit) {
                            intermediate[x2][y] += value * kernel[x2 - (x - radius)]
                        }
                    }
                }
            }

            val outputGrid = Array(dim) { DoubleArray(dim) }
            for (x in lowerLimit..upperLimit) {
                for (y in 0 until dimOld) {
                    val value = intermediate[x][y]
                    if (value != 0.0) {
                        val yUpperLimit = (y + radius).coerceAtMost(upperLimit)
                        for (y2 in (y - radius).coerceAtLeast(lowerLimit)..yUpperLimit) {
                            outputGrid[x - radius][y2 - radius] += value * kernel[y2 - (y - radius)]
                        }
                    }
                }
            }
            return outputGrid
        }

        internal fun colorize(grid: Array<DoubleArray>, colorMap: IntArray, max: Double): Bitmap {
            val maxColor = colorMap.last()
            val colorMapScaling = (colorMap.size - 1) / max
            val dim = grid.size
            val colors = IntArray(dim * dim)
            for (i in 0 until dim) {
                for (j in 0 until dim) {
                    val value = grid[j][i]
                    val index = i * dim + j
                    val col = (value * colorMapScaling).toInt()
                    colors[index] = if (value != 0.0) {
                        if (col < colorMap.size) colorMap[col] else maxColor
                    } else {
                        Color.TRANSPARENT
                    }
                }
            }
            val tile = createBitmap(dim, dim)
            tile.setPixels(colors, 0, dim, 0, 0, dim, dim)
            return tile
        }

        internal fun getMaxValue(
            points: Collection<WeightedLatLng>,
            bounds: Bounds,
            radius: Int,
            screenDim: Int
        ): Double {
            val minX = bounds.minX
            val maxX = bounds.maxX
            val minY = bounds.minY
            val maxY = bounds.maxY
            val boundsDim = (maxX - minX).coerceAtLeast(maxY - minY)
            val nBuckets = (screenDim / (2 * radius) + 0.5).toInt()
            val scale = nBuckets / boundsDim
            val buckets = mutableMapOf<Vector, Double>()

            points.forEach { l ->
                val x = l.point.x
                val y = l.point.y
                val xBucket = ((x - minX) * scale).toInt()
                val yBucket = ((y - minY) * scale).toInt()
                val bucket = Vector(xBucket, yBucket)
                val currentValue = buckets.getOrPut(bucket) { 0.0 }
                buckets[bucket] = currentValue + l.intensity
            }
            return buckets.values.maxOrNull() ?: 0.0
        }
    }
}