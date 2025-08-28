/*
 * Copyright 2025 Google LLC
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewGroup mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // This tells the system that the app will handle drawing behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // This is the root view of my layout.
        // Make sure to replace R.id.root_layout with the actual ID of your root view.
        final View rootView = findViewById(android.R.id.content);

        // Add a listener to handle window insets.
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply the insets as padding to the view.
            // This will push the content down from behind the status bar and up from
            // behind the navigation bar.
            view.setPadding(
                insets.left,
                insets.top,
                insets.right,
                insets.bottom
            );

            // Return CONSUMED to signal that we've handled the insets.
            return WindowInsetsCompat.CONSUMED;
        });

        mListView = findViewById(R.id.list);

        addDemo("Advanced Markers Clustering Example", CustomAdvancedMarkerClusteringDemoActivity.class);
        addDemo("Cluster Algorithms", ClusterAlgorithmsDemoActivity.class);
        addDemo("Clustering", ClusteringDemoActivity.class);
        addDemo("Clustering: Custom Look", CustomMarkerClusteringDemoActivity.class);
        addDemo("Clustering: Diff", ClusteringDiffDemoActivity.class);
        addDemo("Clustering: 2K markers", BigClusteringDemoActivity.class);
        addDemo("Clustering: 20K only visible markers", VisibleClusteringDemoActivity.class);
        addDemo("Clustering: ViewModel", ClusteringViewModelDemoActivity.class);
        addDemo("Clustering: Force on Zoom", ZoomClusteringDemoActivity.class);
        addDemo("PolyUtil.decode", PolyDecodeDemoActivity.class);
        addDemo("PolyUtil.simplify", PolySimplifyDemoActivity.class);
        addDemo("IconGenerator", IconGeneratorDemoActivity.class);
        addDemo("SphericalUtil.computeDistanceBetween", DistanceDemoActivity.class);
        addDemo("Generating tiles", TileProviderAndProjectionDemo.class);
        addDemo("Heatmaps", HeatmapsDemoActivity.class);
        addDemo("Heatmaps with Places API", HeatmapsPlacesDemoActivity.class);
        addDemo("GeoJSON Layer", GeoJsonDemoActivity.class);
        addDemo("KML Layer Overlay", KmlDemoActivity.class);
        addDemo("Multi Layer", MultiLayerDemoActivity.class);
        addDemo("AnimationUtil sample", AnimationUtilDemoActivity.class);
        addDemo("Street View Demo", StreetViewDemoActivity.class);
    }

    private void addDemo(String demoName, Class<? extends Activity> activityClass) {
        Button b = new Button(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        b.setLayoutParams(layoutParams);
        b.setText(demoName);
        b.setTag(activityClass);
        b.setOnClickListener(this);
        mListView.addView(b);
    }

    @Override
    public void onClick(View view) {
        Class activityClass = (Class) view.getTag();
        startActivity(new Intent(this, activityClass));
    }
}
