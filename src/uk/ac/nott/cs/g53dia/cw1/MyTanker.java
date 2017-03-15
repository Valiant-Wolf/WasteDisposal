package uk.ac.nott.cs.g53dia.cw1;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PendingTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PumpEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TaskEvent;
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

	/**
	 * The current Position of the Tanker relative to the starting Position
	 */
	private final Position position = new Position(0, 0);

	/**
	 * A completely pseudo-random number generator
	 */
	private final Random rand = new Random();

	/**
	 * The set of all Points which are not empty and have been previously discovered
	 */
	private final HashSet<Point> discoveredPoints = new HashSet<>();

	/**
	 * The set of all Tasks which are not complete and have been previously discovered
	 */
	private final HashSet<Position> discoveredTasks = new HashSet<>();

	/**
	 * The 2D list of all known FuelPump Positions
	 */
	private final RunnerList2<Position> fuelList = new RunnerList2<>();

	/**
	 * The 2D list of all known Well Positions
	 */
	private final RunnerList2<Position> wellList = new RunnerList2<>();

	/**
	 * The 2D list of all known Station Positions
	 */
	private final RunnerList2<Position> stationList = new RunnerList2<>();

	/**
	 * The 2D list of all known, incomplete Tasks Positions, with references to respective Tasks
	 */
	private final RunnerList2<TaskPosition> stationTaskList = new RunnerList2<>();

	/**
	 * The agent's current Plan, from which it will retrieve the next Action
	 */
	private TankerPlan currentPlan = new WanderPlan(this, new LinkedList<>());

	/**
	 * The list of Events that signify changes or ongoing states in the environment in the current timestep
	 */
	private LinkedList<TankerEvent> events = new LinkedList<>();

	/**
	 * The list of FuelPumps that were discovered this timestep
	 */
	private LinkedList<RunnerList2.Entry<Position>> newFuel = new LinkedList<>();

	/**
	 * The list of Wells that were discovered this timestep
	 */
	private LinkedList<RunnerList2.Entry<Position>> newWells = new LinkedList<>();

	/**
	 * The list of Stations that were discovered this timestep
	 */
	private LinkedList<RunnerList2.Entry<Position>> newStations = new LinkedList<>();

	/**
	 * The list of Tasks that were discovered this timestep
	 */
	private LinkedList<RunnerList2.Entry<TaskPosition>> newTasks = new LinkedList<>();

	/**
	 * Encapsulates the agent's architecture and provides the entry and exit points for each timestep
	 * @param view a 2D array of Cells which the Tanker can currently see
	 * @param timeStep the number of timesteps since the beginning of the simulation
	 * @return the next Action the agent wishes to perform this timestep
	 */
	@Override
	public Action senseAndAct(Cell[][] view, long timeStep) {
		// Process the Tanker's view and create Events for new Cells and Tasks
		discoverCells(view);

		// Check that the Tanker is within ample fuel range of a Pump, and create a
		// CriticalFuelEvent if not
		Position.NearestResult nearestFuel = Position.getNearest(position, fuelList.getAdjacent());
		if (getFuelLevel() < nearestFuel.distance + 2) events.add(new CriticalFuelEvent(getFuelLevel()));

		// Create a PendingTaskEvent is there is a discovered, incomplete Task
		if (!discoveredTasks.isEmpty()) events.add(new PendingTaskEvent());

		// Check that the current Plan is not complete or made invalid by the Events. Select a new
		// Plan if this is the case
		if (currentPlan.isComplete() || !currentPlan.checkValidity(events)) currentPlan = deliberateNewPlan(events);

		// Get the next Action from the current Plan
		Action action = currentPlan.getNextAction();

		// Transform the Action from the current Plan to one which is accepted by the environment if
		// necessary. Perform relevant bookkeeping if this moves the Tanker or changes a Task
		if (action instanceof WanderAction) {
			action = wanderAway(nearestFuel.position);
		} else if (action instanceof MoveToPositionAction) {
			action = moveTowardsPosition(((MoveToPositionAction) action).getPosition());
		} else if (action instanceof LoadWasteAction) {
			discoveredTasks.remove(position);
			boolean success = stationTaskList.removeAt(position.x, position.y);
			if (!success) throw new IllegalStateException();
		}

		// Return an environment-friendly Action to be executed
		return action;
	}

	/**
	 * Extracts and condenses useful information about the Tanker's view.
	 * This method iterates over the array of Cells comprising the Tanker's view and commits any new
	 * Cells to the agent's internal representation of the environment. For each new addition, a
	 * corresponding Event is created and in some cases relevant bookkeeping is performed
	 * @param view the 2D array of Cells comprising the Tanker's current view
	 */
	private void discoverCells(Cell[][] view) {
		// Clear the lists of new Cells and Tasks from last timestep
		events.clear();
		newFuel.clear();
		newWells.clear();
		newStations.clear();
		newTasks.clear();

		// Iterate over each Cell in the Tanker's view
		for (int ix = 0; ix < view.length; ix++) {
			for (int iy = 0; iy < view[0].length; iy++) {
				Cell cell = view[ix][iy];

				boolean discovered = false;

				// Ignore empty Cells and Stations which have been discovered but have no Task
				if (cell instanceof EmptyCell) continue;
				if (discoveredPoints.contains(cell.getPoint())) {
					if (!(cell instanceof Station)) continue;
					if (((Station) cell).getTask() == null) continue;
					discovered = true;
				}

				// Add the current Cell to the set of discovered cells
				discoveredPoints.add(cell.getPoint());

				// Infer the Cell's Position from its position in the Tanker's view
				Position cellPos = new Position(
						position.x + (ix - VIEW_RANGE),
						position.y - (iy - VIEW_RANGE) // this is why +y is usually down and not up
				);

				// Create a new entry in advance
				RunnerList2.Entry<Position> newEntry = new RunnerList2.Entry<>(cellPos.x, cellPos.y, cellPos);

				// Add Pumps to the map and create a new PumpEvent
				if (cell instanceof FuelPump) {
					events.add(new PumpEvent(cellPos));
					newFuel.add(newEntry);

				// Add Wells to the map and create a new WellEvent
				} else if (cell instanceof Well) {
					events.add(new WellEvent(cellPos));
					newWells.add(newEntry);

				// Add Stations to the map. If there is a Task, add it to the map and create a new
				// TaskEvent
				} else if (cell instanceof Station) {
					if (!discovered) newStations.add(newEntry);

					Task task = ((Station) cell).getTask();
					if (task != null && !discoveredTasks.contains(cellPos)) {
						discoveredTasks.add(cellPos);
						events.add(new TaskEvent(cellPos, task.getWasteRemaining()));
						newTasks.add(new RunnerList2.Entry<>(cellPos.x, cellPos.y, new TaskPosition(cellPos, task)));
					}
				}
			}
		}

		// Add all Cells in bulk to their respective lists
		fuelList.addAll(newFuel);
		wellList.addAll(newWells);
		stationList.addAll(newStations);
		stationTaskList.addAll(newTasks);
	}

	/**
	 * Generates a set of new Plans which the agent could follow and selects one to be the current
	 * plan. The Plans are considered in order and are checked for validity, and the first valid
	 * Plan is selected
	 * @param events the Events of the current timestep, to be used in Plan preconditions
	 * @return a valid new Plan to be used as the current Plan
	 */
	private TankerPlan deliberateNewPlan(List<TankerEvent> events) {
		List<TankerPlan> newPlans = new LinkedList<>();

		// Construct one of each new Plan
		newPlans.add(new GatherPlan(this, events));
		newPlans.add(new DepositPlan(this, events));
		newPlans.add(new WanderPlan(this, events));
		newPlans.add(new RefuelPlan(this, events));

		// Iterate through all of the new Plans in order and return the first which is valid
		for (TankerPlan plan : newPlans) {
			if (!plan.isComplete()) return plan;
		}

		// If there are no valid plans, something has gone wrong
		throw new IllegalStateException("No valid plans!");
	}

	/**
	 * Gets a specific 2D list of Cells depending on the type parameter
	 * @param cellType the type of Cells to return. Should be one of CELL_PUMP, CELL_STATION,
	 *                    CELL_TASK or CELL_WELL
	 * @return the corresponding list of Cells
	 */
	private RunnerList2<? extends Position> getCellList(int cellType) {
		switch (cellType) {
			case CELL_PUMP:
				return fuelList;
			case CELL_STATION:
				return stationList;
			case CELL_TASK:
				return stationTaskList;
			case CELL_WELL:
				return wellList;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Retrieves all Cells of a given type within a given range of a given position
	 * @param cellType the type of Cells to return. Should be one of CELL_PUMP, CELL_STATION,
	 *                    CELL_TASK or CELL_WELL
	 * @param position the Position around which the Cells should be considered
	 * @param radius the maximum range from position at which cells should be considered
	 * @return all Cells of cellType type within radius of position
	 */
	public Set<? extends Position> getCellsInRange(int cellType, Position position, int radius) {
		return getCellList(cellType).getAllInRange(position.x, position.y, radius);
	}

	/**
	 * Retrieves all Cells of a given type which are adjacent (closest to along one axis) to the Tanker's current position
	 * @param cellType the type of Cells to return. Should be one of CELL_PUMP, CELL_STATION,
	 *                    CELL_TASK or CELL_WELL
	 * @return all Cells of cellType type which are adjacent to the Tanker's position
	 */
	public Set<? extends Position> getCellsAdjacent(int cellType) {
		return getCellList(cellType).getAdjacent();
	}

	/**
	 * Gives the current Position of the Tanker
	 * @return the current Position of the Tanker
	 */
	public Position getAbsolutePosition() {
		return this.position;
	}

	/**
	 * Updates the position of all the internal lists
	 */
	private void moveAllLists() {
		fuelList.moveTo(position.x, position.y);
		wellList.moveTo(position.x, position.y);
		stationList.moveTo(position.x, position.y);
		stationTaskList.moveTo(position.x, position.y);
	}

	/**
	 * Creates a MoveAction which will move the Tanker one cell toward the given Position. The
	 * Tanker's Position will be updated when this method is called, NOT after the action is
	 * executed
	 * @param target the Position to move towards
	 * @return a MoveAction which will move the Tanker toward the target
	 */
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

		moveAllLists();
		return new MoveAction(Direction.fromInt(intDir).moveCode());
	}

	/**
	 * Creates a MoveAction which moves the Tanker in a specific direction. The Tanker's Position
	 * will be updated when this method is called, NOT after the action is executed
	 * @param direction the Direction in which the Tanker should move
	 * @return a MoveAction which will move the Tanker in the given cirection
	 */
	private MoveAction moveInDirection(Direction direction) {
		position.x += direction.x();
		position.y += direction.y();
		moveAllLists();
		return new MoveAction(direction.moveCode());
	}

	/**
	 * Creates a MoveAction which moves the Tanker randomly. The Tanker's Position will be updated
	 * when this method is called, NOT after the action is executed
	 * @return a MoveAction which moves the Tanker randomly
	 */
	private MoveAction wanderRandomly() {
		int intDir = rand.nextInt(8) + 1;
		Direction direction = Direction.fromInt(intDir);
		return moveInDirection(direction);
	}

	/**
	 * Creates a MoveAction which moves the Tanker randomly, but never towards the given target
	 * Position. If the Tanker is currently at the given target Position, this method functions
	 * identically to wanderRandomly(). The Tanker's Position will be updated when this method is
	 * called, NOT after the action is executed
	 * @param target the Position the Tanker will not move directly towards
	 * @return a MoveAction which moves the Tanker randomly away from the target
	 */
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

	/**
	 * Creates an Action which will either move the Tanker towards the target Position, or refuel
	 * the Tanker if it is at the Position already. The Tanker's Position will be updated when this
	 * method is called, NOT after the action is executed
	 * @param target the Position to move towards or refuel at
	 * @return a MoveAction or RefuelAction which will move toward or refuel at the given Position
	 */
	private Action refuelAt(Position target) {
		if (position.equals(target)) return new RefuelAction();

		return moveTowardsPosition(target);
	}

}
