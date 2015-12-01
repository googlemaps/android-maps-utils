/*
 * Copyright 2013 Google Inc.
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

package com.google.maps.android;

import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each collection's individually managed
 * listeners.
 *
 * <p/> All marker operations (adds and removes) should occur via its collection class. That is, don't add a marker via a collection, then
 * remove it via Marker.remove()
 */
public class MarkerManager implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter {

    private final GoogleMap mMap;
    private final Map<String, MarkerCollection> mNamedCollections;
    private final Map<String, MarkerItemCollection> mNamedItemCollections;
    private final Map<Marker, MarkerCollection> mAllMarkers;

    public MarkerManager(GoogleMap map) {
        mMap = map;
        mNamedCollections = new HashMap<>();
        mAllMarkers = new HashMap<>();
        mNamedItemCollections = new HashMap<>();
    }

    public MarkerCollection newCollection() {
        return new MarkerCollection(this);
    }

    public <T> MarkerItemCollection<T> newItemCollection() {
        return new MarkerItemCollection<>(this);
    }

    /**
     * Create a new named collection, which can later be looked up by {@link #getCollection(String)}
     *
     * @param id a unique id for this collection.
     */
    public MarkerCollection newCollection(String id) {
        if (mNamedCollections.get(id) != null) {
            throw new IllegalArgumentException("collection id is not unique: " + id);
        }
        MarkerCollection collection = new MarkerCollection(this);
        mNamedCollections.put(id, collection);
        return collection;
    }

    public <T> MarkerItemCollection<T> newItemCollection(String id) {
        if (mNamedItemCollections.get(id) != null) {
            throw new IllegalArgumentException("collection id is not unique: " + id);
        }
        MarkerItemCollection<T> collection = new MarkerItemCollection<>(this);
        mNamedItemCollections.put(id, collection);
        return collection;
    }

    /**
     * Gets a named collection that was created by {@link #newCollection(String)}
     *
     * @param id the unique id for this collection.
     */
    public MarkerCollection getCollection(String id) {
        return mNamedCollections.get(id);
    }

    public <T> MarkerItemCollection<T> getItemCollections(String id) {
        //noinspection unchecked
        return (MarkerItemCollection<T>) mNamedItemCollections.get(id);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoWindow(marker);
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoContents(marker);
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mInfoWindowClickListener != null) {
            collection.mInfoWindowClickListener.onInfoWindowClick(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerClickListener != null) {
            return collection.mMarkerClickListener.onMarkerClick(marker);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragEnd(marker);
        }
    }

    /**
     * Removes a marker from its collection.
     *
     * @param marker the marker to remove.
     * @return true if the marker was removed.
     */
    public boolean remove(Marker marker) {
        MarkerCollection collection = mAllMarkers.get(marker);
        return collection != null && collection.remove(marker);
    }

    public static class MarkerCollection {

        private final MarkerManager mMarkerManager;
        private final MarkerCollectionObservable mObservable;
        private final Set<Marker> mMarkers;

        private GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener;
        private GoogleMap.OnMarkerClickListener mMarkerClickListener;
        private GoogleMap.OnMarkerDragListener mMarkerDragListener;
        private GoogleMap.InfoWindowAdapter mInfoWindowAdapter;

        protected MarkerCollection(MarkerManager markerManager) {
            mMarkerManager = markerManager;
            mObservable = new MarkerCollectionObservable(this);
            mMarkers = new HashSet<>();
        }

        /**
         * Register an observer to be notified for changes to this collection.
         *
         * @param observer the observer to register; must not be <code>null</code> nor already registered
         */
        public void registerObserver(MarkerCollectionObserver observer) {
            mObservable.registerObserver(observer);
        }

