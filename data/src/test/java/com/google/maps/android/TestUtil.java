/*
 * Copyright 2026 Google LLC
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
package com.google.maps.android;

public class TestUtil {

    /**
     * Returns true if execution is occurring on GitHub Actions, and false otherwise.
     * This is determined by checking the GITHUB_ACTIONS environment variable.
     *
     * @return true if running on GitHub Actions, false otherwise.
     */
    public static boolean isRunningOnGitHub() {
        return "true".equals(System.getenv("GITHUB_ACTIONS"));
    }
}
