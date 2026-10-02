package com.google.maps.android.clustering.algo

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.AreaClusterProvider
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem

/**
 * An [Algorithm] and [ScreenBasedAlgorithm] implementation that queries an [AreaClusterProvider]
 * on-demand when the map camera moves or changes zoom level.
 *
 * This algorithm allows massive spatial datasets (millions of points) to be rendered with dynamic
 * clustering without loading all points into JVM heap memory.
 *
 * @param T The type of [ClusterItem] to display.
 * @property provider The provider callback supplying clusters and individual markers.
 */
public class AreaClusterProviderAlgorithm<T : ClusterItem>(
    public var provider: AreaClusterProvider<T>? = null,
) : AbstractAlgorithm<T>(), ScreenBasedAlgorithm<T> {

    @Volatile
    private var mCurrentBounds: LatLngBounds? = null

    @Volatile
    private var mCurrentZoom: Float = 0f

    private var mMaxDistance: Int = 35

    /**
     * Updates the current visible geographic bounding box of the map camera.
     *
     * @param bounds The current visible viewport bounds (optionally padded).
     */
    public fun setVisibleBounds(bounds: LatLngBounds) {
        lock()
        try {
            mCurrentBounds = bounds
        } finally {
            unlock()
        }
    }

    public override fun onCameraChange(position: CameraPosition) {
        lock()
        try {
            mCurrentZoom = position.zoom
        } finally {
            unlock()
        }
    }

    public override fun shouldReclusterOnMapMovement(): Boolean = true

    public override var maxDistanceBetweenClusteredItems: Int
        get() = mMaxDistance
        set(value) {
            mMaxDistance = value
        }

    public override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val p: AreaClusterProvider<T>
        val bounds: LatLngBounds?
        lock()
        try {
            p = provider ?: return emptySet()
            bounds = mCurrentBounds
        } finally {
            unlock()
        }

        if (bounds == null) {
            return emptySet()
        }

        val result = p.getClustersForArea(bounds, zoom)
        return if (result is Set<*>) {
            @Suppress("UNCHECKED_CAST")
            result as Set<Cluster<T>>
        } else {
            result.toSet()
        }
    }

    public override fun addItem(item: T): Boolean = false

    public override fun addItems(items: Collection<T>): Boolean = false

    public override fun clearItems() {}

    public override fun removeItem(item: T): Boolean = false

    public override fun removeItems(items: Collection<T>): Boolean = false

    public override fun updateItem(item: T): Boolean = false

    public override val items: Collection<T>
        get() = emptyList()
}
