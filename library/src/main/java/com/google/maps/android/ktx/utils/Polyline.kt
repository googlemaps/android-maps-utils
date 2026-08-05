@file:Suppress("NOTHING_TO_INLINE")
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
package com.google.maps.android.ktx.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.contains as canonicalContains
import com.google.maps.android.sphericalPathLength as canonicalSphericalPathLength

@Deprecated("Moved to com.google.maps.android.contains", ReplaceWith("contains(latLng, tolerance)", "com.google.maps.android.contains"))
public inline fun Polyline.contains(latLng: LatLng, tolerance: Double = 0.1): Boolean = this.canonicalContains(latLng, tolerance)

@Deprecated("Moved to com.google.maps.android.sphericalPathLength", ReplaceWith("sphericalPathLength", "com.google.maps.android.sphericalPathLength"))
public inline val Polyline.sphericalPathLength: Double get() = this.canonicalSphericalPathLength
