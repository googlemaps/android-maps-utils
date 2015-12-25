package com.google.maps.android.heatmaps;

import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Bounds;

import java.util.Collection;

/*
 * HeatmapTileProvider cannot handle more than 1000 points properly so use alternative values
 *
 * https://github.com/googlemaps/android-maps-utils/issues/71
 * http://stackoverflow.com/questions/25773917/google-map-api-v2-heatmap-not-showing-all-locations
 */
public class BigHeatmapTileProvider extends HeatmapTileProvider {

    /**
     * Default points threshold (minimum number of points or coordinates required before the
     * inherited functionality is deviated from)
     */
    public static final int DEFAULT_POINTS_THRESHOLD = 1000;

    /**
     * Default max value to be returned by {@link #getMaxValue(Collection, Bounds, int, int)} when
     * the points threshold has been met
     */
    public static final int DEFAULT_MAX_VALUE = 10;

    private static int sPointsThreshold = DEFAULT_POINTS_THRESHOLD;
    private static int sMaxValue = DEFAULT_MAX_VALUE;

    private BigHeatmapTileProvider(Builder builder) {
        super(builder);
    }

    @Override
    /**
     * @return a fixed max value if the points threshold is met otherwise delegates the task to
     * {@link HeatmapTileProvider#getMaxValue(Collection, Bounds, int, int)}
     */
    double getMaxValue(Collection<WeightedLatLng> points, Bounds bounds, int radius, int screenDim) {
        return points.size() > sPointsThreshold ?
                sMaxValue :
                super.getMaxValue(points, bounds, radius, screenDim);
    }

    public static class Builder extends HeatmapTileProvider.Builder {

        public Builder() {
        }

        /**
         * Setter for pointsThreshold in builder
         *
         * @param val minimum number of points or coordinates required before the inherited
         *            functionality is deviated from
         * @return updated builder object
         * @see #DEFAULT_POINTS_THRESHOLD
         */
        public Builder pointsThreshold(int val) {
            sPointsThreshold = val;
            return this;
        }

        /**
         * Setter for maxValue in builder
         *
         * @param val max value to be returned by {@link #getMaxValue(Collection, Bounds, int, int)}
         *            when the pointsThreshold is met
         * @return updated builder object
         * @see #pointsThreshold(int)
         * @see #DEFAULT_MAX_VALUE
         */
        public Builder maxValue(int val) {
            sMaxValue = val;
            return this;
        }

        /**
         * @see HeatmapTileProvider.Builder#build()
         */
        public TileProvider build() {
            super.build();
            return new BigHeatmapTileProvider(this);
        }
    }

}
