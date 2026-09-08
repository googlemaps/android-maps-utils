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
package com.google.maps.android.ktx

import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.android.gms.maps.model.StreetViewPanoramaLocation
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation
import kotlinx.coroutines.flow.Flow
import com.google.maps.android.awaitStreetViewPanorama as canonicalAwaitStreetViewPanorama
import com.google.maps.android.cameraChangeEvents as canonicalCameraChangeEvents
import com.google.maps.android.changeEvents as canonicalChangeEvents
import com.google.maps.android.clickEvents as canonicalClickEvents
import com.google.maps.android.longClickEvents as canonicalLongClickEvents

@Deprecated(
    message = "Use com.google.maps.android.awaitStreetViewPanorama instead",
    replaceWith = ReplaceWith("awaitStreetViewPanorama()", "com.google.maps.android.awaitStreetViewPanorama"),
    level = DeprecationLevel.WARNING
)
public suspend inline fun StreetViewPanoramaView.awaitStreetViewPanorama(): StreetViewPanorama = this.canonicalAwaitStreetViewPanorama()

@Deprecated(
    message = "Use com.google.maps.android.cameraChangeEvents instead",
    replaceWith = ReplaceWith("cameraChangeEvents()", "com.google.maps.android.cameraChangeEvents"),
    level = DeprecationLevel.WARNING
)
public fun StreetViewPanorama.cameraChangeEvents(): Flow<StreetViewPanoramaCamera> = this.canonicalCameraChangeEvents()

@Deprecated(
    message = "Use com.google.maps.android.changeEvents instead",
    replaceWith = ReplaceWith("changeEvents()", "com.google.maps.android.changeEvents"),
    level = DeprecationLevel.WARNING
)
public fun StreetViewPanorama.changeEvents(): Flow<StreetViewPanoramaLocation> = this.canonicalChangeEvents()

@Deprecated(
    message = "Use com.google.maps.android.clickEvents instead",
    replaceWith = ReplaceWith("clickEvents()", "com.google.maps.android.clickEvents"),
    level = DeprecationLevel.WARNING
)
public fun StreetViewPanorama.clickEvents(): Flow<StreetViewPanoramaOrientation> = this.canonicalClickEvents()

@Deprecated(
    message = "Use com.google.maps.android.longClickEvents instead",
    replaceWith = ReplaceWith("longClickEvents()", "com.google.maps.android.longClickEvents"),
    level = DeprecationLevel.WARNING
)
public fun StreetViewPanorama.longClickEvents(): Flow<StreetViewPanoramaOrientation> = this.canonicalLongClickEvents()
