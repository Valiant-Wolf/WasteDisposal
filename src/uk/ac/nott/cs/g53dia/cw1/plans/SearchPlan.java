package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.Position;
import uk.ac.nott.cs.g53dia.cw1.PumpPosition;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PendingTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TaskEvent;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.RefuelAction;

public class SearchPlan extends TankerPlan {

	private PumpPosition target;

	public SearchPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		// If the Tanker is critically low on fuel, invalidate this plan
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		// Find all Pumps within fuel range of the Tanker's Position
		List<Position.NearestResult> pumpsInRange = Position.getRanges(tankerPosition, tanker.getCellsInRange(MyTanker.CELL_PUMP, tankerPosition, remainingFuel));

		// Iterate through all Pumps
		for (Position.NearestResult rangedPump : pumpsInRange) {
			PumpPosition pump = (PumpPosition) rangedPump.position;

			// Ignore Pumps that are already searched
			if (pump.isCompletelySearched()) continue;

			target = pump;

			// Add Actions to the queue to move to the Pump and refuel
			actionQueue.add(new MoveToPositionAction(pump));
			actionQueue.add(new RefuelAction());

			// For each search Position, add an Action to move to that Position
			for (Position searchPosition : pump.getRemainingSearchPositions()) {
				actionQueue.add(new MoveToPositionAction(searchPosition));
			}

			return;
		}
	}

	@Override
	public Action getNextAction() {
		Action nextAction = actionQueue.peek();

		while (nextAction instanceof MoveToPositionAction) {
			if (((MoveToPositionAction) nextAction).getPosition().equals(tanker.getAbsolutePosition())) {
				// If the next Action will move the Tanker to a search Position, update the Pump
				if (!((MoveToPositionAction) nextAction).getPosition().equals(target)) {
					target.positionSearched();
					// If the Pump is now completely searched, remove it from the list of unsearched Pumps
					if (target.isCompletelySearched()) tanker.reportSearched(target);
				}

				actionQueue.remove();
				nextAction = actionQueue.peek();
			} else {
				break;
			}
		}

		if (!(nextAction instanceof MoveToPositionAction || nextAction == null)) {
			actionQueue.remove();
		}

		return nextAction;
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			// Critical fuel and new Tasks immediately invalidate this plan
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof TaskEvent) return false;
			if (event instanceof PendingTaskEvent) {
				Position tankerPosition = tanker.getAbsolutePosition();
				int remainingFuel = tanker.getFuelLevel();
				// Invalidate this plan if the Tanker moves near a previously discovered, incomplete Task
				if (!tanker.getCellsInRange(MyTanker.CELL_TASK, tankerPosition, remainingFuel / 2).isEmpty()) return false;
			}
		}
		return true;
	}
}
