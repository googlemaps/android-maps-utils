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

package com.google.maps.android.utils.demo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.utils.demo.model.BuildingClusterItem
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// [START maps_android_utils_building_megacluster]
/**
 * Demonstrates mega-scale marker clustering across a 2,719,379 building dataset using an
 * on-demand [com.google.maps.android.clustering.AreaClusterProvider] and memory-mapped spatial pyramid.
 *
 * Key capabilities showcased:
 * 1. Area-based callback clustering: markers and clusters are dynamically resolved on-demand
 *    for the visible map camera bounds and zoom level.
 * 2. Zero JVM heap exhaustion: millions of points are stored out-of-heap in memory-mapped assets.
 * 3. Gremlin leaf markers: zoomed-in individual buildings render using custom Gremlin marker icons.
 * 4. Configurable cluster badges: compact formatting (e.g. 2.7M, 116K) with customizable unit case.
 */
class BuildingMegaClusterDemoActivity : BaseDemoActivity() {

    private lateinit var clusterManager: ClusterManager<BuildingClusterItem>
    private lateinit var spatialStore: BinarySpatialPyramidStore
    private lateinit var renderer: BuildingClusterRenderer

    private var textStats: TextView? = null

    override fun getLayoutId(): Int = R.layout.activity_building_megacluster

    public companion object {
        public const val ACTION_MAP_CONTROL: String =
            "com.google.maps.android.utils.demo.ACTION_MAP_CONTROL"
    }

    private val mapControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val map = map ?: return
            val lat = intent.getDoubleExtra("lat", Double.NaN)
            val lng = intent.getDoubleExtra("lng", Double.NaN)
            val zoom = intent.getFloatExtra("zoom", -1f)
            val duration = intent.getIntExtra("duration", 2000)
            val mapType = intent.getStringExtra("map_type")

