package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import gnu.trove.procedure.TIntProcedure;
import net.sf.jsi.Point;
import net.sf.jsi.Rectangle;
import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;
import net.sf.jsi.rtree.RTree2;

public class Runner {
	public static void main(String[] args) throws IOException {
		// PrintWriter writer = new PrintWriter("/Volumes/350GB/Projects/RTree_MinMax/rtree_java/data/stats.txt", "UTF-8");
		int runSizes[] = { 50, 100, 500, 1000, 10000, 50000, 10000, 500000, 1000000 };
		for (int i = 0; i < runSizes.length; i++) {
			run(runSizes[i]);
			// run(runSizes[i], writer);
			// break;
		}
		// writer.close();
	}

	private static void run(int lines) throws IOException {
		System.out.format("Lines: %d\n", lines);
		// Create and initialize an rtree
		SpatialIndex si = new RTree();
		si.init(null);

		RTree2 si2 = new RTree2();
		si2.init(null);

		long preprocessing, brutetime, opttime;

		// final Rectangle[] rects = initForStaticData(si, si2);
		final Rectangle[] rects = initForDynamicData(si, si2, "/Volumes/350GB/Projects/RTree_MinMax/rtree_java/data/sample.txt", lines);
		int featuresCount = 4;
		long startTime = System.currentTimeMillis();
		si2.computeMinMax(featuresCount);
		long stopTime = System.currentTimeMillis();
		preprocessing = stopTime - startTime;

		final ArrayList<Integer> result = new ArrayList<>();
		final ArrayList<Integer> result2 = new ArrayList<>();

		// final Rectangle q = getQueryForStaticData();
		final Rectangle q = getQueryForDynamicData();
		final float threshold = 0.5f;

		startTime = System.currentTimeMillis();
		bruteForce(si, rects, result, q, threshold);
		stopTime = System.currentTimeMillis();
		brutetime = stopTime - startTime;

		startTime = System.currentTimeMillis();
		minMax(si2, rects, result2, q, threshold);
		stopTime = System.currentTimeMillis();
		opttime = stopTime - startTime;

		Collections.sort(result);
		Collections.sort(result2);
		System.out.println("Bru location + features\t: " + result);
		System.out.println("Opt location + features\t: " + result2);
		System.out.println("Brute Force time: " + brutetime);
		System.out.println("Pre-processing time: " + preprocessing);
		System.out.println("Index time: " + opttime);
		System.out.println("\n\n");
	}

	private static Rectangle getQueryForDynamicData() {
		final Rectangle q = new Rectangle(-180f, -90f, 180f, 90f);
		q.features = new float[] { 1, 2, 3, 4 };
		return q;
	}

	private static Rectangle getQueryForStaticData() {
		// 1 unit from (3, 4)
		final Rectangle q = new Rectangle(2.5f, 2.5f, 4.5f, 4.5f);
		q.features = new float[] { 1, 2, 3, 4 };
		return q;
	}

	private static Rectangle[] initForStaticData(SpatialIndex si, RTree2 si2) {
		final Rectangle[] rects = fetchData();
		addToIndex(si, rects, 4);

		addToIndex(si2, rects, 4);
		return rects;
	}

	private static Rectangle[] initForDynamicData(SpatialIndex si, RTree2 si2, String filename, int maxLinesToRead) throws IOException {
		final Rectangle[] rects = fetchData(filename, maxLinesToRead);
		addToIndex(si, rects, rects.length);

		addToIndex(si2, rects, rects.length);
		return rects;
	}

	private static void minMax(RTree2 si2, final Rectangle[] rects, final ArrayList<Integer> result, final Rectangle q,
			final float threshold) {
		System.out.print("OptContains: ");
		final Counter c = new Counter();
		si2.contains(q, threshold, new TIntProcedure() { // a procedure whose execute() method will be called with the results
			public boolean execute(int i) {
				System.out.format("%d,", i);
				// result.add(rects[i]);
				result.add(i);
				c.count++;
				return true; // return true here to continue receiving results
			}
		});
		System.out.println("\nTotal: " + c.count);
	}

	private static void bruteForce(SpatialIndex si, final Rectangle[] rects, final ArrayList<Integer> result, final Rectangle q,
			final float threshold) {
		System.out.print("BruteContains: ");
		final Counter c = new Counter();
		si.contains(q, new TIntProcedure() { // a procedure whose execute() method will be called with the results
			public boolean execute(int i) {
				System.out.format("%d,", i);
				c.count++;
				if (cosineSimilarity(rects[i].features, q.features) >= threshold) {
					// result.add(rects[i]);
					result.add(i);
				}
				return true; // return true here to continue receiving results
			}
		});
		System.out.println("\nTotal: " + c.count);
	}

	private static void addToIndex(SpatialIndex si, final Rectangle[] rects, int count) {
		for (int i = 0; i < count; i++) {
			si.add(rects[i], i);
		}
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

	private static Rectangle[] fetchData(String filename, int maxLinesToRead) throws IOException {

		final ArrayList<Rectangle> rectsList = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		int linesRead = 0;
		while (line != null && linesRead < maxLinesToRead) {
			if (line.trim().length() == 0)
				continue;
			String coords_features[] = line.split(" ");
			String coords[] = coords_features[0].split(",");
			String features[] = coords_features[1].split(",");
			Rectangle r = new Rectangle(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]), Float.parseFloat(coords[0]),
					Float.parseFloat(coords[1]));
			r.features = new float[features.length];
			for (int i = 0; i < features.length; i++) {
				r.features[i] = Float.parseFloat(features[i]);
			}
			rectsList.add(r);
			line = br.readLine();
			linesRead++;
		}

		br.close();
		Rectangle rects[] = new Rectangle[rectsList.size()];
		return rectsList.toArray(rects);
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

class Counter {
	int count;

	public Counter() {
		count = 0;
	}
}
