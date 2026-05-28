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
package com.google.maps.android.data

import android.graphics.Bitmap
import com.google.android.gms.maps.model.BitmapDescriptor

/**
 * A legacy bridge class representing the old Renderer, used to provide backward
 * compatibility for type-level references like [ImagesCache].
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public open class Renderer {
    /**
     * A lightweight, backward-compatible class representing the old [ImagesCache].
     */
    public class ImagesCache {
        internal val markerImagesCache: MutableMap<String, MutableMap<String, BitmapDescriptor>> = HashMap()
        internal val groundOverlayImagesCache: MutableMap<String, BitmapDescriptor> = HashMap()
        internal val bitmapCache: MutableMap<String, Bitmap> = HashMap()
    }
}
