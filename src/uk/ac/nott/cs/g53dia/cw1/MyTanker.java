package uk.ac.nott.cs.g53dia.cw1;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Well;

class MyTanker extends Tanker{

	private final Position position = new Position(0, 0);
	private final HashSet<Point> discoveredPoints = new HashSet<>();
	private final DualRunnerList<Position> fuelList = new DualRunnerList<>();
	private final DualRunnerList<Position> wellList = new DualRunnerList<>();
	private final Random rand = new Random();

	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {
		Action action;

		discoverCells(view);

		NearestResult nearestFuel = distanceToClosestAt(position, fuelList);
		if (getFuelLevel() < nearestFuel.distance + 2) {
			action = refuelAt(nearestFuel.position);
		} else {
			action = wanderAway(nearestFuel.position);
		}
		return action;
	}

	private void discoverCells(Cell[][] view) {
		LinkedList<DualRunnerList.DualEntry<Position>> newFuel = new LinkedList<>();
		LinkedList<DualRunnerList.DualEntry<Position>> newWells = new LinkedList<>();
		for (int ix = 0; ix < view.length; ix++) {
			for (int iy = 0; iy < view[0].length; iy++) {
				Cell cell = view[ix][iy];

				if (cell instanceof EmptyCell) continue;
				if (discoveredPoints.contains(cell.getPoint())) continue;

				discoveredPoints.add(cell.getPoint());

				Position cellPos = new Position(
						position.x + (ix - VIEW_RANGE),
						position.y - (iy - VIEW_RANGE) // this is why +y is usually down and not up
				);

				DualRunnerList.DualEntry<Position> newEntry = new DualRunnerList.DualEntry<>(cellPos.x, cellPos.y, cellPos);

				if (cell instanceof FuelPump) newFuel.add(newEntry);
				if (cell instanceof Well) newWells.add(newEntry);
			}
		}
		fuelList.addAll(newFuel);
		wellList.addAll(newWells);
	}

	private NearestResult distanceToClosestAt(Position probe, DualRunnerList<Position> sites) {
		sites.moveTo(probe.x, probe.y);

		int minDist = Integer.MAX_VALUE;
		Position closest = null;
		for (Position site : sites.getAdjacent()) {
			int dist = probe.distanceTo(site);
			if (dist < minDist) {
				minDist = dist;
				closest = site;
			}
		}

		sites.moveTo(position.x, position.y);
		return new NearestResult(closest, minDist);
	}

	private MoveAction moveTowardsPosition(Position target) {
		int intDir = 0;

		if (position.y < target.y) {
			position.y++;
			intDir += 1;
		}
		if (position.y > target.y) {
			position.y--;
			intDir += 2;
		}
		if (position.x < target.x) {
			position.x++;
			intDir += 3;
		}
		if (position.x > target.x) {
			position.x--;
			intDir += 6;
		}

		fuelList.moveTo(position.x, position.y);
		return new MoveAction(Direction.fromInt(intDir).moveCode());
	}

	private MoveAction moveInDirection(Direction direction) {
		position.x += direction.x();
		position.y += direction.y();
		return new MoveAction(direction.moveCode());
	}

	private MoveAction wanderRandomly() {
		int intDir = rand.nextInt(8) + 1;
		Direction direction = Direction.fromInt(intDir);
		return moveInDirection(direction);
	}

	private MoveAction wanderAway(Position target) {
		Direction targetDirection = position.directionTowards(target);
		int intDir;

		if (targetDirection != Direction.NONE) {
			intDir = rand.nextInt(7) + 1;
			if (intDir >= targetDirection.ordinal()) intDir++;
		} else {
			intDir = rand.nextInt(8) + 1;
		}

		Direction direction = Direction.fromInt(intDir);
		return moveInDirection(direction);
	}

	private Action refuelAt(Position target) {
		if (position.equals(target)) return new RefuelAction();

		return moveTowardsPosition(target);
	}

	private class Position {

		int x;
		int y;

		Position(int x, int y) {
			this.x = x;
			this.y = y;
		}

		int distanceTo(Position that) {
			int dx = Math.abs(that.x - this.x);
			int dy = Math.abs(that.y - this.y);
			return Math.max(dx, dy);
		}

		Direction directionTowards(Position target) {
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
	}

	private class NearestResult {

		final Position position;
		final int distance;

		NearestResult(Position position, int distance) {
			this.position = position;
			this.distance = distance;
		}
	}

}
