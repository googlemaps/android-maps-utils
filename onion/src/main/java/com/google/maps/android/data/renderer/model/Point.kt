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
package com.google.maps.android.data.renderer.model

/**
 * A data class representing a single point in a coordinate system.
 * Coordinates are stored as (latitude, longitude).
 *
 * This class is platform-agnostic and does not rely on any specific mapping SDK.
 *
 * @property lat The latitude of the point.
 * @property lng The longitude of the point.
 * @property alt The altitude of the point, in meters. Optional.
 */
data class Point(val lat: Double, val lng: Double, val alt: Double? = null)
