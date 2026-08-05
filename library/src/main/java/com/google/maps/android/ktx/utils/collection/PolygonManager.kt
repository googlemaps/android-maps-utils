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
package com.google.maps.android.ktx.utils.collection

import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.flow.Flow
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.addPolygon as canonical_addPolygon
import com.google.maps.android.collections.clickEvents as canonicalClickEvents
import com.google.maps.android.collections.infoWindowClickEvents as canonicalInfoWindowClickEvents
import com.google.maps.android.collections.infoWindowLongClickEvents as canonicalInfoWindowLongClickEvents

@Deprecated(
    message = "Use com.google.maps.android.collections.addPolygon instead",
    replaceWith = ReplaceWith("addPolygon(optionsActions)", "com.google.maps.android.collections.addPolygon"),
    level = DeprecationLevel.WARNING
)
public inline fun PolygonManager.Collection.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon = this.canonical_addPolygon(optionsActions)

@Deprecated("Moved to com.google.maps.android.collections.clickEvents", ReplaceWith("clickEvents()", "com.google.maps.android.collections.clickEvents"))
public fun PolygonManager.Collection.clickEvents(): Flow<Polygon> = this.canonicalClickEvents()