        /**
         * Unregister a previously registered observer.
         *
         * @param observer the observer to unregister; must not be <code>null</code> and already registered
         */
        public void unregisterObserver(MarkerCollectionObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        @Nullable
        public Marker addMarker(MarkerOptions opts) {
            Marker marker = mMarkerManager.mMap.addMarker(opts);
            mMarkers.add(marker);
            mMarkerManager.mAllMarkers.put(marker, this);

            onAddedMarker(marker);

            mObservable.notifyMarkerAdded(marker);
            return marker;
        }

        protected void onAddedMarker(Marker marker) {
        }

        public final boolean remove(Marker marker) {
            if (mMarkers.contains(marker)) {
                mMarkers.remove(marker);
                mMarkerManager.mAllMarkers.remove(marker);

                onRemoveMarker(marker);
                marker.remove();

                mObservable.notifyMarkerRemoved(marker);
                return true;
            }

            return false;
        }

        protected void onRemoveMarker(Marker marker) {
        }

        public void clear() {
            if (!mMarkers.isEmpty()) {
                for (Marker marker : mMarkers) {
                    marker.remove();
                    mMarkerManager.mAllMarkers.remove(marker);
                }
                mMarkers.clear();

                onClear();

                mObservable.notifyMarkerCollectionCleared();
            }
        }

        protected void onClear() {
        }

        public java.util.Collection<Marker> getMarkers() {
            return Collections.unmodifiableCollection(mMarkers);
        }

        public void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener infoWindowClickListener) {
            mInfoWindowClickListener = infoWindowClickListener;
        }

        public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener) {
            mMarkerClickListener = markerClickListener;
        }

