package com.google.maps.android.utils.demo;

import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Arrays;

public class DistanceDemoActivity extends BaseDemoActivity
        implements ClusterManager.OnClusterItemDragListener<DistanceDemoActivity.MyMarker>,
        ClusterManager.OnClusterItemClickListener<DistanceDemoActivity.MyMarker>{
    private TextView mTextView;
    private MyMarker mMarkerA;
    private MyMarker mMarkerB;
    private Polyline mPolyline;
    private ClusterManager<MyMarker> mClusterManager;

    static class MyMarker implements ClusterItem {
        private final String name;
        private LatLng position;

        private MyMarker(String name, LatLng position) {
            this.name = name;
            this.position = position;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        public void setPosition(LatLng position) {
            this.position = position;
        }

        public String getName() {
            return name;
        }

        @Override
        public void OnCreate(MarkerOptions markerOptions) {
            markerOptions.title(name);
            markerOptions.draggable(true);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.distance_demo;
    }

    @Override
    protected void startDemo() {
        mTextView = (TextView) findViewById(R.id.textView);

        mMarkerA = new MyMarker("A", new LatLng(-33.9046, 151.155));
        mMarkerB = new MyMarker("B", new LatLng(-33.8291, 151.248));

        mClusterManager = new ClusterManager(getApplicationContext(), getMap());
        mClusterManager.addItem(mMarkerA);
        mClusterManager.addItem(mMarkerB);
        mPolyline = getMap().addPolyline(new PolylineOptions().geodesic(true));

        mClusterManager.setOnItemMarkerDragListener(this);
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8256, 151.2395), 10));

        Toast.makeText(this, "Drag the markers!", Toast.LENGTH_LONG).show();
        showDistance();
    }

    private void showDistance() {
        double distance = SphericalUtil.computeDistanceBetween(mMarkerA.getPosition(), mMarkerB.getPosition());
        mTextView.setText(String.format("The marker %s and %s are %s apart", mMarkerA.getName(), mMarkerB.getName(), formatNumber(distance)));
    }

    private void updatePolyline() {
        mPolyline.setPoints(Arrays.asList(mMarkerA.getPosition(), mMarkerB.getPosition()));
    }

    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @Override
    public void onClusterItemDragStart(MyMarker item, Marker marker) {

    }

    @Override
    public void onClusterItemDrag(MyMarker item, Marker marker) {
        showDistance();
        updatePolyline();
    }

    @Override
    public void onClusterItemDragEnd(MyMarker item, Marker marker) {
        showDistance();
        updatePolyline();
    }

    @Override
    public boolean onClusterItemClick(MyMarker item) {
        showDistance();
        updatePolyline();
        return true;
    }
}
