package com.google.maps.ktx.geometry

import com.google.maps.android.geometry.Point

/**
 * Returns the x value of this Point.
 *
 * e.g.
 *
 * ```
 * val (x, _) = point
 * ```
 */
inline operator fun Point.component1() = this.x

/**
 * Returns the y value of this Point.
 *
 * e.g.
 *
 * ```
 * val (_, y) = point
 */
inline operator fun Point.component2() = this.y
