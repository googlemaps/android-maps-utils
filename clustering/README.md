# Clustering Module: High-Performance Marker Clustering

The `clustering` module provides algorithms and rendering infrastructure to cluster geospatial markers on the Google Maps Android SDK.

---

## Available Algorithms

| Algorithm | Best For | Typical Scale | Query Latency | Memory Architecture |
| :--- | :--- | :---: | :---: | :--- |
| **`SuperClusterAlgorithm`** | **Mega-scale datasets, smooth 60–120 FPS panning** | **10,000 – 1,000,000** | **< 0.1 ms – 1.0 ms** | **Contiguous primitive arrays (`FlatKdTree`), zero runtime GC pressure** |
| `NonHierarchicalViewBasedAlgorithm` | Medium datasets with viewport clipping | 1,000 – 20,000 | 4 ms – 20 ms | `PointQuadTree` with viewport bounds clipping |
| `NonHierarchicalDistanceBasedAlgorithm` | Small datasets, simple distance grouping | 100 – 2,000 | 50 ms – 600 ms | In-memory `PointQuadTree` traversed on every camera movement |
| `GridBasedAlgorithm` | Basic equal-area grid binning | 100 – 5,000 | 10 ms – 700 ms | Integer grid coordinate grouping |
| `CentroidNonHierarchicalDistanceBasedAlgorithm` | Centroid-recalculating clusters | 100 – 2,000 | 80 ms – 150 ms | Quadtree distance grouping with dynamic centroid repositioning |
| `ContinuousZoomEuclideanCentroidAlgorithm` | Fractional zoom interpolation | 100 – 2,000 | 80 ms – 100 ms | Euclidean coordinate cluster smoothing |

---

## Empirical Performance Benchmarks

Measured using [`ClusteringPerformanceComparisonTest.kt`](src/test/java/com/google/maps/android/clustering/algo/ClusteringPerformanceComparisonTest.kt) across uniform geographic distributions (averaged over 3 iterations per zoom level):

### 100,000 Points

| Algorithm | Ingestion (`addItems`) | Index Build | Zoom 4.0 | Zoom 8.0 | Zoom 12.0 | Zoom 16.0 | Total Query Latency | Memory Delta |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **`SuperClusterAlgorithm` (Viewport)** | **8.73 ms** | **786.21 ms** | **0.16 ms** | **0.04 ms** | **0.01 ms** | **0.01 ms** | **0.22 ms** | 77.9 MB |
| **`SuperClusterAlgorithm` (Unbounded)** | 17.05 ms | 806.95 ms | 0.14 ms | 3.95 ms | 4.05 ms | 3.72 ms | **11.86 ms** | 82.3 MB |
| `NonHierarchicalViewBasedAlgorithm` | 57.32 ms | 0.00 ms | 17.25 ms | 0.09 ms | 0.00 ms | 0.00 ms | 17.35 ms | 33.0 MB |
| `NonHierarchicalDistanceBasedAlgorithm` | 68.20 ms | 0.00 ms | 124.12 ms | 175.25 ms | 165.09 ms | 152.71 ms | 617.17 ms | 30.5 MB |
| `GridBasedAlgorithm` | 8.52 ms | 0.00 ms | 28.66 ms | 496.61 ms | 757.69 ms | 764.31 ms | 2047.27 ms | 7.0 MB |

### 50,000 Points

| Algorithm | Ingestion (`addItems`) | Index Build | Zoom 4.0 | Zoom 8.0 | Zoom 12.0 | Zoom 16.0 | Total Query Latency | Memory Delta |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **`SuperClusterAlgorithm` (Viewport)** | **5.51 ms** | **383.36 ms** | **0.18 ms** | **0.03 ms** | **0.01 ms** | **0.01 ms** | **0.23 ms** | 35.8 MB |
| **`SuperClusterAlgorithm` (Unbounded)** | 8.02 ms | 395.77 ms | 0.26 ms | 3.02 ms | 3.49 ms | 3.52 ms | **10.28 ms** | 54.7 MB |
| `NonHierarchicalViewBasedAlgorithm` | 28.74 ms | 0.00 ms | 8.62 ms | 0.05 ms | 0.00 ms | 0.00 ms | 8.68 ms | 15.5 MB |
| `NonHierarchicalDistanceBasedAlgorithm` | 47.34 ms | 0.00 ms | 96.62 ms | 88.36 ms | 79.15 ms | 72.56 ms | 336.68 ms | 13.3 MB |
| `GridBasedAlgorithm` | 3.92 ms | 0.00 ms | 19.57 ms | 171.33 ms | 202.95 ms | 204.30 ms | 598.14 ms | 3.5 MB |

