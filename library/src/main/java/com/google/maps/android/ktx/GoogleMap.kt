@file:Suppress("NOTHING_TO_INLINE")
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
package com.google.maps.android.ktx

import android.graphics.Bitmap
import android.location.Location
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.IndoorBuilding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import kotlinx.coroutines.flow.Flow

import com.google.maps.android.cameraEvents as canonicalCameraEvents
import com.google.maps.android.awaitAnimateCamera as canonicalAwaitAnimateCamera
import com.google.maps.android.awaitMapLoad as canonicalAwaitMapLoad
import com.google.maps.android.cameraIdleEvents as canonicalCameraIdleEvents
import com.google.maps.android.cameraMoveCanceledEvents as canonicalCameraMoveCanceledEvents
import com.google.maps.android.cameraMoveEvents as canonicalCameraMoveEvents
import com.google.maps.android.awaitSnapshot as canonicalAwaitSnapshot
import com.google.maps.android.cameraMoveStartedEvents as canonicalCameraMoveStartedEvents
import com.google.maps.android.circleClickEvents as canonicalCircleClickEvents
import com.google.maps.android.groundOverlayClicks as canonicalGroundOverlayClicks
import com.google.maps.android.indoorStateChangeEvents as canonicalIndoorStateChangeEvents
import com.google.maps.android.infoWindowClickEvents as canonicalInfoWindowClickEvents
import com.google.maps.android.infoWindowCloseEvents as canonicalInfoWindowCloseEvents
import com.google.maps.android.infoWindowLongClickEvents as canonicalInfoWindowLongClickEvents
import com.google.maps.android.mapClickEvents as canonicalMapClickEvents
import com.google.maps.android.mapLongClickEvents as canonicalMapLongClickEvents
import com.google.maps.android.markerClickEvents as canonicalMarkerClickEvents
import com.google.maps.android.markerDragEvents as canonicalMarkerDragEvents
import com.google.maps.android.myLocationButtonClickEvents as canonicalMyLocationButtonClickEvents
import com.google.maps.android.myLocationClickEvents as canonicalMyLocationClickEvents
import com.google.maps.android.poiClickEvents as canonicalPoiClickEvents
import com.google.maps.android.polygonClickEvents as canonicalPolygonClickEvents
import com.google.maps.android.polylineClickEvents as canonicalPolylineClickEvents
import com.google.maps.android.buildGoogleMapOptions as canonicalBuildGoogleMapOptions
import com.google.maps.android.addCircle as canonicalAddCircle
import com.google.maps.android.addGroundOverlay as canonicalAddGroundOverlay
import com.google.maps.android.addMarker as canonicalAddMarker
import com.google.maps.android.addPolygon as canonicalAddPolygon
import com.google.maps.android.addPolyline as canonicalAddPolyline
import com.google.maps.android.addTileOverlay as canonicalAddTileOverlay

@Deprecated("Moved to com.google.maps.android.MoveStartedReason", ReplaceWith("MoveStartedReason", "com.google.maps.android.MoveStartedReason"))
public typealias MoveStartedReason = com.google.maps.android.MoveStartedReason

@Deprecated("Moved to com.google.maps.android.CameraEvent", ReplaceWith("CameraEvent", "com.google.maps.android.CameraEvent"))
public typealias CameraEvent = com.google.maps.android.CameraEvent

@Deprecated("Moved to com.google.maps.android.CameraIdleEvent", ReplaceWith("CameraIdleEvent", "com.google.maps.android.CameraIdleEvent"))
public typealias CameraIdleEvent = com.google.maps.android.CameraIdleEvent

@Deprecated("Moved to com.google.maps.android.CameraMoveCanceledEvent", ReplaceWith("CameraMoveCanceledEvent", "com.google.maps.android.CameraMoveCanceledEvent"))
public typealias CameraMoveCanceledEvent = com.google.maps.android.CameraMoveCanceledEvent

