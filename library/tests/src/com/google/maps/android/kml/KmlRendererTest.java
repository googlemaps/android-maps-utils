package com.google.maps.android.kml;

import android.test.ActivityTestCase;

/**
 * Created by lavenderch on 1/29/15.
 */
public class KmlRendererTest extends ActivityTestCase {

    public void testGetContainerVisibility() {
        KmlContainer container = new KmlContainer();
        container.setProperty("visibility", "0");
        assertFalse(KmlRenderer.getContainerVisibility(container, false));
        assertFalse(KmlRenderer.getContainerVisibility(container, true));
        container.setProperty("visibility", "1");
        assertFalse(KmlRenderer.getContainerVisibility(container, false));
        assertTrue(KmlRenderer.getContainerVisibility(container, true));
    }


}
