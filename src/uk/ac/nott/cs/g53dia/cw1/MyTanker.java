package uk.ac.nott.cs.g53dia.cw1;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Well;

public class MyTanker extends Tanker {

	private final Position position = new Position(0, 0);
	private final HashSet<Point> discoveredPoints = new HashSet<>();
	private final RunnerList2<Position> fuelList = new RunnerList2<>();
	private final RunnerList2<Position> wellList = new RunnerList2<>();
	private final RunnerList2<Position> stationList = new RunnerList2<>();
	private final Random rand = new Random();

	private LinkedList<RunnerList2.Entry<Position>> newFuel = new LinkedList<>();
	private LinkedList<RunnerList2.Entry<Position>> newWells = new LinkedList<>();
	private LinkedList<RunnerList2.Entry<Position>> newStations = new LinkedList<>();

	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {
		Action action;

		discoverCells(view);

		Position.NearestResult nearestFuel = Position.getNearest(position, fuelList.getAdjacent());
		if (getFuelLevel() < nearestFuel.distance + 2) {
			action = refuelAt(nearestFuel.position);
		} else {
			action = wanderAway(nearestFuel.position);
		}
		return action;
	}

	private void discoverCells(Cell[][] view) {
		newFuel.clear();
		newWells.clear();
		newStations.clear();

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

				RunnerList2.Entry<Position> newEntry = new RunnerList2.Entry<>(cellPos.x, cellPos.y, cellPos);

				if (cell instanceof FuelPump) newFuel.add(newEntry);
				else if (cell instanceof Well) newWells.add(newEntry);
				else if (cell instanceof Station) newStations.add(newEntry);
			}
		}
		fuelList.addAll(newFuel);
		wellList.addAll(newWells);
		stationList.addAll(newStations);
	}

	public Set<Position> getCellsInRange(CellType cellType, Position position, int radius) {
		RunnerList2<Position> list;
		switch (cellType) {
			case PUMP:
				list = fuelList;
				break;
			case STATION:
				list = stationList;
				break;
			case WELL:
				list = wellList;
				break;
			default:
				throw new IllegalArgumentException();
		}

		return list.getAllInRange(position.x, position.y, radius);
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

	public enum CellType {
		PUMP,
		STATION,
		WELL
	}

}
