package com.google.maps.android.quadtree;

import android.util.Log;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import junit.framework.TestCase;

import java.util.Collection;

public class QuadTreeTest extends TestCase {

    private QuadTree<Item> mTree;
    private long startTime;

    public void setUp() {
        mTree = new PointQuadTree<Item>(0, 1, 0, 1);
        Log.d("QuadTreeTest", "--------------------------------------");
        startTime = System.currentTimeMillis();
    }

    public void tearDown() {
        Log.d("QuadTreeTest", "Running time = "
                + ((System.currentTimeMillis() - startTime)/1000.0) + "s");
    }

    public void testAddOnePoint() {
        Log.d("QuadTreeTest", "Running testAddOnePoint");
        Item item = new Item(0,0);
        mTree.add(item);
        Collection<Item> items = searchAll();
        assertEquals(1, items.size());
    }

    public void testEmpty() {
        Log.d("QuadTreeTest", "Running testEmpty");
        Collection<Item> items = searchAll();
        assertEquals(0, items.size());
    }

    public void testMultiplePoints() {
        Log.d("QuadTreeTest", "Running testMultiplePoints");
        Item item1 = new Item(0, 0);
        mTree.add(item1);
        Item item2 = new Item(.1, .1);
        mTree.add(item2);
        Item item3 = new Item(.2, .2);
        mTree.add(item3);

        Collection<Item> items = searchAll();
        assertEquals(3, items.size());

        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
        assertTrue(items.contains(item3));

        mTree.remove(item1);
        mTree.remove(item2);
        mTree.remove(item3);

        assertEquals(0, searchAll().size());
    }

    public void testSameLocationDifferentPoint() {
        Log.d("QuadTreeTest", "Running testSameLocationDifferentPoint");
        mTree.add(new Item(0, 0));
        mTree.add(new Item(0, 0));

        assertEquals(2, searchAll().size());
    }

    public void testClear() {
        Log.d("QuadTreeTest", "Running testClear");
        mTree.add(new Item(.1, .1));
        mTree.add(new Item(.2, .2));
        mTree.add(new Item(.3, .3));

        mTree.clear();
        assertEquals(0, searchAll().size());
    }

    public void testSearch() {
        Log.d("QuadTreeTest", "Running testSearch");
        for (int i = 0; i < 10000; i++) {
            mTree.add(new Item(i / 20000.0, i / 20000.0));
        }

        assertEquals(10000, searchAll().size());
        assertEquals(1, mTree.search(new Bounds((double) 0, 0.00001, (double) 0, 0.00001)).size());
        assertEquals(0, mTree.search(new Bounds(.7, .8, .7, .8)).size());
    }

    public void testFourPoints() {
        Log.d("QuadTreeTest", "Running testFourPoint");
        mTree.add(new Item(0.2, 0.2));
        mTree.add(new Item(0.7, 0.2));
        mTree.add(new Item(0.2, 0.7));
        mTree.add(new Item(0.7, 0.7));

        assertEquals(2, mTree.search(new Bounds(0.0, 0.5, 0.0, 1.0)).size());
    }

    public void testVeryDeepTree() {
        Log.d("QuadTreeTest", "Running testVeryDeepTree");
        for (int i = 0; i < 3000; i++) {
            mTree.add(new Item(0, 0));
        }

        assertEquals(3000, searchAll().size());
        assertEquals(3000, mTree.search(new Bounds(0, .1, 0, .1)).size());
        assertEquals(0, mTree.search(new Bounds(.1, 1, .1, 1)).size());
    }

    public void testManyPoints() {
        Log.d("QuadTreeTest", "Running testManyPoints");
        long start = System.currentTimeMillis();
        for (double i=0; i < 200; i++) {
            for (double j=0; j < 2000; j++) {
                mTree.add(new Item(i/200.0, j/2000.0));
            }
        }
        Log.d("QuadTreeTest", "adding points time: " + (System.currentTimeMillis() - start));

        // searching bounds that are exact subtrees of the main quadTree
        start = System.currentTimeMillis();
        assertEquals(400000, searchAll().size());
        assertEquals(100000, mTree.search(new Bounds(0, .5, 0, .5)).size());
        assertEquals(100000, mTree.search(new Bounds(.5, 1, 0, .5)).size());
        assertEquals(25000, mTree.search(new Bounds(0, .25, 0, .25)).size());
        assertEquals(25000, mTree.search(new Bounds(.75, 1, .75, 1)).size());
        Log.d("QuadTreeTest", "search base2 bounds time: " + (System.currentTimeMillis() - start));

        // searching bounds that do not line up with main quadTree
        start = System.currentTimeMillis();
        assertEquals(399600, mTree.search(new Bounds(0, 0.999, 0, 0.999)).size());
        assertEquals(4000, mTree.search(new Bounds(0.8, 0.9, 0.8, 0.9)).size());
        assertEquals(4000, mTree.search(new Bounds(0, 1, 0, 0.01)).size());
        assertEquals(16000, mTree.search(new Bounds(0.4, 0.6, 0.4, 0.6)).size());
        Log.d("QuadTreeTest", "search strange bounds time: " + (System.currentTimeMillis() - start));

        // searching bounds that are small / have very exact end points
        start = System.currentTimeMillis();
        assertEquals(1, mTree.search(new Bounds(0, .001, 0, .0001)).size());
        assertEquals(26574, mTree.search(new Bounds(0.356, 0.574, 0.678, 0.987)).size());
        assertEquals(44622, mTree.search(new Bounds(0.123, 0.456, 0.456, 0.789)).size());
        assertEquals(4884, mTree.search(new Bounds(0.111, 0.222, 0.333, 0.444)).size());
        Log.d("QuadTreeTest", "search small bounds time: " + (System.currentTimeMillis() - start));

        mTree.clear();
        assertEquals(0, searchAll().size());
    }

    private Collection<Item> searchAll() {
        return mTree.search(new Bounds(0, 1, 0, 1));
    }

    private static class Item implements QuadTree.Item {
        private final Point mPoint;

        private Item(double x, double y) {
            this.mPoint = new Point(x, y);
        }

        @Override
        public Point getPoint() {
            return mPoint;
        }
    }
}
