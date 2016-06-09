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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;
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
public class MarkerManager implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener,
        GoogleMap.InfoWindowAdapter {

    private final GoogleMap mMap;
    private final Map<String, MarkerCollection> mNamedCollections;
    private final Map<String, MarkerItemCollection> mNamedItemCollections;
    private final Map<Marker, AbsMarkerCollection> mAllMarkers;

    public MarkerManager(GoogleMap map) {
        mMap = map;
        mNamedCollections = new HashMap<>();
        mAllMarkers = new HashMap<>();
        mNamedItemCollections = new HashMap<>();
    }

    /**
     * Create a new marker collection.
     *
     * @return the newly created collection
     */
    @NonNull
    public MarkerCollection newCollection() {
        return new MarkerCollection(this);
    }

    /**
     * Create a new marker-item collection.
     *
     * @param <T> the item type
     * @return the newly created collection
     */
    @NonNull
    public <T> MarkerItemCollection<T> newItemCollection() {
        return new MarkerItemCollection<>(this);
    }

    /**
     * Create a new named collection, which can later be looked up by {@link #getCollection(String)}
     *
     * @param id a unique id for this collection.
     * @return the newly created collection
     */
    @NonNull
    public MarkerCollection newCollection(String id) {
        if (mNamedCollections.get(id) != null) {
            throw new IllegalArgumentException("collection id is not unique: " + id);
        }
        MarkerCollection collection = new MarkerCollection(this);
        mNamedCollections.put(id, collection);
        return collection;
    }

    /**
     * Create a new named collection, which can later be looked up by {@link #getItemCollection(String)}
     *
     * @param id  id a unique id for this collection.
     * @param <T> the item type
     * @return the newly created collection
     */
    @NonNull
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
     * @return the named collection or <code>null</code>
     */
    @Nullable
    public MarkerCollection getCollection(String id) {
        return mNamedCollections.get(id);
    }

    /**
     * Gets a named collection that was created by {@link #newItemCollection(String)}
     *
     * @param id the unique id for this collection.
     * @return the named collection or <code>null</code>
     */
    @Nullable
    public <T> MarkerItemCollection<T> getItemCollection(String id) {
        //noinspection unchecked
        return (MarkerItemCollection<T>) mNamedItemCollections.get(id);
    }

    @Override
    @Nullable
    public View getInfoWindow(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        return (collection != null) ? collection.getInfoWindow(marker) : null;
    }

    @Override
    @Nullable
    public View getInfoContents(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        return (collection != null) ? collection.getInfoContents(marker) : null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null) {
            collection.onInfoWindowClick(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        return collection != null && collection.onMarkerClick(marker);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null) {
            collection.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null) {
            collection.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        if (collection != null) {
            collection.onMarkerDragEnd(marker);
        }
    }

    /**
     * Removes a marker from its collection.
     *
     * @param marker the marker to remove.
     * @return true if the marker was removed.
     */
    public boolean remove(Marker marker) {
        AbsMarkerCollection collection = mAllMarkers.get(marker);
        return collection != null && collection.remove(marker);
    }

    public static class AbsMarkerCollection {

        protected final MarkerManager mMarkerManager;
        protected final MarkerCollectionObservable mMarkerCollectionObservable;
        protected final Set<Marker> mMarkers;

        private GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener;
        private GoogleMap.OnMarkerClickListener mMarkerClickListener;
        private GoogleMap.OnMarkerDragListener mMarkerDragListener;
        private GoogleMap.InfoWindowAdapter mInfoWindowAdapter;

        private AbsMarkerCollection(MarkerManager markerManager) {
            mMarkerManager = markerManager;
            mMarkerCollectionObservable = new MarkerCollectionObservable(this);
            mMarkers = new HashSet<>();
        }

        /**
         * Register an observer to be notified for changes to this collection.
         *
         * @param observer the observer to register; must not be <code>null</code> nor already registered
         */
        public void registerObserver(@NonNull MarkerCollectionObserver observer) {
            mMarkerCollectionObservable.registerObserver(observer);
        }

        /**
         * Unregister a previously registered observer.
         *
         * @param observer the observer to unregister; must not be <code>null</code> and already registered
         */
        public void unregisterObserver(@NonNull MarkerCollectionObserver observer) {
            mMarkerCollectionObservable.unregisterObserver(observer);
        }

        /**
         * A marker has been added. This is called right after the marker has been added to the map and before registered observers are
         * called.
         *
         * @param marker the new marker
         */
        protected void onAddedMarker(Marker marker) {
        }

        /**
         * Remove a marker.
         *
         * @param marker the marker to be removed from the map
         * @return {@code true} if the marker has been removed, {@code false} otherwise
         */
        public final boolean remove(Marker marker) {
            if (mMarkers.contains(marker)) {
                mMarkers.remove(marker);
                mMarkerManager.mAllMarkers.remove(marker);

                onRemoveMarker(marker);
                marker.remove();

                mMarkerCollectionObservable.notifyMarkerRemoved(marker);
                return true;
            }

            return false;
        }

        /**
         * A marker is about to be removed. This is called right be before the marker will be removed from the map. Registered observer will
         * be called after this.
         *
         * @param marker
         */
        protected void onRemoveMarker(Marker marker) {
        }

        /**
         * Clear all markers and remove all from the map.
         */
        public void clear() {
            if (!mMarkers.isEmpty()) {
                for (Marker marker : mMarkers) {
                    marker.remove();
                    mMarkerManager.mAllMarkers.remove(marker);
                }
                mMarkers.clear();

                onClear();

                mMarkerCollectionObservable.notifyMarkerCollectionCleared();
            }
        }

        /**
         * All markers have been cleared and removed from the map. This is called after everything has been cleared. Registered observer
         * will be called after this.
         */
        protected void onClear() {
        }

        /**
         * @return an unmodifiable collection of all markers.
         */
        public java.util.Collection<Marker> getMarkers() {
            return Collections.unmodifiableCollection(mMarkers);
        }

        /**
         * Get the info window view for a given marker.
         *
         * @param marker the marker to get the info window for
         * @return the info window view or <code>null</code>
         */
        @Nullable
        protected View getInfoWindow(Marker marker) {
            if (mInfoWindowAdapter != null) {
                return mInfoWindowAdapter.getInfoWindow(marker);
            }

            return null;
        }

        /**
         * Get the info window view contents for a given marker.
         *
         * @param marker the marker to get the info window contents for
         * @return the info window view contents or <code>null</code>
         */
        @Nullable
        protected View getInfoContents(Marker marker) {
            if (mInfoWindowAdapter != null) {
                return mInfoWindowAdapter.getInfoContents(marker);
            }

            return null;
        }

        protected void onInfoWindowClick(Marker marker) {
            if (mInfoWindowClickListener != null) {
                mInfoWindowClickListener.onInfoWindowClick(marker);
            }
        }

        protected boolean onMarkerClick(Marker marker) {
            return mMarkerClickListener != null && mMarkerClickListener.onMarkerClick(marker);
        }

        protected void onMarkerDragStart(Marker marker) {
            if (mMarkerDragListener != null) {
                mMarkerDragListener.onMarkerDragStart(marker);
            }
        }

        protected void onMarkerDrag(Marker marker) {
            if (mMarkerDragListener != null) {
                mMarkerDragListener.onMarkerDrag(marker);
            }
        }

        protected void onMarkerDragEnd(Marker marker) {
            if (mMarkerDragListener != null) {
                mMarkerDragListener.onMarkerDragEnd(marker);
            }
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

    /**
     * A pure collection of map markers.
     */
    public static class MarkerCollection extends AbsMarkerCollection {

        protected MarkerCollection(MarkerManager markerManager) {
            super(markerManager);
        }

        /**
         * Add a new marker.
         *
         * @param opts
         * @return the newly created and added marker, or <code>null</code> if no marker was added.
         */
        @Nullable
        public Marker addMarker(MarkerOptions opts) {
            Marker marker = mMarkerManager.mMap.addMarker(opts);
            mMarkers.add(marker);
            mMarkerManager.mAllMarkers.put(marker, this);

            onAddedMarker(marker);

            mMarkerCollectionObservable.notifyMarkerAdded(marker);
            return marker;
        }

    }

    /**
     * A {@link MarkerCollection} which also takes care of mapping items to related markers.
     *
     * @param <T> the item type
     */
    public static class MarkerItemCollection<T> extends AbsMarkerCollection {

        private final ItemCollectionObservable<T> mItemCollectionObservable;
        private final Map<Marker, T> mMarkerToItem;
        private final Map<T, Marker> mItemToMarker;

        private List<T> mItems;
        private OnItemClickListener<T> mItemMarkerClickListener;
        private OnItemDragListener<T> mItemMarkerDragListener;
        private OnItemInfoWindowClickListener<T> mItemInfoWindowClickListener;

        public MarkerItemCollection(MarkerManager markerManager) {
            super(markerManager);

            mItemCollectionObservable = new ItemCollectionObservable<>(this);
            mMarkerToItem = new HashMap<>();
            mItemToMarker = new HashMap<>();
        }

        /**
         * Register an observer to be notified for changes to this collection.
         *
         * @param observer the observer to register; must not be <code>null</code> nor already registered
         */
        public void registerObserver(MarkerItemCollectionObserver<T> observer) {
            mItemCollectionObservable.registerObserver(observer);
        }

        /**
         * Unregister a previously registered observer.
         *
         * @param observer the observer to unregister; must not be <code>null</code> and already registered
         */
        public void unregisterObserver(MarkerItemCollectionObserver<T> observer) {
            mItemCollectionObservable.unregisterObserver(observer);
        }

        public boolean canAddMarker(T item) {
            return mItemCollectionObservable.canAddMarker(item);
        }

        /**
         * Add a new marker. The item must be set prior to calling this either via {@link #addItem(Object)} or {@link #setItems(List)}.
         *
         * @param opts
         * @param item
         * @return the newly created and added marker, or <code>null</code> if no marker was added.
         */
        @Nullable
        public Marker addMarker(MarkerOptions opts, @NonNull T item) {
            if (mItems != null && mItems.contains(item)) {
                Marker marker = getMarker(item);

                if (marker == null) {
                    marker = mMarkerManager.mMap.addMarker(opts);
                    mMarkers.add(marker);
                    mMarkerManager.mAllMarkers.put(marker, this);

                    onAddedMarker(marker);
                    mMarkerCollectionObservable.notifyMarkerAdded(marker);

                    mMarkerToItem.put(marker, item);
                    mItemToMarker.put(item, marker);
                } else {
                    BitmapDescriptor bitmapDescriptor = opts.getIcon();
                    if (bitmapDescriptor == null) {
                        bitmapDescriptor = BitmapDescriptorFactory.defaultMarker();
                    }

                    marker.setIcon(bitmapDescriptor);
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

        /**
         * Clear all items. All subsequent calls to {@link #addMarker(MarkerOptions, Object)} will result in no markers being added to the
         * map until related items will be set again.
         */
        public void clearItems() {
            if (mItems != null) {
                mItems.clear();

                mItemCollectionObservable.notifyCollectionChanged();
            }
        }

        /**
         * Set a list of items. This prepares to add markers for all items in the list.
         *
         * <p>The supplied list will be used as is, allowing for custom list implementations. Mind, this could change behavior for
         * list-modifying methods such as {@link #clearItems()}, {@link #addItem(Object) addItem()} or {@link #removeItem(Object)
         * removeItem()}.</p>
         *
         * @param items list of items to be set
         */
        public void setItems(@Nullable List<T> items) {
            if (mItems != items) {
                mItems = items;

                if (mItems != null) {
                    mItemCollectionObservable.notifyCollectionChanged();
                } else {
                    mItemCollectionObservable.notifyCollectionInvalidated();
                }
            }
        }

        /**
         * @return number of items
         */
        public int getItemSize() {
            return mItems != null ? mItems.size() : 0;
        }

        /**
         * @param item the new item to be added
         */
        public void addItem(@NonNull T item) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.add(item);
            mItemCollectionObservable.notifyCollectionItemAdded(item);
        }

        public void addAllItems(@NonNull Collection<T> collection) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mItems.addAll(collection);
            mItemCollectionObservable.notifyCollectionItemsAdded(collection);
        }

        /**
         * @param item the item to be removed
         */
        public void removeItem(@NonNull T item) {
            if (mItems != null && mItems.remove(item)) {
                mItemCollectionObservable.notifyCollectionItemRemoved(item);
            }
        }

        /**
         * Get the item at the specified location.
         *
         * @param index the index of the item to return
         * @return the item at the specified location or <code>null</code>
         * @throws IndexOutOfBoundsException if location < 0 || location >= {@link #getItemSize()}
         */
        @Nullable
        public T getItem(int index) throws IndexOutOfBoundsException {
            return mItems != null ? mItems.get(index) : null;
        }

        /**
         * Get the item for the corresponding marker.
         *
         * @param marker the corresponding marker to get the item for
         * @return the item corresponding to the supplied marker or <code>null</code>
         */
        @Nullable
        public T getItem(Marker marker) {
            return mMarkerToItem.get(marker);
        }

        /**
         * Get the marker for the corresponding item.
         *
         * @param item the corresponding item to get the marker for
         * @return the marker corresponding to the supplied item or <code>null</code>
         */
        @Nullable
        public Marker getMarker(T item) {
            return mItemToMarker.get(item);
        }

        /**
         * Notify all registered observers that an item has changed.
         *
         * @param item the changed item (must reside in the items collection)
         */
        public void notifyItemChanged(T item) {
            if (mItems != null && mItems.contains(item)) {
                mItemCollectionObservable.notifyCollectionItemChanged(item);
            }
        }

        /**
         * Get the list of all items.
         *
         * @return an unmodifiable list of all items
         */
        @NonNull
        public List<T> getItems() {
            return mItems != null ? Collections.unmodifiableList(mItems) : Collections.<T>emptyList();
        }

        @Override
        protected boolean onMarkerClick(Marker marker) {
            if (mItemMarkerClickListener != null) {
                return mItemMarkerClickListener.onItemMarkerClick(getItem(marker)) || super.onMarkerClick(marker);
            } else {
                return super.onMarkerClick(marker);
            }
        }

        @Override
        protected void onMarkerDragStart(Marker marker) {
            if (mItemMarkerDragListener != null) {
                mItemMarkerDragListener.onItemMarkerDragStart(getItem(marker));
            }

            super.onMarkerDragStart(marker);
        }

        @Override
        protected void onMarkerDrag(Marker marker) {
            if (mItemMarkerDragListener != null) {
                mItemMarkerDragListener.onItemMarkerDrag(getItem(marker));
            }

            super.onMarkerDrag(marker);
        }

        @Override
        protected void onMarkerDragEnd(Marker marker) {
            if (mItemMarkerDragListener != null) {
                mItemMarkerDragListener.onItemMarkerDragEnd(getItem(marker));
            }

            super.onMarkerDragEnd(marker);
        }

        @Override
        protected void onInfoWindowClick(Marker marker) {
            if (mItemInfoWindowClickListener != null) {
                mItemInfoWindowClickListener.onInfoWindowClick(getItem(marker));
            }

            super.onInfoWindowClick(marker);
        }

        public void setOnItemClickListener(OnItemClickListener<T> listener) {
            mItemMarkerClickListener = listener;
        }

        public void setOnItemDragListener(OnItemDragListener<T> listener) {
            mItemMarkerDragListener = listener;
        }

        public void setOnItemInfoWindowClickListener(OnItemInfoWindowClickListener<T> listener) {
            mItemInfoWindowClickListener = listener;
        }
    }

    /**
     * An observer that will be notified for changes to a {@link MarkerCollection}.
     */
    public static abstract class MarkerCollectionObserver {

        /**
         * A marker was added to the collection (and also to the map).
         *  @param collection the collection that changed
         * @param marker     the newly added marker
         */
        public void onMarkerAdded(AbsMarkerCollection collection, Marker marker) {
        }

        /**
         * A marker was removed from the collection (and also from the map).
         *  @param collection the collection that changed
         * @param marker     the marker that got removed
         */
        public void onMarkerRemoved(AbsMarkerCollection collection, Marker marker) {
        }

        /**
         * All markers were removed from the collection (and also from the map).
         *
         * @param collection the collection that changed
         */
        public void onMarkerCollectionCleared(AbsMarkerCollection collection) {
        }
    }

    private static class MarkerCollectionObservable extends android.database.Observable<MarkerCollectionObserver> {

        private final AbsMarkerCollection mCollection;

        public MarkerCollectionObservable(AbsMarkerCollection collection) {
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

    /**
     * Callback interface invoked when a marker to a corresponding item was clicked.
     *
     * @param <T>
     */
    public interface OnItemClickListener<T> {

        /**
         * The marker corresponding to an item was clicked.
         *
         * @param item the item that was clicked.
         * @return {@code true} of the event was handled, otherwise {@code false}
         */
        boolean onItemMarkerClick(T item);
    }

    /**
     * Callback interface invoked for marker-drag events to a corresponding item.
     *
     * @param <T>
     */
    public interface OnItemDragListener<T> {

        /**
         * A drag started.
         *
         * @param item the item the drag started for
         */
        void onItemMarkerDragStart(T item);

        /**
         * A drag is active.
         *
         * @param item the item being dragged
         */
        void onItemMarkerDrag(T item);

        /**
         * A drag ended.
         *
         * @param item the item the drag stopped
         */
        void onItemMarkerDragEnd(T item);
    }

    /**
     * Callback interface invoked when an info window was clicked
     *
     * @param <T>
     */
    public interface OnItemInfoWindowClickListener<T> {

        /**
         * An info window was clicked.
         *
         * @param item the corresponding item
         */
        void onInfoWindowClick(T item);
    }

    /**
     * An observer that will be notified for changes to a {@link MarkerItemCollection}.
     *
     * @param <T>
     */
    public static abstract class MarkerItemCollectionObserver<T> {

        /**
         * The entire collection changed. All items might need an evaluation.
         *
         * @param collection the collection that changed
         */
        public void onCollectionChanged(MarkerItemCollection<T> collection) {
        }

        /**
         * The entire collection was invalidated.
         *
         * @param collection the collection that was invalidated.
         */
        public void onCollectionInvalidated(MarkerItemCollection<T> collection) {
        }

        /**
         * An item was added to the collection.
         *
         * @param collection the collection that changed
         * @param item       the new item
         */
        public void onItemAdded(MarkerItemCollection<T> collection, T item) {
        }

        /**
         * A collection of items was added to the collection.
         *
         * @param collection the collection that changed
         * @param items      the new items
         */
        public void onItemsAdded(MarkerItemCollection<T> collection, Collection<T> items) {
        }

        /**
         * An item was removed from the collection.
         *
         * @param collection the collection that changed
         * @param item       the old item
         */
        public void onItemRemoved(MarkerItemCollection<T> collection, T item) {
        }

        /**
         * An item changed.
         *
         * @param collection the collection that changed
         * @param item       the changed item
         */
        public void onItemChanged(MarkerItemCollection<T> collection, T item) {
        }

        /**
         * A marker is about to be added for an item.
         *
         * @param collection the collection the item belongs to
         * @param item       the item for which a marker shall be added
         * @return {@code true} to allow to add a marker (default), otherwise {@code false}
         */
        public boolean onCanAddMarker(MarkerItemCollection<T> collection, T item) {
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

        public void notifyCollectionItemsAdded(Collection<T> items) {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onItemsAdded(mCollection, items);
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
                    if (!mObservers.get(i).onCanAddMarker(mCollection, item)) {
                        return false;
                    }
                }
            }

            return true;
        }

    }

}
