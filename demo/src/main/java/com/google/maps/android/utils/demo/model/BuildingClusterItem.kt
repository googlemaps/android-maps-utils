package com.google.maps.android.utils.demo.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * A lightweight [ClusterItem] representing an individual building point.
 *
 * @property lat Latitude in degrees.
 * @property lng Longitude in degrees.
 * @property id Optional identifier of the building.
 */
public class BuildingClusterItem(
    public val lat: Double,
    public val lng: Double,
    public val id: Int = -1,
) : ClusterItem {
    private val mPosition: LatLng = LatLng(lat, lng)

    override val position: LatLng
        get() = mPosition

    override val title: String
        get() = if (id >= 0) "Building #$id" else "Building"

    override val snippet: String
        get() = "Lat: %.5f, Lng: %.5f".format(lat, lng)

    override val zIndex: Float?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BuildingClusterItem) return false
        return lat == other.lat && lng == other.lng && id == other.id
    }

    override fun hashCode(): Int {
        var result = lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + id
        return result
    }
}
