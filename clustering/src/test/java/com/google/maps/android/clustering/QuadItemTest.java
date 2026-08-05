/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class QuadItemTest {

  @Test
  public void testAddRemoveUpdateClear() {
    ClusterItem item_1_5 = new TestingItem("title1", 0.1, 0.5);
    TestingItem item_2_3 = new TestingItem("title2", 0.2, 0.3);

    NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    assertTrue(algo.addItem(item_1_5));
    assertTrue(algo.addItem(item_2_3));

    assertEquals(2, algo.getItems().size());

    assertTrue(algo.removeItem(item_1_5));

    assertEquals(1, algo.getItems().size());

    assertFalse(algo.getItems().contains(item_1_5));
    assertTrue(algo.getItems().contains(item_2_3));

    // Update the item still in the algorithm
    item_2_3.setTitle("newTitle");
    assertTrue(algo.updateItem(item_2_3));

    // Try to remove the item that was already removed
    assertFalse(algo.removeItem(item_1_5));

    // Try to update the item that was already removed
    assertFalse(algo.updateItem(item_1_5));

    algo.clearItems();
    assertEquals(0, algo.getItems().size());

    // Test bulk operations
    List<ClusterItem> items = Arrays.asList(item_1_5, item_2_3);
    assertTrue(algo.addItems(items));

    // Try to bulk add items that were already added
    assertFalse(algo.addItems(items));

    assertTrue(algo.removeItems(items));

    // Try to bulk remove items that were already removed
    assertFalse(algo.removeItems(items));
  }

  /**
   * Test if insertion order into the algorithm is the same as returned item order. This matters
   * because we want repeatable clustering behavior when updating model values and re-clustering.
   */
  @Test
  public void testInsertionOrder() {
    NonHierarchicalDistanceBasedAlgorithm<ClusterItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    for (int i = 0; i < 100; i++) {
      algo.addItem(new TestingItem(Integer.toString(i), 0.0, 0.0));
    }

    assertEquals(100, algo.getItems().size());

    Collection<ClusterItem> items = algo.getItems();
    int counter = 0;
    for (ClusterItem item : items) {
      assertEquals(Integer.toString(counter), item.getTitle());
      counter++;
    }
  }

  @Test
  public void testUpdateItemAfterPositionChange() {
    NonHierarchicalDistanceBasedAlgorithm<TestingItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    TestingItem item = new TestingItem("title1", 0.0, 0.0);
    algo.addItem(item);
    assertEquals(1, algo.getItems().size());

    // Update the position of the mutable item
    item.setPosition(10.0, 10.0);

    // Call updateItem
    assertTrue("updateItem should return true after position change", algo.updateItem(item));
    assertEquals(1, algo.getItems().size());

    // Verify that the old QuadItem at (0, 0) was removed from the tree
    // and only the new position (10, 10) is indexed
    java.util.Set<? extends Cluster<TestingItem>> clusters = algo.getClusters(4.0f);
    assertEquals(1, clusters.size());
    Cluster<TestingItem> cluster = clusters.iterator().next();
    assertEquals(10.0, cluster.getPosition().latitude, 0.001);
    assertEquals(10.0, cluster.getPosition().longitude, 0.001);
  }

  @Test
  public void testRemoveItemAfterPositionChange() {
    NonHierarchicalDistanceBasedAlgorithm<TestingItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    TestingItem item = new TestingItem("title1", 0.0, 0.0);
    algo.addItem(item);
    assertEquals(1, algo.getItems().size());

    // Update the position of the mutable item
    item.setPosition(10.0, 10.0);

    // Removing the item should succeed and remove it from the tree
    assertTrue("removeItem should return true after position change", algo.removeItem(item));
    assertEquals(0, algo.getItems().size());
    assertEquals(0, algo.getClusters(4.0f).size());
  }

  @Test
  public void testUpdateItemPreventsStaleQuadTreeEntries() {
    TestAlgorithm<TestingItem> algo = new TestAlgorithm<>();

    // Add 60 filler items to force PointQuadTree to split (MAX_ELEMENTS = 50)
    for (int i = 0; i < 60; i++) {
      algo.addItem(new TestingItem("filler" + i, 10.0 + i * 0.001, 10.0 + i * 0.001));
    }

    // Add item1 in top-left quadrant
    TestingItem item1 = new TestingItem("item1", 1.0, 1.0);
    algo.addItem(item1);

    assertEquals("QuadTree should contain item1 at (1, 1)", 1, algo.getQuadTreeItemCount(1.0, 1.0, 0.001));

    // Move item1 far across quadrant boundary to (50.0, 50.0) and update
    item1.setPosition(50.0, 50.0);
    algo.updateItem(item1);

    // Without fix, old QuadItem remains at (1.0, 1.0) in mQuadTree because remove traversed the new coordinates
    assertEquals("QuadTree should NOT contain stale entry at (1, 1) after update", 0, algo.getQuadTreeItemCount(1.0, 1.0, 0.001));
    assertEquals("QuadTree should contain item1 at (50, 50)", 1, algo.getQuadTreeItemCount(50.0, 50.0, 0.001));
  }

  @Test
  public void testRemoveItemsAfterPositionChange() {
    NonHierarchicalDistanceBasedAlgorithm<TestingItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    TestingItem item1 = new TestingItem("title1", 0.0, 0.0);
    TestingItem item2 = new TestingItem("title2", 1.0, 1.0);
    algo.addItems(java.util.Arrays.asList(item1, item2));
    assertEquals(2, algo.getItems().size());

    // Update the position of both items
    item1.setPosition(10.0, 10.0);
    item2.setPosition(20.0, 20.0);

    assertTrue("removeItems should return true after position change",
        algo.removeItems(java.util.Arrays.asList(item1, item2)));
    assertEquals(0, algo.getItems().size());
    assertEquals(0, algo.getClusters(4.0f).size());
  }

  @Test
  public void testClearItemsAfterPositionChange() {
    NonHierarchicalDistanceBasedAlgorithm<TestingItem> algo =
        new NonHierarchicalDistanceBasedAlgorithm<>();
    TestingItem item1 = new TestingItem("title1", 0.0, 0.0);
    algo.addItem(item1);
    item1.setPosition(10.0, 10.0);

    algo.clearItems();
    assertEquals(0, algo.getItems().size());
    assertEquals(0, algo.getClusters(4.0f).size());
  }

  private static class TestAlgorithm<T extends ClusterItem> extends NonHierarchicalDistanceBasedAlgorithm<T> {
    private static final com.google.maps.android.projection.SphericalMercatorProjection PROJ =
        new com.google.maps.android.projection.SphericalMercatorProjection(1.0);

    public int getQuadTreeItemCount(double lat, double lng, double span) {
      com.google.maps.android.geometry.Point p = PROJ.toPoint(new LatLng(lat, lng));
      com.google.maps.android.geometry.Bounds bounds = new com.google.maps.android.geometry.Bounds(
          p.x - span, p.x + span, p.y - span, p.y + span);
      return mQuadTree.search(bounds).size();
    }
  }

  private static class TestingItem implements ClusterItem {
    private LatLng mPosition;
    private String mTitle;

    TestingItem(String title, double lat, double lng) {
      mTitle = title;
      mPosition = new LatLng(lat, lng);
    }

    TestingItem(double lat, double lng) {
      mTitle = "";
      mPosition = new LatLng(lat, lng);
    }
    public void setPosition(double lat, double lng) {
    mPosition = new LatLng(lat, lng);
    }

    @NonNull
    @Override
    public LatLng getPosition() {
      return mPosition;
    }

    @Override
    public String getTitle() {
      return mTitle;
    }

    @Override
    public String getSnippet() {
      return null;
    }

    @Nullable
    @Override
    public Float getZIndex() {
      return null;
    }

    public void setTitle(String title) {
      mTitle = title;
    }
  }
}
