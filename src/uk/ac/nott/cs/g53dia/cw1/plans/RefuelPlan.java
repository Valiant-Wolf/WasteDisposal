package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.Position;
import uk.ac.nott.cs.g53dia.cw1.events.PumpEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.RefuelAction;

public class RefuelPlan extends TankerPlan {

	public RefuelPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		Position position = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		// Find the nearest Pump the the Tanker's current Position
		Set<? extends Position> pumpsInRange = tanker.getCellsInRange(MyTanker.CELL_PUMP, position, remainingFuel + 2);
		if (pumpsInRange.isEmpty()) throw new IllegalStateException();
		Position closestPump = Position.getNearest(position, pumpsInRange).position;

		// Add Actions to the queue to refuel at the target Pump
		actionQueue.add(new MoveToPositionAction(closestPump));
		actionQueue.add(new RefuelAction());
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			if (event instanceof PumpEvent) {
				// If the Tanker is already at the target Pump, ignore new Pumps
				Action nextAction = actionQueue.peek();
				if (!(nextAction instanceof MoveToPositionAction)) return false;

				Position tankerPosition = tanker.getAbsolutePosition();
				Position targetPump = ((MoveToPositionAction) nextAction).getPosition();
				Position newPump = ((PumpEvent) event).getPosition();

				// Invalidate this plan if the new Pump is closer to the Tanker than the target Pump
				if (tankerPosition.distanceTo(newPump) < tankerPosition.distanceTo(targetPump)) return false;
			}
		}
		return true;
	}

}
