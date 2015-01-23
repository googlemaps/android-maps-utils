package com.google.maps.android.kml;

/**
 * Represents a KML Container object which can hold one or more KmlFeatures. Container supports
 * nested containers.
 */
public interface KmlContainerInterface {

    /**
     * Gets an iterable of KmlPlacemarks
     *
     * @return iterable of KmlPlacemarks
     */
    public Iterable<KmlPlacemark> getKmlPlacemarks();

    /**
     * Gets whether the container has any placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasKmlPlacemarks();

    /**
     * Gets the value of a property based on the given key
     *
     * @param propertyName property key to find
     * @return value of property found, null if key doesn't exist
     */
    public String getKmlProperty(String propertyName);

    /**
     * Gets whether the container has any properties
     *
     * @return true if there are properties, false otherwise
     */
    public boolean hasKmlProperties();

    /**
     * Gets whether the given key exists in the properties
     *
     * @param keyValue property key to find
     * @return true if key was found, false otherwise
     */
    public boolean hasKmlProperty(String keyValue);

    /**
     * Gets an iterable of the properties hashmap entries
     *
     * @return iterable of the properties hashmap entries
     */
    public Iterable getKmlProperties();

    /**
     * Gets whether the container has containers
     *
     * @return true if there are containers, false otherwise
     */
    public boolean hasNestedKmlContainers();

    /**
     * Gets an iterable of nested KmlContainers
     *
     * @return iterable of KmlContainers
     */
    public Iterable<KmlContainerInterface> getNestedKmlContainers();

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays();

}
