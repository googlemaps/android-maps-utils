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

@RequiresOptIn
@Deprecated(
    message = "The KTX library functionality has been moved to com.google.maps.android. Use com.google.maps.android.MapsExperimentalFeature instead.",
    replaceWith = ReplaceWith("MapsExperimentalFeature", "com.google.maps.android.MapsExperimentalFeature"),
    level = DeprecationLevel.WARNING
)
@Retention(AnnotationRetention.BINARY)
public annotation class MapsExperimentalFeature
