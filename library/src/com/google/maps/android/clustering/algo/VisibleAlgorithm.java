package com.google.maps.android.clustering.algo;

import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Logic for computing clusters only in visible area
 */
public interface VisibleAlgorithm<T extends ClusterItem> extends Algorithm<T> {
	void setVisibleRegion(VisibleRegion visibleRegion);
}
