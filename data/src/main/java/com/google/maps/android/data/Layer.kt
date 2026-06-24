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

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle

/**
 * Common base class for GeoJsonLayer and KmlLayer, bridging to the new data layer.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public abstract class Layer {
    protected var mGoogleMap: GoogleMap? = null

    // Default styles
    protected var mDefaultPointStyle = GeoJsonPointStyle()
    protected var mDefaultLineStringStyle = GeoJsonLineStringStyle()
    protected var mDefaultPolygonStyle = GeoJsonPolygonStyle()

    public abstract fun getMap(): GoogleMap?

    public abstract fun setMap(map: GoogleMap?)

    public abstract fun addLayerToMap()

    public abstract fun removeLayerFromMap()

    public fun interface OnFeatureClickListener {
        public fun onFeatureClick(feature: Feature)
    }

    public open fun setOnFeatureClickListener(listener: OnFeatureClickListener) {
        // Will be overridden or implemented to hook into MapViewRenderer
    }

    public abstract val features: Iterable<Feature>

    public open fun getDefaultPointStyle(): GeoJsonPointStyle = mDefaultPointStyle

    public open fun getDefaultLineStringStyle(): GeoJsonLineStringStyle = mDefaultLineStringStyle

    public open fun getDefaultPolygonStyle(): GeoJsonPolygonStyle = mDefaultPolygonStyle
}
