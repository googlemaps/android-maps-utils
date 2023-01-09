/*
 * Copyright 2015 Sean J. Barbeau
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

package com.google.maps.android.utils.demo;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class PolySimplifyDemoActivity extends BaseDemoActivity {

    private final static String LINE = "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@";
    private final static String OVAL_POLYGON = "}wgjDxw_vNuAd@}AN{A]w@_Au@kAUaA?{@Ke@@_@C]D[FULWFOLSNMTOVOXO\\I\\CX?VJXJTDTNXTVVLVJ`@FXA\\AVLZBTATBZ@ZAT?\\?VFT@XGZAP";
    private final static int ALPHA_ADJUSTMENT = 0x77000000;

    @Override
    protected void startDemo(boolean isRestore) {
        GoogleMap map = getMap();

        // Original line
        List<LatLng> line = PolyUtil.decode(LINE);
        map.addPolyline(new PolylineOptions()
                .addAll(line)
                .color(Color.BLACK));

        if (!isRestore) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.05870, -82.4090), 15));
        }

        List<LatLng> simplifiedLine;

        /*
         * Simplified lines - increasing the tolerance will result in fewer points in the simplified
         * line
         */
        double tolerance = 5; // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        map.addPolyline(new PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.RED - ALPHA_ADJUSTMENT));

        tolerance = 20; // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        map.addPolyline(new PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.GREEN - ALPHA_ADJUSTMENT));

        tolerance = 50; // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        map.addPolyline(new PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.MAGENTA - ALPHA_ADJUSTMENT));

        tolerance = 500; // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        map.addPolyline(new PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.YELLOW - ALPHA_ADJUSTMENT));

        tolerance = 1000; // meters
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        map.addPolyline(new PolylineOptions()
                .addAll(simplifiedLine)
                .color(Color.BLUE - ALPHA_ADJUSTMENT));


        // Triangle polygon - the polygon should be closed
        ArrayList<LatLng> triangle = new ArrayList<>();
        triangle.add(new LatLng(28.06025,-82.41030));  // Should match last point
        triangle.add(new LatLng(28.06129,-82.40945));
        triangle.add(new LatLng(28.06206,-82.40917));
        triangle.add(new LatLng(28.06125,-82.40850));
        triangle.add(new LatLng(28.06035,-82.40834));
        triangle.add(new LatLng(28.06038, -82.40924));
        triangle.add(new LatLng(28.06025,-82.41030));  // Should match first point

        map.addPolygon(new PolygonOptions()
                .addAll(triangle)
                .fillColor(Color.BLUE - ALPHA_ADJUSTMENT)
                .strokeColor(Color.BLUE)
                .strokeWidth(5));

        // Simplified triangle polygon
        tolerance = 88; // meters
        List simplifiedTriangle = PolyUtil.simplify(triangle, tolerance);
        map.addPolygon(new PolygonOptions()
                .addAll(simplifiedTriangle)
                .fillColor(Color.YELLOW - ALPHA_ADJUSTMENT)
                .strokeColor(Color.YELLOW)
                .strokeWidth(5));

        // Oval polygon - the polygon should be closed
        List<LatLng> oval = PolyUtil.decode(OVAL_POLYGON);
        map.addPolygon(new PolygonOptions()
                .addAll(oval)
                .fillColor(Color.BLUE - ALPHA_ADJUSTMENT)
                .strokeColor(Color.BLUE)
                .strokeWidth(5));

        // Simplified oval polygon
        tolerance = 10; // meters
        List simplifiedOval= PolyUtil.simplify(oval, tolerance);
        map.addPolygon(new PolygonOptions()
                .addAll(simplifiedOval)
                .fillColor(Color.YELLOW - ALPHA_ADJUSTMENT)
                .strokeColor(Color.YELLOW)
                .strokeWidth(5));
    }
}
