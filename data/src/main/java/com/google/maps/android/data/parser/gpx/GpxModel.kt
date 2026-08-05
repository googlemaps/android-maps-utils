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
package com.google.maps.android.data.parser.gpx

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

private const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"

@Serializable
@XmlSerialName("gpx", namespace = GPX_NAMESPACE, prefix = "")
data class Gpx(
    @XmlSerialName("version")
    val version: String = "1.1",
    @XmlSerialName("creator")
    val creator: String? = null,
    @XmlElement(true)
    @XmlSerialName("metadata", namespace = GPX_NAMESPACE, prefix = "")
    val metadata: Metadata? = null,
    @XmlElement(true)
    @XmlSerialName("wpt", namespace = GPX_NAMESPACE, prefix = "")
    val waypoints: List<Wpt> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("rte", namespace = GPX_NAMESPACE, prefix = "")
    val routes: List<Rte> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("trk", namespace = GPX_NAMESPACE, prefix = "")
    val tracks: List<Trk> = emptyList(),
)

@Serializable
@XmlSerialName("metadata", namespace = GPX_NAMESPACE, prefix = "")
data class Metadata(
    @XmlElement(true)
    @XmlSerialName("name", namespace = GPX_NAMESPACE, prefix = "")
    val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("desc", namespace = GPX_NAMESPACE, prefix = "")
    val desc: String? = null,
    @XmlElement(true)
    @XmlSerialName("time", namespace = GPX_NAMESPACE, prefix = "")
    val time: String? = null,
)

/**
 * Represents a GPX waypoint (`<wpt>`), point of interest, or named feature on a map.
 *
 * @property lat The latitude of the waypoint.
 * @property lon The longitude of the waypoint.
 * @property ele The elevation (in meters) of the waypoint, or null if unspecified.
 * @property time The timestamp of the waypoint.
 * @property name The name of the waypoint.
 * @property desc A description of the waypoint.
 * @property sym The symbol name or icon for the waypoint.
 */
@Serializable
@XmlSerialName("wpt", namespace = GPX_NAMESPACE, prefix = "")
data class Wpt(
    @XmlSerialName("lat")
    val lat: Double,
    @XmlSerialName("lon")
    val lon: Double,
    @XmlElement(true)
    @XmlSerialName("ele", namespace = GPX_NAMESPACE, prefix = "")
    val ele: Double? = null,
    @XmlElement(true)
    @XmlSerialName("time", namespace = GPX_NAMESPACE, prefix = "")
    val time: String? = null,
    @XmlElement(true)
    @XmlSerialName("name", namespace = GPX_NAMESPACE, prefix = "")
    val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("desc", namespace = GPX_NAMESPACE, prefix = "")
    val desc: String? = null,
    @XmlElement(true)
    @XmlSerialName("sym", namespace = GPX_NAMESPACE, prefix = "")
    val sym: String? = null,
) {
    /**
     * Descriptive alias for [ele] (elevation in meters).
     */
    val elevation: Double?
        get() = ele

    init {
        require(lat.isFinite() && lon.isFinite() && (elevation?.isFinite() ?: true)) {
            "GPX coordinate contains a non-finite value"
        }
    }
}

@Serializable
@XmlSerialName("rte", namespace = GPX_NAMESPACE, prefix = "")
data class Rte(
    @XmlElement(true)
    @XmlSerialName("name", namespace = GPX_NAMESPACE, prefix = "")
    val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("desc", namespace = GPX_NAMESPACE, prefix = "")
    val desc: String? = null,
    @XmlElement(true)
    @XmlSerialName("rtept", namespace = GPX_NAMESPACE, prefix = "")
    val routePoints: List<Wpt> = emptyList(),
)

@Serializable
@XmlSerialName("trk", namespace = GPX_NAMESPACE, prefix = "")
data class Trk(
    @XmlElement(true)
    @XmlSerialName("name", namespace = GPX_NAMESPACE, prefix = "")
    val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("desc", namespace = GPX_NAMESPACE, prefix = "")
    val desc: String? = null,
    @XmlElement(true)
    @XmlSerialName("trkseg", namespace = GPX_NAMESPACE, prefix = "")
    val trackSegments: List<TrkSeg> = emptyList(),
)

@Serializable
@XmlSerialName("trkseg", namespace = GPX_NAMESPACE, prefix = "")
data class TrkSeg(
    @XmlElement(true)
    @XmlSerialName("trkpt", namespace = GPX_NAMESPACE, prefix = "")
    val trackPoints: List<Wpt> = emptyList(),
)
