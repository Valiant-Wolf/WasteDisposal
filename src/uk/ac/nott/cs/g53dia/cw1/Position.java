package uk.ac.nott.cs.g53dia.cw1;

import java.util.Set;

/**
 * Represents an offset from the environment's origin (the Tanker's initial position)
 */
public class Position {

	/**
	 * The X offset of this Position
	 */
	public int x;

	/**
	 * The Y offset of this Position
	 */
	public int y;

	/**
	 * Creates a new instance with the given x and y offsets
	 * @param x the x offset
	 * @param y the y offset
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Calculates the Chebyshev distance between this and another Position
	 * @param that the Position to find the distance to
	 * @return the Chebyshev distance between the two Positions
	 */
	public int distanceTo(Position that) {
		int dx = Math.abs(that.x - this.x);
		int dy = Math.abs(that.y - this.y);
		return Math.max(dx, dy);
	}

	/**
	 * Calculates the direction of the line between this and another Position
	 * @param target the Position to find the direction to
	 * @return the Direction of the other Position relative to this Position
	 */
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

	/**
	 * Returns whether this and another Position represent the same point
	 * @param obj the other Position
	 * @return true if this and another Position represent the same point, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Position)) return false;

		Position that = (Position) obj;
		return (this.x == that.x && this.y == that.y);
	}

	/**
	 * Creates a hashcode consistent with equals(). Two equivalent Positions will produce the same
	 * hashcode
	 * @return an integer hashcode for this Position
	 */
	@Override
	public int hashCode() {
		return (((x & 0xff) << 16) + (y & 0xff));
	}

	/**
	 * Finds the nearest Position and its distance from a target Position from a set of Positions
	 * @param target the Position to measure from
	 * @param positions the set of Positions from which the result should be chosen
	 * @return the nearest Position and its distance
	 */
	public static NearestResult getNearest(Position target, Set<? extends Position> positions) {
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

	/**
	 * Encapsulates a Position and a distance
	 */
	public static class NearestResult {

		public final Position position;
		public final int distance;

		NearestResult(Position position, int distance) {
			this.position = position;
			this.distance = distance;
		}
	}
}
