package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;

import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.WanderAction;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.PendingTaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TaskEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public class WanderPlan extends TankerPlan {

	private static WanderAction action = new WanderAction();

	public WanderPlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		// If the Tanker's fuel is critical, this plan is invalid
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		// Add a WanderAction to the queue
		actionQueue.add(action);
	}

	@Override
	public Action getNextAction() {
		// Return but do not remove the WanderAction from the queue
		return actionQueue.peek();
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			// Critical fuel instantly invalidates this plan
			if (event instanceof CriticalFuelEvent) return false;
			// Any incomplete Task invalidates this plan
			if (event instanceof TaskEvent) return false;
			if (event instanceof PendingTaskEvent) return false;
		}
		return true;
	}
}
