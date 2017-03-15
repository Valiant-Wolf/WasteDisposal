package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.Position;
import uk.ac.nott.cs.g53dia.cw1.TaskPosition;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;

public class GatherPlan extends TankerPlan {

	public GatherPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		// If the Tanker's fuel is critical this plan is invalid
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		// If the Tanker cannot hold any more waste this plan is invalid
		if (tanker.getWasteLevel() == tanker.getWasteCapacity()) return;

		// Set up the initial state of the Tanker
		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();
		int remainingCapacity = tanker.getWasteCapacity();
		Set<TaskPosition> possibleSites;
		Set<TaskPosition> visitedSites = new HashSet<>();

		// Continue iterating until the Tanker would be completely full
		while (remainingCapacity > 0) {
			// Consider all Tasks which the Tanker has enough fuel to reach and has not yet visited
			//noinspection unchecked
			possibleSites = (Set<TaskPosition>) tanker.getCellsInRange(MyTanker.CELL_TASK, tankerPosition, remainingFuel);
			possibleSites.removeAll(visitedSites);

			// If the Tanker cannot reach any Tasks, stop planning
			if (possibleSites.isEmpty()) break;

			// Find the nearest Task and update the planned state as if the Tanker were to move there
			Position.NearestResult nearest = Position.getNearest(tankerPosition, possibleSites);
			tankerPosition = nearest.position;
			remainingFuel -= nearest.distance;

			// If moving to the nearest Task would strand the Tanker, stop planning
			if (remainingFuel < 0) break;

			// Update the planned state as if the Tanker were to load waste
			TaskPosition nearestTask = (TaskPosition) nearest.position;
			visitedSites.add(nearestTask);
			remainingCapacity -= nearestTask.task.getWasteRemaining();

			// If the Tanker would be out of fuel range of a Pump at the planned Task, stop planning
			if (tanker.getCellsInRange(MyTanker.CELL_PUMP, tankerPosition, remainingFuel).isEmpty()) break;

			// Add Actions to the queue to move the Tanker to the selected Task
			actionQueue.add(new MoveToPositionAction(tankerPosition));
			actionQueue.add(new LoadWasteAction(nearestTask.task));
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			// Critical fuel instantly invalidates this plan
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof TaskEvent) {
				// If the Tanker is at the target Task or has no target, ignore new Tasks
				if (actionQueue.size() < 2) continue;

				// Recall the Tanker's current target Task
				LinkedList<Action> actionList = (LinkedList<Action>) actionQueue;
				Action action = actionList.get(0);
				if (action instanceof LoadWasteAction) action = actionList.get(1);

				Position tankerPosition = tanker.getAbsolutePosition();
				Position nextTask = ((MoveToPositionAction)action).getPosition();
				Position newTask = ((TaskEvent) event).getPosition();

				// If the new Task is closer to the Tanker than the current target Task, invalidate this plan
				if (tankerPosition.distanceTo(newTask) < tankerPosition.distanceTo(nextTask)) return false;
			}
		}
		return true;
	}
}
