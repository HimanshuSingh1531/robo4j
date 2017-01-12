package com.robo4j.math.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.robo4j.math.geometry.Point2D;
import com.robo4j.math.geometry.ScanResult2D;

public class ScanResultImpl implements ScanResult2D {
	private static final PointComparator POINT_COMPARATOR = new PointComparator();
	private static int SCANCOUNTER;
	
	private final List<Point2D> points;

	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	private int scanID;
	
	private Point2D farthestPoint;
	private Point2D closestPoint;

	private final ScanPointEvent scanPointEvent = new ScanPointEvent();

	public ScanResultImpl() {
		this(70);
	}

	public ScanResultImpl(int size) {
		scanID = SCANCOUNTER++;
		points = new ArrayList<Point2D>(size);
	}
	
	public double getMaxX() {
		return maxX;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinY() {
		return minY;
	}

	public int getScanID() {
		return scanID;
	}
	
	public void addPoint(Point2D p) {
		if (p.getRange() < 0.05) {
			return;
		}
		points.add(p);
		emitEvent(p);
		updateBoundaries(p);
	}

	@SuppressWarnings("deprecation")
	private void emitEvent(Point2D p) {
		scanPointEvent.reset();
		scanPointEvent.setPoint(p);
		scanPointEvent.setScanID(scanID);
		scanPointEvent.commit();
	}

	private void updateBoundaries(Point2D p) {
		maxX = Math.max(maxX, p.getX());
		maxY = Math.max(maxY, p.getY());
		minX = Math.min(minX, p.getX());
		minY = Math.min(minY, p.getY());
		if (closestPoint == null) {
			closestPoint = p;
		} else {
			if (p.closer(closestPoint)) {
				closestPoint = p;
			}
		}
		if (farthestPoint == null) {
			farthestPoint = p;
		} else {
			if (p.farther(farthestPoint)) {
				farthestPoint = p;
			}
		}
	}

	public List<Point2D> getPoints() {
		return points;
	}

	public void addPoint(float range, float angle) {
		addPoint(new Point2D(range, angle));
	}

	public void addResult(ScanResultImpl result) {
		for (Point2D p : result.getPoints()) {
			addPoint(p);
		}
	}

	public Point2D getNearestPoint() {
		return closestPoint;
	}

	public Point2D getFarthestPoint() {
		return farthestPoint;
	}
	
	public void sort() {
		Collections.sort(points, POINT_COMPARATOR);
	}

	private static class PointComparator implements Comparator<Point2D> {
		@Override
		public int compare(Point2D o1, Point2D o2) {
			return Float.compare(o1.getAngle(), o2.getAngle());
		}
	}
}
