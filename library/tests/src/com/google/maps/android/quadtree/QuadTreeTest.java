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
        //mTree = new PointQuadTree<Item>(0, 1, 0, 1);
        mTree = new LinearQuadTree<Item>(0, 1, 0, 1, 8);
        startTime = System.currentTimeMillis();
    }

    public void tearDown() {
        Log.d("LOL", "Running time = " + ((System.currentTimeMillis() - startTime)/1000.0));
    }

    public void testAddOnePoint() {
        Item item = new Item(0,0);
        mTree.add(item);
        Collection<Item> items = searchAll();
        assertEquals(1, items.size());
    }

    public void testEmpty() {
        Collection<Item> items = searchAll();
        assertEquals(0, items.size());
    }

    public void testMultiplePoints() {
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
        mTree.add(new Item(0, 0));
        mTree.add(new Item(0, 0));

        assertEquals(2, searchAll().size());
    }

    public void testClear() {
        mTree.add(new Item(.1, .1));
        mTree.add(new Item(.2, .2));
        mTree.add(new Item(.3, .3));

        mTree.clear();
        assertEquals(0, searchAll().size());
    }

    public void testSearch() {
        for (int i = 0; i < 1000; i++) {
            mTree.add(new Item(i / 2000.0, i / 2000.0));
        }

        assertEquals(1000, searchAll().size());
        assertEquals(1, mTree.search(new Bounds((double) 0, 0.0001, (double) 0, 0.0001)).size());
        assertEquals(0, mTree.search(new Bounds(.7, .8, .7, .8)).size());
    }

    public void testFourPoints() {
        mTree.add(new Item(0.2, 0.2));
        mTree.add(new Item(0.7, 0.2));
        mTree.add(new Item(0.2, 0.7));
        mTree.add(new Item(0.7, 0.7));

        assertEquals(2, mTree.search(new Bounds(0.0, 0.5, 0.0, 1.0)).size());
    }

    public void testVeryDeepTree() {
        for (int i = 0; i < 3000; i++) {
            mTree.add(new Item(0, 0));
        }

        assertEquals(3000, searchAll().size());
        assertEquals(3000, mTree.search(new Bounds(0, .1, 0, .1)).size());
        assertEquals(0, mTree.search(new Bounds(.1, 1, .1, 1)).size());
    }

    public void testManyPoints() {
        for (double i=0; i < 200; i++) {
            for (double j=0; j < 200; j++) {
                mTree.add(new Item(i/200.0, j/200.0));
            }
        }

        assertEquals(40000, searchAll().size());
        assertEquals(10000, mTree.search(new Bounds(0, .5, 0, .5)).size());
        assertEquals(2500, mTree.search(new Bounds(0, .25, 0, .25)).size());
        assertEquals(1, mTree.search(new Bounds(0,.001, 0, .001)).size());

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