### 10,000 Points

| Algorithm | Ingestion (`addItems`) | Index Build | Zoom 4.0 | Zoom 8.0 | Zoom 12.0 | Zoom 16.0 | Total Query Latency | Memory Delta |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **`SuperClusterAlgorithm` (Viewport)** | **0.81 ms** | **67.24 ms** | **1.02 ms** | **0.03 ms** | **0.02 ms** | **0.01 ms** | **1.07 ms** | 15.0 MB |
| **`SuperClusterAlgorithm` (Unbounded)** | 1.44 ms | 128.41 ms | 4.81 ms | 0.32 ms | 0.33 ms | 0.41 ms | **5.88 ms** | 15.9 MB |
| `NonHierarchicalViewBasedAlgorithm` | 5.09 ms | 0.00 ms | 4.17 ms | 0.04 ms | 0.00 ms | 0.00 ms | 4.22 ms | 3.0 MB |
| `NonHierarchicalDistanceBasedAlgorithm` | 11.44 ms | 0.00 ms | 22.23 ms | 13.56 ms | 10.61 ms | 9.30 ms | 55.70 ms | 3.0 MB |
| `GridBasedAlgorithm` | 0.51 ms | 0.00 ms | 10.30 ms | 17.40 ms | 14.31 ms | 12.54 ms | 54.55 ms | 0.5 MB |

---

## SuperCluster Architecture

1. **Primitive Storage (`FlatKdTree`)**:
   Coordinates are packed in a contiguous `DoubleArray` (`[x0, y0, x1, y1, ...]`) and identities in an `IntArray`. Quickselect median selection partitions the tree in-place on alternating axes (X and Y), eliminating millions of node allocations on the Java heap.
2. **Bottom-Up Hierarchical Zoom Pyramid**:
   Raw points enter at `maxZoom + 1`. The algorithm aggregates downward: clusters from level $z + 1$ form the candidate inputs for level $z$. As the user zooms out, point counts decrease exponentially.
3. **Sub-Millisecond Viewport Queries**:
   During camera pans and zooms, `getClusters` performs **zero clustering calculation**. It executes a range query against the pre-computed static tree in $O(\log N + K)$ time (< 0.1 ms).
4. **Coincident Point Preservation**:
   Points sharing identical geographic coordinates are clustered at the base level and preserved across all zoom levels, preventing overlapping, unclickable duplicate markers.

---

## Usage Guide

```kotlin
// 1. Initialize ClusterManager
val clusterManager = ClusterManager<MyItem>(context, googleMap)

// 2. Configure SuperClusterAlgorithm with screen dimensions (in dp)
val (widthDp, heightDp) = getScreenDimensionsDp()
val superCluster = SuperClusterAlgorithm<MyItem>(
    minZoom = 0,
    maxZoom = 16,
    radius = 64.0,
    extent = 512.0,
    viewWidth = widthDp,
    viewHeight = heightDp,
)
clusterManager.setAlgorithm(superCluster)

// 3. Connect camera idle and marker click listeners
googleMap.setOnCameraIdleListener(clusterManager)
googleMap.setOnMarkerClickListener(clusterManager)

// 4. Ingest dataset and cluster
clusterManager.addItems(largeDataset) // e.g. 100,000 items
clusterManager.cluster()
```

### Location Updating

`SuperClusterAlgorithm` supports dynamic location updates via `updateItem(item: T)`:

```kotlin
// Update item position (item should have stable id/identity)
item.position = newLatLng
clusterManager.updateItem(item)

// Triggers background rebuild of the spatial pyramid
clusterManager.cluster()
```
