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
import uk.ac.nott.cs.g53dia.cw1.events.StationTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;

public class GatherPlan extends TankerPlan {

	public GatherPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		if (tanker.getWasteLevel() == tanker.getWasteCapacity()) return;

		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();
		int remainingCapacity = tanker.getWasteCapacity();
		Set<TaskPosition> possibleSites;
		Set<TaskPosition> visitedSites = new HashSet<>();

		while (remainingCapacity > 0) {
			//noinspection unchecked
			possibleSites = (Set<TaskPosition>) tanker.getCellsInRange(MyTanker.CELL_TASK, tankerPosition, remainingFuel);
			possibleSites.removeAll(visitedSites);
			if (possibleSites.isEmpty()) break;

			Position.NearestResult nearest = Position.getNearest(tankerPosition, possibleSites);
			tankerPosition = nearest.position;
			remainingFuel -= nearest.distance;
			if (remainingFuel < 0) break;

			TaskPosition nearestTask = (TaskPosition) nearest.position;
			visitedSites.add(nearestTask);
			remainingCapacity -= nearestTask.task.getWasteRemaining();

			if (tanker.getCellsInRange(MyTanker.CELL_PUMP, tankerPosition, remainingFuel).isEmpty()) break;

			actionQueue.add(new MoveToPositionAction(tankerPosition));
			actionQueue.add(new LoadWasteAction(nearestTask.task));
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof StationTaskEvent) {
				if (actionQueue.size() < 2) continue;

				LinkedList<Action> actionList = (LinkedList<Action>) actionQueue;
				Action action = actionList.get(0);
				if (action instanceof LoadWasteAction) action = actionList.get(1);

				Position tankerPosition = tanker.getAbsolutePosition();
				Position nextTask = ((MoveToPositionAction)action).getPosition();
				Position newTask = ((StationTaskEvent) event).getPosition();

				if (tankerPosition.distanceTo(newTask) < tankerPosition.distanceTo(nextTask)) return false;
			}
		}
		return true;
	}
}
