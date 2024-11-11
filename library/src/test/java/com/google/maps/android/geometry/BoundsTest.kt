package com.google.maps.android.geometry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BoundsTest {

    @Test
    fun testConstructorAndProperties() {
        val bounds = Bounds(1.0, 3.0, 2.0, 4.0)
        assertThat(bounds.minX).isEqualTo(1.0)
        assertThat(bounds.minY).isEqualTo(2.0)
        assertThat(bounds.maxX).isEqualTo(3.0)
        assertThat(bounds.maxY).isEqualTo(4.0)
        assertThat(bounds.midX).isEqualTo(2.0)
        assertThat(bounds.midY).isEqualTo(3.0)
    }

    @Test
    fun testContainsPoint() {
        val bounds = Bounds(1.0, 3.0, 2.0, 4.0)
        assertThat(bounds.contains(2.0, 3.0)).isTrue()     // Inside
        assertThat(bounds.contains(0.5, 2.5)).isFalse()    // Outside
        assertThat(bounds.contains(1.0, 2.0)).isTrue()     // On boundary
        assertThat(bounds.contains(3.0, 4.0)).isTrue()     // On boundary
    }

    @Test
    fun testContainsBounds() {
        val bounds = Bounds(1.0, 5.0, 2.0, 6.0)
        val insideBounds = Bounds(2.0, 4.0, 3.0, 5.0)
        val outsideBounds = Bounds(0.0, 2.0, 1.0, 3.0)
        val overlappingBounds = Bounds(4.0, 6.0, 5.0, 7.0)

        assertThat(bounds.contains(insideBounds)).isTrue()
        assertThat(bounds.contains(outsideBounds)).isFalse()
        assertThat(bounds.contains(overlappingBounds)).isFalse()
    }

    @Test
    fun testIntersects() {
        val bounds = Bounds(1.0, 3.0, 2.0, 4.0)
        assertThat(bounds.intersects(0.0, 2.0, 1.0, 3.0)).isTrue()   // Overlap
        assertThat(bounds.intersects(4.0, 5.0, 5.0, 6.0)).isFalse()  // No overlap
        assertThat(bounds.intersects(2.0, 4.0, 3.0, 5.0)).isTrue()   // Containment is intersection
        assertThat(bounds.intersects(1.0, 3.0, 2.0, 4.0)).isTrue()   // Exact match
    }
}
