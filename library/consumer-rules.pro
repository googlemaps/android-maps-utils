# play-services-location is an optional compileOnly dependency for FusedLocationProviderClient extensions.
# Suppress R8 missing-class warnings for consumers that do not include play-services-location.
-dontwarn com.google.android.gms.location.**

# Navigation SDK consumers exclude play-services-maps, which transitively provides GooglePlayServicesNotAvailableException.
-dontwarn com.google.android.gms.common.GooglePlayServicesNotAvailableException
