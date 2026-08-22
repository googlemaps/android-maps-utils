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

import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;

/**
 * Default, secure {@link KmlUrlSanitizer} used when a layer loads remote icons/overlays.
 *
 * <p>Icon and GroundOverlay {@code <href>} values come from the parsed KML/GeoJSON document,
 * which is frequently untrusted (downloaded, user-provided, or shared). Fetching those URLs
 * without validation is a Server-Side Request Forgery (SSRF) primitive: a crafted document can
 * make the app issue requests to loopback, link-local (including the {@code 169.254.169.254}
 * metadata endpoint), or private-network hosts reachable from the device.
 *
 * <p>This sanitizer allows only {@code http}/{@code https} URLs whose host does not resolve to a
 * loopback, any-local, link-local, site-local (RFC 1918), or multicast address, and returns
 * {@code null} (block) otherwise. Public icon/overlay URLs continue to load unchanged.
 */
public class DefaultKmlUrlSanitizer implements KmlUrlSanitizer {

    @Override
    public String sanitizeUrl(String url) {
        if (url == null) {
            return null;
        }
        final URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            return null;
        }

        final String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }
        final String lowerScheme = scheme.toLowerCase(Locale.ROOT);
        if (!lowerScheme.equals("http") && !lowerScheme.equals("https")) {
            return null;
        }

        final String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            return null;
        }

        try {
            // Block if ANY resolved address is internal, to defeat split-horizon / multi-A tricks.
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isLoopbackAddress()
                        || address.isAnyLocalAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    return null;
                }
            }
        } catch (Exception e) {
            // Unresolvable host: fail closed.
            return null;
        }

        return url;
    }
}
