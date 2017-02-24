package uk.ac.nott.cs.g53dia.cw1;

public enum Direction {

	NONE(-1, 0, 0),
	NORTH(0, 0, 1),
	SOUTH(1, 0, -1),
	EAST(2, 1, 0),
	NORTH_EAST(4, 1, 1),
	SOUTH_EAST(6, 1, -1),
	WEST(3, -1, 0),
	NORTH_WEST(5, -1, 1),
	SOUTH_WEST(7, -1, -1);

	private final int moveCode;
	private final int x;
	private final int y;
	private static final Direction[] VALUES = Direction.values();

	Direction(int moveCode, int x, int y) {
		this.moveCode = moveCode;
		this.x = x;
		this.y = y;
	}

	public int moveCode() {
		return moveCode;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	/**
	 * Converts a ternary representation of a direction to a Direction object.
	 * @param intDir an int containing a ternary direction, where:</p>
	 *     <ul>
	 *         <li>1 - North</li>
	 *         <li>2 - South</li>
	 *         <li>3 - East</li>
	 *         <li>6 - West</li>
	 *     </ul>
	 * @return a Direction object with the corresponding direction
	 */
	public static Direction fromInt(int intDir) {
		return VALUES[intDir];
	}
}
