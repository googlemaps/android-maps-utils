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

package com.google.maps.android.utils.demo;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TileProviderAndProjectionDemo extends BaseDemoActivity {
    @Override
    protected void startDemo() {
        PointTileOverlay pto = new PointTileOverlay();
        pto.addPoint(new LatLng(0, 0));
        pto.addPoint(new LatLng(21, -10));
        getMap().addTileOverlay(new TileOverlayOptions().tileProvider(pto));
    }

    private class PointTileOverlay implements TileProvider {
        private List<Point> mPoints = new ArrayList<Point>();
        private int mTileSize = 256;
        private SphericalMercatorProjection mProjection = new SphericalMercatorProjection(mTileSize);
        private int mScale = 2;
        private int mDimension = mScale * mTileSize;

        @Override
        public Tile getTile(int x, int y, int zoom) {
            Matrix matrix = new Matrix();
            float scale = (float) Math.pow(2, zoom) * mScale;
            matrix.postScale(scale, scale);
            matrix.postTranslate(-x * mDimension, -y * mDimension);

            Bitmap bitmap = Bitmap.createBitmap(mDimension, mDimension, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            c.setMatrix(matrix);

            for (Point p : mPoints) {
                c.drawCircle((float) p.x, (float) p.y, 1, new Paint());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return new Tile(mDimension, mDimension, baos.toByteArray());
        }

        public void addPoint(LatLng latLng) {
            mPoints.add(mProjection.toPoint(latLng));
        }
    }
}
