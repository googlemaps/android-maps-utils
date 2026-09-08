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
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Returns a cold flow that emits the device's coarse location updates using [LocationManager.NETWORK_PROVIDER]
 * (or [LocationManager.PASSIVE_PROVIDER] if network provider is not available).
 *
 * The location updates start streaming ONLY when the flow is collected, and stop streaming immediately
 * when the collector cancels or closes the subscription.
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
@RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
public fun LocationManager.coarseLocationEvents(
    minTimeMs: Long = 1000L,
    minDistanceM: Float = 1f
): Flow<Location> =
    callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Deprecated in API 29, left empty for backward compatibility with minSdk 23
            }

            override fun onProviderEnabled(provider: String) {
                // Left empty for backward compatibility
            }

            override fun onProviderDisabled(provider: String) {
                close(kotlinx.coroutines.CancellationException("Location provider $provider was disabled"))
            }
        }

        val provider = if (allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else {
            LocationManager.PASSIVE_PROVIDER
        }

        requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)

        awaitClose {
            removeUpdates(listener)
        }
    }

/**
 * Returns a cold flow that emits the device's fine location updates using [LocationManager.GPS_PROVIDER].
 *
 * The location updates start streaming ONLY when the flow is collected, and stop streaming immediately
 * when the collector cancels or closes the subscription.
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
public fun LocationManager.fineLocationEvents(
    minTimeMs: Long = 1000L,
    minDistanceM: Float = 1f
): Flow<Location> =
    callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Deprecated in API 29, left empty for backward compatibility with minSdk 23
            }

            override fun onProviderEnabled(provider: String) {
                // Left empty for backward compatibility
            }

            override fun onProviderDisabled(provider: String) {
                close(kotlinx.coroutines.CancellationException("Location provider $provider was disabled"))
            }
        }

        requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistanceM, listener)

        awaitClose {
            removeUpdates(listener)
        }
    }
