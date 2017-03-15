package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.Position;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.cw1.events.WellEvent;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.DisposeWasteAction;

public class DepositPlan extends TankerPlan {

	public DepositPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		// If the Tanker has no waste to deposit, this plan is invalid
		if (tanker.getWasteLevel() == 0) return;

		// if the Tanker is critically low on fuel, this plan is invalid
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		Position position = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		// Find all Wells within fuel range of the Tanker's current Position
		Set<? extends Position> wellsInRange = tanker.getCellsInRange(MyTanker.CELL_WELL, position, remainingFuel);

		// Consider all possible Wells
		while (!wellsInRange.isEmpty()) {
			// Find the nearest Well
			Position.NearestResult nearest = Position.getNearest(position, wellsInRange);

			// If the nearest Well is not within fuel range of a Pump, ignore it
			if (tanker.getCellsInRange(MyTanker.CELL_PUMP, nearest.position, remainingFuel - nearest.distance).isEmpty()) {
				wellsInRange.remove(nearest.position);
				continue;
			}

			// Add Actions to the queue to move to the nearest Well and deposit waste, then stop planning
			actionQueue.add(new MoveToPositionAction(nearest.position));
			actionQueue.add(new DisposeWasteAction());
			break;
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			// Critical fuel instantly invalidates this plan
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof WellEvent) {
				// If the Tanker is already at the target Well, ignore new Wells
				Action nextAction = actionQueue.peek();
				if (!(nextAction instanceof MoveToPositionAction)) return false;

				Position tankerPosition = tanker.getAbsolutePosition();
				Position targetWell = ((MoveToPositionAction) nextAction).getPosition();
				Position newWell = ((WellEvent) event).getPosition();

				// Invalidate this plan if the new Well is closer to the Tanker than the target Well
				if (tankerPosition.distanceTo(newWell) < tankerPosition.distanceTo(targetWell)) return false;
			}
		}
		return true;
	}
}
