/*
 * Copyright 2026 Google Inc.
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

/**
 * Interface for sanitizing URLs in KML documents.
 * Developers can implement this to control which external resources (images, etc.) are loaded.
 */
public interface KmlUrlSanitizer {
    /**
     * Sanitizes a URL before it is used to fetch a resource.
     *
     * @param url The raw URL from the KML.
     * @return A safe, validated URL string, or null to block this resource.
     */
    String sanitizeUrl(String url);
}
