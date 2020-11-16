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
        listOf("setInfoWindowAdapter", "println")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator
        if (!evaluator.isMemberInClass(method, "com.google.android.gms.maps.GoogleMap")) {
            return
        }
        context.report(INFO_WINDOW_OVERRIDE, node, context.getLocation(node), INFO_WINDOW_OVERRIDE.getBriefDescription(TextFormat.TEXT))
    }

    companion object {
        val INFO_WINDOW_OVERRIDE = Issue.create(
            id = "GoogleMapUtilsInfoWindowOverride",
            briefDescription = "Potential custom info window collision.",
            explanation = """
                This lint warns for potential misuse of creating a custom info window adapter while
                using the Maps SDK for Android Utility Library. For example, if implementations are
                using a MarkerManager.Collection class, such as while using the clustering feature,
                setting the custom info window must be done on the MarkerManager.Collection object
                instead.
                
                e.g.
                
                ```
                ClusterManager clusterManager = // ...
                MarkerManager.Collection collection = clusterManager.getMarkerCollection();
                collection.setInfoWindowAdapter(...);
                ```
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