@Deprecated("Moved to com.google.maps.android.CameraMoveEvent", ReplaceWith("CameraMoveEvent", "com.google.maps.android.CameraMoveEvent"))
public typealias CameraMoveEvent = com.google.maps.android.CameraMoveEvent

@Deprecated("Moved to com.google.maps.android.CameraMoveStartedEvent", ReplaceWith("CameraMoveStartedEvent", "com.google.maps.android.CameraMoveStartedEvent"))
public typealias CameraMoveStartedEvent = com.google.maps.android.CameraMoveStartedEvent

@Deprecated("Moved to com.google.maps.android.OnMarkerDragEvent", ReplaceWith("OnMarkerDragEvent", "com.google.maps.android.OnMarkerDragEvent"))
public typealias OnMarkerDragEvent = com.google.maps.android.OnMarkerDragEvent

@Deprecated("Moved to com.google.maps.android.MarkerDragEvent", ReplaceWith("MarkerDragEvent", "com.google.maps.android.MarkerDragEvent"))
public typealias MarkerDragEvent = com.google.maps.android.MarkerDragEvent

@Deprecated("Moved to com.google.maps.android.MarkerDragEndEvent", ReplaceWith("MarkerDragEndEvent", "com.google.maps.android.MarkerDragEndEvent"))
public typealias MarkerDragEndEvent = com.google.maps.android.MarkerDragEndEvent

@Deprecated("Moved to com.google.maps.android.MarkerDragStartEvent", ReplaceWith("MarkerDragStartEvent", "com.google.maps.android.MarkerDragStartEvent"))
public typealias MarkerDragStartEvent = com.google.maps.android.MarkerDragStartEvent

@Deprecated("Moved to com.google.maps.android.IndoorChangeEvent", ReplaceWith("IndoorChangeEvent", "com.google.maps.android.IndoorChangeEvent"))
public typealias IndoorChangeEvent = com.google.maps.android.IndoorChangeEvent

@Deprecated("Moved to com.google.maps.android.IndoorBuildingFocusedEvent", ReplaceWith("IndoorBuildingFocusedEvent", "com.google.maps.android.IndoorBuildingFocusedEvent"))
public typealias IndoorBuildingFocusedEvent = com.google.maps.android.IndoorBuildingFocusedEvent

@Deprecated("Moved to com.google.maps.android.IndoorLevelActivatedEvent", ReplaceWith("IndoorLevelActivatedEvent", "com.google.maps.android.IndoorLevelActivatedEvent"))
public typealias IndoorLevelActivatedEvent = com.google.maps.android.IndoorLevelActivatedEvent

@Suppress("DEPRECATION")
@Deprecated("Use cameraIdleEvents(), cameraMoveCanceledEvents(), cameraMoveEvents() or cameraMoveStartedEvents")
public fun GoogleMap.cameraEvents(): Flow<CameraEvent> = this.canonicalCameraEvents()

@Deprecated("Moved to com.google.maps.android.awaitAnimateCamera", ReplaceWith("awaitAnimateCamera(cameraUpdate, durationMs)", "com.google.maps.android.awaitAnimateCamera"))
public suspend inline fun GoogleMap.awaitAnimateCamera(cameraUpdate: CameraUpdate, durationMs: Int = 3000): Unit = this.canonicalAwaitAnimateCamera(cameraUpdate, durationMs)

@Deprecated("Moved to com.google.maps.android.awaitMapLoad", ReplaceWith("awaitMapLoad()", "com.google.maps.android.awaitMapLoad"))
public suspend inline fun GoogleMap.awaitMapLoad(): Unit = this.canonicalAwaitMapLoad()

@Deprecated("Moved to com.google.maps.android.cameraIdleEvents", ReplaceWith("cameraIdleEvents()", "com.google.maps.android.cameraIdleEvents"))
public fun GoogleMap.cameraIdleEvents(): Flow<Unit> = this.canonicalCameraIdleEvents()

@Deprecated("Moved to com.google.maps.android.cameraMoveCanceledEvents", ReplaceWith("cameraMoveCanceledEvents()", "com.google.maps.android.cameraMoveCanceledEvents"))
public fun GoogleMap.cameraMoveCanceledEvents(): Flow<Unit> = this.canonicalCameraMoveCanceledEvents()

