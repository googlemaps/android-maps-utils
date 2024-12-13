/*
 * Copyright 2015 Google Inc.
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

package com.google.maps.android.clustering;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.collections.MarkerManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

public class ClusterManagerTest<T extends ClusterItem> {

    private ClusterManager<T> classUnderTest;
    private Algorithm<T> algorithm;

    private Context mockContext;
    private GoogleMap mockMap;
    private MarkerManager mockMarkerManager;

    @Before
    public void setUp() {
        // Create mocks for dependencies
        mockContext = mock(Context.class);
        mockMap = mock(GoogleMap.class);
        mockMarkerManager = mock(MarkerManager.class);
        algorithm = mock(Algorithm.class);

        // Initialize the class under test with the mocked dependencies
        classUnderTest = new ClusterManager<T>(mockContext, mockMap, mockMarkerManager) {
            @Override
            public Algorithm<T> getAlgorithm() {
                return algorithm;
            }
        };
    }

    @Test
    public void testDiff() {
        // Use a specific class type for T
        Class<T> type = (Class) String.class; // Replace with actual type
        algorithm = mock(Algorithm.class);

        classUnderTest = spy(new ClusterManager<T>(mock(Context.class), mock(GoogleMap.class), mock(MarkerManager.class)) {
            @Override
            public Algorithm<T> getAlgorithm() {
                return algorithm;
            }
        });

        // Mock input collections using the explicit type
        T addItem1 = mock(type);
        T addItem2 = mock(type);
        Collection<T> add = Arrays.asList(addItem1, addItem2);

        T removeItem = mock(type);
        Collection<T> remove = Arrays.asList(removeItem);

        T modifyItem1 = mock(type);
        T modifyItem2 = mock(type);
        T modifyItem3 = mock(type);
        Collection<T> modify = Arrays.asList(modifyItem1, modifyItem2, modifyItem3);

        // Call the method under test
        classUnderTest.diff(add, remove, modify);

        // Verify algorithm lock and unlock
        verify(algorithm).lock();
        verify(algorithm).unlock();

        // Verify add items
        for (T item : add) {
            verify(algorithm).addItem(item);
        }

        // Verify remove items
        verify(algorithm).removeItems(remove);

        // Verify modify items
        for (T item : modify) {
            verify(classUnderTest).updateItem(item);
        }
    }

    @Test
    public void testDiffWithNullCollections() {
        // Call the method with null collections
        classUnderTest.diff(null, null, null);

        // Verify locking and unlocking
        verify(algorithm).lock();
        verify(algorithm).unlock();

        // Verify no operations are called
        verifyNoMoreInteractions(algorithm);
    }
}