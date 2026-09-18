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
 * An immutable camera position: target location, zoom, tilt and bearing.
 *
 * On Android this is a typealias for [com.google.android.gms.maps.model.CameraPosition].
 */
expect class CameraPosition(target: LatLng, zoom: Float, tilt: Float, bearing: Float)

expect val CameraPosition.target: LatLng

expect val CameraPosition.zoom: Float

expect val CameraPosition.tilt: Float

expect val CameraPosition.bearing: Float
