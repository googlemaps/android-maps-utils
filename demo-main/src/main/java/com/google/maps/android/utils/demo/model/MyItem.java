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

package com.google.maps.android.utils.demo.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import androidx.annotation.NonNull;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private Float zIndex;

    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        mTitle = null;
        mSnippet = null;
    }

    public MyItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() { return mTitle; }

    @Override
    public String getSnippet() { return mSnippet; }

    @Override
    public Float getZIndex() {
        return zIndex;
    }

    /**
     * Set the title of the marker
     * @param title string to be set as title
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        mSnippet = snippet;
    }

    /**
     * Set the z-index of the marker
     * @param zIndex float to be set as z-index
     */
    public void setZIndex(Float zIndex) {
        this.zIndex = zIndex;
    }
}
