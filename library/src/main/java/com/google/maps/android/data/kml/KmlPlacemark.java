/*
 * Copyright 2020 Google Inc.
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
package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;

import java.util.Map;

/**
 * Represents a placemark which is either a {@link com.google.maps.android.data.kml.KmlPoint},
 * {@link
 * com.google.maps.android.data.kml.KmlLineString}, {@link com.google.maps.android.data.kml.KmlPolygon} or a
 * {@link com.google.maps.android.data.kml.KmlMultiGeometry}. Stores the properties and styles of the
 * place.
 */
public class KmlPlacemark extends Feature {

    private final String mStyle;

    private final KmlStyle mInlineStyle;

    /**
     * Creates a new KmlPlacemark object
     *
     * @param geometry   geometry object to store
     * @param style      style id to store
     * @param properties properties hashmap to store
     */
    public KmlPlacemark(Geometry geometry, String style, KmlStyle inlineStyle,
                        Map<String, String> properties) {
        super(geometry, style, properties);
        mStyle = style;
        mInlineStyle = inlineStyle;
    }

    /**
     * Gets the style id associated with the basic_placemark
     *
     * @return style id
     */
    public String getStyleId() {
        return super.getId();
    }

    /**
     * Gets the inline style that was found
     *
     * @return InlineStyle or null if not found
     */
    public KmlStyle getInlineStyle() {
        return mInlineStyle;
    }

    /**
     * Gets a PolygonOption
     *
     * @return new PolygonOptions
     */
    public PolygonOptions getPolygonOptions() {
        if (mInlineStyle == null) {
            return null;
        }
        return mInlineStyle.getPolygonOptions();
    }

    /**
     * Gets a MarkerOption
     *
     * @return A new MarkerOption
     */
    public MarkerOptions getMarkerOptions() {
        if (mInlineStyle == null) {
            return null;
        }
        return mInlineStyle.getMarkerOptions();
    }

    /**
     * Gets a PolylineOption
     *
     * @return new PolylineOptions
     */
    public PolylineOptions getPolylineOptions() {
        if (mInlineStyle == null) {
            return null;
        }
        return mInlineStyle.getPolylineOptions();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Placemark").append("{");
        sb.append("\n style id=").append(mStyle);
        sb.append(",\n inline style=").append(mInlineStyle);
        sb.append("\n}\n");
        return sb.toString();
    }
}
