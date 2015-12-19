package main;

import java.util.ArrayList;

import gnu.trove.procedure.TIntProcedure;
import net.sf.jsi.Point;
import net.sf.jsi.Rectangle;
import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;
import net.sf.jsi.rtree.RTree2;

public class Runner {
	public static void main(String[] args) {
		// Create and initialize an rtree
		SpatialIndex si = new RTree();
		si.init(null);

		RTree2 si2 = new RTree2();
		si2.init(null);

		final Rectangle[] rects = fetchData();
		addToIndex(si, rects);

		addToIndex(si2, rects);
		si2.computeMinMax(4);
		System.out.println("\n\n");

		final ArrayList<Rectangle> result = new ArrayList<Rectangle>();
		final ArrayList<Rectangle> result2 = new ArrayList<Rectangle>();

		// 1 unit from (3, 4)
		final Rectangle q = new Rectangle(2.5f, 2.5f, 4.5f, 4.5f);
		q.features = new float[] { 1, 2, 3, 4 };
		final float threshold = 0.5f;

		bruteForce(si, rects, result, q, threshold);
		minMax(si2, rects, result2, q, threshold);

		System.out.println("Brute location + features: " + result);
		System.out.println("Opt location + features: " + result2);

	}

	private static void minMax(RTree2 si2, final Rectangle[] rects, final ArrayList<Rectangle> result, final Rectangle q,
			final float threshold) {
		si2.contains(q, threshold, new TIntProcedure() { // a procedure whose execute() method will be called with the results
			public boolean execute(int i) {
				System.out.println("OptContains Rectangle " + i + " " + rects[i]);
				result.add(rects[i]);
				return true; // return true here to continue receiving results
			}
		});
	}

	private static void bruteForce(SpatialIndex si, final Rectangle[] rects, final ArrayList<Rectangle> result, final Rectangle q,
			final float threshold) {
		si.contains(q, new TIntProcedure() { // a procedure whose execute() method will be called with the results
			public boolean execute(int i) {
				System.out.println("BruteContains Rectangle " + i + " " + rects[i]);
				if (cosineSimilarity(rects[i].features, q.features) >= threshold)
					result.add(rects[i]);
				return true; // return true here to continue receiving results
			}
		});
	}

	private static void addToIndex(SpatialIndex si, final Rectangle[] rects) {
		// Do not start index of rectangle from 0
		si.add(rects[0], 0);
		si.add(rects[1], 1);
		si.add(rects[2], 2);
		si.add(rects[3], 3);
	}

	private static Rectangle[] fetchData() {
		final Rectangle[] rects = new Rectangle[100];
		rects[0] = new Rectangle(1, 1, 1, 1);
		rects[0].features = new float[] { 1, 1, 1, 1 };

		rects[1] = new Rectangle(2, 2, 2, 2);
		rects[1].features = new float[] { 2, 2, 2, 2 };

		rects[2] = new Rectangle(3, 3, 3, 3);
		rects[2].features = new float[] { 13, 3.3f, 9.3f, 6.43f };

		rects[3] = new Rectangle(4, 4, 4, 4);
		rects[3].features = new float[] { -4, -4, -4, -4 };
		return rects;
	}

	private static double cosineSimilarity(float a[], float b[]) {
		double sim = 0;
		if (a.length != b.length)
			throw new IllegalArgumentException("Both the vectors should be of the same size");
		float numerator = 0.0f, asqsum = 0.0f, bsqsum = 0.0f;
		for (int i = 0; i < a.length; i++) {
			numerator += a[i] * b[i];
			asqsum += a[i] * a[i];
			bsqsum += b[i] * b[i];
		}
		sim = numerator / (Math.sqrt(asqsum) * Math.sqrt(bsqsum));
		return sim;
	}

	@SuppressWarnings("unused")
	private static void testIntersect(SpatialIndex si, final Rectangle[] rects, Rectangle q) {
		si.intersects(q, new TIntProcedure() { // a procedure whose execute() method will be called with the results
			public boolean execute(int i) {
				System.out.println("Intersect Rectangle " + i + " " + rects[i]);
				return true; // return true here to continue receiving results
			}
		});
	}

	@SuppressWarnings("unused")
	private static void testNearestN(SpatialIndex si, final Rectangle[] rects) {
		final Point p = new Point(3.5f, 3.5f);
		si.nearestN(p, // the point for which we want to find nearby rectangles
				new TIntProcedure() { // a procedure whose execute() method will be called with the results
					public boolean execute(int i) {
						System.out.println("Nearest Rectangle " + i + " " + rects[i] + ", distance=" + rects[i].distance(p));
						return true; // return true here to continue receiving results
					}
				}, 1, // the number of nearby rectangles to find
				Float.MAX_VALUE // Don't bother searching further than this. MAX_VALUE means search everything
		);
	}
}
