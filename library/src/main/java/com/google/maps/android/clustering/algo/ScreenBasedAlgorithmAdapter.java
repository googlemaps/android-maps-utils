/*
 * Copyright 2016 Google Inc.
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

package com.google.maps.android.clustering.algo;

import com.google.android.libraries.maps.model.CameraPosition;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Collection;
import java.util.Set;

public class ScreenBasedAlgorithmAdapter<T extends ClusterItem> extends AbstractAlgorithm<T> implements ScreenBasedAlgorithm<T> {

    private Algorithm<T> mAlgorithm;

    public ScreenBasedAlgorithmAdapter(Algorithm<T> algorithm) {
        mAlgorithm = algorithm;
    }

    @Override
    public boolean shouldReclusterOnMapMovement() {
        return false;
    }

    @Override
    public boolean addItem(T item) {
        return mAlgorithm.addItem(item);
    }

    @Override
    public boolean addItems(Collection<T> items) {
        return mAlgorithm.addItems(items);
    }

    @Override
    public void clearItems() {
        mAlgorithm.clearItems();
    }

    @Override
    public boolean removeItem(T item) {
        return mAlgorithm.removeItem(item);
    }

    @Override
    public boolean removeItems(Collection<T> items) {
        return mAlgorithm.removeItems(items);
    }

    @Override
    public boolean updateItem(T item) {
        return mAlgorithm.updateItem(item);
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(float zoom) {
        return mAlgorithm.getClusters(zoom);
    }

    @Override
    public Collection<T> getItems() {
        return mAlgorithm.getItems();
    }

    @Override
    public void setMaxDistanceBetweenClusteredItems(int maxDistance) {
        mAlgorithm.setMaxDistanceBetweenClusteredItems(maxDistance);
    }

    @Override
    public int getMaxDistanceBetweenClusteredItems() {
        return mAlgorithm.getMaxDistanceBetweenClusteredItems();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // stub
    }

}
