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

package com.google.maps.android.data;

import android.util.Log;

/**
 * Utility class for logging renderer-related debug output.
 *
 * <p>Use {@link #setEnabled(boolean)} to toggle logging globally.
 * This class avoids the need for scattered conditionals in the codebase.</p>
 */
public final class RendererLogger {

    private static boolean enabled = false;

    private RendererLogger() {
        // Prevent instantiation
    }

    /**
     * Enables or disables logging.
     *
     * @param value {@code true} to enable logging; {@code false} to disable it.
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Logs a debug message if logging is enabled.
     *
     * @param tag Tag for the log message.
     * @param message The debug message to log.
     */
    public static void d(String tag, String message) {
        if (enabled) {
            Log.d(tag, message);
        }
    }

    /**
     * Logs an info message if logging is enabled.
     *
     * @param tag Tag for the log message.
     * @param message The info message to log.
     */
    public static void i(String tag, String message) {
        if (enabled) {
            Log.i(tag, message);
        }
    }

    /**
     * Logs a warning message if logging is enabled.
     *
     * @param tag Tag for the log message.
     * @param message The warning message to log.
     */
    public static void w(String tag, String message) {
        if (enabled) {
            Log.w(tag, message);
        }
    }

    /**
     * Logs an error message if logging is enabled.
     *
     * @param tag Tag for the log message.
     * @param message The error message to log.
     */
    public static void e(String tag, String message) {
        if (enabled) {
            Log.e(tag, message);
        }
    }
}
