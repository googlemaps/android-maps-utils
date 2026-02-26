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
 * Represents the style for a GroundOverlay.
 *
 * @property iconUrl The URL of the image to display.
 * @property zIndex The z-index of the overlay.
 * @property transparency The transparency of the overlay (0.0 to 1.0).
 * @property visibility Whether the overlay is visible.
 */
data class GroundOverlayStyle(
    val iconUrl: String?,
    val zIndex: Float = 0f,
    val transparency: Float = 0f,
    val visibility: Boolean = true
) : Style
