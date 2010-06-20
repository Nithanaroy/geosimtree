package com.infomatiq.jsi.test;

import gnu.trove.TIntProcedure;

import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

public class DeleteAllEntriesTest extends TestCase {

  Rectangle[] rects = null;
  
  class Counter implements TIntProcedure {
    public int count = 0;
    public boolean execute(int arg0) {
      count++;
      return true;
    }
  };
  
  public DeleteAllEntriesTest(String name) {
    super(name);
  }
  
  public void testDeleteAllEntries() {
    System.out.println("testDeleteAllEntries");
    
    int numRects = 500;
    
    rects = new Rectangle[numRects];
    Random r = new Random();
    r.setSeed(0);
    for (int i = 0; i < numRects; i+=1) {
      rects[i] = new Rectangle(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat());
    }
    
    run(1, 2, numRects);
    run(1, 3, numRects);
    run(2, 4, numRects);
    run(2, 5, numRects);
    run(2, 6, numRects);
  }
  
  private void run(int minNodeEntries, int maxNodeEntries, int numRects) {
    Properties p = new Properties();
    p.setProperty("MinNodeEntries", Integer.toString(minNodeEntries));
    p.setProperty("MaxNodeEntries", Integer.toString(maxNodeEntries));
    RTree rtree = (RTree) SpatialIndexFactory.newInstance("rtree.RTree", p);
    
    for (int i = 0; i <= numRects; i+=100) {
      // add some entries
      for (int j = 0; j < i; j++) {
        rtree.add(rects[j], j); 
      }
      assertTrue(rtree.checkConsistency());
      
      // now delete them all
      for (int j = 0; j < i; j++) {
        rtree.delete(rects[j], j);    
      }
      assertTrue(rtree.size() == 0);
      assertTrue(rtree.checkConsistency());
      
      // check that we can make queries on an empty rtree without error.
      Rectangle testRect = new Rectangle(1,2,3,4);
      Point testPoint = new Point(1,2);
      
      Counter counter = new Counter();
      rtree.intersects(testRect, counter);
      assertTrue(counter.count == 0);
      
      rtree.nearest(testPoint, counter, Float.MAX_VALUE);
      assertTrue(counter.count == 0);
      
      rtree.nearestN(testPoint, counter, 10, Float.MAX_VALUE);
      assertTrue(counter.count == 0);
      
      rtree.nearestNUnsorted(testPoint, counter, 10, Float.MAX_VALUE);
      assertTrue(counter.count == 0);
      
      rtree.contains(testRect, counter);
      assertTrue(counter.count == 0);
      
      rtree.nearestN_orig(testPoint, counter, 10, Float.MAX_VALUE);
      assertTrue(counter.count == 0);
      
    }
  }
}