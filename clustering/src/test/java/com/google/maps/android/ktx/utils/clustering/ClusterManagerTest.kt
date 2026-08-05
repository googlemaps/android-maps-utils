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
 *
 */

package com.google.maps.android.ktx.utils.clustering

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class ClusterManagerTest {

    @Mock
    private lateinit var clusterManager: ClusterManager<ClusterItem>

    @Mock
    private lateinit var cluster: Cluster<ClusterItem>

    @Mock
    private lateinit var clusterItem: ClusterItem

    @Captor
    private lateinit var clusterClickListener: ArgumentCaptor<ClusterManager.OnClusterClickListener<ClusterItem>>

    @Captor
    private lateinit var clusterItemClickListener: ArgumentCaptor<ClusterManager.OnClusterItemClickListener<ClusterItem>>

    @Captor
    private lateinit var clusterInfoWindowClickListener: ArgumentCaptor<ClusterManager.OnClusterInfoWindowClickListener<ClusterItem>>

    @Captor
    private lateinit var clusterInfoWindowLongClickListener: ArgumentCaptor<ClusterManager.OnClusterInfoWindowLongClickListener<ClusterItem>>

    @Captor
    private lateinit var clusterItemInfoWindowClickListener: ArgumentCaptor<ClusterManager.OnClusterItemInfoWindowClickListener<ClusterItem>>

    @Captor
    private lateinit var clusterItemInfoWindowLongClickListener: ArgumentCaptor<ClusterManager.OnClusterItemInfoWindowLongClickListener<ClusterItem>>

    @Test
    public fun testClusterClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterClickEvents().first()
            assertThat(event).isEqualTo(cluster)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterClickListener(clusterClickListener.capture())
        clusterClickListener.value.onClusterClick(cluster)
        job.cancel()
    }

    @Test
    public fun testClusterItemClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterItemClickEvents().first()
            assertThat(event).isEqualTo(clusterItem)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterItemClickListener(clusterItemClickListener.capture())
        clusterItemClickListener.value.onClusterItemClick(clusterItem)
        job.cancel()
    }

    @Test
    public fun testClusterInfoWindowClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterInfoWindowClickEvents().first()
            assertThat(event).isEqualTo(cluster)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterInfoWindowClickListener(clusterInfoWindowClickListener.capture())
        clusterInfoWindowClickListener.value.onClusterInfoWindowClick(cluster)
        job.cancel()
    }

    @Test
    public fun testClusterInfoWindowLongClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterInfoWindowLongClickEvents().first()
            assertThat(event).isEqualTo(cluster)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterInfoWindowLongClickListener(clusterInfoWindowLongClickListener.capture())
        clusterInfoWindowLongClickListener.value.onClusterInfoWindowLongClick(cluster)
        job.cancel()
    }

    @Test
    public fun testClusterItemInfoWindowClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterItemInfoWindowClickEvents().first()
            assertThat(event).isEqualTo(clusterItem)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterItemInfoWindowClickListener(clusterItemInfoWindowClickListener.capture())
        clusterItemInfoWindowClickListener.value.onClusterItemInfoWindowClick(clusterItem)
        job.cancel()
    }

    @Test
    public fun testClusterItemInfoWindowLongClickEvents(): Unit = runTest {
        val job = launch {
            val event = clusterManager.clusterItemInfoWindowLongClickEvents().first()
            assertThat(event).isEqualTo(clusterItem)
        }
        advanceUntilIdle()
        verify(clusterManager).setOnClusterItemInfoWindowLongClickListener(clusterItemInfoWindowLongClickListener.capture())
        clusterItemInfoWindowLongClickListener.value.onClusterItemInfoWindowLongClick(clusterItem)
        job.cancel()
    }
}
