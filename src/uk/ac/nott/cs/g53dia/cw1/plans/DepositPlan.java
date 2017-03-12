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

		if (tanker.getWasteLevel() == 0) return;

		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		Position position = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		Set<? extends Position> wellsInRange = tanker.getCellsInRange(MyTanker.CELL_WELL, position, remainingFuel);

		while (!wellsInRange.isEmpty()) {
			Position.NearestResult nearest = Position.getNearest(position, wellsInRange);

			if (tanker.getCellsInRange(MyTanker.CELL_PUMP, nearest.position, remainingFuel - nearest.distance).isEmpty()) {
				wellsInRange.remove(nearest.position);
				continue;
			}

			actionQueue.add(new MoveToPositionAction(nearest.position));
			actionQueue.add(new DisposeWasteAction());
			break;
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof WellEvent) {
				Action nextAction = actionQueue.peek();
				if (!(nextAction instanceof MoveToPositionAction)) return false;

				Position tankerPosition = tanker.getAbsolutePosition();
				Position targetWell = ((MoveToPositionAction) nextAction).getPosition();
				Position newWell = ((WellEvent) event).getPosition();

				if (tankerPosition.distanceTo(newWell) < tankerPosition.distanceTo(targetWell)) return false;
			}
		}
		return true;
	}
}
