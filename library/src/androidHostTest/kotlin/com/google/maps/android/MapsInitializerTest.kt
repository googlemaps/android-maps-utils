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

package com.google.maps.android

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
 * Unit test suite for canonical Maps SDK coroutine initialization extension [Context.awaitMapsSdkInitialized].
 *
 * **Purpose:**
 * Validates that [Context.awaitMapsSdkInitialized] properly bridges [MapsInitializer.initialize]
 * callbacks into coroutine resumption with the loaded [MapsInitializer.Renderer], handles `null`
 * and default preferences, and converts initialization failure status codes into
 * [GooglePlayServicesNotAvailableException].
 *
 * **How it works:**
 * Uses Mockito static mocking (`mockStatic(MapsInitializer::class.java)`) to intercept static calls
 * to [MapsInitializer.initialize] on the provided mock [Context]. Test methods trigger either the
 * asynchronous callback or return failure status codes and verify the returned result or thrown exception.
 *
 * **How we know it is correct:**
 * - **Success cases:** Asserts the returned renderer matches the value supplied to [OnMapsSdkInitializedCallback].
 * - **Error cases:** Asserts [GooglePlayServicesNotAvailableException] is thrown with the exact [ConnectionResult] error code.
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
     * **Purpose:** Verifies [Context.awaitMapsSdkInitialized] resumes with the loaded renderer when initialization succeeds.
     * **How it works:** Mocks [MapsInitializer.initialize] to invoke callback with [MapsInitializer.Renderer.LEGACY] and return [ConnectionResult.SUCCESS].
     * **How we know it is correct:** Asserts the returned renderer equals [MapsInitializer.Renderer.LEGACY].
     */
    @Suppress("DEPRECATION")
    @Test
    public fun testAwaitMapsSdkInitializedReturnsActualRenderer(): Unit = runTest {
        mapsInitializerMock.`when`<Int> {
            MapsInitializer.initialize(
                eq(context),
                eq(MapsInitializer.Renderer.LATEST),
                any(OnMapsSdkInitializedCallback::class.java)
            )
        }.thenAnswer { invocation ->
            invocation.getArgument<OnMapsSdkInitializedCallback>(2)
                .onMapsSdkInitialized(MapsInitializer.Renderer.LEGACY)
            ConnectionResult.SUCCESS
        }

        val renderer = context.awaitMapsSdkInitialized(MapsInitializer.Renderer.LATEST)

        assertThat(renderer).isEqualTo(MapsInitializer.Renderer.LEGACY)
    }

    /**
     * **Purpose:** Verifies [Context.awaitMapsSdkInitialized] throws [GooglePlayServicesNotAvailableException] on initialization failure.
     * **How it works:** Mocks [MapsInitializer.initialize] to return [ConnectionResult.SERVICE_MISSING] without triggering the callback.
     * **How we know it is correct:** Asserts [GooglePlayServicesNotAvailableException] is thrown with [ConnectionResult.SERVICE_MISSING] error code.
     */
    @Test
    public fun testAwaitMapsSdkInitializedThrowsForNonSuccessStatus(): Unit = runTest {
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

    /**
     * **Purpose:** Verifies [Context.awaitMapsSdkInitialized] handles an explicit `null` preferred renderer parameter.
     * **How it works:** Mocks [MapsInitializer.initialize] with `null` preferred renderer and invokes callback with [MapsInitializer.Renderer.LATEST].
     * **How we know it is correct:** Asserts the returned renderer equals [MapsInitializer.Renderer.LATEST].
     */
    @Suppress("DEPRECATION")
    @Test
    public fun testAwaitMapsSdkInitializedWithNullPreferredRenderer(): Unit = runTest {
        mapsInitializerMock.`when`<Int> {
            MapsInitializer.initialize(
                eq(context),
                eq(null),
                any(OnMapsSdkInitializedCallback::class.java)
            )
        }.thenAnswer { invocation ->
            invocation.getArgument<OnMapsSdkInitializedCallback>(2)
                .onMapsSdkInitialized(MapsInitializer.Renderer.LATEST)
            ConnectionResult.SUCCESS
        }

        val renderer = context.awaitMapsSdkInitialized(null)

        assertThat(renderer).isEqualTo(MapsInitializer.Renderer.LATEST)
    }

    /**
     * **Purpose:** Verifies [Context.awaitMapsSdkInitialized] defaults to `null` preferred renderer when called without arguments.
     * **How it works:** Calls `context.awaitMapsSdkInitialized()` with default argument and verifies callback resolution.
     * **How we know it is correct:** Asserts the returned renderer equals [MapsInitializer.Renderer.LATEST].
     */
    @Suppress("DEPRECATION")
    @Test
    public fun testAwaitMapsSdkInitializedWithDefaultNullRenderer(): Unit = runTest {
        mapsInitializerMock.`when`<Int> {
            MapsInitializer.initialize(
                eq(context),
                eq(null),
                any(OnMapsSdkInitializedCallback::class.java)
            )
        }.thenAnswer { invocation ->
            invocation.getArgument<OnMapsSdkInitializedCallback>(2)
                .onMapsSdkInitialized(MapsInitializer.Renderer.LATEST)
            ConnectionResult.SUCCESS
        }

        val renderer = context.awaitMapsSdkInitialized()

        assertThat(renderer).isEqualTo(MapsInitializer.Renderer.LATEST)
    }
}
