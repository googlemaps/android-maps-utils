/*
 * Copyright 2017 Google Inc.
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

package com.google.maps.android.collections;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;

/**
 * Keeps track of collections of ground overlays on the map. Delegates all GroundOverlay-related events to each
 * collection's individually managed listeners.
 * <p>
 * All ground overlay operations (adds and removes) should occur via its collection class. That is, don't
 * add a ground overlay via a collection, then remove it via GroundOverlay.remove()
 */
public class GroundOverlayManager extends MapObjectManager<GroundOverlay, GroundOverlayManager.Collection> implements GoogleMap.OnGroundOverlayClickListener {

    public GroundOverlayManager(@NonNull GoogleMap map) {
        super(map);
    }

    @Override
    void setListenersOnUiThread() {
        if (mMap != null) {
            mMap.setOnGroundOverlayClickListener(this);
        }
    }

    @Override
    public Collection newCollection() {
        return new Collection();
    }

    @Override
    protected void removeObjectFromMap(GroundOverlay object) {
        object.remove();
    }

    @Override
    public void onGroundOverlayClick(@NonNull GroundOverlay groundOverlay) {
        Collection collection = mAllObjects.get(groundOverlay);
        if (collection != null && collection.mGroundOverlayClickListener != null) {
            collection.mGroundOverlayClickListener.onGroundOverlayClick(groundOverlay);
        }
    }

    public class Collection extends MapObjectManager.Collection {
        private GoogleMap.OnGroundOverlayClickListener mGroundOverlayClickListener;

        public Collection() {
        }

        public GroundOverlay addGroundOverlay(GroundOverlayOptions opts) {
            GroundOverlay groundOverlay = mMap.addGroundOverlay(opts);
            super.add(groundOverlay);
            return groundOverlay;
        }

        public void addAll(java.util.Collection<GroundOverlayOptions> opts) {
            for (GroundOverlayOptions opt : opts) {
                addGroundOverlay(opt);
            }
        }

        public void addAll(java.util.Collection<GroundOverlayOptions> opts, boolean defaultVisible) {
            for (GroundOverlayOptions opt : opts) {
                addGroundOverlay(opt).setVisible(defaultVisible);
            }
        }

        public void showAll() {
            for (GroundOverlay groundOverlay : getGroundOverlays()) {
                groundOverlay.setVisible(true);
            }
        }

        public void hideAll() {
            for (GroundOverlay groundOverlay : getGroundOverlays()) {
                groundOverlay.setVisible(false);
            }
        }

        public boolean remove(GroundOverlay groundOverlay) {
            return super.remove(groundOverlay);
        }

        public java.util.Collection<GroundOverlay> getGroundOverlays() {
            return getObjects();
        }

        public void setOnGroundOverlayClickListener(GoogleMap.OnGroundOverlayClickListener groundOverlayClickListener) {
            mGroundOverlayClickListener = groundOverlayClickListener;
        }
    }
}
