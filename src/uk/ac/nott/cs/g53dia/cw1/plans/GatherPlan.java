package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public class GatherPlan extends TankerPlan {

	private Queue<Action> actionQueue = new LinkedList<>();

	public GatherPlan(MyTanker parent) {
		super(parent);

		int remainingFuel = parent.getFuelLevel();

	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		return false;
	}

	@Override
	public Action getNextAction() {
		return null;
	}
}
