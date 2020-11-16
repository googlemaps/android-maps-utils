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

package com.googe.maps.android.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.google.maps.android.lint.checks.GoogleMapDetector

@Suppress("UnstableApiUsage")
class GoogleMapDetectorTest : LintDetectorTest() {
    fun testBasic() {
        lint().files(
            java("""
                package test.pkg;
                
                import com.google.android.gms.maps.GoogleMap;
                
                public class TestClass {
                    private GoogleMap map;
                    
                    private void foo() { 
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
                    }
                }
            """
            ).indented(),
            java(GOOGLE_MAP_STUB).indented())
            .run()
            .expect("""
                    src/test/pkg/TestClass.java:9: Warning: Potential custom info window collision. [GoogleMapUtilsInfoWindowOverride]
                            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            ^
                    0 errors, 1 warnings
                    """
            )
    }

    override fun getDetector(): Detector {
        return GoogleMapDetector()
    }

    override fun getIssues(): List<Issue> {
        return listOf(GoogleMapDetector.INFO_WINDOW_OVERRIDE)
    }

    companion object {
        val GOOGLE_MAP_STUB = """
        package com.google.android.gms.maps;
        
        import android.view.View;
        import com.google.android.gms.maps.model.Marker;
        
        public class GoogleMap {
           public final void setInfoWindowAdapter(GoogleMap.InfoWindowAdapter adapter) {
             // STUB
           }
           
           public interface InfoWindowAdapter {
             View getInfoWindow(Marker marker);
             View getInfoContents(Marker marker);
           }
        }
        """
    }
}
