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

		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		List<Position.NearestResult> pumpsInRange = Position.getRanges(tankerPosition, tanker.getCellsInRange(MyTanker.CELL_PUMP, tankerPosition, remainingFuel));

		for (Position.NearestResult rangedPump : pumpsInRange) {
			PumpPosition pump = (PumpPosition) rangedPump.position;

			if (pump.isCompletelySearched()) continue;

			target = pump;

			actionQueue.add(new MoveToPositionAction(pump));
			actionQueue.add(new RefuelAction());
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
				if (!((MoveToPositionAction) nextAction).getPosition().equals(target)) {
					target.positionSearched();
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
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof TaskEvent) return false;
			if (event instanceof PendingTaskEvent) {
				Position tankerPosition = tanker.getAbsolutePosition();
				int remainingFuel = tanker.getFuelLevel();
				if (!tanker.getCellsInRange(MyTanker.CELL_TASK, tankerPosition, remainingFuel / 2).isEmpty()) return false;
			}
		}
		return true;
	}
}
