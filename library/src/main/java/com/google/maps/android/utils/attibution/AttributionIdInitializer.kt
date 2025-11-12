/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may not use this file except in compliance with the License.
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

package com.google.maps.android.utils.attibution

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.maps.MapsApiSettings
import com.google.maps.android.utils.meta.AttributionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Initializes the AttributionId at application startup.
 */
internal class AttributionIdInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // 🚨 CRITICAL: Launch the potentially blocking call on the IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            // This is now safely off the main thread
            MapsApiSettings.addInternalUsageAttributionId(
                /* context = */ context,
                /* internalUsageAttributionId = */ AttributionId.VALUE
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
