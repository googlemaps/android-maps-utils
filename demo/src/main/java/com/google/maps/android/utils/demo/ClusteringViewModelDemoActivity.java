/*
 * Copyright 2019 Google Inc.
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

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

public class ClusteringViewModelDemoActivity extends BaseDemoActivity {
    private ClusterManager<MyItem> mClusterManager;
    private ClusteringViewModel mViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ClusteringViewModel.class);
        if (savedInstanceState == null) {
            try {
                mViewModel.readItems(getResources());
            } catch (JSONException e) {
                Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthDp = (int) (metrics.widthPixels / metrics.density);
        int heightDp = (int) (metrics.heightPixels / metrics.density);

        mViewModel.getAlgorithm().updateViewSize(widthDp, heightDp);

        mClusterManager = new ClusterManager<>(this, getMap());
        mClusterManager.setAlgorithm(mViewModel.getAlgorithm());

        getMap().setOnCameraIdleListener(mClusterManager);
    }
}
