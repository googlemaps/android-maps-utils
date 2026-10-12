/*
 * Copyright 2026 Google LLC
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
package com.google.maps.android.utils.demo.smoke

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import com.google.maps.android.utils.demo.MainActivity
import com.google.maps.android.utils.demo.demoGroups
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Keeps the menu in [MainActivity] and the manifest in sync, so every demo activity is reachable
 * from the menu and therefore covered by [DemoSmokeTest].
 */
@RunWith(AndroidJUnit4::class)
class DemoCatalogTest {
    @Test
    fun everyDeclaredActivityIsInTheMenu() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        @Suppress("DEPRECATION")
        val declared =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
                .activities
                .orEmpty()
                .map { it.name }
                .filter { it.startsWith(MainActivity::class.java.`package`!!.name) }
                .toSet() - MainActivity::class.java.name

        val inMenu = demoGroups().flatMap { it.demos }.map { it.activityClass.name }.toSet()

        assertWithMessage("Activities declared in the manifest but missing from the menu")
            .that(declared - inMenu)
            .isEmpty()
        assertWithMessage("Menu entries that are not declared in the manifest")
            .that(inMenu - declared)
            .isEmpty()
    }
}
