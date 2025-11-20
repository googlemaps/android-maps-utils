/*
 * Copyright 2020 Google Inc.
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
package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class KmlPolygonTest {
    private KmlPolygon createRegularPolygon() {
        List<LatLng> outerCoordinates = new ArrayList<>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        List<List<LatLng>> innerCoordinates = new ArrayList<>();
        List<LatLng> innerHole = new ArrayList<>();
        innerHole.add(new LatLng(20, 20));
        innerHole.add(new LatLng(10, 10));
        innerHole.add(new LatLng(20, 20));
        innerCoordinates.add(innerHole);
        return new KmlPolygon(outerCoordinates, innerCoordinates);
    }

    private KmlPolygon createOuterPolygon() {
        ArrayList<LatLng> outerCoordinates = new ArrayList<>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        return new KmlPolygon(outerCoordinates, null);
    }

    @Test
    public void testGetType() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryType());
        assertEquals("Polygon", kmlPolygon.getGeometryType());
    }

    @Test
    public void testGetOuterBoundaryCoordinates() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
    }

    @Test
    public void testGetInnerBoundaryCoordinates() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getInnerBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNull(kmlPolygon.getInnerBoundaryCoordinates());
    }

    @Test
    public void testGetKmlGeometryObject() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryObject());
        assertEquals(2, kmlPolygon.getGeometryObject().size());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryObject());
        assertEquals(1, kmlPolygon.getGeometryObject().size());
    }
}
