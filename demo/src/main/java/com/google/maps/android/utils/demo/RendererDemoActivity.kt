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

package com.google.maps.android.utils.demo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.renderer.GoogleMapRenderer
import com.google.maps.android.renderer.model.Layer
import com.google.maps.android.data.renderer.model.Layer as OnionLayer
import com.google.maps.android.renderer.model.Marker
import com.google.maps.android.utils.demo.databinding.RendererDemoBinding
import kotlinx.coroutines.launch

/**
 * Demo activity for the new Renderer system.
 */
class RendererDemoActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: RendererDemoBinding
    private lateinit var map: GoogleMap
    private lateinit var googleMapRenderer: GoogleMapRenderer
    private lateinit var mapViewRenderer: MapViewRenderer

    private var peaksLayer: OnionLayer? = null
    private var rangesLayer: OnionLayer? = null
    private val addedLayers = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<OnionLayer, Boolean>())

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
            android.util.Log.d(
                "RendererDemo",
                "Maps SDK initialized with renderer: $renderer"
            )
        }

        binding = RendererDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMapRenderer = GoogleMapRenderer(map)
        val iconProvider = UrlIconProvider(lifecycleScope)
        mapViewRenderer = MapViewRenderer(map, iconProvider)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.422, -122.084), 10f))
        addDefaultLayer()

        binding.clearLayersButton.setOnClickListener {
            googleMapRenderer.clear()
            mapViewRenderer.clear()
            addedLayers.clear()
        }

        binding.loadFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = arrayOf("application/vnd.google-earth.kml+xml", "application/json", "application/geo+json")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            filePickerLauncher.launch(intent)
        }

        binding.loadTopPeaksButton.setOnClickListener {
            toggleLayer(peaksLayer, "top_peaks.kml") { layer -> peaksLayer = layer }
        }

        binding.loadMountainRangesButton.setOnClickListener {
            toggleLayer(rangesLayer, "mountain_ranges.kml") { layer -> rangesLayer = layer }
        }

        binding.advancedMarkersSwitch.setOnCheckedChangeListener { _, isChecked ->
            mapViewRenderer.useAdvancedMarkers = isChecked
            Toast.makeText(this, "Advanced Markers: $isChecked", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Renderer Demo Loaded", Toast.LENGTH_SHORT).show()
    }

    private fun addDefaultLayer() {
        val layer = Layer()
        // Add a Marker
        val marker = Marker(LatLng(37.422, -122.084)).apply {
            title = "Googleplex"
            snippet = "Mountain View, CA"
        }
        layer.addMapObject(marker)
        googleMapRenderer.addLayer(layer)
    }

    private fun toggleLayer(layer: OnionLayer?, filename: String, onLayerLoaded: (OnionLayer) -> Unit) {
        if (layer != null) {
            if (addedLayers.contains(layer)) {
                mapViewRenderer.removeLayer(layer)
                addedLayers.remove(layer)
                Toast.makeText(this, "Removed $filename", Toast.LENGTH_SHORT).show()
            } else {
                mapViewRenderer.addLayer(layer)
                addedLayers.add(layer)
                Toast.makeText(this, "Added $filename", Toast.LENGTH_SHORT).show()
            }
        } else {
            lifecycleScope.launch {
                val newLayer = loadLayerFromAsset(filename)
                if (newLayer != null) {
                    onLayerLoaded(newLayer)
                    mapViewRenderer.addLayer(newLayer)
                    addedLayers.add(newLayer)
                    Toast.makeText(this@RendererDemoActivity, "Loaded $filename", Toast.LENGTH_SHORT).show()
                    newLayer.boundingBox?.let { bounds ->
                        try {
                            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadLayerFromAsset(filename: String): OnionLayer? {
        return try {
            val inputStream = assets.open(filename)
            val kml = KmlParser().parse(inputStream)
            KmlMapper.toLayer(kml)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@RendererDemoActivity, "Error loading $filename", Toast.LENGTH_SHORT).show()
            null
        }
    }

    // kept for reference or other usages if any, but currently replaced by toggleLayer logic for buttons
    private fun loadAssetKml(filename: String) {
        lifecycleScope.launch {
            val layer = loadLayerFromAsset(filename)
            if (layer != null) {
                mapViewRenderer.addLayer(layer)
                addedLayers.add(layer)
                layer.boundingBox?.let { bounds ->
                    try {
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Toast.makeText(this@RendererDemoActivity, "Loaded $filename", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGeoFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                when (contentResolver.getType(uri)) {
                    "application/vnd.google-earth.kml+xml" -> {
                        val kml = KmlParser().parse(inputStream)
                        val layer = KmlMapper.toLayer(kml)
                        android.util.Log.d("RendererDemo", "Parsed KML features: ${layer.features.size}")
                        layer.features.forEach { feature ->
                            android.util.Log.d("RendererDemo", "Feature: ${feature.geometry}, Style: ${feature.style}")
                        }
                        mapViewRenderer.addLayer(layer)
                        layer.boundingBox?.let { bounds ->
                            try {
                                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    "application/json", "application/geo+json" -> {
                        val geoJson = GeoJsonParser().parse(inputStream)
                        if (geoJson != null) {
                            val layer = GeoJsonMapper.toLayer(geoJson)
                            mapViewRenderer.addLayer(layer)
                            layer.boundingBox?.let { bounds ->
                                try {
                                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            Toast.makeText(this@RendererDemoActivity, "Failed to parse GeoJSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        Toast.makeText(this@RendererDemoActivity, "Unsupported file type", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RendererDemoActivity, "Error loading file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}