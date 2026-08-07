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

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.addMarker
import com.google.maps.android.awaitAnimateCamera
import com.google.maps.android.awaitMap
import com.google.maps.android.awaitMapsSdkInitialized
import com.google.maps.android.ktx.addCircle as deprecatedBridgeAddCircle
import com.google.maps.android.mapClickEvents
import kotlinx.coroutines.launch

/**
 * A demo activity illustrating the consolidated reactive Coroutine/Flow/Builder extensions
 * in [com.google.maps.android], as well as compatibility verification for the deprecated
 * [com.google.maps.android.ktx] package bridges.
 */
class KtxExtensionsDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ReactiveMapScreen()
            }
        }
    }

    @Composable
    private fun ReactiveMapScreen() {
        val context = LocalContext.current
        val mapView = remember { MapView(context) }

        LaunchedEffect(mapView) {
            // 1. Canonical awaitMapsSdkInitialized() coroutine suspension
            context.awaitMapsSdkInitialized(MapsInitializer.Renderer.LATEST)

            mapView.onCreate(Bundle())
            mapView.onStart()
            mapView.onResume()

            // 2. Canonical awaitMap() coroutine suspension
            val googleMap: GoogleMap = mapView.awaitMap()

            val sydney = LatLng(-33.852, 151.211)
            
            // 3. Canonical addMarker builder DSL
            googleMap.addMarker {
                position(sydney)
                title("Sydney Opera House (Canonical Builder)")
            }

            // 3. Deprecated bridge addCircle check (verifying zero conflicts with canonical builder)
            @Suppress("DEPRECATION")
            googleMap.deprecatedBridgeAddCircle {
                center(LatLng(-33.870, 151.200))
                radius(500.0)
            }

            // 4. Canonical awaitAnimateCamera suspension
            googleMap.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12f), 1500)

            // 5. Canonical Flow observation for map clicks
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    googleMap.mapClickEvents().collect { latLng ->
                        Toast.makeText(
                            context,
                            "Clicked at: ${latLng.latitude}, ${latLng.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { mapView })
        }
    }
}
