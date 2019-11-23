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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

/**
 * Keeps track of collections of circles on the map. Delegates all Circle-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All circle operations (adds and removes) should occur via its collection class. That is, don't
 * add a circle via a collection, then remove it via Circle.remove()
 */
public class CircleManager extends MapObjectManager<Circle, CircleManager.Collection> implements GoogleMap.OnCircleClickListener {

    public CircleManager(@NonNull GoogleMap map) {
        super(map);
    }

    @Override
    void setListenersOnUiThread() {
        if (mMap != null) {
            mMap.setOnCircleClickListener(this);
        }
    }

    @Override
    public Collection newCollection() {
        return new Collection();
    }

    @Override
    protected void removeObjectFromMap(Circle object) {
        object.remove();
    }

    @Override
    public void onCircleClick(Circle circle) {
        Collection collection = mAllObjects.get(circle);
        if (collection != null && collection.mCircleClickListener != null) {
            collection.mCircleClickListener.onCircleClick(circle);
        }
    }

    public class Collection extends MapObjectManager.Collection {
        private GoogleMap.OnCircleClickListener mCircleClickListener;

        public Collection() {
        }

        public Circle addCircle(CircleOptions opts) {
            Circle circle = mMap.addCircle(opts);
            super.add(circle);
            return circle;
        }

        public void addAll(java.util.Collection<CircleOptions> opts) {
            for (CircleOptions opt : opts) {
                addCircle(opt);
            }
        }

        public void addAll(java.util.Collection<CircleOptions> opts, boolean defaultVisible) {
            for (CircleOptions opt : opts) {
                addCircle(opt).setVisible(defaultVisible);
            }
        }

        public void showAll() {
            for (Circle circle : getCircles()) {
                circle.setVisible(true);
            }
        }

        public void hideAll() {
            for (Circle circle : getCircles()) {
                circle.setVisible(false);
            }
        }

        public boolean remove(Circle circle) {
            return super.remove(circle);
        }

        public java.util.Collection<Circle> getCircles() {
            return getObjects();
        }

        public void setOnCircleClickListener(GoogleMap.OnCircleClickListener circleClickListener) {
            mCircleClickListener = circleClickListener;
        }
    }
}
