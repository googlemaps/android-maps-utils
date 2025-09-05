package com.google.maps.android.utils.demo

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

class PolylineProgressDemoActivity : AppCompatActivity(), OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

    private var planeIcon: BitmapDescriptor? = null
    private lateinit var map: GoogleMap
    private lateinit var originalPolyline: Polyline
    private var progressPolyline: Polyline? = null
    private var progressMarker: Marker? = null

    private lateinit var seekBar: SeekBar
    private lateinit var percentageTextView: TextView

    private var isReady = false
    private var animationJob: Job? = null

    private val progress = MutableLiveData<Int>()
    private val direction = MutableLiveData<Int>()
    private val stepSize = 1

    private val polylinePoints = listOf(
        LatLng(40.7128, -74.0060),   // New York
        LatLng(34.0522, -118.2437),  // Los Angeles
        LatLng(41.8781, -87.6298),   // Chicago
        LatLng(29.7604, -95.3698),   // Houston
        LatLng(39.9526, -75.1652)    // Philadelphia
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isReady = false
        setContentView(R.layout.activity_polyline_progress_demo)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        progress.value = 0
        direction.value = 1
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isReady = true

        planeIcon = bitmapDescriptorFromVector(R.drawable.baseline_airplanemode_active_24, "#FFD700".toColorInt())

        originalPolyline = map.addPolyline(
            PolylineOptions()
                .addAll(polylinePoints)
                .color(Color.GRAY)
                .width(15f) // Set the width of the base polyline
                .geodesic(true) // Be sure to set the geodesic flag!
        )

        val bounds = with(com.google.android.gms.maps.model.LatLngBounds.Builder()) {
            for (point in polylinePoints) {
                include(point)
            }
            build()
        }

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        percentageTextView = findViewById(R.id.percentageTextView)
        seekBar = findViewById<SeekBar>(R.id.seekBar).also {
            it.setOnSeekBarChangeListener(this)
            it.progress = 0
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            if (animationJob != null) {
                animationJob?.cancel()
                animationJob = null
            }
            direction.value = 1
            progress.value = 0
            startAnimation()
        }

        findViewById<Button>(R.id.pauseButton).setOnClickListener {
            if (animationJob != null) {
                animationJob?.cancel()
                animationJob = null
            } else {
                startAnimation()
            }
        }

        progress.observe(this) {
            seekBar.progress = it
            percentageTextView.text = "$it%"
            updateProgress(it / 100.0)
        }

        startAnimation()
    }

    private fun startAnimation() {
        stopAnimation() // Stop any existing animation first

        // Start a coroutine to animate the polyline progress
        animationJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val currentProgress = progress.value ?: 0
                val currentDirection = direction.value ?: 1
                var nextProgress = currentProgress + currentDirection * stepSize
                if (nextProgress >= 100) {
                    nextProgress = 100
                    direction.value = -1
                } else if (nextProgress <= 0) {
                    nextProgress = 0
                    direction.value = 1
                }
                progress.value = nextProgress
                delay(50)
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    override fun onPause() {
        super.onPause()
        animationJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startAnimation()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            stopAnimation()
            this.progress.value = progress
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private fun updateProgress(percentage: Double) {
        if (!isReady) return

        progressPolyline?.remove()

        val prefix = SphericalUtil.getPolylinePrefix(polylinePoints, percentage)
        if (prefix.isNotEmpty()) {
            progressPolyline = map.addPolyline(
                PolylineOptions()
                    .addAll(prefix)
                    .color(Color.BLUE)
                    .width(7f) // Set the width of the progress polyline
                    .zIndex(1f) // Set the z-index to draw it on top
                    .geodesic(true) // Be sure to set the geodesic flag!
            )
        }

        val point = SphericalUtil.getPointOnPolyline(polylinePoints, percentage)
        if (point != null) {
            // Get the next point to calculate the heading
            val nextPoint = SphericalUtil.getPointOnPolyline(polylinePoints, percentage + 0.0001)
            var heading = nextPoint?.let { SphericalUtil.computeHeading(point, it) }
            if (direction.value == -1) {
                heading = heading?.plus(180)
            }

            if (progressMarker == null) {
                progressMarker = map.addMarker(
                    MarkerOptions()
                        .position(point)
                        .flat(true)
                        .draggable(false)
                        .also {
                            if (heading != null) {
                                it.rotation(heading.toFloat())
                            }
                            if (planeIcon != null) {
                                it.icon(planeIcon)
                            }
                        }
                )
            } else {
                progressMarker?.position = point
                if (heading != null) {
                    progressMarker?.rotation = heading.toFloat()
                }
            }
        }
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int, color: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(this, vectorResId)?.run {
            setTint(color)
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}