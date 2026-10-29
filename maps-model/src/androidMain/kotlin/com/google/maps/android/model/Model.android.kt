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

public actual typealias LatLng = com.google.android.gms.maps.model.LatLng

// These getters bind to the Java fields of the Play Services classes (member resolution
// wins over the extension inside the getter body, so this is not self-recursive).
public actual val LatLng.latitude: Double get() = this.latitude

public actual val LatLng.longitude: Double get() = this.longitude

public actual typealias CameraPosition = com.google.android.gms.maps.model.CameraPosition

public actual val CameraPosition.target: LatLng get() = this.target

public actual val CameraPosition.zoom: Float get() = this.zoom

public actual val CameraPosition.tilt: Float get() = this.tilt

public actual val CameraPosition.bearing: Float get() = this.bearing
