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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.google.maps.android.utils.demo.model.Demo
import com.google.maps.android.utils.demo.model.DemoGroup

/**
 * The main entry point of the Demo App.
 *
 * This Activity displays a categorized list of demos using Jetpack Compose.
 * It demonstrates a simple "Accordion" UI pattern where only one group can be expanded at a time,
 * keeping the list clean and focused.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // Scaffold provides a framework for basic material design layout,
                // automatically handling system bars and insets.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoList(
                        groups = getDemoGroups(),
                        contentPadding = innerPadding,
                        onDemoClick = { activityClass ->
                            startActivity(Intent(this, activityClass))
                        }
                    )
                }
            }
        }
    }

    /**
     * Returns the static list of demo groups.
     *
     * In a real production app, this might come from a ViewModel or a resource file,
     * but for a self-contained demo, hardcoding the hierarchy here is simple and clear.
     */
    private fun getDemoGroups(): List<DemoGroup> {
        return listOf(
            DemoGroup(
                R.string.category_clustering, listOf(
                    Demo(R.string.demo_title_clustering_advanced, CustomAdvancedMarkerClusteringDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_algorithms, ClusterAlgorithmsDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_default, ClusteringDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_custom_look, CustomMarkerClusteringDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_diff, ClusteringDiffDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_2k, BigClusteringDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_20k, VisibleClusteringDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_viewmodel, ClusteringViewModelDemoActivity::class.java),
                    Demo(R.string.demo_title_clustering_force_zoom, ZoomClusteringDemoActivity::class.java)
                )
            ),
            DemoGroup(
                R.string.category_data_layers, listOf(
                    Demo(R.string.demo_title_geojson, GeoJsonDemoActivity::class.java),
                    Demo(R.string.demo_title_kml, KmlDemoActivity::class.java),
                    Demo(R.string.demo_title_heatmaps, HeatmapsDemoActivity::class.java),
                    Demo(R.string.demo_title_heatmaps_places, HeatmapsPlacesDemoActivity::class.java),
                    Demo(R.string.demo_title_multi_layer, MultiLayerDemoActivity::class.java),
                    Demo(R.string.demo_title_transit_layer, TransitLayerDemoActivity::class.java),
                    Demo(R.string.demo_title_renderer, RendererDemoActivity::class.java)
                )
            ),
            DemoGroup(
                R.string.category_geometry, listOf(
                    Demo(R.string.demo_title_poly_decode, PolyDecodeDemoActivity::class.java),
                    Demo(R.string.demo_title_poly_simplify, PolySimplifyDemoActivity::class.java),
                    Demo(R.string.demo_title_polyline_progress, PolylineProgressDemoActivity::class.java),
                    Demo(R.string.demo_title_spherical_distance, DistanceDemoActivity::class.java)
                )
            ),
            DemoGroup(
                R.string.category_utilities, listOf(
                    Demo(R.string.demo_title_icon_generator, IconGeneratorDemoActivity::class.java),
                    Demo(R.string.demo_title_tile_provider, TileProviderAndProjectionDemo::class.java),
                    Demo(R.string.demo_title_animation_util, AnimationUtilDemoActivity::class.java)
                )
            ),
            DemoGroup(
                R.string.category_street_view, listOf(
                    Demo(R.string.demo_title_street_view, StreetViewDemoActivity::class.java),
                    Demo(R.string.demo_title_street_view_java, StreetViewDemoJavaActivity::class.java)
                )
            )
        )
    }
}

/**
 * Renders the list of demo groups.
 *
 * @param groups The list of groups to display.
 * @param contentPadding Padding to apply to the content (handling system bars).
 * @param onDemoClick Callback when a specific demo is clicked.
 */
@Composable
fun DemoList(
    groups: List<DemoGroup>,
    contentPadding: PaddingValues,
    onDemoClick: (Class<out Activity>) -> Unit
) {
    // We use rememberSaveable to preserve the expanded state across configuration changes (e.g. rotation).
    // Storing the ID of the expanded group ensures only one group is open at a time (accordion style).
    var expandedGroupResId by rememberSaveable { mutableStateOf<Int?>(null) }

    LazyColumn(contentPadding = contentPadding) {
        items(groups) { group ->
            DemoGroupItem(
                group = group,
                isExpanded = expandedGroupResId == group.titleResId,
                onHeaderClick = {
                    // Toggle expansion: if clicking the already open group, collapse it. Otherwise expand the new one.
                    expandedGroupResId = if (expandedGroupResId == group.titleResId) null else group.titleResId
                },
                onDemoClick = onDemoClick
            )
            HorizontalDivider(
                thickness = dimensionResource(id = R.dimen.divider_thickness),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/**
 * Renders a single group of demos, including the header and the expandable content.
 *
 * @param group The group data.
 * @param isExpanded Whether this group's content should be visible.
 * @param onHeaderClick Callback to toggle expansion.
 * @param onDemoClick Callback when a child demo item is clicked.
 */
@Composable
fun DemoGroupItem(
    group: DemoGroup,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onDemoClick: (Class<out Activity>) -> Unit
) {
    Column {
        // Group Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHeaderClick)
                .background(MaterialTheme.colorScheme.surfaceVariant) // Use semantic color
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(group.titleResId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            // Animate the arrow rotation for a polished feel
            val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrowRotation")
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Expandable Content
        // AnimatedVisibility provides a smooth expand/collapse animation
        AnimatedVisibility(visible = isExpanded) {
            Column {
                group.demos.forEach { demo ->
                    Text(
                        text = stringResource(demo.titleResId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDemoClick(demo.activityClass) }
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.padding_large),
                                vertical = dimensionResource(id = R.dimen.padding_medium)
                            ), // Indented for hierarchy
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(
                        thickness = dimensionResource(id = R.dimen.divider_thickness),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }
        }
    }
}
