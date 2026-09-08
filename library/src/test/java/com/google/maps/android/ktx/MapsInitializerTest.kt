@file:Suppress("DEPRECATION")
/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.ktx

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit test suite for the backward-compatibility KTX shim [com.google.maps.android.ktx.awaitMapsSdkInitialized].
 *
 * **Purpose:**
 * Validates that the deprecated KTX shim [com.google.maps.android.ktx.awaitMapsSdkInitialized] forwards
 * seamlessly to the canonical [com.google.maps.android.awaitMapsSdkInitialized] implementation.
 *
 * **How it works:**
 * Uses Mockito static mocking (`mockStatic(MapsInitializer::class.java)`) to intercept SDK calls and
 * verifies that the deprecated KTX extension correctly returns loaded renderers and propagates exceptions.
 *
 * **How we know it is correct:**
 * - Verifies return value parity with canonical implementation.
 * - Verifies exception propagation matches canonical implementation.
 */
@RunWith(MockitoJUnitRunner::class)
public class MapsInitializerTest {

    @Mock
    private lateinit var context: Context

    private lateinit var mapsInitializerMock: MockedStatic<MapsInitializer>

    @Before
    public fun setUp() {
        mapsInitializerMock = mockStatic(MapsInitializer::class.java)
    }

    @After
    public fun tearDown() {
        mapsInitializerMock.close()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitMapsSdkInitialized` returns the loaded renderer.
     * **How it works:** Calls [com.google.maps.android.ktx.awaitMapsSdkInitialized] and simulates successful SDK initialization.
     * **How we know it is correct:** Asserts returned renderer equals [MapsInitializer.Renderer.LATEST].
     */
    @Test
    public fun testKtxAwaitMapsSdkInitializedReturnsActualRenderer(): Unit = runTest {
        mapsInitializerMock.`when`<Int> {
            MapsInitializer.initialize(
                eq(context),
                eq(MapsInitializer.Renderer.LATEST),
                any(OnMapsSdkInitializedCallback::class.java)
            )
        }.thenAnswer { invocation ->
            invocation.getArgument<OnMapsSdkInitializedCallback>(2)
                .onMapsSdkInitialized(MapsInitializer.Renderer.LATEST)
            ConnectionResult.SUCCESS
        }

        val renderer = context.awaitMapsSdkInitialized(MapsInitializer.Renderer.LATEST)

        assertThat(renderer).isEqualTo(MapsInitializer.Renderer.LATEST)
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitMapsSdkInitialized` propagates initialization exceptions.
     * **How it works:** Simulates SDK missing error and calls [com.google.maps.android.ktx.awaitMapsSdkInitialized].
     * **How we know it is correct:** Asserts [GooglePlayServicesNotAvailableException] is thrown with [ConnectionResult.SERVICE_MISSING].
     */
    @Test
    public fun testKtxAwaitMapsSdkInitializedThrowsForNonSuccessStatus(): Unit = runTest {
        mapsInitializerMock.`when`<Int> {
            MapsInitializer.initialize(
                eq(context),
                eq(MapsInitializer.Renderer.LATEST),
                any(OnMapsSdkInitializedCallback::class.java)
            )
        }.thenReturn(ConnectionResult.SERVICE_MISSING)

        val exception = runCatching {
            context.awaitMapsSdkInitialized(MapsInitializer.Renderer.LATEST)
        }.exceptionOrNull()

        assertThat(exception).isInstanceOf(GooglePlayServicesNotAvailableException::class.java)
        assertThat((exception as GooglePlayServicesNotAvailableException).errorCode)
            .isEqualTo(ConnectionResult.SERVICE_MISSING)
    }
}
