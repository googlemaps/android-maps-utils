/*
 * Copyright 2026 Google LLC
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
package com.google.maps.android.ktx.utils.clustering

import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.flow.Flow
import com.google.maps.android.clustering.clusterClickEvents as canonicalClusterClickEvents
import com.google.maps.android.clustering.clusterItemClickEvents as canonicalClusterItemClickEvents
import com.google.maps.android.clustering.clusterInfoWindowClickEvents as canonicalClusterInfoWindowClickEvents
import com.google.maps.android.clustering.clusterInfoWindowLongClickEvents as canonicalClusterInfoWindowLongClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowClickEvents as canonicalClusterItemInfoWindowClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowLongClickEvents as canonicalClusterItemInfoWindowLongClickEvents

@Deprecated("Moved to com.google.maps.android.clustering.clusterClickEvents", ReplaceWith("clusterClickEvents()", "com.google.maps.android.clustering.clusterClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterClickEvents(): Flow<Cluster<T>> = this.canonicalClusterClickEvents()

@Deprecated("Moved to com.google.maps.android.clustering.clusterItemClickEvents", ReplaceWith("clusterItemClickEvents()", "com.google.maps.android.clustering.clusterItemClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterItemClickEvents(): Flow<T> = this.canonicalClusterItemClickEvents()

@Deprecated("Moved to com.google.maps.android.clustering.clusterInfoWindowClickEvents", ReplaceWith("clusterInfoWindowClickEvents()", "com.google.maps.android.clustering.clusterInfoWindowClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterInfoWindowClickEvents(): Flow<Cluster<T>> = this.canonicalClusterInfoWindowClickEvents()

@Deprecated("Moved to com.google.maps.android.clustering.clusterInfoWindowLongClickEvents", ReplaceWith("clusterInfoWindowLongClickEvents()", "com.google.maps.android.clustering.clusterInfoWindowLongClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterInfoWindowLongClickEvents(): Flow<Cluster<T>> = this.canonicalClusterInfoWindowLongClickEvents()

@Deprecated("Moved to com.google.maps.android.clustering.clusterItemInfoWindowClickEvents", ReplaceWith("clusterItemInfoWindowClickEvents()", "com.google.maps.android.clustering.clusterItemInfoWindowClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterItemInfoWindowClickEvents(): Flow<T> = this.canonicalClusterItemInfoWindowClickEvents()

@Deprecated("Moved to com.google.maps.android.clustering.clusterItemInfoWindowLongClickEvents", ReplaceWith("clusterItemInfoWindowLongClickEvents()", "com.google.maps.android.clustering.clusterItemInfoWindowLongClickEvents"))
public fun <T : ClusterItem> ClusterManager<T>.clusterItemInfoWindowLongClickEvents(): Flow<T> = this.canonicalClusterItemInfoWindowLongClickEvents()
