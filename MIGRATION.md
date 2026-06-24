# Migration Guide: Upgrading from v4.x to v5.x (Modular Kotlin Rewrite)

This guide outlines the major architectural changes, breaking changes, and source migrations required when upgrading the **Google Maps Android Utility Library** from `v4.x` to the new Kotlin-rewritten `v5.x` release.

---

## 1. Multi-Module Architecture

To improve build times and allow modern modular applications to import only what they need, the library has been split from a monolithic structure into several submodules:

* **`android-maps-utils-core` (`:library`)**: The base utility code, including `PolyUtil`, `SphericalUtil`, `MathUtil`, and base collection managers (`MarkerManager`, `PolygonManager`, `PolylineManager`, etc.).
* **`android-maps-utils-data` (`:data`)**: KML and GeoJSON parsing and rendering.
* **`android-maps-utils-clustering` (`:clustering`)**: High-performance marker clustering and algorithm utilities.
* **`android-maps-utils-heatmaps` (`:heatmaps`)**: Heatmap overlay tile providers.
* **`android-maps-utils-ui` (`:ui`)**: Custom markers, bubble drawables, and rotation layouts.

### How to Import:
If your project uses all utilities, you can import the aggregator artifact directly:
```toml
# gradle/libs.versions.toml
[versions]
androidMapsUtils = "5.0.0-rc1" # x-release-please-version

[libraries]
android-maps-utils = { group = "com.google.maps.android", name = "android-maps-utils", version.ref = "androidMapsUtils" }
```
This aggregator dynamically and transitively pulls all individual submodules. Alternatively, you can choose to import only the specific submodules your app requires (e.g., `android-maps-utils-core` and `android-maps-utils-clustering`).

---

## 2. Kotlin Property Syntax Overrides (Breaking Changes for Kotlin Callers)

Many core classes have been rewritten in **idiomatic Kotlin**. Because Kotlin does not synthesize property getter/setter methods for Kotlin-defined functions, **Kotlin callers** must migrate from calling traditional Java-style getter/setter methods to accessing **properties** directly.

### A. `ClusterItem` Property Overrides
The `ClusterItem` interface is now written in Kotlin. Custom cluster item classes (such as data classes) can now directly override the properties in the constructor, eliminating verbose method overrides.

#### **Before (v4.x Workaround):**
```kotlin
data class MyItem(
    val latLng: LatLng, 
    val myTitle: String?, 
    val mySnippet: String?, 
    val myZIndex: Float?
) : ClusterItem {
    override fun getPosition() = latLng
    override fun getTitle() = myTitle
    override fun getSnippet() = mySnippet
    override fun getZIndex() = myZIndex
}
```

#### **After (v5.x Idiomatic Kotlin):**
```kotlin
data class MyItem(
    override val position: LatLng,
    override val title: String?,
    override val snippet: String?,
    override val zIndex: Float?
) : ClusterItem
```

### B. `Layer.features`
The abstract method `getFeatures()` in `Layer` has been migrated to a read-only property `features`.
- **Kotlin callers**: Access `layer.features` instead of `layer.getFeatures()`.
- **Java callers**: Seamlessly continue calling `layer.getFeatures()` (supported via Kotlin JVM bytecode generation).

### C. `GeoJsonFeature` Style Properties
Styles on `GeoJsonFeature` have been converted to first-class properties:
- **Kotlin callers**: Use `.pointStyle`, `.lineStringStyle`, and `.polygonStyle` directly instead of calling `.getPointStyle()` / `.setPointStyle(...)`.

#### **Example:**
```kotlin
// Before
feature.setLineStringStyle(GeoJsonLineStringStyle().apply {
    setColor(Color.RED)
})

// After
feature.lineStringStyle = GeoJsonLineStringStyle().apply {
    color = Color.RED
}
```

---

## 3. Lambda and SAM Conversions for Feature Clicks

The `Layer.OnFeatureClickListener` interface has been converted to a Kotlin **`fun interface`** (functional interface):
```kotlin
public fun interface OnFeatureClickListener {
    public fun onFeatureClick(feature: Feature)
}
```
This enables Kotlin developers to seamlessly pass clean click lambdas using standard SAM conversion:
```kotlin
geoJsonLayer.setOnFeatureClickListener { feature ->
    Toast.makeText(context, "Clicked feature: ${feature.id}", Toast.LENGTH_SHORT).show()
}
```
Unlike the previous version, this SAM conversion is fully supported for native Kotlin callers without requiring anonymous object syntax (`object : Layer.OnFeatureClickListener { ... }`).
