// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.maps.android.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.TextFormat

@Suppress("UnstableApiUsage")
class GoogleMapDetectorTest : LintDetectorTest() {

    fun testSetOnMarkerDragListener() {
        lint().files(
            testFile("""
                        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override 
                            public void onMarkerDragStart(Marker marker) {
                            }
                            
                            @Override 
                            public void onMarkerDrag(Marker marker) {
                            }
                            
                            @Override 
                            public void onMarkerDragEnd(Marker marker) {
                            }
                      });
            """),
            java(GOOGLE_MAP_STUB).indented()
        ).run().expect(expectedText("map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {"))
    }

    fun testSetOnMarkerClickListener() {
        lint().files(
            testFile("""
                        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override 
                            public boolean onMarkerClick(Marker marker) {
                                return false;
                            }
                      });
            """),
            java(GOOGLE_MAP_STUB).indented()
        ).run().expect(expectedText("map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {"))
    }

    fun testSetOnInfoWindowLongClickListener() {
        lint().files(
            testFile("""
                        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                            @Override 
                            public void onInfoWindowLongClick(Marker marker) {
                            }
                      });
            """),
            java(GOOGLE_MAP_STUB).indented()
        ).run().expect(expectedText("map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {"))
    }

    fun testSetOnInfoWindowClickListener() {
        lint().files(
            testFile("""
                        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override 
                            public void onInfoWindowClick(Marker marker) {
                            }
                      });
            """),
            java(GOOGLE_MAP_STUB).indented()
        ).run().expect(expectedText("map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {"))
    }

    fun testSetInfoWindowAdapter() {
        lint().files(
            testFile(
            """
                        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override 
                            public View getInfoWindow(Marker marker) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                return null;
                            }
                      });
            """),
            java(GOOGLE_MAP_STUB).indented()
        ).run().expect(expectedText("map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {"))
    }

    private fun expectedText(text: String): String =
        """
        src/test/pkg/TestClass.java:10: Warning: ${
            GoogleMapDetector.POTENTIAL_BEHAVIOR_OVERRIDE.getBriefDescription(
                TextFormat.TEXT
            )} [${GoogleMapDetector.POTENTIAL_BEHAVIOR_OVERRIDE.id}]
                $text
                ^
        0 errors, 1 warnings
        """

    private fun testFile(containing: String): TestFile {
        return java(
            """
                package test.pkg;
                
                import com.google.android.gms.maps.GoogleMap;
                
                public class TestClass {
                    private GoogleMap map;
                    
                    private void foo() { 
                        $containing
                    }
                }
            """
        ).indented()
    }

    override fun getDetector(): Detector {
        return GoogleMapDetector()
    }

    override fun getIssues(): List<Issue> {
        return listOf(GoogleMapDetector.POTENTIAL_BEHAVIOR_OVERRIDE)
    }

    companion object {
        val GOOGLE_MAP_STUB = """
        package com.google.android.gms.maps;
        
        import android.view.View;
        
        public class GoogleMap {
           public final void setInfoWindowAdapter(GoogleMap.InfoWindowAdapter adapter) {
             // STUB
           }

           public final void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener l) {
             // STUB
           }
           
           public final void setOnInfoWindowLongClickListener(GoogleMap.OnInfoWindowLongClickListener l) {
             // STUB
           }
           
           public final void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener l) {
             // STUB
           }
           
           
           public final void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener l) {
             // STUB
           }
           
           public interface OnMarkerDragListener {
             void onMarkerDragStart(Marker marker);
             void onMarkerDrag(Marker marker);
             void onMarkerDragEnd(Marker marker);
           }
           
           public interface OnMarkerClickListener {
             boolean onMarkerClick(Marker marker);
           }
           
           public interface OnInfoWindowLongClickListener {
             void onInfoWindowLongClick(Marker marker);
           }
           
           public interface OnInfoWindowClickListener {
             void onInfoWindowClick(Marker marker);
           }
           
           public interface InfoWindowAdapter {
             View getInfoWindow(Marker marker);
             View getInfoContents(Marker marker);
           }
        }
        """
    }
}