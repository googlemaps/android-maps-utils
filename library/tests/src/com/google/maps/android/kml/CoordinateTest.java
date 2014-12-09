package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

/**
 *
             *
            /.\
           /..\
          /'.'\
         /.''.'\
         /.'.'.\
        /'.''.'.\
        ^^^[_]^^^
 */
public class CoordinateTest extends TestCase {

    Coordinate c = new Coordinate();

    public void testInitialisation() {
        //Testing that newly created coordinate does not contain any values
        assertEquals(c.getBoundary(), -1);
        assertEquals(c.getType(), -1);
        assertEquals(c.getCoordinateList(), null);

    }

    public void testType () {
        //Testing that valid types are accepted
        c.setType(0);
        assertEquals(c.getType(), 0);
        c.setType(1);
        assertEquals(c.getType(), 1);
        c.setType(2);
        assertEquals(c.getType(), 2);

        //Testing that invalid types are uninitialized
        c.setType(4);
        assertEquals(c.getType(), -1);
        c.setType(9);
        assertEquals(c.getType(), -1);
        c.setType(-1);
        assertEquals(c.getType(), -1);
    }

    public void testConvertToLatLng() {

        String[] latLng1 = {"5", "5"};
        assertNotNull(c.convertToLatLng(latLng1));
        String[] latLng2 = {"Hello", "World"};
        assertNull(c.convertToLatLng(latLng2));
        String[] latLng3 = null;
        assertNull(c.convertToLatLng(latLng3));
    }

    public void testConvertCoordinateList() {
        String list = "45, 65 \n 65, 45\n 23, 45\n";
        c.setCoordinateList(list);
        assertEquals(c.getCoordinateList().size(), 3);



    }


}
