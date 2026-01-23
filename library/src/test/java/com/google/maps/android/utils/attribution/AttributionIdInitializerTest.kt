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

package com.google.maps.android.utils.attribution

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.MapsApiSettings
import com.google.maps.android.utils.meta.AttributionId
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AttributionIdInitializerTest {

    @Before
    fun setUp() {
        mockkStatic(MapsApiSettings::class)
        every { MapsApiSettings.addInternalUsageAttributionId(any(), any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkStatic(MapsApiSettings::class)
    }

    @Test
    fun `create adds internal usage attribution id`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initializer = AttributionIdInitializer()

        initializer.create(context)

        verify {
            MapsApiSettings.addInternalUsageAttributionId(
                context,
                AttributionId.VALUE
            )
        }
    }
}
