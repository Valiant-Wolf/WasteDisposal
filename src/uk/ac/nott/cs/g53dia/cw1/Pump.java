package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collection;
import java.util.PriorityQueue;

import uk.ac.nott.cs.g53dia.library.Tanker;

public class Pump {

	private static final int RADIUS = 2 * Tanker.VIEW_RANGE;
	private static final int EXPECTED_WELLS = (int) (0.0015 * Math.pow(2 * RADIUS + 1, 2));
	private static final Position[] searchPositions = new Position[] {
			new Position(Tanker.VIEW_RANGE, Tanker.VIEW_RANGE),
			new Position(-Tanker.VIEW_RANGE, Tanker.VIEW_RANGE),
			new Position(-Tanker.VIEW_RANGE, -Tanker.VIEW_RANGE),
			new Position(Tanker.VIEW_RANGE, -Tanker.VIEW_RANGE)
	};

	public final Position position;
	public int rating = 0;
	public int searchProgress = 0;

	private int stationScore = 0;
	private PriorityQueue<Integer> wells = new PriorityQueue<>(EXPECTED_WELLS);

	public Pump(Position position) {
		this.position = position;
	}

	public void addStations(Collection<Position> newStations) {
		for (Position station : newStations) {
			stationScore += distanceScore(station);
		}
	}

	public void addWells(Collection<Position> newWells) {
		for (Position well : newWells) {
			wells.add(position.distanceTo(well));
		}
	}

	private int distanceScore(Position pos) {
		return (RADIUS - position.distanceTo(pos)) + 1;
	}

	public int calculateRating() {
		int newRating = stationScore;

		if (wells.isEmpty()) newRating = 0;
		else newRating *= (RADIUS - wells.peek()) + 1;

		rating = newRating;
		return newRating;
	}
}
