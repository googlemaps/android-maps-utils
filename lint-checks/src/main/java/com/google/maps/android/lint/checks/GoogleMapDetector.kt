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

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

@Suppress("UnstableApiUsage")
class GoogleMapDetector : Detector(), SourceCodeScanner  {
    override fun getApplicableMethodNames(): List<String>? =
        listOf(
            "setInfoWindowAdapter",
            "setOnInfoWindowClickListener",
            "setOnInfoWindowLongClickListener",
            "setOnMarkerClickListener",
            "setOnMarkerDragListener",
        )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator
        if (!evaluator.isMemberInClass(method, "com.google.android.gms.maps.GoogleMap")) {
            return
        }
        context.report(
            issue = POTENTIAL_BEHAVIOR_OVERRIDE,
            scope = node,
            location = context.getLocation(node),
            message = POTENTIAL_BEHAVIOR_OVERRIDE.getBriefDescription(TextFormat.TEXT)
        )
    }

    companion object {
        private val migrationGuideUrl = "https://bit.ly/3kTpQmY"

        val POTENTIAL_BEHAVIOR_OVERRIDE = Issue.create(
            id = "PotentialBehaviorOverride",
            briefDescription = "Using this method may override behaviors set by the Maps SDK for " +
                "Android Utility Library. If you are not using clustering, GeoJson, or KML, you " +
                "can safely suppress this warning, otherwise, refer to the utility " +
                "library's migration guide: $migrationGuideUrl",
            explanation = """
                This lint warns for potential behavior override while using clustering, GeoJson, or
                KML since these features use this method in their internal implementations. As such,
                to achieve the desired behavior requires using an alternative API.
                
                For example, if you are using the clustering feature and want to set a custom info
                window, rather than invoking `GoogleMap#setInfoWindowAdapter(...)`, you would need
                to set the custom info window on the MarkerManager.Collection object instead.
                
                e.g.
                
                ```
                ClusterManager clusterManager = // ...
                MarkerManager.Collection collection = clusterManager.getMarkerCollection();
                collection.setInfoWindowAdapter(...);
                ```
                
                Refer to the migration guide for more info: $migrationGuideUrl
            """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                GoogleMapDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
