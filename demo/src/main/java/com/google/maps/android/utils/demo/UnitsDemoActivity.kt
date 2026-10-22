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

import android.graphics.Color
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.Meters
import com.google.maps.android.UnitSystem
import com.google.maps.android.UnitsConverter
import com.google.maps.android.getUnitsConverter
import com.google.maps.android.km
import com.google.maps.android.meters
import com.google.maps.android.miles
import com.google.maps.android.sphericalDistance
import com.google.maps.android.sphericalPathLength
import com.google.maps.android.toDistanceString
import com.google.maps.android.toElevationString
import java.util.Locale

// [START maps_android_utils_units_kotlin]
/**
 * Demonstrates how to use [Meters], [UnitSystem], [UnitsConverter], [getUnitsConverter],
 * [toDistanceString], and [toElevationString] to format distances and elevations in either
 * the device's default locale unit system or an explicit Metric / Imperial override.
 */
class UnitsDemoActivity : BaseDemoActivity() {

    // [START maps_android_utils_units_demo_model]
    /**
     * Domain model for a mountain summit using the type-safe [Meters] inline value class.
     */
    data class MountainPeak(val name: String, val position: LatLng, val elevation: Meters)

    /**
     * User-selectable unit preference mode, allowing automatic locale detection or explicit
     * Metric / Imperial selection.
     */
    enum class UnitPreference {
        DEVICE_DEFAULT,
        METRIC,
        IMPERIAL,
        ;

        fun resolveConverter(locale: Locale = Locale.getDefault()): UnitsConverter = when (this) {
            DEVICE_DEFAULT -> getUnitsConverter(locale)
            METRIC -> getUnitsConverter(UnitSystem.METRIC)
            IMPERIAL -> getUnitsConverter(UnitSystem.IMPERIAL)
        }
    }

    private val peaks = listOf(
        MountainPeak("Mount Elbert", LatLng(39.1178, -106.4454), 4401.2.meters),
        MountainPeak("Mount Blue Sky", LatLng(39.5883, -105.6438), 4348.0.meters),
        MountainPeak("Longs Peak", LatLng(40.2550, -105.6151), 4346.0.meters),
        MountainPeak("Pikes Peak", LatLng(38.8409, -105.0423), 4302.3.meters),
    )

    private val sampleDistances = listOf(
        "Short (< 0.25 mi)" to 150.meters,
        "Quarter mile (0.25 mi)" to 0.25.miles,
        "Medium (2.0 km)" to 2.0.km,
        "Multi-mile (2.5 mi)" to 2.5.miles,
        "Long (>= 10 km)" to 12.4.km,
    )
    // [END maps_android_utils_units_demo_model]

    private val peakMarkers = mutableListOf<Pair<MountainPeak, Marker>>()
    private var selectedPreference by mutableStateOf(UnitPreference.DEVICE_DEFAULT)

    override fun getLayoutId(): Int = R.layout.activity_units_demo

    override fun startDemo(isRestore: Boolean) {
        val currentMap = map ?: return

        val coordinates = peaks.map { it.position }
        currentMap.addPolyline(
            PolylineOptions()
                .addAll(coordinates)
                .color(Color.parseColor("#1A73E8"))
                .width(10f)
                .geodesic(true),
        )

        peakMarkers.clear()
        val converter = selectedPreference.resolveConverter()
        for (peak in peaks) {
            val marker = currentMap.addMarker(
                MarkerOptions()
                    .position(peak.position)
                    .title(peak.name)
                    .snippet(converter.toElevationString(resources, peak.elevation)),
            )
            if (marker != null) {
                peakMarkers.add(peak to marker)
            }
        }
        peakMarkers.firstOrNull()?.second?.showInfoWindow()

        val bounds = LatLngBounds.builder().apply {
            coordinates.forEach { include(it) }
        }.build()
        var hasCenteredCamera = isRestore

        val composeView = findViewById<ComposeView>(R.id.units_control_panel)
        composeView.setContent {
            MaterialTheme {
                UnitsControlPanel(
                    preference = selectedPreference,
                    onPreferenceSelected = { newPreference ->
                        selectedPreference = newPreference
                        refreshMarkerSnippets(newPreference.resolveConverter())
                    },
                    onPanelHeightChanged = { heightPx ->
                        if (heightPx > 0) {
                            currentMap.setPadding(0, 0, 0, heightPx)
                            if (!hasCenteredCamera) {
                                hasCenteredCamera = true
                                currentMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140))
                            }
                        }
                    },
                )
            }
        }
    }

    private fun refreshMarkerSnippets(converter: UnitsConverter) {
        for ((peak, marker) in peakMarkers) {
            marker.snippet = converter.toElevationString(resources, peak.elevation)
            if (marker.isInfoWindowShown) {
                marker.showInfoWindow()
            }
        }
    }

    @Composable
    private fun UnitsControlPanel(
        preference: UnitPreference,
        onPreferenceSelected: (UnitPreference) -> Unit,
        onPanelHeightChanged: (Int) -> Unit = {},
    ) {
        // [START maps_android_utils_units_demo_formatting]
        val converter = preference.resolveConverter()
        val firstLegDistance: Meters = peaks[0].position.sphericalDistance(peaks[1].position).meters
        val totalRouteDistance: Meters = peaks.map { it.position }.sphericalPathLength().meters
        val elevationDifference: Meters = peaks[0].elevation - peaks.last().elevation
        // [END maps_android_utils_units_demo_formatting]

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .onSizeChanged { size -> onPanelHeightChanged(size.height) },
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.units_demo_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.units_demo_active_system,
                        converter.unitSystem.name,
                        Locale.getDefault().country.ifEmpty { "default" },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = preference == UnitPreference.DEVICE_DEFAULT,
                        onClick = { onPreferenceSelected(UnitPreference.DEVICE_DEFAULT) },
                        label = { Text(stringResource(R.string.units_pref_default)) },
                    )
                    FilterChip(
                        selected = preference == UnitPreference.METRIC,
                        onClick = { onPreferenceSelected(UnitPreference.METRIC) },
                        label = { Text(stringResource(R.string.units_pref_metric)) },
                    )
                    FilterChip(
                        selected = preference == UnitPreference.IMPERIAL,
                        onClick = { onPreferenceSelected(UnitPreference.IMPERIAL) },
                        label = { Text(stringResource(R.string.units_pref_imperial)) },
                    )
                }

                HorizontalDivider()

                SummaryRow(
                    label = "${peaks[0].name} Elevation",
                    value = peaks[0].elevation.toElevationString(converter),
                )
                SummaryRow(
                    label = "Elevation Delta (${peaks[0].name} → ${peaks.last().name})",
                    value = elevationDifference.toElevationString(converter),
                )
                SummaryRow(
                    label = "Leg 1 (${peaks[0].name} → ${peaks[1].name})",
                    value = firstLegDistance.toDistanceString(converter),
                )
                SummaryRow(
                    label = "Total Geodesic Route",
                    value = totalRouteDistance.toDistanceString(converter),
                )

                HorizontalDivider()

                Text(
                    text = stringResource(R.string.units_demo_logical_formatting_header),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                for ((label, distance) in sampleDistances) {
                    SummaryRow(
                        label = label,
                        value = distance.toDistanceString(converter),
                    )
                }
            }
        }
    }

    @Composable
    private fun SummaryRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
// [END maps_android_utils_units_kotlin]
