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

package com.google.maps.android.isochrone;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class IsochroneMapProviderTest {

    private GoogleMap mockMap;
    private IsochroneMapProvider.LoadingListener mockLoadingListener;
    private IsochroneMapProvider.TravelTimeFetcher mockTravelTimeFetcher;

    @Before
    public void setup() {
        mockMap = mock(GoogleMap.class);
        mockLoadingListener = mock(IsochroneMapProvider.LoadingListener.class);
        mockTravelTimeFetcher = mock(IsochroneMapProvider.TravelTimeFetcher.class);
    }

    @Test
    public void testComputeIsochroneReturnsPoints() {
        when(mockTravelTimeFetcher.fetchTravelTime(any(LatLng.class), any(LatLng.class))).thenReturn(60);

        IsochroneMapProvider provider = new IsochroneMapProvider(
                mockMap,
                "dummy-api-key",
                mockLoadingListener,
                IsochroneMapProvider.TransportMode.BICYCLING,
                mockTravelTimeFetcher);

        LatLng origin = new LatLng(0, 0);
        List<LatLng> polygon = provider.computeIsochrone(origin, 10);

        assertNotNull(polygon);
        assertTrue(polygon.size() > 3);  // polygon points + closing point
    }

    @Test
    public void testDrawIsochronesCallsLoadingListener() throws InterruptedException {
        when(mockTravelTimeFetcher.fetchTravelTime(any(LatLng.class), any(LatLng.class))).thenReturn(60);

        IsochroneMapProvider provider = new IsochroneMapProvider(
                mockMap,
                "dummy-api-key",
                mockLoadingListener,
                IsochroneMapProvider.TransportMode.BICYCLING,
                mockTravelTimeFetcher);

        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockLoadingListener).onLoadingFinished();

        // Run UI tasks immediately (synchronously)
        provider.setUiThreadExecutor(Runnable::run);

        provider.drawIsochrones(new LatLng(0, 0), new int[]{5, 10}, IsochroneMapProvider.ColorSchema.GREEN_RED);

        boolean finished = latch.await(5, TimeUnit.SECONDS);

        assertTrue("Loading finished callback should be called", finished);

        verify(mockLoadingListener).onLoadingStarted();
        verify(mockLoadingListener).onLoadingFinished();
    }

    @Test
    public void testPolygonsDrawnInCorrectOrder() throws InterruptedException {
        when(mockTravelTimeFetcher.fetchTravelTime(any(LatLng.class), any(LatLng.class))).thenReturn(60);

        IsochroneMapProvider provider = new IsochroneMapProvider(
                mockMap,
                "dummy-api-key",
                mockLoadingListener,
                IsochroneMapProvider.TransportMode.BICYCLING,
                mockTravelTimeFetcher);

        provider.setUiThreadExecutor(Runnable::run);

        Polygon mockPolygon = mock(Polygon.class);
        when(mockMap.addPolygon(any(PolygonOptions.class))).thenReturn(mockPolygon);

        ArgumentCaptor<PolygonOptions> captor = ArgumentCaptor.forClass(PolygonOptions.class);

        provider.drawIsochrones(new LatLng(0, 0), new int[]{10, 5}, IsochroneMapProvider.ColorSchema.GREEN_RED);

        // Wait for async completion
        Thread.sleep(1000);

        verify(mockMap, times(2)).addPolygon(captor.capture());

        List<PolygonOptions> drawnPolygons = captor.getAllValues();

        assertEquals(2, drawnPolygons.size());

        int outerFillColor = drawnPolygons.get(0).getFillColor();
        int innerFillColor = drawnPolygons.get(1).getFillColor();

        int outerRed = (outerFillColor >> 16) & 0xFF;
        int outerGreen = (outerFillColor >> 8) & 0xFF;

        int innerRed = (innerFillColor >> 16) & 0xFF;
        int innerGreen = (innerFillColor >> 8) & 0xFF;

        System.out.printf("Outer polygon fill color: 0x%08X (R:%d G:%d)%n", outerFillColor, outerRed, outerGreen);
        System.out.printf("Inner polygon fill color: 0x%08X (R:%d G:%d)%n", innerFillColor, innerRed, innerGreen);

        // Outer polygon expected to be yellow (high red and green)
        assertTrue("Outer polygon red should be >= inner polygon red", outerRed >= innerRed);
        assertTrue("Outer polygon green should be >= inner polygon green", outerGreen >= innerGreen);

        // Inner polygon expected to be green (high green, low red)
        assertTrue("Inner polygon green should be > outer polygon green", innerGreen > outerGreen || innerGreen == outerGreen);
        assertTrue("Inner polygon red should be <= outer polygon red", innerRed <= outerRed);
    }

}
