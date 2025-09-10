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

package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [StreetViewJavaHelper].
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class StreetViewHelperTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(StreetViewUtils)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Tests that [StreetViewJavaHelper.fetchStreetViewData] calls the onStreetViewResult callback with the OK status when the call is successful.
     */
    @Test
    fun `fetchStreetViewData should call onStreetViewResult with OK status`() = runTest {
        // Arrange
        val latLng = LatLng(1.0, 2.0)
        val apiKey = "some_api_key"
        val callback = mockk<StreetViewJavaHelper.StreetViewCallback>(relaxed = true)
        coEvery { StreetViewUtils.fetchStreetViewData(latLng, apiKey) } returns Status.OK

        // Act
        StreetViewJavaHelper.fetchStreetViewData(latLng, apiKey, callback)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify { callback.onStreetViewResult(Status.OK) }
    }

    /**
     * Tests that [StreetViewJavaHelper.fetchStreetViewData] calls the onStreetViewError callback when an exception occurs.
     */
    @Test
    fun `fetchStreetViewData should call onStreetViewError when an exception occurs`() = runTest {
        // Arrange
        val latLng = LatLng(1.0, 2.0)
        val apiKey = "some_api_key"
        val callback = mockk<StreetViewJavaHelper.StreetViewCallback>(relaxed = true)
        val exception = Exception("some_error")
        coEvery { StreetViewUtils.fetchStreetViewData(latLng, apiKey) } throws exception

        // Act
        StreetViewJavaHelper.fetchStreetViewData(latLng, apiKey, callback)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify { callback.onStreetViewError(exception) }
    }
}