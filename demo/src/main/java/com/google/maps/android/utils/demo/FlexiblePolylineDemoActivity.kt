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

import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.FlexiblePolylineUtil

/**
 * Decodes a flexible polyline with [FlexiblePolylineUtil] and draws it.
 *
 * [ENCODED_WALKWAY] holds ten points along the Sydney Harbour Bridge walkway at seven decimal
 * places. [FlexiblePolylineUtil.decodeToLatLngs] supplies the path for [PolylineOptions].
 */
class FlexiblePolylineDemoActivity : BaseDemoActivity() {
    override fun startDemo(isRestore: Boolean) {
        val map = map ?: return
        val path = FlexiblePolylineUtil.decodeToLatLngs(ENCODED_WALKWAY)

        map.addPolyline(
            PolylineOptions()
                .addAll(path)
                .width(POLYLINE_WIDTH)
                .color(POLYLINE_COLOR),
        )

        Toast.makeText(
            this,
            getString(R.string.flexible_polyline_summary, path.size),
            Toast.LENGTH_SHORT,
        ).show()

        if (!isRestore) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(CAMERA_TARGET, CAMERA_ZOOM))
        }
    }

    private companion object {
        /**
         * The Sydney Harbour Bridge walkway, encoded at precision 7. The alphabet is URL safe,
         * so the string needs no escaping in source, a URL or a JSON field. The Google format
         * sample in [PolyDecodeDemoActivity] contains four backslashes that are doubled in the
         * Java literal.
         */
        const val ENCODED_WALKWAY =
            "BH__k0lU4kokk6CnzQnqDnzQvwDnzQvwDnzQvwDnzQvwDnzQvwDnzQvwDnzQvwDnzQvwD"

        val CAMERA_TARGET = LatLng(-33.85343, 151.21083)
        const val CAMERA_ZOOM = 15f
        const val POLYLINE_WIDTH = 12f
        const val POLYLINE_COLOR = 0xFF1A73E8.toInt()
    }
}
