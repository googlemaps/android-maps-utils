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

package com.google.maps.android.utils.demo

import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class TransitLayerDemoActivity : BaseDemoActivity() {
    private var checkBox: CheckBox? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_transit_layer_demo
    }

    override fun startDemo(isRestore: Boolean) {
        if (!isRestore) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(51.503186, -0.126446), // London
                    10f
                )
            )
        }

        checkBox = findViewById<CheckBox>(R.id.transit_toggle).apply {
            isChecked = map.isTransitEnabled
            setOnCheckedChangeListener { _, isChecked ->
                map.isTransitEnabled = isChecked
                updateMessage()
            }
        }

        updateMessage()
    }

    private fun updateMessage() {
        val status = if (map.isTransitEnabled) "ENABLED" else "DISABLED"
        Toast.makeText(this, "Transit Layer is $status", Toast.LENGTH_SHORT).show()
    }
}
