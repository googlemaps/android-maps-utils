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
package com.google.maps.android.ktx.utils.geometry

import com.google.maps.android.geometry.Point
import com.google.maps.android.geometry.component1 as canonicalComponent1
import com.google.maps.android.geometry.component2 as canonicalComponent2

@Deprecated("Moved to com.google.maps.android.geometry.component1", ReplaceWith("component1()", "com.google.maps.android.geometry.component1"))
public inline operator fun Point.component1(): Double = this.canonicalComponent1()

@Deprecated("Moved to com.google.maps.android.geometry.component2", ReplaceWith("component2()", "com.google.maps.android.geometry.component2"))
public inline operator fun Point.component2(): Double = this.canonicalComponent2()