            if (mapType != null) {
                val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_map_type_group)
                when (mapType.lowercase()) {
                    "normal", "map" -> {
                        map.mapType = GoogleMap.MAP_TYPE_NORMAL
                        toggleGroup?.check(R.id.btn_map_type_normal)
                    }
                    "satellite" -> {
                        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        toggleGroup?.check(R.id.btn_map_type_satellite)
                    }
                    "hybrid" -> {
                        map.mapType = GoogleMap.MAP_TYPE_HYBRID
                        toggleGroup?.check(R.id.btn_map_type_hybrid)
                    }
                }
            }

            if (!lat.isNaN() && !lng.isNaN() && zoom > 0) {
                val update = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom)
                map.animateCamera(update, duration, null)
            } else if (!lat.isNaN() && !lng.isNaN()) {
                val update = CameraUpdateFactory.newLatLng(LatLng(lat, lng))
                map.animateCamera(update, duration, null)
            } else if (zoom > 0) {
                val update = CameraUpdateFactory.zoomTo(zoom)
                map.animateCamera(update, duration, null)
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun startDemo(isRestore: Boolean) {
        val map = map ?: return

        textStats = findViewById(R.id.text_stats)

        // Read initial camera and map type from intent if provided
        val startLat = intent.getDoubleExtra("extra_lat", 32.55)
        val startLng = intent.getDoubleExtra("extra_lng", -116.2)
        val startZoom = intent.getFloatExtra("extra_zoom", 9f)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(startLat, startLng), startZoom))

        val startMapType = intent.getStringExtra("extra_map_type")
        if (startMapType != null) {
            when (startMapType.lowercase()) {
                "satellite" -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    findViewById<MaterialButtonToggleGroup>(R.id.toggle_map_type_group)?.check(R.id.btn_map_type_satellite)
                }
                "hybrid" -> {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    findViewById<MaterialButtonToggleGroup>(R.id.toggle_map_type_group)?.check(R.id.btn_map_type_hybrid)
                }
                else -> {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    findViewById<MaterialButtonToggleGroup>(R.id.toggle_map_type_group)?.check(R.id.btn_map_type_normal)
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            mapControlReceiver,
            IntentFilter(ACTION_MAP_CONTROL),
            ContextCompat.RECEIVER_EXPORTED,
        )

        lifecycleScope.launch(Dispatchers.Default) {
            val store = BinarySpatialPyramidStore(this@BuildingMegaClusterDemoActivity)
            withContext(Dispatchers.Main) {
                spatialStore = store
                setupClusterManager(map, store)
            }
        }

        findViewById<android.view.View>(R.id.fab_settings)?.setOnClickListener {
            showClusterSettingsDialog()
        }

        val toggleMapTypeGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_map_type_group)
        toggleMapTypeGroup?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_map_type_normal -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
                R.id.btn_map_type_satellite -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                R.id.btn_map_type_hybrid -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(mapControlReceiver)
        } catch (_: Exception) {}
    }

    private fun setupClusterManager(map: GoogleMap, store: BinarySpatialPyramidStore) {
        clusterManager = ClusterManager(this, map)
        renderer = BuildingClusterRenderer(this, map, clusterManager)

        // Configure cluster renderer with compact number formatting
        renderer.useCompactNumberFormatting = true
        renderer.compactUnitUppercase = true
        renderer.maxNonZeroDigits = 3
        renderer.setAnimation(true)
        clusterManager.renderer = renderer

        // Connect the on-demand AreaClusterProvider callback
        clusterManager.setAreaClusterProvider(store)

        map.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
            updateStatsDisplay(map.cameraPosition.zoom)
        }

        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        clusterManager.setOnClusterClickListener { cluster ->
            val targetZoom = (map.cameraPosition.zoom + 2f).coerceAtMost(20f)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.position, targetZoom))
            true
        }

        clusterManager.setOnClusterItemClickListener { item ->
            Toast.makeText(this, "Building at ${item.snippet}", Toast.LENGTH_SHORT).show()
            false
        }

        clusterManager.cluster()
        updateStatsDisplay(map.cameraPosition.zoom)
    }

    private fun updateStatsDisplay(zoom: Float) {
        val mode = if (zoom >= spatialStore.minZoomForLeafMarkers) "Candy Houses & Badges" else "Clusters Only"
        textStats?.text = String.format(Locale.US, "Zoom: %.1f | %s", zoom, mode)
    }

    /**
     * Custom renderer that styles individual building markers as Hansel & Gretel candy houses and
     * styles composite clusters with dynamic count badges.
     */
    private class BuildingClusterRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<BuildingClusterItem>,
    ) : DefaultClusterRenderer<BuildingClusterItem>(context, map, clusterManager) {

        private val mCandyHouseIcon: BitmapDescriptor =
            BitmapDescriptorFactory.fromResource(R.drawable.candy_house_marker)

        override fun shouldRenderAsCluster(cluster: Cluster<BuildingClusterItem>): Boolean {
            // A cluster badge must ALWAYS represent at least 2 items (never a badge labeled "1")
            return cluster.size >= minClusterSize.coerceAtLeast(2)
        }

        override fun onBeforeClusterItemRendered(
            item: BuildingClusterItem,
            markerOptions: MarkerOptions,
        ) {
            markerOptions.icon(mCandyHouseIcon)
            markerOptions.title(item.title)
            markerOptions.snippet(item.snippet)
        }

        override fun getColor(clusterSize: Int): Int {
            return when {
                clusterSize >= 1_000_000 -> Color.rgb(180, 0, 0)       // Crimson for 1M+
                clusterSize >= 100_000 -> Color.rgb(220, 50, 0)        // Deep orange for 100K+
                clusterSize >= 10_000 -> Color.rgb(235, 120, 0)        // Amber orange for 10K+
                clusterSize >= 1_000 -> Color.rgb(240, 180, 0)         // Golden yellow for 1K+
                clusterSize >= 100 -> Color.rgb(30, 140, 60)           // Emerald green for 100+
                else -> Color.rgb(26, 115, 232)                        // Google Blue for smaller
            }
        }
    }

    private fun showClusterSettingsDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_cluster_settings, null)

        // Hide dataset selection and radius sections since this demo is dedicated to the 2.72M spatial pyramid
        view.findViewById<android.view.View>(R.id.section_dataset)?.visibility = android.view.View.GONE
        view.findViewById<android.view.View>(R.id.section_radius)?.visibility = android.view.View.GONE

        val textMinSizeTitle = view.findViewById<TextView>(R.id.text_minsize_title)
        val sliderMinSize = view.findViewById<Slider>(R.id.slider_minsize)
        val currentMinSize = renderer.minClusterSize
        sliderMinSize.value = currentMinSize.toFloat().coerceIn(sliderMinSize.valueFrom, sliderMinSize.valueTo)
        textMinSizeTitle.text = getString(R.string.clustering_settings_minsize_format, currentMinSize)
        sliderMinSize.addOnChangeListener { _, value, _ ->
            textMinSizeTitle.text = getString(R.string.clustering_settings_minsize_format, value.toInt())
        }

        val textDigitsTitle = view.findViewById<TextView>(R.id.text_digits_title)
        val sliderDigits = view.findViewById<Slider>(R.id.slider_digits)
        val currentDigits = renderer.maxNonZeroDigits
        sliderDigits.value = currentDigits.toFloat().coerceIn(sliderDigits.valueFrom, sliderDigits.valueTo)
        textDigitsTitle.text = getString(R.string.clustering_settings_digits_format, currentDigits)
        sliderDigits.addOnChangeListener { _, value, _ ->
            textDigitsTitle.text = getString(R.string.clustering_settings_digits_format, value.toInt())
        }

        val switchCompact = view.findViewById<SwitchMaterial>(R.id.switch_compact_notation)
        val switchUppercase = view.findViewById<SwitchMaterial>(R.id.switch_uppercase_units)
        val switchShowExact = view.findViewById<SwitchMaterial>(R.id.switch_show_exact)

        switchCompact.isChecked = renderer.useCompactNumberFormatting
        switchUppercase.isChecked = renderer.compactUnitUppercase
        switchShowExact.isChecked = renderer.showExactCount

        MaterialAlertDialogBuilder(this)
            .setTitle("Clustering & Badge Settings")
            .setView(view)
            .setPositiveButton("Apply") { _, _ ->
                renderer.useCompactNumberFormatting = switchCompact.isChecked
                renderer.compactUnitUppercase = switchUppercase.isChecked
                renderer.showExactCount = switchShowExact.isChecked
                renderer.maxNonZeroDigits = sliderDigits.value.toInt()
                renderer.minClusterSize = sliderMinSize.value.toInt()
                renderer.clearIconCache()
                clusterManager.cluster()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
// [END maps_android_utils_building_megacluster]
