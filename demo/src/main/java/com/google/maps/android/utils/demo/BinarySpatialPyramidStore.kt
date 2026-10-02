package com.google.maps.android.utils.demo

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.AreaCluster
import com.google.maps.android.clustering.AreaClusterProvider
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.utils.demo.model.BuildingClusterItem
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Memory-mapped spatial pyramid reader for the 2.72M building coordinate dataset.
 *
 * Implements [AreaClusterProvider] to serve clusters and individual markers on-demand based on the
 * visible map camera bounds and zoom level.
 *
 * Enforces zoom thresholds and viewport density limits to prevent marker clutter on screen.
 */
public class BinarySpatialPyramidStore(
    context: Context,
    assetName: String = "buildings_pyramid.scbin",
) : AreaClusterProvider<BuildingClusterItem> {

    private val buffer: ByteBuffer
    public val totalPoints: Int
    public val minZoom: Int
    public val maxClusterZoom: Int
    public val minLat: Float
    public val maxLat: Float
    public val minLng: Float
    public val maxLng: Float

    private val zoomCounts: IntArray
    private val zoomOffsets: LongArray
    private val rawCount: Int
    private val rawOffset: Long

    /** Minimum zoom level where individual building markers are permitted to render. */
    public var minZoomForLeafMarkers: Float = 17.0f

    /** Maximum individual leaf markers allowed in any single viewport query to protect frame rates. */
    public var maxMarkersPerViewport: Int = 80

    init {
        val mappedBuffer = try {
            val afd = context.assets.openFd(assetName)
            val channel = FileInputStream(afd.fileDescriptor).channel
            channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.length)
        } catch (e: Exception) {
            val bytes = context.assets.open(assetName).use { it.readBytes() }
            ByteBuffer.wrap(bytes)
        }
        mappedBuffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer = mappedBuffer

        // Validate Magic (8 bytes: "SCBIN2\0\0")
        val magic0 = buffer.getInt(0)
        val magic1 = buffer.getInt(4)
        check(magic0 == 0x49424353 && magic1 == 0x0000324E) {
            "Invalid SCBIN2 magic header: %08X %08X".format(magic0, magic1)
        }

        totalPoints = buffer.getInt(8)
        minZoom = buffer.getInt(12)
        maxClusterZoom = buffer.getInt(16)
        minLat = buffer.getFloat(20)
        maxLat = buffer.getFloat(24)
        minLng = buffer.getFloat(28)
        maxLng = buffer.getFloat(32)

        val levelsCount = maxClusterZoom + 1
        zoomCounts = IntArray(levelsCount)
        zoomOffsets = LongArray(levelsCount)

        var tablePos = 40
        for (z in 0 until levelsCount) {
            zoomCounts[z] = buffer.getInt(tablePos)
            zoomOffsets[z] = buffer.getLong(tablePos + 4)
            tablePos += 16
        }

        rawCount = buffer.getInt(tablePos)
        rawOffset = buffer.getLong(tablePos + 4)
    }

    override fun getClustersForArea(
        bounds: LatLngBounds,
        zoom: Float,
    ): Collection<Cluster<BuildingClusterItem>> {
        val z = zoom.toInt()
        val latMargin = (bounds.northeast.latitude - bounds.southwest.latitude) * 0.15
        val lngMargin = (bounds.northeast.longitude - bounds.southwest.longitude) * 0.15

        val minQueryLat = (bounds.southwest.latitude - latMargin).toFloat()
        val maxQueryLat = (bounds.northeast.latitude + latMargin).toFloat()
        val minQueryLng = (bounds.southwest.longitude - lngMargin).toFloat()
        val maxQueryLng = (bounds.northeast.longitude + lngMargin).toFloat()

        val results = ArrayList<Cluster<BuildingClusterItem>>()
        var leafMarkerCount = 0

        if (z <= maxClusterZoom) {
            val targetZoom = z.coerceIn(minZoom, maxClusterZoom)
            val count = zoomCounts[targetZoom]
            val offset = zoomOffsets[targetZoom]
            if (count == 0) return results

            // Binary search for minQueryLat
            val startIndex = binarySearchLat(offset, count, 12, minQueryLat)
            var idx = startIndex
            while (idx < count) {
                val entryOffset = (offset + idx * 12).toInt()
                val lat = buffer.getFloat(entryOffset)
                if (lat > maxQueryLat) break

                val lng = buffer.getFloat(entryOffset + 4)
                if (lng in minQueryLng..maxQueryLng) {
                    val clusterSize = buffer.getInt(entryOffset + 8)
                    val pos = LatLng(lat.toDouble(), lng.toDouble())

                    // Only permit individual leaf markers when zoomed in past minZoomForLeafMarkers
                    // AND below the maxMarkersPerViewport threshold
                    if (clusterSize == 1 && zoom >= minZoomForLeafMarkers && leafMarkerCount < maxMarkersPerViewport) {
                        val item = BuildingClusterItem(lat.toDouble(), lng.toDouble(), idx)
                        results.add(AreaCluster(pos, 1, item))
                        leafMarkerCount++
                    } else {
                        // Keep as a cluster badge (preserves count without leaf clutter)
                        results.add(AreaCluster(pos, clusterSize, null))
                    }
                }
                idx++
            }
        } else {
            // Zoom > maxClusterZoom: High detail street level view
            if (rawCount == 0) return results
            val startIndex = binarySearchLat(rawOffset, rawCount, 8, minQueryLat)
            var idx = startIndex
            while (idx < rawCount) {
                val entryOffset = (rawOffset + idx * 8).toInt()
                val lat = buffer.getFloat(entryOffset)
                if (lat > maxQueryLat) break

                val lng = buffer.getFloat(entryOffset + 4)
                if (lng in minQueryLng..maxQueryLng) {
                    val pos = LatLng(lat.toDouble(), lng.toDouble())
                    if (leafMarkerCount < maxMarkersPerViewport) {
                        val item = BuildingClusterItem(lat.toDouble(), lng.toDouble(), idx)
                        results.add(AreaCluster(pos, 1, item))
                        leafMarkerCount++
                    } else {
                        results.add(AreaCluster(pos, 1, null))
                    }
                }
                idx++
            }
        }

        return results
    }

    private fun binarySearchLat(baseOffset: Long, count: Int, entrySize: Int, targetLat: Float): Int {
        var low = 0
        var high = count - 1
        var result = count

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midOffset = (baseOffset + mid * entrySize).toInt()
            val midLat = buffer.getFloat(midOffset)

            if (midLat >= targetLat) {
                result = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return result
    }
}
