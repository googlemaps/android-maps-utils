package com.google.maps.android.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class MapsUtilsIssueRegistry : IssueRegistry() {

    override val issues: List<Issue> =
        listOf(GoogleMapDetector.INFO_WINDOW_OVERRIDE)

    override val api: Int =
        CURRENT_API

    override val minApi: Int
        get() = 1
}