package com.google.maps.android.utils.demo.model
 
 import android.app.Activity
 import androidx.annotation.StringRes
 
 /**
  * Represents a single demo activity in the list.
  *
  * @property titleResId The string resource ID for the demo title.
  * @property activityClass The Activity class to launch when this demo is selected.
  */
 data class Demo(
     @StringRes val titleResId: Int,
     val activityClass: Class<out Activity>
 )
 
 /**
  * Represents a group of related demos (e.g., "Clustering", "Data Layers").
  *
  * @property titleResId The string resource ID for the group header.
  * @property demos The list of demos contained within this group.
  */
 data class DemoGroup(
     @StringRes val titleResId: Int,
     val demos: List<Demo>
 )
