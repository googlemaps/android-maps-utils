package com.google.maps.android.utils.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                "Clustering", listOf(
                    Demo("Advanced Markers Clustering Example", CustomAdvancedMarkerClusteringDemoActivity::class.java),
                    Demo("Cluster Algorithms", ClusterAlgorithmsDemoActivity::class.java),
                    Demo("Clustering", ClusteringDemoActivity::class.java),
                    Demo("Clustering: Custom Look", CustomMarkerClusteringDemoActivity::class.java),
                    Demo("Clustering: Diff", ClusteringDiffDemoActivity::class.java),
                    Demo("Clustering: 2K markers", BigClusteringDemoActivity::class.java),
                    Demo("Clustering: 20K only visible markers", VisibleClusteringDemoActivity::class.java),
                    Demo("Clustering: ViewModel", ClusteringViewModelDemoActivity::class.java),
                    Demo("Clustering: Force on Zoom", ZoomClusteringDemoActivity::class.java)
                )
            ),
            DemoGroup(
                "Data Layers", listOf(
                    Demo("GeoJSON Layer", GeoJsonDemoActivity::class.java),
                    Demo("KML Layer Overlay", KmlDemoActivity::class.java),
                    Demo("Heatmaps", HeatmapsDemoActivity::class.java),
                    Demo("Heatmaps with Places API", HeatmapsPlacesDemoActivity::class.java),
                    Demo("Multi Layer", MultiLayerDemoActivity::class.java),
                    Demo("Transit Layer Demo", TransitLayerDemoActivity::class.java)
                )
            ),
            DemoGroup(
                "Geometry", listOf(
                    Demo("PolyUtil.decode", PolyDecodeDemoActivity::class.java),
                    Demo("PolyUtil.simplify", PolySimplifyDemoActivity::class.java),
                    Demo("Polyline Progress", PolylineProgressDemoActivity::class.java),
                    Demo("SphericalUtil.computeDistanceBetween", DistanceDemoActivity::class.java)
                )
            ),
            DemoGroup(
                "Utilities", listOf(
                    Demo("IconGenerator", IconGeneratorDemoActivity::class.java),
                    Demo("Generating tiles", TileProviderAndProjectionDemo::class.java),
                    Demo("AnimationUtil sample", AnimationUtilDemoActivity::class.java)
                )
            ),
            DemoGroup(
                "Street View", listOf(
                    Demo("Street View Demo", StreetViewDemoActivity::class.java),
                    Demo("Street View Demo (Java)", StreetViewDemoJavaActivity::class.java)
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
    // Storing the title of the expanded group ensures only one group is open at a time (accordion style).
    var expandedGroupTitle by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = contentPadding) {
        items(groups) { group ->
            DemoGroupItem(
                group = group,
                isExpanded = expandedGroupTitle == group.title,
                onHeaderClick = {
                    // Toggle expansion: if clicking the already open group, collapse it. Otherwise expand the new one.
                    expandedGroupTitle = if (expandedGroupTitle == group.title) null else group.title
                },
                onDemoClick = onDemoClick
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
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
                .background(Color(0xFFEEEEEE)) // Light gray background for separation
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            // Animate the arrow rotation for a polished feel
            val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrowRotation")
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotation),
                tint = Color.Gray
            )
        }

        // Expandable Content
        // AnimatedVisibility provides a smooth expand/collapse animation
        AnimatedVisibility(visible = isExpanded) {
            Column {
                group.demos.forEach { demo ->
                    Text(
                        text = demo.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDemoClick(demo.activityClass) }
                            .padding(horizontal = 32.dp, vertical = 16.dp), // Indented for hierarchy
                        fontSize = 16.sp,
                        color = Color(0xFF222222)
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
