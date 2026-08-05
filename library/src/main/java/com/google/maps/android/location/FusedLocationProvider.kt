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
 *
 */

package com.google.maps.android.location

import android.Manifest
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Returns a cold flow that emits device location updates using [FusedLocationProviderClient.requestLocationUpdates].
 *
 * The location updates start streaming ONLY when the flow is collected, and stop streaming immediately
 * when the collector cancels or closes the subscription.
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active callback completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 *
 * @param locationRequest The [LocationRequest] specifying the quality of service (e.g. interval, priority).
 * @param looper The [Looper] on which the callback runs. Defaults to [Looper.getMainLooper()].
 */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
public fun FusedLocationProviderClient.locationEvents(
    locationRequest: LocationRequest,
    looper: Looper = Looper.getMainLooper()
): Flow<Location> =
    callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    trySend(location)
                }
            }
        }

        requestLocationUpdates(locationRequest, callback, looper)

        awaitClose {
            removeLocationUpdates(callback)
        }
    }

/**
 * Simplified helper returning a cold flow that emits device location updates from [FusedLocationProviderClient].
 *
 * @param intervalMs The desired interval for location updates in milliseconds. Defaults to 2000 ms.
 * @param minUpdateDistanceM The minimum distance between location updates in meters. Defaults to 1 meter.
 * @param priority The location priority accuracy mode. Defaults to [Priority.PRIORITY_HIGH_ACCURACY].
 * @param looper The [Looper] on which the callback runs. Defaults to [Looper.getMainLooper()].
 */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
public fun FusedLocationProviderClient.fusedLocationEvents(
    intervalMs: Long = 2000L,
    minUpdateDistanceM: Float = 1f,
    priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
    looper: Looper = Looper.getMainLooper()
): Flow<Location> {
    val request = LocationRequest.Builder(priority, intervalMs)
        .setMinUpdateDistanceMeters(minUpdateDistanceM)
        .build()
    return locationEvents(request, looper)
}
