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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.parser.kml.KmzParser
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.utils.demo.databinding.RendererDemoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var peaksLayer: DataLayer? = null
    private var rangesLayer: DataLayer? = null
    private val addedLayers = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<DataLayer, Boolean>())

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
        val iconProvider = UrlIconProvider(lifecycleScope)
        mapViewRenderer = MapViewRenderer(map, iconProvider)
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
            // No need to hide button, it's in the bottom sheet now
            Toast.makeText(this, "Map Cleared", Toast.LENGTH_SHORT).show()
        }

        binding.chipPeaks.setOnClickListener {
            toggleLayer(peaksLayer, "top_peaks.kml") { layer -> peaksLayer = layer }
        }

        binding.chipRanges.setOnClickListener {
            toggleLayer(rangesLayer, "mountain_ranges.kml") { layer -> rangesLayer = layer }
        }

        binding.chipComplexKml.setOnClickListener {
            toggleLayer(null, "kml-types.kml") { /* No persistence needed for demo */ }
        }

        binding.chipComplexGeojson.setOnClickListener {
            lifecycleScope.launch {
                val layer = loadGeoJsonFromAsset("geojson-types.json")
                if (layer != null) {
                    addLayerToMap(layer, "geojson-types.json")
                }
            }
        }

        binding.chipGroundOverlay.setOnClickListener {
            lifecycleScope.launch {
                val layer = loadLayerFromAsset("ground_overlay.kml")
                if (layer != null) {
                    addLayerToMap(layer, "ground_overlay.kml")
                }
            }
        }

        binding.chipBrightAngel.setOnClickListener {
            lifecycleScope.launch {
                val layer = loadGpxFromAsset("BrightAngel.gpx")
                if (layer != null) {
                    addLayerToMap(layer, "BrightAngel.gpx")
                }
            }
        }

        binding.switchAdvancedMarkers.setOnCheckedChangeListener { _, isChecked ->
            mapViewRenderer.useAdvancedMarkers = isChecked
            Toast.makeText(this, "Advanced Markers: $isChecked", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Renderer Demo Loaded", Toast.LENGTH_SHORT).show()
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

    private fun addLayerToMap(layer: DataLayer, filename: String) {
        mapViewRenderer.addLayer(layer)
        addedLayers.add(layer)
        Toast.makeText(this@RendererDemoActivity, "Loaded $filename", Toast.LENGTH_SHORT).show()
        layer.boundingBox?.let { bounds ->
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleLayer(layer: DataLayer?, filename: String, onLayerLoaded: (DataLayer) -> Unit) {
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
                    addLayerToMap(newLayer, filename)
                }
            }
        }
    }

    private suspend fun loadLayerFromAsset(filename: String): DataLayer? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = assets.open(filename)
                val kml = KmlParser().parse(inputStream)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RendererDemoActivity,
                        "Loaded $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                KmlMapper.toLayer(kml)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RendererDemoActivity, "Error loading $filename", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    private suspend fun loadGeoJsonFromAsset(filename: String): DataLayer? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = assets.open(filename)
                val geoJson = GeoJsonParser().parse(inputStream)
                if (geoJson != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RendererDemoActivity,
                            "Loaded $filename",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    GeoJsonMapper.toLayer(geoJson)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RendererDemoActivity, "Error loading $filename", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    private suspend fun loadGpxFromAsset(filename: String): DataLayer? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = assets.open(filename)
                val gpx = com.google.maps.android.data.parser.gpx.GpxParser().parse(inputStream)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RendererDemoActivity,
                        "Loaded $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                com.google.maps.android.data.renderer.mapper.GpxMapper.toLayer(gpx)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RendererDemoActivity, "Error loading $filename", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    private fun loadGeoFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                    val type = contentResolver.getType(uri)
                    val filename = getFileName(uri)
                    android.util.Log.d("RendererDemo", "Loading file with type: $type, filename: $filename, uri: $uri")
                    
                    // Simple extension check fallback if type is generic xml or octet-stream
                    var isGpx = type == "application/gpx+xml" || (filename != null && filename.endsWith(".gpx", ignoreCase = true))
                    var isKml = type == "application/vnd.google-earth.kml+xml" || (filename != null && filename.endsWith(".kml", ignoreCase = true))
                    var isKmz = type == "application/vnd.google-earth.kmz" || (filename != null && filename.endsWith(".kmz", ignoreCase = true))
                    var isGeoJson = type == "application/json" || type == "application/geo+json"
                            || (filename != null && (filename.endsWith(".json", ignoreCase = true) || filename.endsWith(".geojson", ignoreCase = true)))

                    if (!isGpx && !isKml && !isKmz && !isGeoJson) {
                        // Sniff content
                        val header = contentResolver.openInputStream(uri)?.use { stream ->
                            val buffer = ByteArray(1024)
                            val read = stream.read(buffer)
                            if (read > 0) String(buffer, 0, read, java.nio.charset.StandardCharsets.UTF_8) else ""
                        } ?: ""

                        if (com.google.maps.android.data.parser.gpx.GpxParser.canParse(header)) {
                            isGpx = true
                        } else if (KmlParser.canParse(header)) {
                            isKml = true
                        } else if (KmzParser.canParse(header)) {
                            isKmz = true
                        } else if (GeoJsonParser.canParse(header)) {
                            isGeoJson = true
                        }
                    }

                    val layer = when {
                        isKmz -> {
                            val kml = KmzParser().parse(inputStream)
                            KmlMapper.toLayer(kml)
                        }
                        isKml -> {
                            val kml = KmlParser().parse(inputStream)
                            KmlMapper.toLayer(kml)
                        }
                        isGeoJson -> {
                            val geoJson = GeoJsonParser().parse(inputStream)
                            if (geoJson != null) {
                                GeoJsonMapper.toLayer(geoJson)
                            } else {
                                null
                            }
                        }
                        isGpx -> {
                            val gpx = com.google.maps.android.data.parser.gpx.GpxParser().parse(inputStream)
                            com.google.maps.android.data.renderer.mapper.GpxMapper.toLayer(gpx)
                        }
                        else -> null
                    }
                    
                    Triple(layer, filename, type)
                }

                if (result == null) {
                     // InputStream null or other early exit
                     return@launch
                }
                
                val (layer, filename, type) = result

                if (layer != null) {
                    if (type != null && !type.contains("gpx") && !type.contains("kml") && !type.contains("json")) {
                         // It was sniffed or extension matched, but type might be generic
                    }
                    android.util.Log.d("RendererDemo", "Parsed features: ${layer.features.size}")
                    addLayerToMap(layer, filename ?: "External File")
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
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}