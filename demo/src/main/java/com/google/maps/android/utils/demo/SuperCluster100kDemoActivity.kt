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
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.SuperClusterAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.utils.demo.model.MyItem
import java.util.ArrayList
import java.util.Locale
import kotlin.math.ln
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A demo activity showcasing mega-scale clustering of 100,000 to 1,000,000 markers using [SuperClusterAlgorithm].
 *
 * Demonstrates sub-millisecond viewport queries, instant zoom transitions, and zero UI stuttering
 * on massive datasets, with zoomed-in unclustered points rendering as mischievous gremlins.
 */
class SuperCluster100kDemoActivity : BaseDemoActivity() {

    enum class DatasetMode {
        DATASET_100K_SF,
        DATASET_1M_USA,
    }

    private lateinit var clusterManager: ClusterManager<MyItem>
    private lateinit var superClusterAlgorithm: SuperClusterAlgorithm<MyItem>
    private lateinit var gremlinRenderer: GremlinClusterRenderer
    private var currentDataset: DatasetMode = DatasetMode.DATASET_100K_SF
    private var loadJob: Job? = null

    override fun getLayoutId(): Int = R.layout.activity_supercluster_100k

    @SuppressLint("PotentialBehaviorOverride")
    override fun startDemo(isRestore: Boolean) {
        val (widthDp, heightDp) = getScreenDimensionsDp()

        if (!isRestore) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(37.7749, -122.4194),
                    9f,
                ),
            )
        }

        // [START maps_android_utils_supercluster_100k_demo]
        // 1. Initialize ClusterManager with custom Gremlin renderer
        clusterManager = ClusterManager(this, map)
        gremlinRenderer = GremlinClusterRenderer(this, map, clusterManager).apply {
            showExactCount = false // Only show exact numbers if explicitly requested
            useCompactNumberFormatting = true
            maxNonZeroDigits = 1 // 1 non-zero digit: 5, 7, 10+, 50+, 100+, 1k+
            compactUnitUppercase = false // lowercase 'k' and 'm'
        }
        clusterManager.renderer = gremlinRenderer

        // Set cluster click listener to display the exact verified item count
        clusterManager.setOnClusterClickListener { cluster ->
            Toast.makeText(
                this@SuperCluster100kDemoActivity,
                "Cluster contains exactly %,d gremlins".format(cluster.size),
                Toast.LENGTH_SHORT,
            ).show()
            false
        }

        // 2. Configure SuperClusterAlgorithm with a wider cluster radius (140px)
        // to keep on-screen cluster density low and uncluttered.
        superClusterAlgorithm = SuperClusterAlgorithm(
            minZoom = 0,
            maxZoom = 17,
            radius = 140.0,
            extent = 512.0,
            viewWidth = widthDp,
            viewHeight = heightDp,
        )
        clusterManager.setAlgorithm(superClusterAlgorithm)

        // 3. Connect progress listener to display real-time spatial indexing feedback
        clusterManager.onClusteringProgressListener = ClusterManager.OnClusteringProgressListener { progress, status ->
            lifecycleScope.launch(Dispatchers.Main) {
                val progressContainer = findViewById<View>(R.id.layout_progress_container)
                val progressBar = findViewById<LinearProgressIndicator>(R.id.progress_clustering)
                val textProgress = findViewById<TextView>(R.id.text_clustering_progress)

                if (progress >= 1.0f) {
                    progressBar?.setProgressCompat(100, true)
                    textProgress?.text = status
                    progressContainer?.postDelayed({
                        progressContainer.visibility = View.GONE
                    }, 500)
                } else {
                    progressContainer?.visibility = View.VISIBLE
                    val pct = (progress * 100).toInt()
                    progressBar?.setProgressCompat(pct, true)
                    textProgress?.text = status
                }
            }
        }

        // 4. Connect camera idle and marker click listeners
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        // 5. Connect quick switcher toggle group
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_dataset_group)
        toggleGroup?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_dataset_100k -> switchDataset(DatasetMode.DATASET_100K_SF)
                    R.id.btn_dataset_1m -> switchDataset(DatasetMode.DATASET_1M_USA)
                }
            }
        }

        // 6. Ingest initial dataset (100,000 markers in SF Bay Area)
        switchDataset(DatasetMode.DATASET_100K_SF)
        // [END maps_android_utils_supercluster_100k_demo]

        // 7. Connect floating action button to interactive clustering settings dialog
        findViewById<View>(R.id.fab_cluster_settings)?.setOnClickListener {
            showClusterSettingsDialog()
        }
    }

    private fun switchDataset(mode: DatasetMode) {
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggle_dataset_group)
        val targetButtonId = if (mode == DatasetMode.DATASET_100K_SF) R.id.btn_dataset_100k else R.id.btn_dataset_1m
        if (toggleGroup?.checkedButtonId != targetButtonId) {
            toggleGroup?.check(targetButtonId)
        }

        if (currentDataset == mode && clusterManager.algorithm.items.isNotEmpty()) {
            return
        }
        currentDataset = mode

        val progressContainer = findViewById<View>(R.id.layout_progress_container)
        val progressBar = findViewById<LinearProgressIndicator>(R.id.progress_clustering)
        val textProgress = findViewById<TextView>(R.id.text_clustering_progress)
        progressContainer?.visibility = View.VISIBLE
        progressBar?.setProgressCompat(0, false)
        toggleGroup?.isEnabled = false

        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val loadingMsg = if (mode == DatasetMode.DATASET_100K_SF) {
                getString(R.string.loading_100k_markers)
            } else {
                getString(R.string.loading_1m_markers)
            }
            textProgress?.text = loadingMsg

            if (mode == DatasetMode.DATASET_100K_SF) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.7749, -122.4194), 9f))
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.8283, -98.5795), 4.2f))
            }

            val startBuild = System.currentTimeMillis()
            if (mode == DatasetMode.DATASET_100K_SF) {
                val items = withContext(Dispatchers.Default) { generate100kItems() }
                clusterManager.clearItems()
                clusterManager.addItems(items)
            } else {
                val coords = withContext(Dispatchers.Default) { generate1mCoordinatesUnitedStates() }
                clusterManager.clearItems()
                superClusterAlgorithm.setCoordinates(coords) { id, pos ->
                    MyItem(pos.latitude, pos.longitude, "Gremlin #$id", "1M US Supercluster Point")
                }
            }
            val elapsedMs = System.currentTimeMillis() - startBuild

            gremlinRenderer.clearIconCache()
            clusterManager.cluster()

            toggleGroup?.isEnabled = true

            val countFormatted = if (mode == DatasetMode.DATASET_100K_SF) "100,000" else "1,000,000"
            val region = if (mode == DatasetMode.DATASET_100K_SF) "SF Bay Area" else "United States"
            Toast.makeText(
                this@SuperCluster100kDemoActivity,
                "Indexed $countFormatted gremlins across $region in ${elapsedMs}ms! Tap any cluster for exact count.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun showClusterSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cluster_settings, null)

        val radio100k = dialogView.findViewById<RadioButton>(R.id.radio_dataset_100k)
        val radio1m = dialogView.findViewById<RadioButton>(R.id.radio_dataset_1m)
        if (currentDataset == DatasetMode.DATASET_100K_SF) {
            radio100k.isChecked = true
        } else {
            radio1m.isChecked = true
        }

        val textRadiusTitle = dialogView.findViewById<TextView>(R.id.text_radius_title)
        val sliderRadius = dialogView.findViewById<Slider>(R.id.slider_radius)
        val textMinSizeTitle = dialogView.findViewById<TextView>(R.id.text_minsize_title)
        val sliderMinSize = dialogView.findViewById<Slider>(R.id.slider_minsize)
        val textDigitsTitle = dialogView.findViewById<TextView>(R.id.text_digits_title)
        val sliderDigits = dialogView.findViewById<Slider>(R.id.slider_digits)
        val switchShowExact = dialogView.findViewById<SwitchMaterial>(R.id.switch_show_exact)
        val switchCompact = dialogView.findViewById<SwitchMaterial>(R.id.switch_compact_notation)
        val switchUppercase = dialogView.findViewById<SwitchMaterial>(R.id.switch_uppercase_units)

        // Bind initial values
        val currentRadius = superClusterAlgorithm.radius.toInt()
        sliderRadius.value = currentRadius.toFloat().coerceIn(sliderRadius.valueFrom, sliderRadius.valueTo)
        textRadiusTitle.text = getString(R.string.clustering_settings_radius_format, currentRadius)
        sliderRadius.addOnChangeListener { _, value, _ ->
            textRadiusTitle.text = getString(R.string.clustering_settings_radius_format, value.toInt())
        }

        val currentMinSize = gremlinRenderer.minClusterSize
        sliderMinSize.value = currentMinSize.toFloat().coerceIn(sliderMinSize.valueFrom, sliderMinSize.valueTo)
        textMinSizeTitle.text = getString(R.string.clustering_settings_minsize_format, currentMinSize)
        sliderMinSize.addOnChangeListener { _, value, _ ->
            textMinSizeTitle.text = getString(R.string.clustering_settings_minsize_format, value.toInt())
        }

        val currentDigits = gremlinRenderer.maxNonZeroDigits
        sliderDigits.value = currentDigits.toFloat().coerceIn(sliderDigits.valueFrom, sliderDigits.valueTo)
        textDigitsTitle.text = getString(R.string.clustering_settings_digits_format, currentDigits)
        sliderDigits.addOnChangeListener { _, value, _ ->
            textDigitsTitle.text = getString(R.string.clustering_settings_digits_format, value.toInt())
        }

        switchShowExact.isChecked = gremlinRenderer.showExactCount
        switchCompact.isChecked = gremlinRenderer.useCompactNumberFormatting
        switchUppercase.isChecked = gremlinRenderer.compactUnitUppercase

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clustering_settings_title)
            .setView(dialogView)
            .setPositiveButton(R.string.clustering_settings_apply) { _, _ ->
                val newRadius = sliderRadius.value.toDouble()
                val newMinSize = sliderMinSize.value.toInt()
                val newDigits = sliderDigits.value.toInt()
                val newShowExact = switchShowExact.isChecked
                val newCompact = switchCompact.isChecked
                val newUppercase = switchUppercase.isChecked

                gremlinRenderer.minClusterSize = newMinSize
                gremlinRenderer.maxNonZeroDigits = newDigits
                gremlinRenderer.showExactCount = newShowExact
                gremlinRenderer.useCompactNumberFormatting = newCompact
                gremlinRenderer.compactUnitUppercase = newUppercase

                if (superClusterAlgorithm.radius != newRadius) {
                    superClusterAlgorithm.radius = newRadius
                }

                gremlinRenderer.clearIconCache()
                clusterManager.cluster()

                val selectedDataset = if (radio100k.isChecked) DatasetMode.DATASET_100K_SF else DatasetMode.DATASET_1M_USA
                if (selectedDataset != currentDataset) {
                    switchDataset(selectedDataset)
                }

                Toast.makeText(
                    this,
                    "Settings applied: Radius ${newRadius.toInt()}px, MinSize $newMinSize, Digits $newDigits",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            .setNeutralButton(R.string.clustering_settings_reset) { _, _ ->
                superClusterAlgorithm.radius = 140.0
                gremlinRenderer.minClusterSize = 2
                gremlinRenderer.maxNonZeroDigits = 1
                gremlinRenderer.showExactCount = false
                gremlinRenderer.useCompactNumberFormatting = true
                gremlinRenderer.compactUnitUppercase = false

                gremlinRenderer.clearIconCache()
                clusterManager.cluster()

                if (currentDataset != DatasetMode.DATASET_100K_SF) {
                    switchDataset(DatasetMode.DATASET_100K_SF)
                }

                Toast.makeText(this, "Reset to default clustering settings", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private class GremlinClusterRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<MyItem>,
    ) : DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

        private val gremlinIcon: BitmapDescriptor =
            BitmapDescriptorFactory.fromResource(R.drawable.gremlin_marker)

        init {
            minClusterSize = 2 // Keeps on-screen density clean by grouping 2+ items into badges
            buckets = MEGA_BUCKETS
        }

        override fun getColor(clusterSize: Int): Int {
            // Logarithmic color mapping across stops from 2 to 100,000+ points
            val stops = COLOR_STOPS
            if (clusterSize <= stops.first().size) return stops.first().color
            if (clusterSize >= stops.last().size) return stops.last().color

            val logSize = ln(clusterSize.toDouble())
            for (i in 0 until stops.size - 1) {
                val lower = stops[i]
                val upper = stops[i + 1]
                if (clusterSize in lower.size..upper.size) {
                    val logLower = ln(lower.size.toDouble())
                    val logUpper = ln(upper.size.toDouble())
                    val ratio = ((logSize - logLower) / (logUpper - logLower)).toFloat()
                    return interpolateColor(lower.color, upper.color, ratio)
                }
            }
            return stops.last().color
        }

        private fun interpolateColor(c1: Int, c2: Int, ratio: Float): Int {
            val r = (Color.red(c1) + ratio * (Color.red(c2) - Color.red(c1))).toInt().coerceIn(0, 255)
            val g = (Color.green(c1) + ratio * (Color.green(c2) - Color.green(c1))).toInt().coerceIn(0, 255)
            val b = (Color.blue(c1) + ratio * (Color.blue(c2) - Color.blue(c1))).toInt().coerceIn(0, 255)
            return Color.rgb(r, g, b)
        }

        override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
            markerOptions
                .icon(gremlinIcon)
                .anchor(0.5f, 0.5f)
                .title(item.title ?: "Gremlin")
                .snippet("Mischievous Gremlin!")
        }

        override fun onClusterItemUpdated(item: MyItem, marker: Marker) {
            marker.setIcon(gremlinIcon)
        }

        companion object {
            private val MEGA_BUCKETS = intArrayOf(
                10, 25, 50, 100, 250, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000, 250_000, 500_000, 1_000_000,
            )

            private class ColorStop(val size: Int, val color: Int)

            private val COLOR_STOPS = listOf(
                ColorStop(2, Color.rgb(30, 136, 229)),       // Blue (2-10)
                ColorStop(10, Color.rgb(0, 172, 193)),      // Teal / Cyan (10-25)
                ColorStop(25, Color.rgb(67, 160, 71)),      // Vibrant Green (25-50)
                ColorStop(100, Color.rgb(124, 179, 66)),    // Lime Green (50-200)
                ColorStop(500, Color.rgb(251, 192, 45)),    // Sunny Yellow (250-500)
                ColorStop(1_500, Color.rgb(251, 140, 0)),   // Vivid Orange (1k-2.5k)
                ColorStop(5_000, Color.rgb(229, 57, 53)),   // Crimson Red (2.5k-10k)
                ColorStop(15_000, Color.rgb(216, 27, 96)),  // Deep Magenta (10k-25k)
                ColorStop(40_000, Color.rgb(142, 36, 170)), // Royal Purple (25k-50k)
                ColorStop(100_000, Color.rgb(49, 27, 146)), // Deep Midnight Indigo (50k-100k)
                ColorStop(300_000, Color.rgb(74, 20, 140)), // Deep Violet (100k-300k)
                ColorStop(1_000_000, Color.rgb(136, 14, 79)), // Electric Berry / Crimson Plum (300k-1M+)
            )
        }
    }

    private fun generate100kItems(): List<MyItem> {
        val count = 100_000
        val random = Random(42)
        val items = ArrayList<MyItem>(count)

        // Centered around the San Francisco Bay Area across California
        val centerLat = 37.7749
        val centerLng = -122.4194

        for (i in 0 until count) {
            // Gaussian-like concentration near urban centers with regional dispersal
            val dLat = (random.nextDouble() - 0.5) * 4.0
            val dLng = (random.nextDouble() - 0.5) * 4.0
            items.add(MyItem(centerLat + dLat, centerLng + dLng, "Gremlin #$i", "100k Supercluster Point"))
        }
        return items
    }

    private fun generate1mCoordinatesUnitedStates(): DoubleArray {
        val count = 1_000_000
        val random = Random(1337)
        val coords = DoubleArray(2 * count)

        // Metropolitan population centers across the United States [lat, lng, spreadDeg]
        val hubs = arrayOf(
            doubleArrayOf(40.71, -74.00, 1.2),  // New York / Tri-State
            doubleArrayOf(34.05, -118.24, 1.3), // Los Angeles / SoCal
            doubleArrayOf(41.87, -87.63, 1.0),  // Chicago / Great Lakes
            doubleArrayOf(29.76, -95.36, 1.2),  // Houston
            doubleArrayOf(32.77, -96.79, 1.0),  // Dallas-Fort Worth
            doubleArrayOf(37.77, -122.41, 0.8), // SF Bay Area
            doubleArrayOf(33.74, -84.38, 1.1),  // Atlanta
            doubleArrayOf(25.76, -80.19, 0.9),  // Miami / South Florida
            doubleArrayOf(47.60, -122.33, 0.9), // Seattle / Puget Sound
            doubleArrayOf(39.73, -104.99, 0.9), // Denver / Front Range
            doubleArrayOf(33.44, -112.07, 0.9), // Phoenix / Valley of the Sun
            doubleArrayOf(42.36, -71.05, 0.8),  // Boston / New England
            doubleArrayOf(38.90, -77.03, 1.0),  // Washington DC / Baltimore
            doubleArrayOf(44.97, -93.26, 0.8),  // Minneapolis-St. Paul
            doubleArrayOf(38.62, -90.19, 0.9),  // St. Louis
            doubleArrayOf(36.16, -86.78, 0.8),  // Nashville
            doubleArrayOf(30.26, -97.74, 0.8),  // Austin / Central Texas
            doubleArrayOf(45.51, -122.67, 0.8), // Portland / Willamette
            doubleArrayOf(35.22, -80.84, 0.8),  // Charlotte
            doubleArrayOf(28.53, -81.37, 0.8),  // Orlando / Central Florida
            doubleArrayOf(39.95, -75.16, 0.9),  // Philadelphia
            doubleArrayOf(42.33, -83.04, 0.9),  // Detroit
            doubleArrayOf(39.76, -86.15, 0.8),  // Indianapolis
            doubleArrayOf(39.09, -94.57, 0.8),  // Kansas City
            doubleArrayOf(36.17, -115.13, 0.8), // Las Vegas
            doubleArrayOf(40.76, -111.89, 0.8), // Salt Lake City
        )

        // 650,000 points clustered around population hubs
        val hubCount = 650_000
        for (i in 0 until hubCount) {
            val hub = hubs[random.nextInt(hubs.size)]
            val dLat = (random.nextDouble() - 0.5) * 2 * hub[2]
            val dLng = (random.nextDouble() - 0.5) * 2 * hub[2]
            coords[2 * i] = hub[0] + dLat
            coords[2 * i + 1] = hub[1] + dLng
        }

        // 350,000 points broadly dispersed across the contiguous US landmass
        val broadCount = count - hubCount
        for (i in 0 until broadCount) {
            val idx = hubCount + i
            val lat = 25.5 + random.nextDouble() * 23.0  // 25.5°N to 48.5°N
            val lng = -124.0 + random.nextDouble() * 57.0 // -124.0°W to -67.0°W
            coords[2 * idx] = lat
            coords[2 * idx + 1] = lng
        }

        return coords
    }

    @Suppress("DEPRECATION")
    private fun getScreenDimensionsDp(): Pair<Int, Int> {
        val widthPixels: Int
        val heightPixels: Int
        val density: Float

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            widthPixels = bounds.width()
            heightPixels = bounds.height()
            density = resources.displayMetrics.density
        } else {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            widthPixels = metrics.widthPixels
            heightPixels = metrics.heightPixels
            density = metrics.density
        }

        val widthDp = (widthPixels / density).toInt()
        val heightDp = (heightPixels / density).toInt()
        return Pair(widthDp, heightDp)
    }
}
