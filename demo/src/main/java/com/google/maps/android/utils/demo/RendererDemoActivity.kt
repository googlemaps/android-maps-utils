/*
 * Copyright 2026 Google LLC
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

package com.google.maps.android.utils.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.maps.android.data.DataLayerLoader
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.utils.demo.databinding.RendererDemoBinding
import kotlinx.coroutines.launch
import com.google.maps.android.data.renderer.model.DataLayer

/**
 * Demo activity for the new Renderer system.
 *
 * This activity demonstrates:
 * 1.  **Unified Rendering**: Using `MapViewRenderer` to render KML, GeoJSON, and GPX data.
 * 2.  **Modern UI**: A full-screen map experience with a persistent Bottom Sheet for controls.
 * 3.  **Layer Management**: Toggling layers (Peaks, Ranges) and handling multiple data sources.
 * 4.  **Feature Interaction**: Loading external files via the system file picker.
 * 5.  **Advanced Markers**: Toggling between legacy and Advanced Markers.
 */
class RendererDemoActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: RendererDemoBinding
    private lateinit var map: GoogleMap
    private lateinit var mapViewRenderer: MapViewRenderer

    private val addedLayers = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<DataLayer, Boolean>())
    
    // Map Chip ID to loaded DataLayer
    private val chipLayers = mutableMapOf<Int, DataLayer>()

    private val layerCache = mutableMapOf<String, DataLayer>()

    private val dataLayerLoader = DataLayerLoader(this)

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                loadGeoFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
            Log.d(
                "RendererDemo",
                "Maps SDK initialized with renderer: $renderer"
            )
        }

        binding = RendererDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply bottom inset to Bottom Sheet (padding)
            binding.bottomSheet.setPadding(
                binding.bottomSheet.paddingLeft,
                binding.bottomSheet.paddingTop,
                binding.bottomSheet.paddingRight,
                insets.bottom + (16 * resources.displayMetrics.density).toInt() // Original padding + inset
            )

            WindowInsetsCompat.CONSUMED
        }

        val mapFragment: SupportMapFragment
        val existingFragment = supportFragmentManager.findFragmentById(R.id.map_container)
        if (existingFragment is SupportMapFragment) {
            mapFragment = existingFragment
        } else {
            val app = application as DemoApplication
            val mapId = app.mapId
            
            val mapOptions = com.google.android.gms.maps.GoogleMapOptions()
            if (mapId != null) {
                mapOptions.mapId(mapId)
            }
            
            mapFragment = SupportMapFragment.newInstance(mapOptions)
            supportFragmentManager.beginTransaction()
                .add(R.id.map_container, mapFragment)
                .commit()
        }
            
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val iconProvider = UrlIconProvider()
        mapViewRenderer = MapViewRenderer(map, iconProvider)

        // Add padding to the map to account for the bottom sheet's peek height
        val behavior = BottomSheetBehavior.from(binding.bottomSheet)
        map.setPadding(0, 0, 0, behavior.peekHeight)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.422, -122.084), 10f))
        addDefaultLayer()

        binding.btnLoadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            filePickerLauncher.launch(intent)
        }

        binding.btnClear.setOnClickListener {
            mapViewRenderer.clear()
            addedLayers.clear()
            chipLayers.clear()
            binding.chipGroupLayers.clearCheck()
        }
        
        binding.btnToggleSheet.setOnClickListener {
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.chipPeaks.setOnClickListener {
            onChipClicked(binding.chipPeaks, "top_peaks.kml")
        }

        binding.chipRanges.setOnClickListener {
            onChipClicked(binding.chipRanges, "mountain_ranges.kml")
        }

        binding.chipComplexKml.setOnClickListener {
            onChipClicked(binding.chipComplexKml, "kml-types.kml")
        }

        binding.chipComplexGeojson.setOnClickListener {
            onChipClicked(binding.chipComplexGeojson, "geojson-types.json")
        }

        binding.chipGroundOverlay.setOnClickListener {
            onChipClicked(binding.chipGroundOverlay, "ground_overlay.kml")
        }

        binding.chipBrightAngel.setOnClickListener {
            onChipClicked(binding.chipBrightAngel, "BrightAngel.gpx")
        }

        binding.switchAdvancedMarkers.setOnCheckedChangeListener { _, isChecked ->
            mapViewRenderer.useAdvancedMarkers = isChecked
        }
    }
    
    private fun onChipClicked(chip: Chip, filename: String) {
        if (chip.isChecked) {
            // Load and add
            val existingLayer = chipLayers[chip.id]
            if (existingLayer != null) {
                addLayerToMap(existingLayer)
            } else {
                lifecycleScope.launch {
                    loadLayerFromAsset(filename) { layer ->
                        chipLayers[chip.id] = layer
                        addLayerToMap(layer)
                    }
                }
            }
        } else {
            // Remove
            val layer = chipLayers[chip.id]
            if (layer != null) {
                mapViewRenderer.removeLayer(layer)
                addedLayers.remove(layer)

            }
        }
    }

    private suspend fun loadLayerFromAsset(
        filename: String,
        onSuccess: (DataLayer) -> Unit
    ) {
        dataLayerLoader.loadAsset(filename)?.let { layer ->  onSuccess(layer) }
    }

    private fun addDefaultLayer() {
        // Add a Marker using the new DataRenderer system
        val point = Point(37.422, -122.084)
        val properties = mapOf(
            "name" to "Googleplex",
            "description" to "Mountain View, CA"
        )
        val style = PointStyle(
            iconUrl = null // Use default marker
        )
        val feature = Feature(
            geometry = PointGeometry(point),
            properties = properties,
            style = style
        )
        val layer = DataLayer(features = listOf(feature))
        mapViewRenderer.addLayer(layer)
        addedLayers.add(layer)
    }

    private fun addLayerToMap(layer: DataLayer) {
        mapViewRenderer.addLayer(layer)
        addedLayers.add(layer)

        layer.boundingBox?.let { bounds ->
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadGeoFile(uri: Uri) {
        lifecycleScope.launch {
            val filename = getFileName(uri) ?: uri.toString()

            val cacheKey = uri.toString()

            if (layerCache.containsKey(cacheKey)) {
                val layer = layerCache[cacheKey]!!
                addLayerToMap(layer)
                return@launch
            }

            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val layer = dataLayerLoader.loadInputStream(inputStream, filename)

                if (layer != null) {
                    layerCache[cacheKey] = layer
                    addLayerToMap(layer)
                } else {
                    Toast.makeText(this@RendererDemoActivity, "Unsupported or failed to parse file", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RendererDemoActivity, "Error loading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}