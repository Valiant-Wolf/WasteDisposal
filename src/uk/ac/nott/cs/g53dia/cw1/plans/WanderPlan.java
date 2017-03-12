package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;

import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.WanderAction;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PendingTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.StationTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public class WanderPlan extends TankerPlan {

	private static WanderAction action = new WanderAction();

	public WanderPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		actionQueue.add(action);
	}

	@Override
	public Action getNextAction() {
		return actionQueue.peek();
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof StationTaskEvent) return false;
			if (event instanceof PendingTaskEvent) return false;
		}
		return true;
	}
}
