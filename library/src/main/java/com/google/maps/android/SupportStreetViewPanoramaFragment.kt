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

package com.google.maps.android

import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A suspending function that provides an instance of a [StreetViewPanorama] from this
 * [SupportStreetViewPanoramaFragment]. This is an alternative to using
 * [SupportStreetViewPanoramaFragment.getStreetViewPanoramaAsync] by using coroutines to obtain a
 * [StreetViewPanorama].
 *
 * @return the [StreetViewPanorama]
 */
public suspend inline fun SupportStreetViewPanoramaFragment.awaitStreetViewPanorama(): StreetViewPanorama =
    suspendCoroutine { continuation ->
        getStreetViewPanoramaAsync {
            continuation.resume(it)
        }
    }
