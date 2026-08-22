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
package com.google.maps.android.data.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for {@link DefaultKmlUrlSanitizer}. Uses IP-literal hosts so the tests are
 * hermetic (no DNS/network required).
 */
public class DefaultKmlUrlSanitizerTest {

    private final DefaultKmlUrlSanitizer sanitizer = new DefaultKmlUrlSanitizer();

    @Test
    public void blocksLoopback() {
        assertNull(sanitizer.sanitizeUrl("http://127.0.0.1:8080/INTERNAL-ADMIN"));
        assertNull(sanitizer.sanitizeUrl("http://[::1]/x"));
    }

    @Test
    public void blocksLinkLocalMetadataEndpoint() {
        // 169.254.169.254 is the cloud/instance metadata endpoint.
        assertNull(sanitizer.sanitizeUrl("http://169.254.169.254/latest/meta-data/"));
    }

    @Test
    public void blocksPrivateRfc1918() {
        assertNull(sanitizer.sanitizeUrl("http://10.0.0.5/internal"));
        assertNull(sanitizer.sanitizeUrl("http://192.168.1.1/router"));
        assertNull(sanitizer.sanitizeUrl("http://172.16.0.1/x"));
    }

    @Test
    public void blocksAnyLocalAddress() {
        assertNull(sanitizer.sanitizeUrl("http://0.0.0.0/x"));
    }

    @Test
    public void blocksNonHttpSchemes() {
        assertNull(sanitizer.sanitizeUrl("file:///etc/passwd"));
        assertNull(sanitizer.sanitizeUrl("ftp://8.8.8.8/x"));
        assertNull(sanitizer.sanitizeUrl("gopher://8.8.8.8/x"));
    }

    @Test
    public void blocksMalformedOrHostless() {
        assertNull(sanitizer.sanitizeUrl(null));
        assertNull(sanitizer.sanitizeUrl("not a url"));
        assertNull(sanitizer.sanitizeUrl("http://"));
    }

    @Test
    public void allowsPublicHttpAndHttps() {
        // 8.8.8.8 is a public address (not loopback/link-local/site-local/multicast/any-local).
        assertEquals("http://8.8.8.8/mapfiles/icon.png",
                sanitizer.sanitizeUrl("http://8.8.8.8/mapfiles/icon.png"));
        assertEquals("https://8.8.8.8/mapfiles/icon.png",
                sanitizer.sanitizeUrl("https://8.8.8.8/mapfiles/icon.png"));
    }
}
