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

import static com.google.maps.android.utils.demo.ApiKeyValidatorKt.hasMapsApiKey;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Objects;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseDemoActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private boolean mIsRestore;

    protected int getLayoutId() {
        return R.layout.map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasMapsApiKey(this)) {
            Toast.makeText(this, R.string.bad_maps_api_key, Toast.LENGTH_LONG).show();
            finish();
        }

        mIsRestore = savedInstanceState != null;
        setContentView(getLayoutId());
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
        setUpMap();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        if (mMap != null) {
            return;
        }
        mMap = map;
        startDemo(mIsRestore);
    }

    private void setUpMap() {
        ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map))).getMapAsync(this);
    }

    /**
     * Run the demo-specific code.
     */
    protected abstract void startDemo(boolean isRestore);

    protected GoogleMap getMap() {
        return mMap;
    }
}