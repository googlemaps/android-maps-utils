# Renderer Design

## Core Components

*   **`Renderer`**: The central orchestrator. It will be responsible for managing `Layer` objects and drawing them onto the `GoogleMap`.
    *   Key methods: `addLayer(layer)`, `removeLayer(layer)`, `getLayers()`
    *   It will hold a reference to the `GoogleMap` instance.

*   **`Layer`**: A container for a collection of `MapObject`s. A layer represents a single, logical set of data, such as the contents of one KML file.

*   **`MapObject`**: A common interface or base class for all renderable items. We'll have concrete implementations for each primitive type:
    *   `MarkerObject`
    *   `PolylineObject`
    *   `PolygonObject`
    *   `CircleObject`
    *   These will encapsulate the Google Maps SDK's objects and associated styling.

*   **`DataParser`**: An interface for parsing data files. We'll start with two implementations:
    *   `KmlParser`
    *   `GeoJsonParser`
    *   The output of a parser will be a structured, in-memory representation of the data, specific to its format.

*   **`Mapper`**: A component that translates the format-specific object model from a `DataParser` into a `Layer` of generic `MapObject`s that the `Renderer` can work with.
    *   `KmlMapper`
    *   `GeoJsonMapper`

## High-Level Workflow

1.  **Instantiation**: A developer creates an instance of the `Renderer`, associating it with a `GoogleMap` object.
2.  **Parsing**: The developer provides a data source (e.g., an `InputStream` of a GeoJSON file) to the appropriate `DataParser` (e.g., `GeoJsonParser`). The parser returns a structured representation of the GeoJSON data.
3.  **Mapping**: The developer passes the parsed data object to the corresponding `Mapper` (e.g., `GeoJsonMapper`). The mapper converts the data into a `Layer` instance, which contains a list of `MapObject`s (like `PolygonObject`, `MarkerObject`, etc.).
4.  **Rendering**: The developer adds the `Layer` to the `Renderer` using `renderer.addLayer(layer)`.
5.  **Drawing**: The `Renderer` iterates through the `MapObject`s in the `Layer` and uses the Google Maps SDK to draw the corresponding shapes and markers on the map.
