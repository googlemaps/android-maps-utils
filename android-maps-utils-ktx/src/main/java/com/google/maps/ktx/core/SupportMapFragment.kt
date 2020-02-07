package com.google.maps.ktx.core

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun SupportMapFragment.awaitMap(): GoogleMap =
    suspendCoroutine { continuation ->
        getMapAsync {
            continuation.resume(it)
        }
    }
