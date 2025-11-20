package com.google.maps.android.data.renderer

class Layer {
    private val mapObjects = mutableListOf<MapObject>()

    fun addMapObject(mapObject: MapObject) {
        mapObjects.add(mapObject)
    }

    fun removeMapObject(mapObject: MapObject) {
        mapObjects.remove(mapObject)
    }

    fun getMapObjects(): List<MapObject> {
        return mapObjects.toList()
    }

    internal fun addLayerToMap() {
        for (mapObject in mapObjects) {
            mapObject.render()
        }
    }

    internal fun removeLayerFromMap() {
        for (mapObject in mapObjects) {
            mapObject.remove()
        }
    }
}
