/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.utils.demo

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.AbstractAlgorithm
import com.google.maps.android.clustering.algo.CentroidNonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.ContinuousZoomEuclideanCentroidAlgorithm
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.utils.demo.model.MyItem
import kotlin.random.Random

/**
 * A demo activity that showcases the various clustering algorithms
 * available in the library.
 */
class ClusterAlgorithmsDemoActivity : BaseDemoActivity() {

    private var clusterManager: ClusterManager<MyItem>? = null
    private lateinit var mapView: MapView

    override fun getLayoutId(): Int {
        return R.layout.activity_cluster_algorithms_demo
    }

    override fun startDemo(isRestore: Boolean) {

        if (!isRestore) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(51.503186, -0.126446), 10f
                )
            )
        }

        setupSpinner()

        setupClusterer(0)
    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.algorithm_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.clustering_algorithms, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                setupClusterer(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Sets up the ClusterManager with the chosen algorithm and populates it with items.
     */
    private fun setupClusterer(algorithmPosition: Int) {
        // 1. Clear the map and previous cluster manager
        map.clear()

        // 2. Initialize a new ClusterManager, using getMap() from BaseDemoActivity
        clusterManager = ClusterManager(this, map)

        // 3. Set the desired algorithm based on the spinner position
        clusterManager?.algorithm = when (algorithmPosition) {
            1 -> GridBasedAlgorithm()
            2 -> NonHierarchicalDistanceBasedAlgorithm()
            3 -> CentroidNonHierarchicalDistanceBasedAlgorithm()
            4 -> NonHierarchicalViewBasedAlgorithm(mapView.width, mapView.height)
            5 -> ContinuousZoomEuclideanCentroidAlgorithm()
            else -> error("Unsupported algorithm position: $algorithmPosition")
        }

        // 4. Point the map's listeners to the ClusterManager
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        // 5. Generate and add cluster items to the manager
        val items = generateItems()
        clusterManager?.addItems(items)

        // 6. Trigger the initial clustering
        clusterManager?.cluster()
    }

    private fun generateItems(): List<MyItem> {
        val items = mutableListOf<MyItem>()
        // Add 100 random items in the map region
        for (i in 0 until 100) {
            val lat = 51.5145 + (Random.nextDouble() - 0.5) / 2.0
            val lng = -0.1245 + (Random.nextDouble() - 0.5) / 2.0
            items.add(MyItem(lat, lng, "Marker #$i", "Snippet for marker #$i"))
        }
        return items
    }
}