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

	public List<Position> getRemainingSearchPositions() {
		List<Position> result = new LinkedList<>();

		for (int i = searchProgress; i < 4; i++) {
			result.add(new Position(x + searchPositions[i][0], y + searchPositions[i][1]));
		}

		return result;
	}

	public void positionSearched() {
		searchProgress++;
	}

	public boolean isCompletelySearched() {
		return searchProgress >= 4;
	}

}
