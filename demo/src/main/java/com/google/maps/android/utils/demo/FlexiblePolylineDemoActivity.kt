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

/**
 * Demonstrates encoding, decoding, and scrubbing a 4D flexible polyline track carrying
 * sub-metre coordinates (`precision = 6`, ~11 cm), decimetre elevation
 * ([ThirdDimension.ELEVATION], `thirdDimensionPrecision = 1`, 10 cm), and sub-second timestamps
 * (`timePrecision = 1`, 100 ms).
 */
class FlexiblePolylineDemoActivity :
    BaseDemoActivity(),
    SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: ActivityFlexiblePolylineDemoBinding
    private lateinit var decodedTrack: FlexiblePolyline
    private var activePolyline: Polyline? = null
    private var positionMarker: Marker? = null
    private var playbackJob: Job? = null
    private var currentFraction: Double = 0.0

    override fun getLayoutId(): Int = R.layout.activity_flexible_polyline_demo

    override fun startDemo(isRestore: Boolean) {
        val map = map ?: return
        val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        binding = ActivityFlexiblePolylineDemoBinding.bind(rootView)

        val encodedTrack =
            FlexiblePolylineUtil.encode(
                points = BRIDGE_CLIMB_TRACK,
                precision = 6,
                thirdDimension = ThirdDimension.ELEVATION,
                thirdDimensionPrecision = 1,
                hasTime = true,
                timePrecision = 1,
            )
        decodedTrack = FlexiblePolylineUtil.decode(encodedTrack)

        binding.headerMetaText.text =
            getString(
                R.string.flexible_polyline_header_fmt,
                decodedTrack.points.size,
                decodedTrack.precision,
                decodedTrack.thirdDimension.name,
                decodedTrack.thirdDimensionPrecision,
                decodedTrack.timePrecision,
            )

        map.addPolyline(
            PolylineOptions()
                .addAll(decodedTrack.toLatLngs())
                .width(BACKGROUND_POLYLINE_WIDTH)
                .color(BACKGROUND_POLYLINE_COLOR),
        )

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

        if (!isRestore) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(CAMERA_TARGET, CAMERA_ZOOM))
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

        val currentPoint = decodedTrack.interpolateAtTime(targetMillis) ?: return
        val elevationMeters = currentPoint.thirdDimensionValue ?: 0.0
        val elapsedSeconds = (targetMillis - startMillis) / 1000.0
        val totalSeconds = durationMillis / 1000.0

        binding.telemetryText.text =
            getString(
                R.string.flexible_polyline_telemetry_fmt,
                elevationMeters,
                elapsedSeconds,
                totalSeconds,
            )

        val prefixLatLngs = decodedTrack.getPrefixAtTime(targetMillis).map(FlexiblePoint::toLatLng)
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
                        .title(getString(R.string.flexible_polyline_track_title)),
                )
        } else {
            marker.position = latLng
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

    private companion object {
        const val BASE_TIME_MILLIS = 1_761_403_200_000L

        /**
         * Ten 4D points across the Sydney Harbour Bridge arch: `(lat, lng, elevationMeters, timestampMillis)`.
         * Elevation rises from deck level (~52 m) to the summit of the arch (134.00 m) and back down.
         */
        val BRIDGE_CLIMB_TRACK =
            listOf(
                FlexiblePoint(-33.8496000, 151.2116300, 52.10, BASE_TIME_MILLIS),
                FlexiblePoint(-33.8504500, 151.2114500, 68.45, BASE_TIME_MILLIS + 4_850L),
                FlexiblePoint(-33.8513000, 151.2112700, 91.20, BASE_TIME_MILLIS + 9_925L),
                FlexiblePoint(-33.8521500, 151.2110900, 114.80, BASE_TIME_MILLIS + 15_120L),
                FlexiblePoint(-33.8530000, 151.2109100, 131.65, BASE_TIME_MILLIS + 20_375L),
                FlexiblePoint(-33.8538500, 151.2107300, 134.00, BASE_TIME_MILLIS + 25_000L),
                FlexiblePoint(-33.8547000, 151.2105500, 119.35, BASE_TIME_MILLIS + 30_150L),
                FlexiblePoint(-33.8555500, 151.2103700, 95.70, BASE_TIME_MILLIS + 35_280L),
                FlexiblePoint(-33.8564000, 151.2101900, 71.15, BASE_TIME_MILLIS + 40_110L),
                FlexiblePoint(-33.8572500, 151.2100200, 53.00, BASE_TIME_MILLIS + 45_000L),
            )

        val CAMERA_TARGET = LatLng(-33.85343, 151.21083)
        const val CAMERA_ZOOM = 15f
        const val BACKGROUND_POLYLINE_WIDTH = 10f
        const val ACTIVE_POLYLINE_WIDTH = 14f
        const val BACKGROUND_POLYLINE_COLOR = 0x665F6368
        const val ACTIVE_POLYLINE_COLOR = 0xFF1A73E8.toInt()
        const val SEEK_BAR_MAX = 1000
        const val PLAYBACK_STEP = 0.01
        const val PLAYBACK_DELAY_MS = 60L
    }
}