@Deprecated("Moved to com.google.maps.android.cameraMoveEvents", ReplaceWith("cameraMoveEvents()", "com.google.maps.android.cameraMoveEvents"))
public fun GoogleMap.cameraMoveEvents(): Flow<Unit> = this.canonicalCameraMoveEvents()

@Deprecated("Moved to com.google.maps.android.awaitSnapshot", ReplaceWith("awaitSnapshot(bitmap)", "com.google.maps.android.awaitSnapshot"))
public suspend inline fun GoogleMap.awaitSnapshot(bitmap: Bitmap? = null): Bitmap? = this.canonicalAwaitSnapshot(bitmap)

@Deprecated("Moved to com.google.maps.android.cameraMoveStartedEvents", ReplaceWith("cameraMoveStartedEvents()", "com.google.maps.android.cameraMoveStartedEvents"))
public fun GoogleMap.cameraMoveStartedEvents(): Flow<Int> = this.canonicalCameraMoveStartedEvents()

@Deprecated("Moved to com.google.maps.android.circleClickEvents", ReplaceWith("circleClickEvents()", "com.google.maps.android.circleClickEvents"))
public fun GoogleMap.circleClickEvents(): Flow<Circle> = this.canonicalCircleClickEvents()

@Deprecated("Moved to com.google.maps.android.groundOverlayClicks", ReplaceWith("groundOverlayClicks()", "com.google.maps.android.groundOverlayClicks"))
public fun GoogleMap.groundOverlayClicks(): Flow<GroundOverlay> = this.canonicalGroundOverlayClicks()

@Suppress("DEPRECATION")
@Deprecated("Moved to com.google.maps.android.indoorStateChangeEvents", ReplaceWith("indoorStateChangeEvents()", "com.google.maps.android.indoorStateChangeEvents"))
public fun GoogleMap.indoorStateChangeEvents(): Flow<IndoorChangeEvent> = this.canonicalIndoorStateChangeEvents()

@Deprecated("Moved to com.google.maps.android.infoWindowClickEvents", ReplaceWith("infoWindowClickEvents()", "com.google.maps.android.infoWindowClickEvents"))
public fun GoogleMap.infoWindowClickEvents(): Flow<Marker> = this.canonicalInfoWindowClickEvents()

@Deprecated("Moved to com.google.maps.android.infoWindowCloseEvents", ReplaceWith("infoWindowCloseEvents()", "com.google.maps.android.infoWindowCloseEvents"))
public fun GoogleMap.infoWindowCloseEvents(): Flow<Marker> = this.canonicalInfoWindowCloseEvents()

@Deprecated("Moved to com.google.maps.android.infoWindowLongClickEvents", ReplaceWith("infoWindowLongClickEvents()", "com.google.maps.android.infoWindowLongClickEvents"))
public fun GoogleMap.infoWindowLongClickEvents(): Flow<Marker> = this.canonicalInfoWindowLongClickEvents()

@Deprecated("Moved to com.google.maps.android.mapClickEvents", ReplaceWith("mapClickEvents()", "com.google.maps.android.mapClickEvents"))
public fun GoogleMap.mapClickEvents(): Flow<LatLng> = this.canonicalMapClickEvents()

@Deprecated("Moved to com.google.maps.android.mapLongClickEvents", ReplaceWith("mapLongClickEvents()", "com.google.maps.android.mapLongClickEvents"))
public fun GoogleMap.mapLongClickEvents(): Flow<LatLng> = this.canonicalMapLongClickEvents()

@Deprecated("Moved to com.google.maps.android.markerClickEvents", ReplaceWith("markerClickEvents()", "com.google.maps.android.markerClickEvents"))
public fun GoogleMap.markerClickEvents(): Flow<Marker> = this.canonicalMarkerClickEvents()

