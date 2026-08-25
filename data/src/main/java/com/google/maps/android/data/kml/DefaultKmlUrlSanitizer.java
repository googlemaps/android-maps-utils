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
 * <p>This sanitizer allows only {@code http}/{@code https} URLs whose host does not resolve to an
 * internal address (see {@link #isInternalAddress(InetAddress)}), and returns {@code null} (block)
 * otherwise. Public icon/overlay URLs continue to load unchanged.
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
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                return null;
            }
            for (InetAddress address : addresses) {
                if (isInternalAddress(address)) {
                    return null;
                }
            }
        } catch (Exception e) {
            // Unresolvable host: fail closed.
            return null;
        }

        return url;
    }

    /**
     * Returns {@code true} if {@code address} is one that a fetch triggered by an untrusted
     * document must never reach.
     *
     * <p>Covers the ranges recognized by {@link InetAddress} (loopback, wildcard/any-local,
     * link-local, IPv4 RFC 1918 site-local, multicast) plus ranges that {@code InetAddress} does
     * not classify but are still internal or special-purpose:
     *
     * <ul>
     *   <li>{@code fc00::/7} IPv6 Unique Local Addresses (RFC 4193) not covered by
     *       {@code isSiteLocalAddress()}.
     *   <li>{@code 100.64.0.0/10} IPv4 Carrier-Grade NAT / shared address space (RFC 6598).
     *   <li>{@code 192.0.0.0/24} IETF protocol assignments (RFC 6890), which includes NAT64 and
     *       other internal-only special-use addresses.
     * </ul>
     */
    public static boolean isInternalAddress(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        final byte[] b = address.getAddress();
        if (b.length == 4) {
            final int b0 = b[0] & 0xFF;
            final int b1 = b[1] & 0xFF;
            final int b2 = b[2] & 0xFF;
            // 100.64.0.0/10 (CGNAT, RFC 6598): 100.64.0.0 - 100.127.255.255.
            if (b0 == 100 && (b1 & 0xC0) == 0x40) {
                return true;
            }
            // 192.0.0.0/24 (IETF protocol assignments, RFC 6890).
            if (b0 == 192 && b1 == 0 && b2 == 0) {
                return true;
            }
        } else if (b.length == 16) {
            // fc00::/7 (IPv6 Unique Local Addresses, RFC 4193): first byte fc or fd.
            if ((b[0] & 0xFE) == 0xFC) {
                return true;
            }
        }
        return false;
    }
}
