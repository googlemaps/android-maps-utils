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
package com.google.maps.android.model

/**
 * Mirrors the clamping/wrapping behavior of the Play Services LatLng constructor so that
 * algorithms behave identically across platforms.
 */
actual class LatLng actual constructor(latitude: Double, longitude: Double) {
    val latitude: Double = latitude.coerceIn(-90.0, 90.0)
    val longitude: Double =
        if (longitude in -180.0..180.0) {
            if (longitude == 180.0) -180.0 else longitude
        } else {
            (longitude - 180.0).mod(360.0) - 180.0
        }

    override fun equals(other: Any?): Boolean =
        other is LatLng && latitude == other.latitude && longitude == other.longitude

    override fun hashCode(): Int {
        var result = 31 + latitude.toRawBits().let { (it xor (it ushr 32)).toInt() }
        result = 31 * result + longitude.toRawBits().let { (it xor (it ushr 32)).toInt() }
        return result
    }

    override fun toString(): String = "lat/lng: ($latitude,$longitude)"
}

actual val LatLng.latitude: Double get() = this.latitude

actual val LatLng.longitude: Double get() = this.longitude

actual class CameraPosition actual constructor(
    val target: LatLng,
    val zoom: Float,
    val tilt: Float,
    val bearing: Float
)

actual val CameraPosition.target: LatLng get() = this.target

actual val CameraPosition.zoom: Float get() = this.zoom

actual val CameraPosition.tilt: Float get() = this.tilt

actual val CameraPosition.bearing: Float get() = this.bearing
