/*
 * Copyright 2020 Google LLC
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.data.Layer;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;
import com.google.maps.android.data.Renderer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer extends Layer {

    private static final int DEFAULT_MAX_KMZ_ENTRY_COUNT = 200;
    private static final long DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML or KMZ file
     * @param context The Context
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context)
            throws XmlPullParserException, IOException {
        this(map, context.getResources().openRawResource(resourceId), context, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map), null, null, DEFAULT_MAX_KMZ_ENTRY_COUNT, DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML or KMZ file
     * @param context The Context
     * @param maxKmzEntryCount The maximum number of entries a KMZ file can contain.
     * @param maxKmzUncompressedTotalSize The maximum size of the uncompressed KMZ file in bytes.
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context, int maxKmzEntryCount, long maxKmzUncompressedTotalSize)
            throws XmlPullParserException, IOException {
        this(map, context.getResources().openRawResource(resourceId), context, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map), null, null, maxKmzEntryCount, maxKmzUncompressedTotalSize);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, InputStream stream, Context context)
            throws XmlPullParserException, IOException {
        this(map, stream, context, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map), null, null, DEFAULT_MAX_KMZ_ENTRY_COUNT, DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @param maxKmzEntryCount The maximum number of entries a KMZ file can contain.
     * @param maxKmzUncompressedTotalSize The maximum size of the uncompressed KMZ file in bytes.
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map, InputStream stream, Context context, int maxKmzEntryCount, long maxKmzUncompressedTotalSize)
            throws XmlPullParserException, IOException {
        this(map, stream, context, new MarkerManager(map), new PolygonManager(map), new PolylineManager(map), new GroundOverlayManager(map), null, null, maxKmzEntryCount, maxKmzUncompressedTotalSize);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    @RawRes int resourceId,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache)
            throws XmlPullParserException, IOException {
        this(map, context.getResources().openRawResource(resourceId), context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, null, DEFAULT_MAX_KMZ_ENTRY_COUNT, DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @param maxKmzEntryCount The maximum number of entries a KMZ file can contain.
     * @param maxKmzUncompressedTotalSize The maximum size of the uncompressed KMZ file in bytes.
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    @RawRes int resourceId,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache,
                    int maxKmzEntryCount,
                    long maxKmzUncompressedTotalSize)
            throws XmlPullParserException, IOException {
        this(map, context.getResources().openRawResource(resourceId), context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, null, maxKmzEntryCount, maxKmzUncompressedTotalSize);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    InputStream stream,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache)
            throws XmlPullParserException, IOException {
        this(map, stream, context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, null, DEFAULT_MAX_KMZ_ENTRY_COUNT, DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @param maxKmzEntryCount The maximum number of entries a KMZ file can contain.
     * @param maxKmzUncompressedTotalSize The maximum size of the uncompressed KMZ file in bytes.
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    InputStream stream,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache,
                    int maxKmzEntryCount,
                    long maxKmzUncompressedTotalSize)
            throws XmlPullParserException, IOException {
        this(map, stream, context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, null, maxKmzEntryCount, maxKmzUncompressedTotalSize);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @param urlSanitizer sanitizer to be used for external URLs
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    InputStream stream,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache,
                    @Nullable KmlUrlSanitizer urlSanitizer)
            throws XmlPullParserException, IOException {
        this(map, stream, context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, urlSanitizer, DEFAULT_MAX_KMZ_ENTRY_COUNT, DEFAULT_MAX_KMZ_UNCOMPRESSED_TOTAL_SIZE);
    }

    /**
     * Creates a new KmlLayer object - addLayerToMap() must be called to trigger rendering onto a map.
     * <p>
     * Constructor may be called on a background thread, as I/O and parsing may be long-running.
     * <p>
     * Use this constructor with shared object managers in order to handle multiple layers with
     * their own event handlers on the map.
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML or KMZ file
     * @param context The Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param cache cache to be used for fetched images
     * @param urlSanitizer sanitizer to be used for external URLs
     * @param maxKmzEntryCount The maximum number of entries a KMZ file can contain.
     * @param maxKmzUncompressedTotalSize The maximum size of the uncompressed KMZ file in bytes.
     * @throws XmlPullParserException if file cannot be parsed
     * @throws IOException if I/O error
     */
    public KmlLayer(GoogleMap map,
                    InputStream stream,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    Renderer.ImagesCache cache,
                    @Nullable KmlUrlSanitizer urlSanitizer,
                    int maxKmzEntryCount,
                    long maxKmzUncompressedTotalSize)
            throws XmlPullParserException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("KML InputStream cannot be null");
        }
        KmlRenderer renderer = new KmlRenderer(map, context, markerManager, polygonManager, polylineManager, groundOverlayManager, cache, urlSanitizer);

        BufferedInputStream bis = new BufferedInputStream(stream);
        bis.mark(1024);
        ZipInputStream zip = new ZipInputStream(bis);
        CountingInputStream countingStream = new CountingInputStream(zip, maxKmzUncompressedTotalSize);
        try {
            KmlParser parser = null;
            ZipEntry entry = zip.getNextEntry();
            if (entry != null) { // is a KMZ zip file
                int entryCount = 0;
                HashMap<String, Bitmap> images = new HashMap<>();
                while (entry != null) {
                    entryCount++;
                    if (entryCount > maxKmzEntryCount) {
                        throw new IOException("Zip bomb detected! Max number of entries exceeded: " + maxKmzEntryCount);
                    }
                    if (parser == null && entry.getName().toLowerCase().endsWith(".kml")) {
                        parser = parseKml(countingStream);
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeStream(countingStream);
                        if (bitmap != null) {
                            images.put(entry.getName(), bitmap);
                        } else {
                            Log.w("KmlLayer", "Unsupported KMZ contents file type: " + entry.getName());
                        }
                    }
                    entry = zip.getNextEntry();
                }
                if (parser == null) {
                    throw new IllegalArgumentException("KML not found in InputStream");
                }
                renderer.storeKmzData(parser.getStyles(), parser.getStyleMaps(), parser.getPlacemarks(),
                        parser.getContainers(), parser.getGroundOverlays(), images);
            } else { // is a KML
                bis.reset();
                parser = parseKml(bis);
                renderer.storeKmlData(parser.getStyles(), parser.getStyleMaps(), parser.getPlacemarks(),
                        parser.getContainers(), parser.getGroundOverlays());
            }
            storeRenderer(renderer);
        } finally {
            stream.close();
            bis.close();
            zip.close();
        }
    }

    /**
     * Wrapper for an InputStream that counts the number of bytes read and throws an IOException
     * if the limit is exceeded.
     */
    private static class CountingInputStream extends InputStream {
        private final InputStream mIn;
        private long mTotalBytes = 0;
        private final long mMaxBytes;

        public CountingInputStream(InputStream in, long maxBytes) {
            mIn = in;
            mMaxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int b = mIn.read();
            if (b != -1) {
                mTotalBytes++;
                checkLimit();
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = mIn.read(b, off, len);
            if (n != -1) {
                mTotalBytes += n;
                checkLimit();
            }
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = mIn.skip(n);
            if (skipped > 0) {
                mTotalBytes += skipped;
                checkLimit();
            }
            return skipped;
        }

        private void checkLimit() throws IOException {
            if (mTotalBytes > mMaxBytes) {
                throw new IOException("Zip bomb detected! Uncompressed size exceeds limit of " + mMaxBytes + " bytes.");
            }
        }
    }

    private static KmlParser parseKml(InputStream stream) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = createXmlParser(stream);
        KmlParser parser = new KmlParser(xmlPullParser);
        parser.parseKml();
        return parser;
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    private static XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
        factory.setFeature(XmlPullParser.FEATURE_VALIDATION, false);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Adds the KML data to the map - must be called on the main UI thread
     */
    @Override
    public void addLayerToMap() {
        super.addKMLToMap();
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasPlacemarks() {
        return hasFeatures();
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    public Iterable<KmlPlacemark> getPlacemarks() {
        return (Iterable<KmlPlacemark>) getFeatures();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    public boolean hasContainers() {
        return super.hasContainers();
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    public Iterable<KmlContainer> getContainers() {
        return super.getContainers();
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return super.getGroundOverlays();
    }

}
