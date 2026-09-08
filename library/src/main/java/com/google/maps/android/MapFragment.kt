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

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


/**
 * A suspending function that provides an instance of a [GoogleMap] from this [MapFragment].
 * This is an alternative to [MapFragment.getMapAsync] by using coroutines to obtain a [GoogleMap].
 *
 * @return the [GoogleMap] instance
 */
public suspend inline fun MapFragment.awaitMap(): GoogleMap =
    suspendCancellableCoroutine { continuation ->
        getMapAsync {
            continuation.resume(it)
        }
    }
