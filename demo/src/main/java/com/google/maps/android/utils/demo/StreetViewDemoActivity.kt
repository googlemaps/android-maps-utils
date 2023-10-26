package com.google.maps.android.utils.demo

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.StreetViewUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StreetViewDemoActivity : Activity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.street_view_demo)

        GlobalScope.launch(Dispatchers.Main) {
            val response1 =
                StreetViewUtils.fetchStreetViewData(
                    LatLng(48.1425918, 11.5386121),
                    BuildConfig.MAPS_API_KEY
                )
            val response2 = StreetViewUtils.fetchStreetViewData(
                LatLng(8.1425918, 11.5386121),
                BuildConfig.MAPS_API_KEY
            )

            findViewById<TextView>(R.id.textViewFirstLocation).text = getString(R.string.location_1_is_supported_in_streetview, response1)
            findViewById<TextView>(R.id.textViewSecondLocation).text = getString(R.string.location_1_is_supported_in_streetview, response2)
        }
    }
}

