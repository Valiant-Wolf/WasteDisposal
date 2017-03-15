package uk.ac.nott.cs.g53dia.cw1;

import java.util.LinkedList;
import java.util.List;

import uk.ac.nott.cs.g53dia.library.Tanker;

public class PumpPosition extends Position {

	private static final int[][] searchPositions = new int[][] {
			{ Tanker.VIEW_RANGE, Tanker.VIEW_RANGE },
			{ -Tanker.VIEW_RANGE, Tanker.VIEW_RANGE },
			{ -Tanker.VIEW_RANGE, -Tanker.VIEW_RANGE },
			{ Tanker.VIEW_RANGE, -Tanker.VIEW_RANGE }
	};

	private int searchProgress = 0;

	public PumpPosition(Position position) {
		super(position.x, position.y);
	}

	/**
	 * Returns a list of unsearched Positions around this Pump
	 * @return the remaining search Positions
	 */
	public List<Position> getRemainingSearchPositions() {
		List<Position> result = new LinkedList<>();

		for (int i = searchProgress; i < 4; i++) {
			result.add(new Position(x + searchPositions[i][0], y + searchPositions[i][1]));
		}

		return result;
	}

	/**
	 * Increment the search progress of this Pump
	 */
	public void positionSearched() {
		searchProgress++;
	}

	/**
	 * Checks if this Pump is completely searched
	 * @return true if all Positions around this Pump have been visited
	 */
	public boolean isCompletelySearched() {
		return searchProgress >= 4;
	}

}
