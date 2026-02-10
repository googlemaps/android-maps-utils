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

package com.google.maps.android.utils.attribution

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.maps.MapsApiSettings
import com.google.maps.android.utils.meta.AttributionId

/**
 * Adds a usage attribution ID to the initializer, which helps Google understand which libraries
 * and samples are helpful to developers, such as usage of this library.
 * To opt out of sending the usage attribution ID, please remove this initializer from your manifest.
 */
internal class AttributionIdInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // See [AttributionIdInitializer]
        MapsApiSettings.addInternalUsageAttributionId(
            /* context = */ context,
            /* internalUsageAttributionId = */ AttributionId.VALUE
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
