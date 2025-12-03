# Onion Module: Unified Geospatial Renderer

The `onion` module provides a unified, platform-agnostic architecture for parsing and rendering geospatial data (KML, GeoJSON, GPX) on Google Maps Android SDK.

## Architecture

The architecture follows a "Clean Architecture" approach, separating data parsing, internal modeling, and rendering:

1.  **Parsers (`com.google.maps.android.data.parser`)**:
    *   Responsible for parsing raw file formats (KML, GeoJSON, GPX) into intermediate objects.
    *   **Key Classes**: `KmlParser`, `GeoJsonParser`, `GpxParser`.

2.  **Mappers (`com.google.maps.android.data.renderer.mapper`)**:
    *   Transform parsed objects into a unified, platform-agnostic internal model (`DataScene`, `DataLayer`, `Feature`).
    *   **Key Classes**: `KmlMapper`, `GeoJsonMapper`, `GpxMapper`.

3.  **Internal Model (`com.google.maps.android.data.renderer.model`)**:
    *   A unified representation of geospatial data.
    *   **`DataScene`**: The top-level container for a map scene.
    *   **`DataLayer`**: A collection of features (e.g., "Peaks", "Ranges").
    *   **`Feature`**: A single entity with `Geometry`, `Style`, and `Properties`.
    *   **`Geometry`**: `Point`, `LineString`, `Polygon`, `MultiGeometry`, `GroundOverlay`.
    *   **`Style`**: `PointStyle`, `LineStyle`, `PolygonStyle`, `GroundOverlayStyle`.

4.  **Renderer (`com.google.maps.android.data.renderer.mapview`)**:
    *   Renders the internal model onto a `GoogleMap`.
    *   **`MapViewRenderer`**: The main class that handles adding/removing Markers, Polylines, Polygons, and GroundOverlays.
    *   Supports **Advanced Markers** via `useAdvancedMarkers` property.
    *   Handles asynchronous icon loading via `IconProvider`.

## Usage

### 1. Parsing and Mapping

```kotlin
// Load KML
val kml = KmlParser().parse(inputStream)
val kmlLayer = KmlMapper.toLayer(kml)

// Load GeoJSON
val geoJson = GeoJsonParser().parse(inputStream)
val geoJsonLayer = GeoJsonMapper.toLayer(geoJson)

// Load GPX
val gpx = GpxParser().parse(inputStream)
val gpxLayer = GpxMapper.toLayer(gpx)
```

### 2. Rendering

```kotlin
// Initialize Renderer
val renderer = MapViewRenderer(googleMap, UrlIconProvider(lifecycleScope))

// Add Layer
renderer.addLayer(kmlLayer)

// Remove Layer
renderer.removeLayer(kmlLayer)

// Enable Advanced Markers
renderer.useAdvancedMarkers = true
```

## Key Features

*   **Unified API**: Treat KML, GeoJSON, and GPX identically once parsed.
*   **Separation of Concerns**: Parsers don't know about Google Maps; Renderers don't know about file formats.
*   **Async Icon Loading**: Icons are loaded asynchronously using Coroutines, preventing UI jank.
*   **Ground Overlays**: Full support for KML GroundOverlays (rotated, styled).
*   **Advanced Markers**: Ready for the latest Google Maps features.