        public void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener markerDragListener) {
            mMarkerDragListener = markerDragListener;
        }

        public void setOnInfoWindowAdapter(GoogleMap.InfoWindowAdapter infoWindowAdapter) {
            mInfoWindowAdapter = infoWindowAdapter;
        }
    }

    public static class MarkerItemCollection<T> extends MarkerCollection {

        private final ItemCollectionObservable<T> mObservable;
        private final Map<Marker, T> mMarkerToItem;
        private final Map<T, Marker> mItemToMarker;

        private List<T> mItems;

        public MarkerItemCollection(MarkerManager markerManager) {
            super(markerManager);

            mObservable = new ItemCollectionObservable<>(this);
            mMarkerToItem = new HashMap<>();
            mItemToMarker = new HashMap<>();
        }

        /**
         * Register an observer to be notified for changes to this collection.
         *
         * @param observer the observer to register; must not be <code>null</code> nor already registered
         */
        public void registerObserver(MarkerItemCollectionObserver<T> observer) {
            mObservable.registerObserver(observer);
        }

        /**
         * Unregister a previously registered observer.
         *
         * @param observer the observer to unregister; must not be <code>null</code> and already registered
         */
        public void unregisterObserver(MarkerItemCollectionObserver<T> observer) {
            mObservable.unregisterObserver(observer);
        }

        @Nullable
        @Override
        public final Marker addMarker(MarkerOptions opts) {
            return null;
        }

        public boolean canAddMarker(T item) {
            return mObservable.canAddMarker(item);
        }

        @Nullable
        public Marker addMarker(MarkerOptions opts, T item) {
            if (mItems != null && mItems.contains(item)) {
                Marker marker = getMarker(item);

                if (marker == null) {
                    marker = super.addMarker(opts);

                    mMarkerToItem.put(marker, item);
                    mItemToMarker.put(item, marker);
                } else {
                    marker.setIcon(opts.getIcon());
                    marker.setAlpha(opts.getAlpha());
                    marker.setAnchor(opts.getAnchorU(), opts.getAnchorV());
                    marker.setDraggable(opts.isDraggable());
                    marker.setFlat(opts.isFlat());
                    marker.setPosition(opts.getPosition());
                    marker.setInfoWindowAnchor(opts.getInfoWindowAnchorU(), opts.getInfoWindowAnchorV());
                    marker.setRotation(opts.getRotation());
                    marker.setSnippet(opts.getSnippet());
                    marker.setTitle(opts.getTitle());
                    marker.setVisible(opts.isVisible());
                }

                return marker;
            }

            return null;
        }

        @Override
        protected void onRemoveMarker(Marker marker) {
            super.onRemoveMarker(marker);

            T item = mMarkerToItem.remove(marker);
            mItemToMarker.remove(item);
        }

        @Override
        protected void onClear() {
            super.onClear();

            mMarkerToItem.clear();
            mItemToMarker.clear();
        }

        public void setItems(List<T> items) {
            if (mItems != items) {
                mItems = items;

                if (mItems != null) {
                    mObservable.notifyCollectionChanged();
                } else {
                    mObservable.notifyCollectionInvalidated();
                }
            }
        }

        public int getItemSize() {
            return mItems != null ? mItems.size() : 0;
        }

        public void addItem(T item) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.add(item);
            mObservable.notifyCollectionItemAdded(item);
        }

        public void removeItem(T item) {
            if (mItems != null && mItems.remove(item)) {
                mObservable.notifyCollectionItemRemoved(item);
            }
        }

        public T getItem(int index) {
            return mItems != null ? mItems.get(index) : null;
        }

        public T getItem(Marker marker) {
            return mMarkerToItem.get(marker);
        }

        public Marker getMarker(T item) {
            return mItemToMarker.get(item);
        }

        public void notifyItemChanged(T item) {
            if (mItems != null && mItems.contains(item)) {
                mObservable.notifyCollectionItemChanged(item);
            }
        }

        @Nullable
        public List<T> getItems() {
            return mItems != null ? Collections.unmodifiableList(mItems) : null;
        }
    }

    /**
     * An observer that will be notified for changes to a {@link MarkerCollection}.
     */
    public static abstract class MarkerCollectionObserver {

        /**
         * A marker was added to the collection (and also to the map).
         *
         * @param collection the collection that changed
         * @param marker     the newly added marker
         */
        public void onMarkerAdded(MarkerCollection collection, Marker marker) {
        }

        /**
         * A marker was removed from the collection (and also from the map).
         *
         * @param collection the collection that changed
         * @param marker     the marker that got removed
         */
        public void onMarkerRemoved(MarkerCollection collection, Marker marker) {
        }

        /**
         * All markers were removed from the collection (and also from the map).
         *
         * @param collection the collection that changed
         */
        public void onMarkerCollectionCleared(MarkerCollection collection) {
        }
    }

    private static class MarkerCollectionObservable extends android.database.Observable<MarkerCollectionObserver> {

        private final MarkerCollection mCollection;

        public MarkerCollectionObservable(MarkerCollection collection) {
            mCollection = collection;
        }

        public void notifyMarkerAdded(Marker marker) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onMarkerAdded(mCollection, marker);
                }
            }
        }

        public void notifyMarkerRemoved(Marker marker) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onMarkerRemoved(mCollection, marker);
                }
            }
        }

        public void notifyMarkerCollectionCleared() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onMarkerCollectionCleared(mCollection);
                }
            }
        }
    }

    public static abstract class MarkerItemCollectionObserver<T> {

        public void onCollectionChanged(MarkerItemCollection<T> collection) {
        }

        public void onCollectionInvalidated(MarkerItemCollection<T> collection) {
        }

        public void onItemAdded(MarkerItemCollection<T> collection, T item) {
        }

        public void onItemRemoved(MarkerItemCollection<T> collection, T item){
        }

        public void onItemChanged(MarkerItemCollection<T> collection, T item) {
        }

        public boolean onCanAddMarker(T item) {
            return true;
        }
    }

    private static class ItemCollectionObservable<T> extends android.database.Observable<MarkerItemCollectionObserver<T>> {

        private final MarkerItemCollection<T> mCollection;

        public ItemCollectionObservable(MarkerItemCollection<T> collection) {
            mCollection = collection;
        }

        public void notifyCollectionChanged() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onCollectionChanged(mCollection);
                }
            }
        }

        public void notifyCollectionInvalidated() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onCollectionInvalidated(mCollection);
                }
            }
        }

        public void notifyCollectionItemAdded(T item) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onItemAdded(mCollection, item);
                }
            }
        }

        public void notifyCollectionItemRemoved(T item) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onItemRemoved(mCollection, item);
                }
            }
        }

        public void notifyCollectionItemChanged(T item) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onItemChanged(mCollection, item);
                }
            }
        }

        public boolean canAddMarker(T item) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    ;
                    if (!mObservers.get(i).onCanAddMarker(item)) {
                        return false;
                    }
                }
            }

            return true;
        }

    }

}
