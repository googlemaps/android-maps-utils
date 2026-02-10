package com.google.maps.android.utils.demo.model

import android.app.Activity

/**
 * Represents a single demo activity in the list.
 *
 * @property title The display title of the demo.
 * @property activityClass The Activity class to launch when this demo is selected.
 */
data class Demo(
    val title: String,
    val activityClass: Class<out Activity>
)

/**
 * Represents a group of related demos (e.g., "Clustering", "Data Layers").
 *
 * @property title The title of the group header.
 * @property demos The list of demos contained within this group.
 */
data class DemoGroup(
    val title: String,
    val demos: List<Demo>
)
