/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.data.renderer

import android.graphics.Bitmap

/**
 * Interface for providing icons to the renderer.
 * Implementations can load icons from URLs, assets, or other sources.
 */
interface IconProvider {
    /**
     * Loads an icon from the given URL.
     *
     * @param url The URL of the icon to load.
     * @param callback A callback to be invoked when the icon is loaded. The bitmap may be null if loading failed.
     */
    fun loadIcon(url: String, callback: (Bitmap?) -> Unit)
}
