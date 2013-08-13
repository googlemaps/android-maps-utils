package com.google.maps.android.geometry;

/**
 * Represents an area in the cartesian plane.
 */
public class Bounds {
    public final double minX;
    public final double minY;

    public final double maxX;
    public final double maxY;

    public final double midX;
    public final double midY;

    public Bounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        midX = (minX + maxX) / 2;
        midY = (minY + minY) / 2;
    }

    public boolean contains(double x, double y) {
        return minX <= x && x < maxX && minY <= y && y < maxY;
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }

    public boolean intersects(double minX, double maxX, double minY, double maxY) {
        if (minX >= this.maxX) return false;
        if (this.minX >= maxX) return false;

        if (minY >= this.maxY) return false;
        if (this.minY >= maxY) return false;

        return true;
    }

    public boolean intersects(Bounds searchBounds) {
        return intersects(searchBounds.minX, searchBounds.maxX, searchBounds.minY, searchBounds.maxY);
    }
}