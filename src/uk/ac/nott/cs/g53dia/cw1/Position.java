package uk.ac.nott.cs.g53dia.cw1;

import java.util.Set;

public class Position {

	public int x;
	public int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int distanceTo(Position that) {
		int dx = Math.abs(that.x - this.x);
		int dy = Math.abs(that.y - this.y);
		return Math.max(dx, dy);
	}

	public Direction directionTowards(Position target) {
		int xDiff = target.x - this.x;
		int xAbs = Math.abs(xDiff);
		int yDiff = target.y - this.y;
		int yAbs = Math.abs(yDiff);
		int intDir = 0;

		if (yDiff > 0) intDir += 1;
		if (yDiff < 0) intDir += 2;
		if (xDiff > 0) {
			if (xAbs > 2 * yAbs) intDir = 3;
			else if (yAbs < 2 * xAbs) intDir += 3;
		}
		if (xDiff < 0) {
			if (xAbs > 2 * yAbs) intDir = 6;
			else if (yAbs > 2 * xAbs) intDir += 6;
		}

		return Direction.fromInt(intDir);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Position)) return false;

		Position that = (Position) obj;
		return (this.x == that.x && this.y == that.y);
	}

	public static NearestResult getNearest(Position target, Set<Position> positions) {
		int minDist = Integer.MAX_VALUE;
		Position closest = null;
		for (Position position : positions) {
			int dist = target.distanceTo(position);
			if (dist < minDist) {
				minDist = dist;
				closest = position;
			}
		}

		return new NearestResult(closest, minDist);
	}

	public static class NearestResult {

		final Position position;
		final int distance;

		NearestResult(Position position, int distance) {
			this.position = position;
			this.distance = distance;
		}
	}
}
