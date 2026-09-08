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
import com.google.android.gms.maps.MapsApiSettings
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.utils.meta.AttributionId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit test suite for [AttributionIdInitializer] and its deprecated KTX compatibility shim
 * [com.google.maps.android.ktx.utils.attribution.AttributionIdInitializer].
 *
 * **Purpose:**
 * Verifies that the Jetpack App Startup `Initializer` for library usage attribution correctly
 * registers the library's unique attribution identifier (`AttributionId.VALUE` = "maps-utils-android")
 * with the Google Maps SDK's [MapsApiSettings] upon application startup, and declares no dependent
 * startup initializers.
 *
 * **How it works:**
 * Uses Mockito's static mocking (`mockStatic(MapsApiSettings::class.java)`) to intercept calls
 * to the static SDK method [MapsApiSettings.addInternalUsageAttributionId]. Each test instantiates
 * an initializer, checks its dependencies, and invokes `create(context)`.
 *
 * **How we know it is correct:**
 * - **Code under test:** Correct if invoking `create(context)` registers `AttributionId.VALUE` exactly
 *   once with the provided `Context`, and `dependencies()` returns an empty list.
 * - **Test:** Correct because `mapsApiSettingsMock.verify { ... }` will fail the test if the static
 *   attribution registration method was not invoked or received incorrect parameters.
 */
@RunWith(MockitoJUnitRunner::class)
class AttributionIdInitializerTest {

    @Mock
    private lateinit var context: Context

    private lateinit var mapsApiSettingsMock: MockedStatic<MapsApiSettings>

    @Before
    fun setUp() {
        mapsApiSettingsMock = mockStatic(MapsApiSettings::class.java)
    }

    @After
    fun tearDown() {
        mapsApiSettingsMock.close()
    }

    /**
     * **Purpose:** Tests that the canonical [AttributionIdInitializer] registers usage attribution
     * and has no initializer dependencies.
     *
     * **How it works:** Instantiates [AttributionIdInitializer], asserts `dependencies()` is empty,
     * calls `create(context)`, and verifies static invocation of [MapsApiSettings.addInternalUsageAttributionId].
     *
     * **How we know it is correct:**
     * - **Code under test:** Proves canonical initialization correctly passes `AttributionId.VALUE` to Maps SDK.
     * - **Test:** Verifies exact static invocation arguments; fails if attribution registration is omitted or altered.
     */
    @Test
    fun `test canonical AttributionIdInitializer create and dependencies`() {
        val initializer = AttributionIdInitializer()
        assertThat(initializer.dependencies()).isEmpty()
        initializer.create(context)
        mapsApiSettingsMock.verify {
            MapsApiSettings.addInternalUsageAttributionId(context, AttributionId.VALUE)
        }
    }

    /**
     * **Purpose:** Tests that the deprecated KTX compatibility shim
     * [com.google.maps.android.ktx.utils.attribution.AttributionIdInitializer] continues to register
     * usage attribution without breaking existing apps.
     *
     * **How it works:** Instantiates the KTX shim class and performs the exact same dependency
     * and creation verification as the canonical initializer test.
     *
     * **How we know it is correct:**
     * - **Code under test:** Proves backwards-compatible KTX typealias/shim preserves identical startup behavior.
     * - **Test:** Fails if the KTX shim fails to invoke [MapsApiSettings.addInternalUsageAttributionId].
     */
    @Test
    fun `test ktx AttributionIdInitializer create and dependencies`() {
        val initializer = com.google.maps.android.ktx.utils.attribution.AttributionIdInitializer()
        assertThat(initializer.dependencies()).isEmpty()
        initializer.create(context)
        mapsApiSettingsMock.verify {
            MapsApiSettings.addInternalUsageAttributionId(context, AttributionId.VALUE)
        }
    }
}
