/*
 * Copyright 2023 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Style;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * A class that allows for GeoJsonLineString objects to be styled and for these styles to be
 * translated into a PolylineOptions object. {@see
 * <a href="https://developers.google.com/android/reference/com/google/android/gms/maps/model/PolylineOptions">
 * PolylineOptions docs</a> for more details about the options.}
 */
public class GeoJsonLineStringStyle extends Style implements GeoJsonStyle {

    private final static String[] GEOMETRY_TYPE = {"LineString", "MultiLineString",
            "GeometryCollection"};

    /**
     * Creates a new LineStringStyle object
     */
    public GeoJsonLineStringStyle() {
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.clickable(true);
    }

    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    public int getColor() {
        return mPolylineOptions.getColor();
    }

    public void setColor(int color) {
        mPolylineOptions.color(color);
        styleChanged();
    }

    public boolean isClickable() {
        return mPolylineOptions.isClickable();
    }

    public void setClickable(boolean clickable) {
        mPolylineOptions.clickable(clickable);
        styleChanged();
    }

    public boolean isGeodesic() {
        return mPolylineOptions.isGeodesic();
    }

    public void setGeodesic(boolean geodesic) {
        mPolylineOptions.geodesic(geodesic);
        styleChanged();
    }

    public float getWidth() {
        return mPolylineOptions.getWidth();
    }

    public void setWidth(float width) {
        setLineStringWidth(width);
        styleChanged();
    }

    public float getZIndex() {
        return mPolylineOptions.getZIndex();
    }

    public void setZIndex(float zIndex) {
        mPolylineOptions.zIndex(zIndex);
        styleChanged();
    }

    @Override
    public boolean isVisible() {
        return mPolylineOptions.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        mPolylineOptions.visible(visible);
        styleChanged();
    }

    private void styleChanged() {
        setChanged();
        notifyObservers();
    }

    public PolylineOptions toPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(mPolylineOptions.getColor());
        polylineOptions.clickable(mPolylineOptions.isClickable());
        polylineOptions.geodesic(mPolylineOptions.isGeodesic());
        polylineOptions.visible(mPolylineOptions.isVisible());
        polylineOptions.width(mPolylineOptions.getWidth());
        polylineOptions.zIndex(mPolylineOptions.getZIndex());
        polylineOptions.pattern(getPattern());
        polylineOptions.startCap(getStartCap());
        polylineOptions.endCap(getEndCap());
        return polylineOptions;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LineStringStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
        sb.append(",\n color=").append(getColor());
        sb.append(",\n clickable=").append(isClickable());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n width=").append(getWidth());
        sb.append(",\n z index=").append(getZIndex());
        sb.append(",\n pattern=").append(getPattern());
        sb.append(",\n startCap=").append(getStartCap());
        sb.append(",\n endCap=").append(getEndCap());
        sb.append("\n}\n");
        return sb.toString();
    }

    public List<PatternItem> getPattern() {
        return mPolylineOptions.getPattern();
    }

    public void setPattern(List<PatternItem> pattern) {
        mPolylineOptions.pattern(pattern);
        styleChanged();
    }

    public void setStartCap(@NonNull Cap cap) {
        mPolylineOptions.startCap(cap);
        styleChanged();
    }

    public void setEndCap(@NonNull Cap cap) {
        mPolylineOptions.endCap(cap);
        styleChanged();
    }

    public Cap getStartCap() {
        return mPolylineOptions.getStartCap();
    }

    public Cap getEndCap() {
        return mPolylineOptions.getEndCap();
    }
}
