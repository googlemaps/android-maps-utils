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
package com.google.maps.android.heatmaps

/**
 * Constants shared between the multiplatform heatmap model and the platform tile providers.
 * They were historically defined on HeatmapTileProvider, which remains Android-only; its
 * companion aliases these values to preserve the public API.
 */
object HeatmapConstants {
    /** Default opacity of heatmap overlay. */
    const val DEFAULT_OPACITY: Double = 0.7

    /** Width of the world (in the Mercator projection used by the heatmap quadtree). */
    internal const val WORLD_WIDTH: Double = 1.0
}
