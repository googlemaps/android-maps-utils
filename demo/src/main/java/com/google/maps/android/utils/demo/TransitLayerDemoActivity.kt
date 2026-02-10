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

package com.google.maps.android.utils.demo

import android.widget.CheckBox
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

/**
 * Demonstrates the Transit Layer feature of the Google Maps API.
 *
 * This activity allows users to toggle the transit layer on and off via a checkbox.
 * The transit layer displays public transport lines and stations on the map.
 */
class TransitLayerDemoActivity : BaseDemoActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_transit_layer_demo
    }

    override fun startDemo(isRestore: Boolean) {
        if (!isRestore) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LONDON,
                    DEFAULT_ZOOM
                )
            )
        }

        val checkBox = findViewById<CheckBox>(R.id.transit_toggle)
        checkBox.isChecked = map.isTransitEnabled
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            map.isTransitEnabled = isChecked
            updateMessage()
        }

        updateMessage()
    }

    private fun updateMessage() {
        val status = if (map.isTransitEnabled) {
            getString(R.string.status_enabled)
        } else {
            getString(R.string.status_disabled)
        }
        Toast.makeText(this, getString(R.string.transit_layer_status_fmt, status), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val LONDON = LatLng(51.503186, -0.126446)
        private const val DEFAULT_ZOOM = 10f
    }
}
