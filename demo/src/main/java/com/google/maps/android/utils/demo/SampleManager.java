package com.google.maps.android.utils.demo;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.collections.GroundOverlayManager;


import androidx.annotation.NonNull;

public class SampleManager extends GroundOverlayManager implements GoogleMap.OnGroundOverlayClickListener {


    public SampleManager(@NonNull GoogleMap map) {
        super(map);
    }
}
