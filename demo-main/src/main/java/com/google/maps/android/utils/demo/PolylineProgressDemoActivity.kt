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

import android.graphics.Canvas
import android.graphics.Color
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.utils.demo.databinding.ActivityPolylineProgressDemoBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This demo showcases how to animate a marker along a geodesic polyline, illustrating
 * key features of the Android Maps Utils library and modern Android development practices.
 */
class PolylineProgressDemoActivity : BaseDemoActivity(), SeekBar.OnSeekBarChangeListener {

    companion object {
        private const val POLYLINE_WIDTH = 15f
        private const val PROGRESS_POLYLINE_WIDTH = 7f
        private const val ANIMATION_STEP_SIZE = 1
        private const val ANIMATION_DELAY_MS = 75L
    }

    private lateinit var binding: ActivityPolylineProgressDemoBinding
    private lateinit var originalPolyline: Polyline
    private var progressPolyline: Polyline? = null
    private var progressMarker: Marker? = null

    private val planeIcon: BitmapDescriptor by lazy {
        bitmapDescriptorFromVector(R.drawable.baseline_airplanemode_active_24, "#FFD700".toColorInt())
    }

    private data class AnimationState(val progress: Int, val direction: Int)

    private val animationState = MutableLiveData<AnimationState>()
    private var animationJob: Job? = null

    private val polylinePoints = listOf(
        LatLng(40.7128, -74.0060),   // New York
        LatLng(47.6062, -122.3321),  // Seattle
        LatLng(39.7392, -104.9903),  // Denver
        LatLng(37.7749, -122.4194),  // San Francisco
        LatLng(34.0522, -118.2437),  // Los Angeles
        LatLng(41.8781, -87.6298),   // Chicago
        LatLng(29.7604, -95.3698),   // Houston
        LatLng(39.9526, -75.1652)    // Philadelphia
    )

    override fun getLayoutId(): Int = R.layout.activity_polyline_progress_demo

    /**
     * This is where the demo begins. It is called from the base activity's `onMapReady` callback.
     */
    override fun startDemo(isRestore: Boolean) {
        // The layout is already inflated by the base class. We can now bind to it.
        val rootView = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
        binding = ActivityPolylineProgressDemoBinding.bind(rootView)

        setupMap()
        setupUI()
        // Set the initial state. The observer in setupUI will handle the first UI update.
        animationState.value = AnimationState(progress = 0, direction = 1)
        startAnimation()
    }

    private fun setupMap() {
        originalPolyline = map.addPolyline(
            PolylineOptions()
                .addAll(polylinePoints)
                .color(Color.GRAY)
                .width(POLYLINE_WIDTH)
                .geodesic(true) // A geodesic polyline follows the curvature of the Earth.
        )

        val bounds = LatLngBounds.builder().apply {
            polylinePoints.forEach { include(it) }
        }.build()
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun setupUI() {
        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.resetButton.setOnClickListener {
            stopAnimation()
            animationState.value = AnimationState(progress = 0, direction = 1)
            startAnimation()
        }
        binding.pauseButton.setOnClickListener {
            if (animationJob?.isActive == true) {
                stopAnimation()
            } else {
                startAnimation()
            }
        }

        animationState.observe(this) { state ->
            binding.seekBar.progress = state.progress
            binding.percentageTextView.text = getString(R.string.percentage_format, state.progress)
            updateProgressOnMap(state.progress / 100.0, state.direction)
        }
    }

    private fun startAnimation() {
        stopAnimation()
        val currentState = animationState.value ?: return

        animationJob = lifecycleScope.launch {
            var progress = currentState.progress
            var direction = currentState.direction
            while (true) {
                progress = when {
                    progress > 100 -> {
                        direction = -1
                        100
                    }
                    progress < 0 -> {
                        direction = 1
                        0
                    }
                    else -> progress + direction * ANIMATION_STEP_SIZE
                }

                animationState.postValue(AnimationState(progress, direction))
                delay(ANIMATION_DELAY_MS)
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            stopAnimation()
            animationState.value = AnimationState(progress, animationState.value?.direction ?: 1)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) { /* No-op */ }

    override fun onStopTrackingTouch(seekBar: SeekBar?) { /* No-op */ }

    private fun updateProgressOnMap(percentage: Double, direction: Int) {
        progressPolyline?.remove()

        val prefix = SphericalUtil.getPolylinePrefix(polylinePoints, percentage)
        if (prefix.isNotEmpty()) {
            progressPolyline = map.addPolyline(
                PolylineOptions()
                    .addAll(prefix)
                    .color(Color.BLUE)
                    .width(PROGRESS_POLYLINE_WIDTH)
                    .zIndex(1f)
                    .geodesic(true)
            )
        }

        SphericalUtil.getPointOnPolyline(polylinePoints, percentage)?.let { point ->
            updateMarker(point, percentage, direction)
        }
    }

    private fun updateMarker(point: LatLng, percentage: Double, direction: Int) {
        val heading = SphericalUtil.getPointOnPolyline(polylinePoints, percentage + 0.0001)
            ?.let { SphericalUtil.computeHeading(point, it) }
            ?.let { if (direction == -1) it + 180 else it } // Adjust for reverse direction.

        if (progressMarker == null) {
            progressMarker = map.addMarker(
                MarkerOptions()
                    .position(point)
                    .flat(true)
                    .draggable(false)
                    .icon(planeIcon)
                    .apply { heading?.let { rotation(it.toFloat()) } }
            )
        } else {
            progressMarker?.also {
                it.position = point
                heading?.let { newHeading -> it.rotation = newHeading.toFloat() }
            }
        }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int, color: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)!!
        vectorDrawable.setTint(color)
        val bitmap = createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
