package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public abstract class TankerPlan {

	protected final MyTanker tanker;
	protected final Queue<Action> actionQueue = new LinkedList<>();

	public TankerPlan(MyTanker tanker) {
		this.tanker = tanker;
	}

	public abstract boolean checkValidity(List<TankerEvent> events);

	public boolean isComplete() {
		return actionQueue.isEmpty();
	}

	public abstract Action getNextAction();

}