@Suppress("DEPRECATION")
@Deprecated("Moved to com.google.maps.android.markerDragEvents", ReplaceWith("markerDragEvents()", "com.google.maps.android.markerDragEvents"))
public fun GoogleMap.markerDragEvents(): Flow<OnMarkerDragEvent> = this.canonicalMarkerDragEvents()

@Deprecated("Moved to com.google.maps.android.myLocationButtonClickEvents", ReplaceWith("myLocationButtonClickEvents()", "com.google.maps.android.myLocationButtonClickEvents"))
public fun GoogleMap.myLocationButtonClickEvents(): Flow<Unit> = this.canonicalMyLocationButtonClickEvents()

@Deprecated("Moved to com.google.maps.android.myLocationClickEvents", ReplaceWith("myLocationClickEvents()", "com.google.maps.android.myLocationClickEvents"))
public fun GoogleMap.myLocationClickEvents(): Flow<Location> = this.canonicalMyLocationClickEvents()

@Deprecated("Moved to com.google.maps.android.poiClickEvents", ReplaceWith("poiClickEvents()", "com.google.maps.android.poiClickEvents"))
public fun GoogleMap.poiClickEvents(): Flow<PointOfInterest> = this.canonicalPoiClickEvents()

@Deprecated("Moved to com.google.maps.android.polygonClickEvents", ReplaceWith("polygonClickEvents()", "com.google.maps.android.polygonClickEvents"))
public fun GoogleMap.polygonClickEvents(): Flow<Polygon> = this.canonicalPolygonClickEvents()

@Deprecated("Moved to com.google.maps.android.polylineClickEvents", ReplaceWith("polylineClickEvents()", "com.google.maps.android.polylineClickEvents"))
public fun GoogleMap.polylineClickEvents(): Flow<Polyline> = this.canonicalPolylineClickEvents()

@Deprecated("Moved to com.google.maps.android.buildGoogleMapOptions", ReplaceWith("buildGoogleMapOptions(optionsActions)", "com.google.maps.android.buildGoogleMapOptions"))
public inline fun buildGoogleMapOptions(optionsActions: GoogleMapOptions.() -> Unit): GoogleMapOptions = canonicalBuildGoogleMapOptions(optionsActions)

@Deprecated("Moved to com.google.maps.android.addCircle", ReplaceWith("addCircle(optionsActions)", "com.google.maps.android.addCircle"))
public inline fun GoogleMap.addCircle(optionsActions: CircleOptions.() -> Unit): Circle = this.canonicalAddCircle(optionsActions)

@Deprecated("Moved to com.google.maps.android.addGroundOverlay", ReplaceWith("addGroundOverlay(optionsActions)", "com.google.maps.android.addGroundOverlay"))
public inline fun GoogleMap.addGroundOverlay(optionsActions: GroundOverlayOptions.() -> Unit): GroundOverlay? = this.canonicalAddGroundOverlay(optionsActions)

@Deprecated("Moved to com.google.maps.android.addMarker", ReplaceWith("addMarker(optionsActions)", "com.google.maps.android.addMarker"))
public inline fun GoogleMap.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker? = this.canonicalAddMarker(optionsActions)

@Deprecated("Moved to com.google.maps.android.addPolygon", ReplaceWith("addPolygon(optionsActions)", "com.google.maps.android.addPolygon"))
public inline fun GoogleMap.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon = this.canonicalAddPolygon(optionsActions)

@Deprecated("Moved to com.google.maps.android.addPolyline", ReplaceWith("addPolyline(optionsActions)", "com.google.maps.android.addPolyline"))
public inline fun GoogleMap.addPolyline(optionsActions: PolylineOptions.() -> Unit): Polyline = this.canonicalAddPolyline(optionsActions)

@Deprecated("Moved to com.google.maps.android.addTileOverlay", ReplaceWith("addTileOverlay(optionsActions)", "com.google.maps.android.addTileOverlay"))
public inline fun GoogleMap.addTileOverlay(optionsActions: TileOverlayOptions.() -> Unit): TileOverlay? = this.canonicalAddTileOverlay(optionsActions)
