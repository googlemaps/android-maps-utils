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

import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.FlexiblePoint
import com.google.maps.android.FlexiblePolyline
import com.google.maps.android.FlexiblePolylineUtil
import com.google.maps.android.ThirdDimension
import com.google.maps.android.utils.demo.databinding.ActivityFlexiblePolylineDemoBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Demonstrates encoding, decoding, and scrubbing synthetic 4D flexible polyline tracks carrying
 * sub-metre coordinates (`precision = 6`, ~11 cm), decimetre vertical height
 * ([ThirdDimension.ELEVATION] or [ThirdDimension.ALTITUDE], `thirdDimensionPrecision = 1`, 10 cm),
 * and sub-second timestamps (`timePrecision = 1`, 100 ms).
 */
class FlexiblePolylineDemoActivity :
    BaseDemoActivity(),
    SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: ActivityFlexiblePolylineDemoBinding
    private lateinit var decodedTrack: FlexiblePolyline
    private var backgroundPolyline: Polyline? = null
    private var activePolyline: Polyline? = null
    private var positionMarker: Marker? = null
    private var playbackJob: Job? = null
    private var currentFraction: Double = 0.0

    override fun getLayoutId(): Int = R.layout.activity_flexible_polyline_demo

    override fun startDemo(isRestore: Boolean) {
        val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        binding = ActivityFlexiblePolylineDemoBinding.bind(rootView)

        binding.presetHikeButton.setOnClickListener {
            loadPreset(TrackPreset.ALPINE_RUN, moveCamera = true)
        }
        binding.presetBikeButton.setOnClickListener {
            loadPreset(TrackPreset.BIKE_CLIMB, moveCamera = true)
        }
        binding.presetFlightButton.setOnClickListener {
            loadPreset(TrackPreset.SCENIC_FLIGHT, moveCamera = true)
        }

        binding.timeSeekBar.setOnSeekBarChangeListener(this)
        binding.restartButton.setOnClickListener {
            stopPlayback()
            updateScrubber(0.0)
            startPlayback()
        }
        binding.playPauseButton.setOnClickListener {
            if (playbackJob?.isActive == true) {
                stopPlayback()
            } else {
                startPlayback()
            }
        }

        loadPreset(TrackPreset.ALPINE_RUN, moveCamera = !isRestore)
    }

    private fun loadPreset(preset: TrackPreset, moveCamera: Boolean) {
        val map = map ?: return
        stopPlayback()

        binding.trackTitleText.setText(preset.titleResId)

        // [START maps_android_utils_flexible_polyline_encode_4d]
        val encodedTrack =
            FlexiblePolylineUtil.encode(
                points = preset.points,
                precision = 6, // ~11 cm lat/lng resolution
                thirdDimension = preset.thirdDimension,
                thirdDimensionPrecision = 1, // 10 cm vertical resolution
                hasTime = true,
                timePrecision = 1, // 100 ms timestamp resolution
            )
        // [END maps_android_utils_flexible_polyline_encode_4d]

        // [START maps_android_utils_flexible_polyline_decode_4d]
        decodedTrack = FlexiblePolylineUtil.decode(encodedTrack)
        // [END maps_android_utils_flexible_polyline_decode_4d]

        binding.headerMetaText.text =
            getString(
                R.string.flexible_polyline_header_fmt,
                decodedTrack.points.size,
                encodedTrack.length,
                decodedTrack.precision,
                decodedTrack.thirdDimension.name,
                decodedTrack.thirdDimensionPrecision,
                decodedTrack.timePrecision,
            )

        backgroundPolyline?.remove()
        backgroundPolyline =
            map.addPolyline(
                PolylineOptions()
                    .addAll(decodedTrack.toLatLngs())
                    .width(BACKGROUND_POLYLINE_WIDTH)
                    .color(BACKGROUND_POLYLINE_COLOR),
            )

        if (moveCamera) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(preset.cameraTarget, preset.cameraZoom))
        }

        updateScrubber(0.0)
        startPlayback()
    }

    private fun startPlayback() {
        stopPlayback()
        binding.playPauseButton.setText(R.string.polyline_progress_pause)
        playbackJob =
            lifecycleScope.launch {
                while (true) {
                    val next = if (currentFraction >= 1.0) 0.0 else (currentFraction + PLAYBACK_STEP).coerceAtMost(1.0)
                    updateScrubber(next)
                    delay(PLAYBACK_DELAY_MS)
                }
            }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        if (::binding.isInitialized) {
            binding.playPauseButton.setText(R.string.flexible_polyline_play)
        }
    }

    private fun updateScrubber(fraction: Double) {
        currentFraction = fraction
        binding.timeSeekBar.progress = (fraction * SEEK_BAR_MAX).toInt()

        val startMillis = decodedTrack.points.first().timestampMillis ?: return
        val durationMillis = decodedTrack.durationMillis ?: return
        val targetMillis = startMillis + (durationMillis * fraction).toLong()

        // [START maps_android_utils_flexible_polyline_interpolate]
        val currentPoint = decodedTrack.interpolateAtTime(targetMillis) ?: return
        val prefixLatLngs = decodedTrack.getPrefixAtTime(targetMillis).map(FlexiblePoint::toLatLng)
        // [END maps_android_utils_flexible_polyline_interpolate]

        val verticalMeters = currentPoint.thirdDimensionValue ?: 0.0
        val elapsedSeconds = (targetMillis - startMillis) / 1000.0
        val totalSeconds = durationMillis / 1000.0

        binding.telemetryText.text =
            getString(
                R.string.flexible_polyline_telemetry_fmt,
                decodedTrack.thirdDimension.name,
                verticalMeters,
                elapsedSeconds,
                totalSeconds,
            )

        activePolyline?.remove()
        if (prefixLatLngs.isNotEmpty()) {
            activePolyline =
                map?.addPolyline(
                    PolylineOptions()
                        .addAll(prefixLatLngs)
                        .width(ACTIVE_POLYLINE_WIDTH)
                        .color(ACTIVE_POLYLINE_COLOR)
                        .zIndex(1f),
                )
        }

        val latLng = currentPoint.toLatLng()
        val marker = positionMarker
        if (marker == null) {
            positionMarker =
                map?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(binding.trackTitleText.text.toString()),
                )
        } else {
            marker.position = latLng
            marker.title = binding.trackTitleText.text.toString()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            stopPlayback()
            updateScrubber(progress.toDouble() / SEEK_BAR_MAX)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        stopPlayback()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    /**
     * Purely synthetic 4D track presets illustrating [ThirdDimension.ELEVATION] and
     * [ThirdDimension.ALTITUDE] with sub-second timestamps.
     */
    private enum class TrackPreset(
        @StringRes val titleResId: Int,
        val thirdDimension: ThirdDimension,
        val cameraTarget: LatLng,
        val cameraZoom: Float,
        val points: List<FlexiblePoint>,
    ) {
        ALPINE_RUN(
            titleResId = R.string.flexible_polyline_title_hike,
            thirdDimension = ThirdDimension.ELEVATION,
            cameraTarget = LatLng(46.7980, -121.7280),
            cameraZoom = 13.5f,
            points = generateAlpineTrailTrack(),
        ),
        BIKE_CLIMB(
            titleResId = R.string.flexible_polyline_title_bike,
            thirdDimension = ThirdDimension.ELEVATION,
            cameraTarget = LatLng(37.8285, -122.4995),
            cameraZoom = 14.2f,
            points = generateBikeClimbTrack(),
        ),
        SCENIC_FLIGHT(
            titleResId = R.string.flexible_polyline_title_flight,
            thirdDimension = ThirdDimension.ALTITUDE,
            cameraTarget = LatLng(-33.8540, 151.2150),
            cameraZoom = 13.2f,
            points = generateScenicFlightTrack(),
        ),
    }

    private companion object {
        const val BASE_TIME_MILLIS = 1_767_254_400_000L // 2026-01-01T08:00:00Z
        const val POINT_COUNT = 240
        const val BACKGROUND_POLYLINE_WIDTH = 10f
        const val ACTIVE_POLYLINE_WIDTH = 14f
        const val BACKGROUND_POLYLINE_COLOR = 0x665F6368
        const val ACTIVE_POLYLINE_COLOR = 0xFF1A73E8.toInt()
        const val SEEK_BAR_MAX = 1000
        const val PLAYBACK_STEP = 0.008
        const val PLAYBACK_DELAY_MS = 50L

        /** Generates a synthetic 240-point alpine switchback trail near Mount Rainier. */
        fun generateAlpineTrailTrack(): List<FlexiblePoint> {
            var currentMillis = BASE_TIME_MILLIS
            return (0 until POINT_COUNT).map { step ->
                val p = step / (POINT_COUNT - 1.0)
                val lat = 46.786000 + p * 0.024000 + 0.001600 * sin(p * 18.0)
                val lng = -121.736000 + p * 0.016000 + 0.002200 * cos(p * 18.0)
                val elevation = 1_650.0 + 630.0 * sin(p * Math.PI) + 14.0 * sin(p * 28.0)
                if (step > 0) {
                    // Pace slows during the first-half climb and quickens on the descent
                    val stepDurationMillis = if (p < 0.5) 2_400L else 1_500L
                    currentMillis += stepDurationMillis
                }
                FlexiblePoint(lat, lng, elevation, currentMillis)
            }
        }

        /** Generates a synthetic 240-point hairpin cycling loop in the Marin Headlands. */
        fun generateBikeClimbTrack(): List<FlexiblePoint> {
            var currentMillis = BASE_TIME_MILLIS
            return (0 until POINT_COUNT).map { step ->
                val p = step / (POINT_COUNT - 1.0)
                val angle = p * 2.0 * Math.PI
                val lat = 37.828500 + 0.008500 * sin(angle) + 0.001200 * sin(angle * 6.0)
                val lng = -122.499500 + 0.011000 * cos(angle) + 0.001200 * cos(angle * 6.0)
                val elevation = 25.0 + 255.0 * sin(p * Math.PI)
                if (step > 0) {
                    val stepDurationMillis = if (p < 0.5) 1_800L else 700L
                    currentMillis += stepDurationMillis
                }
                FlexiblePoint(lat, lng, elevation, currentMillis)
            }
        }

        /** Generates a synthetic 240-point scenic flight circuit over Sydney Harbour. */
        fun generateScenicFlightTrack(): List<FlexiblePoint> {
            var currentMillis = BASE_TIME_MILLIS
            return (0 until POINT_COUNT).map { step ->
                val p = step / (POINT_COUNT - 1.0)
                val angle = p * 2.0 * Math.PI
                val lat = -33.854000 + 0.014000 * sin(angle)
                val lng = 151.215000 + 0.020000 * cos(angle) * cos(angle * 0.5)
                val altitude = 15.0 + 835.0 * sin(p * Math.PI)
                if (step > 0) {
                    currentMillis += 500L
                }
                FlexiblePoint(lat, lng, altitude, currentMillis)
            }
        }
    }
}
