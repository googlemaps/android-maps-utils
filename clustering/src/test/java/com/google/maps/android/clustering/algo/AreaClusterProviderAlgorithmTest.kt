package com.google.maps.android.clustering.algo

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.AreaCluster
import com.google.maps.android.clustering.AreaClusterProvider
import com.google.maps.android.clustering.ClusterItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaClusterProviderAlgorithmTest {

    private class TestItem(
        override val position: LatLng,
        override val title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = null,
    ) : ClusterItem

    @Test
    fun testAreaClusterLeafItem() {
        val pos = LatLng(32.5, -117.0)
        val item = TestItem(pos, "Building 1")
        val cluster = AreaCluster(pos, 1, item)

        assertTrue(cluster.items.contains(item))
        assertEquals(1, cluster.size)
        assertEquals(pos, cluster.position)
        assertEquals(item, cluster.leafItem)
    }

    @Test
    fun testAreaClusterComposite() {
        val pos = LatLng(32.5, -117.0)
        val cluster = AreaCluster<TestItem>(pos, 42, null)

        assertTrue(cluster.items.isEmpty())
        assertEquals(42, cluster.size)
        assertEquals(pos, cluster.position)
    }

    @Test
    fun testAlgorithmDelegation() {
        var requestedBounds: LatLngBounds? = null
        var requestedZoom = -1f

        val provider = AreaClusterProvider<TestItem> { bounds, zoom ->
            requestedBounds = bounds
            requestedZoom = zoom

            listOf(
                AreaCluster(LatLng(32.0, -116.0), 100),
                AreaCluster(LatLng(32.5, -116.5), 1, TestItem(LatLng(32.5, -116.5))),
            )
        }

        val algo = AreaClusterProviderAlgorithm(provider)
        assertTrue(algo.shouldReclusterOnMapMovement())

        // Initial query before bounds are set returns empty
        assertTrue(algo.getClusters(10f).isEmpty())

        // Set bounds and query
        val bounds = LatLngBounds(LatLng(31.0, -118.0), LatLng(33.0, -115.0))
        algo.setVisibleBounds(bounds)
        algo.onCameraChange(CameraPosition(LatLng(32.0, -116.5), 12f, 0f, 0f))

        val clusters = algo.getClusters(12f)
        assertEquals(2, clusters.size)
        assertEquals(bounds, requestedBounds)
        assertEquals(12f, requestedZoom, 0.001f)
    }

    @Test
    fun testNullProviderReturnsEmpty() {
        val algo = AreaClusterProviderAlgorithm<TestItem>()
        algo.setVisibleBounds(LatLngBounds(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        assertTrue(algo.getClusters(5f).isEmpty())
    }
}
