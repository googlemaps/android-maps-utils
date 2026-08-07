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

import android.content.Context
import com.google.android.gms.maps.MapsInitializer
import com.google.maps.android.awaitMapsSdkInitialized as canonicalAwaitMapsSdkInitialized

/**
 * Suspends until the Google Maps SDK is initialized and returns the [MapsInitializer.Renderer]
 * that was actually loaded.
 *
 * @deprecated Use [com.google.maps.android.awaitMapsSdkInitialized] instead.
 */
@Deprecated(
    message = "Use com.google.maps.android.awaitMapsSdkInitialized instead",
    replaceWith = ReplaceWith(
        "awaitMapsSdkInitialized(preferredRenderer)",
        "com.google.maps.android.awaitMapsSdkInitialized"
    ),
    level = DeprecationLevel.WARNING
)
public suspend inline fun Context.awaitMapsSdkInitialized(
    preferredRenderer: MapsInitializer.Renderer? = null
): MapsInitializer.Renderer = this.canonicalAwaitMapsSdkInitialized(preferredRenderer)
