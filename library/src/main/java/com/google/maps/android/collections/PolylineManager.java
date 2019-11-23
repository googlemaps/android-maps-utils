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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Keeps track of collections of polylines on the map. Delegates all Polyline-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All polyline operations (adds and removes) should occur via its collection class. That is, don't
 * add a polyline via a collection, then remove it via Polyline.remove()
 */
public class PolylineManager extends MapObjectManager<Polyline, PolylineManager.Collection> implements GoogleMap.OnPolylineClickListener {

    public PolylineManager(@NonNull GoogleMap map) {
        super(map);
    }

    @Override
    void setListenersOnUiThread() {
        if (mMap != null) {
            mMap.setOnPolylineClickListener(this);
        }
    }

    @Override
    public Collection newCollection() {
        return new Collection();
    }

    @Override
    protected void removeObjectFromMap(Polyline object) {
        object.remove();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Collection collection = mAllObjects.get(polyline);
        if (collection != null && collection.mPolylineClickListener != null) {
            collection.mPolylineClickListener.onPolylineClick(polyline);
        }
    }

    public class Collection extends MapObjectManager.Collection {
        private GoogleMap.OnPolylineClickListener mPolylineClickListener;

        public Collection() {
        }

        public Polyline addPolyline(PolylineOptions opts) {
            Polyline polyline = mMap.addPolyline(opts);
            super.add(polyline);
            return polyline;
        }

        public void addAll(java.util.Collection<PolylineOptions> opts) {
            for (PolylineOptions opt : opts) {
                addPolyline(opt);
            }
        }

        public void addAll(java.util.Collection<PolylineOptions> opts, boolean defaultVisible) {
            for (PolylineOptions opt : opts) {
                addPolyline(opt).setVisible(defaultVisible);
            }
        }

        public void showAll() {
            for (Polyline polyline : getPolylines()) {
                polyline.setVisible(true);
            }
        }

        public void hideAll() {
            for (Polyline polyline : getPolylines()) {
                polyline.setVisible(false);
            }
        }

        public boolean remove(Polyline polyline) {
            return super.remove(polyline);
        }

        public java.util.Collection<Polyline> getPolylines() {
            return getObjects();
        }

        public void setOnPolylineClickListener(GoogleMap.OnPolylineClickListener polylineClickListener) {
            mPolylineClickListener = polylineClickListener;
        }
    }
}
