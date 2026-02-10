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
