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
package com.google.maps.android.ktx.utils.location

import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import com.google.maps.android.location.coarseLocationEvents as canonicalCoarseLocationEvents
import com.google.maps.android.location.fineLocationEvents as canonicalFineLocationEvents

@RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
@Deprecated("Moved to com.google.maps.android.location.coarseLocationEvents", ReplaceWith("coarseLocationEvents(minTimeMs, minDistanceM)", "com.google.maps.android.location.coarseLocationEvents"))
public fun LocationManager.coarseLocationEvents(
    minTimeMs: Long = 1000L,
    minDistanceM: Float = 1f
): Flow<Location> = this.canonicalCoarseLocationEvents(minTimeMs, minDistanceM)

@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
@Deprecated("Moved to com.google.maps.android.location.fineLocationEvents", ReplaceWith("fineLocationEvents(minTimeMs, minDistanceM)", "com.google.maps.android.location.fineLocationEvents"))
public fun LocationManager.fineLocationEvents(
    minTimeMs: Long = 1000L,
    minDistanceM: Float = 1f
): Flow<Location> = this.canonicalFineLocationEvents(minTimeMs, minDistanceM)
