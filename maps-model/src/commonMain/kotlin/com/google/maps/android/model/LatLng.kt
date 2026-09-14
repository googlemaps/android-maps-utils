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
 * An immutable pair of latitude and longitude, in degrees.
 *
 * On Android this is a typealias for [com.google.android.gms.maps.model.LatLng], so existing
 * code that uses the Maps SDK type keeps working unchanged. On other platforms it is a plain
 * value holder with the same clamping/wrapping semantics: latitude is clamped to [-90, 90] and
 * longitude is wrapped to [-180, 180).
 *
 * The coordinates are exposed as extension properties rather than members because the Play
 * Services class exposes them as Java fields, and Java fields cannot actualize `expect`
 * member properties. On each platform, member resolution wins over these extensions, so
 * platform code binds directly to the underlying field/property with no indirection.
 */
expect class LatLng(latitude: Double, longitude: Double)

expect val LatLng.latitude: Double

expect val LatLng.longitude: Double
