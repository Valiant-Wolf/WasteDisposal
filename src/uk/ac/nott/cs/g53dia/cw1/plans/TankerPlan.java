package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.List;

import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public abstract class TankerPlan {

	protected final MyTanker parent;

	public TankerPlan(MyTanker parent) {
		this.parent = parent;
	}

	public abstract boolean checkValidity(List<TankerEvent> events);

	public abstract Action getNextAction();

}
