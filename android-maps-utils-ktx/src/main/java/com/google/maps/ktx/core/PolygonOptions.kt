package com.google.maps.ktx.core

import com.google.android.gms.maps.model.PolygonOptions

/**
 * Builds a new [PolygonOptions] using the provided [optionsActions].
 */
inline fun buildPolygonOptions(optionsActions: PolygonOptions.() -> Unit): PolygonOptions {
    val options = PolygonOptions()
    options.optionsActions()
    return options
}
