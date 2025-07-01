package com.google.maps.android.utils.demo;

import static com.google.maps.android.utils.demo.ApiKeyValidatorKt.getMapsApiKey;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.isochrone.IsochroneMapProvider;

public class IsochroneMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "IsochroneMapActivity";

    private FrameLayout rootLayout;
    private MapView mapView;
    private ProgressBar progressBar;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private IsochroneMapProvider isochroneMapProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootLayout = new FrameLayout(this);
        mapView = new MapView(this);

        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(android.view.View.GONE);

        FrameLayout.LayoutParams mapParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        rootLayout.addView(mapView, mapParams);
        rootLayout.addView(progressBar, progressParams);

        setContentView(rootLayout);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        getLocationAndDraw();
                    } else {
                        Log.e(TAG, "Location permission denied");
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        checkPermissionAndStart();
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getLocationAndDraw();
        }
    }

    private void getLocationAndDraw() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 14f));

                if (isochroneMapProvider == null) {
                    String apiKey = getMapsApiKey(this);
                    isochroneMapProvider = new IsochroneMapProvider(map, apiKey, new IsochroneMapProvider.LoadingListener() {
                        @Override
                        public void onLoadingStarted() {
                            runOnUiThread(() -> progressBar.setVisibility(android.view.View.VISIBLE));
                        }

                        @Override
                        public void onLoadingFinished() {
                            runOnUiThread(() -> progressBar.setVisibility(android.view.View.GONE));
                        }
                    }, IsochroneMapProvider.TransportMode.BICYCLING);
                    isochroneMapProvider.setUiThreadExecutor(this::runOnUiThread);
                }

                isochroneMapProvider.drawIsochrones(origin, new int[]{2,4,6,9}, IsochroneMapProvider.ColorSchema.GREEN_RED);

            } else {
                Log.e(TAG, "Location is null");
            }
        });
    }


    // MapView lifecycle methods
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
