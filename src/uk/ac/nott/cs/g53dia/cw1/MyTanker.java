package uk.ac.nott.cs.g53dia.cw1;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PendingTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PumpEvent;
import uk.ac.nott.cs.g53dia.cw1.events.StationTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.cw1.events.WellEvent;
import uk.ac.nott.cs.g53dia.cw1.plans.DepositPlan;
import uk.ac.nott.cs.g53dia.cw1.plans.GatherPlan;
import uk.ac.nott.cs.g53dia.cw1.plans.RefuelPlan;
import uk.ac.nott.cs.g53dia.cw1.plans.TankerPlan;
import uk.ac.nott.cs.g53dia.cw1.plans.WanderPlan;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.EmptyCell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.Point;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;
import uk.ac.nott.cs.g53dia.library.Task;
import uk.ac.nott.cs.g53dia.library.Well;

public class MyTanker extends Tanker {

	public static final int CELL_PUMP = 1;
	public static final int CELL_STATION = 2;
	public static final int CELL_WELL = 3;
	public static final int CELL_TASK = 4;


	private final Position position = new Position(0, 0);
	private final Random rand = new Random();

	//region MAPS
	private final HashSet<Point> discoveredPoints = new HashSet<>();
	private final HashSet<Position> discoveredTasks = new HashSet<>();
	private final RunnerList2<Position> fuelList = new RunnerList2<>();
	private final RunnerList2<Position> wellList = new RunnerList2<>();
	private final RunnerList2<Position> stationList = new RunnerList2<>();
	private final RunnerList2<TaskPosition> stationTaskList = new RunnerList2<>();
	//endregion MAPS

	private TankerPlan currentPlan = new WanderPlan(this, new LinkedList<>());
	private LinkedList<TankerEvent> events = new LinkedList<>();

	private LinkedList<RunnerList2.Entry<Position>> newFuel = new LinkedList<>();
	private LinkedList<RunnerList2.Entry<Position>> newWells = new LinkedList<>();
	private LinkedList<RunnerList2.Entry<Position>> newStations = new LinkedList<>();
	private LinkedList<RunnerList2.Entry<TaskPosition>> newTasks = new LinkedList<>();

	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {

		discoverCells(view);

		Position.NearestResult nearestFuel = Position.getNearest(position, fuelList.getAdjacent());
		if (getFuelLevel() < nearestFuel.distance + 2) events.add(new CriticalFuelEvent(getFuelLevel()));

		if (!discoveredTasks.isEmpty()) events.add(new PendingTaskEvent());

		if (currentPlan.isComplete() || !currentPlan.checkValidity(events)) currentPlan = deliberateNewPlan(events);

		Action action = currentPlan.getNextAction();

		if (action instanceof WanderAction) {
			action = wanderAway(nearestFuel.position);
		} else if (action instanceof MoveToPositionAction) {
			action = moveTowardsPosition(((MoveToPositionAction) action).getPosition());
		} else if (action instanceof LoadWasteAction) {
			discoveredTasks.remove(position);
			boolean success = stationTaskList.removeAt(position.x, position.y);
			if (!success) throw new IllegalStateException();
		}

		return action;
	}

	private void discoverCells(Cell[][] view) {
		events.clear();
		newFuel.clear();
		newWells.clear();
		newStations.clear();
		newTasks.clear();

		for (int ix = 0; ix < view.length; ix++) {
			for (int iy = 0; iy < view[0].length; iy++) {
				Cell cell = view[ix][iy];

				boolean discovered = false;

				if (cell instanceof EmptyCell) continue;
				if (discoveredPoints.contains(cell.getPoint())) {
					if (!(cell instanceof Station)) continue;
					if (((Station) cell).getTask() == null) continue;
					discovered = true;
				}

				discoveredPoints.add(cell.getPoint());

				Position cellPos = new Position(
						position.x + (ix - VIEW_RANGE),
						position.y - (iy - VIEW_RANGE) // this is why +y is usually down and not up
				);

				RunnerList2.Entry<Position> newEntry = new RunnerList2.Entry<>(cellPos.x, cellPos.y, cellPos);

				if (cell instanceof FuelPump) {
					events.add(new PumpEvent(cellPos));
					newFuel.add(newEntry);

				} else if (cell instanceof Well) {
					events.add(new WellEvent(cellPos));
					newWells.add(newEntry);

				} else if (cell instanceof Station) {
					if (!discovered) newStations.add(newEntry);

					Task task = ((Station) cell).getTask();
					if (task != null && !discoveredTasks.contains(cellPos)) {
						discoveredTasks.add(cellPos);
						events.add(new StationTaskEvent(cellPos, task.getWasteRemaining()));
						newTasks.add(new RunnerList2.Entry<TaskPosition>(cellPos.x, cellPos.y, new TaskPosition(cellPos, task)));
					}
				}
			}
		}

		fuelList.addAll(newFuel);
		wellList.addAll(newWells);
		stationList.addAll(newStations);
		stationTaskList.addAll(newTasks);
	}

	private TankerPlan deliberateNewPlan(List<TankerEvent> events) {
		List<TankerPlan> newPlans = new LinkedList<>();

		newPlans.add(new GatherPlan(this, events));
		newPlans.add(new DepositPlan(this, events));
		newPlans.add(new WanderPlan(this, events));
		newPlans.add(new RefuelPlan(this, events));

		for (TankerPlan plan : newPlans) {
			if (!plan.isComplete()) return plan;
		}

		throw new IllegalStateException("No valid plans!");
	}

	public Set<? extends Position> getCellsInRange(int cellType, Position position, int radius) {
		RunnerList2<? extends Position> list;
		switch (cellType) {
			case CELL_PUMP:
				list = fuelList;
				break;
			case CELL_STATION:
				list = stationList;
				break;
			case CELL_TASK:
				list = stationTaskList;
				break;
			case CELL_WELL:
				list = wellList;
				break;
			default:
				throw new IllegalArgumentException();
		}

		return list.getAllInRange(position.x, position.y, radius);
	}

	public Position getAbsolutePosition() {
		return this.position;
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

}
