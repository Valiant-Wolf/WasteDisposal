package uk.ac.nott.cs.g53dia.cw1;

public class DirectionHelper {

	/**
	 * Converts a bit-flag representation of a direction to an integer compatible with MoveAction.
	 * @param input a byte containing a bit-flag direction, where:</p>
	 *     <ul>
	 *         <li><code>0x1</code> - North</li>
	 *         <li><code>0x2</code> - South</li>
	 *         <li><code>0x4</code> - East</li>
	 *         <li><code>0x8</code> - West</li>
	 *     </ul>
	 * @return an int direction compatible with MoveAction
	 */
	static int flagsToInt(byte input) {
		switch(input) {
			case 1: return 0; // N
			case 2: return 1; // S
			case 4: return 2; // E
			case 8: return 3; // W
			case 5: return 4; // NE
			case 9: return 5; // NW
			case 6: return 6; // SE
			case 10: return 7;// SW
			default: throw new IllegalArgumentException();
		}
	}

}
