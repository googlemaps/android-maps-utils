package com.google.maps.ktx.core

import com.google.android.gms.maps.model.PolygonOptions

/**
 * Builds a new [PolygonOptions] using the provided [optionsActions].
 * CORE
 */
inline fun buildPolygonOptions(optionsActions: PolygonOptions.() -> Unit): PolygonOptions =
    PolygonOptions().apply(optionsActions)

