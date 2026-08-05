@file:Suppress("NOTHING_TO_INLINE")
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
package com.google.maps.android.ktx.model

import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.maps.android.model.streetViewPanoramaCamera as canonical_streetViewPanoramaCamera

@Deprecated(
    message = "Use com.google.maps.android.model.streetViewPanoramaCamera instead",
    replaceWith = ReplaceWith("streetViewPanoramaCamera(optionsActions)", "com.google.maps.android.model.streetViewPanoramaCamera"),
    level = DeprecationLevel.WARNING
)
public inline fun streetViewPanoramaCamera(optionsActions: StreetViewPanoramaCamera.Builder.() -> Unit): StreetViewPanoramaCamera = canonical_streetViewPanoramaCamera(optionsActions)
