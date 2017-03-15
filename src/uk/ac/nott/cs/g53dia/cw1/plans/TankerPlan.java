package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.library.Action;

public abstract class TankerPlan {

	protected final MyTanker tanker;
	protected final Queue<Action> actionQueue = new LinkedList<>();

	/**
	 * Creates a new Plan for the given Tanker
	 * @param tanker the Tanker to which this Plan belongs
	 * @param events the current Events for this timestep, to be used in preconditions
	 */
	public TankerPlan(MyTanker tanker, List<TankerEvent> events) {
		this.tanker = tanker;
	}

	/**
	 * Checks whether the events of this timestep have invalidated this Plan
	 * @param events the events occurring in this timestep
	 * @return true if this Plan is still valid, false otherwise
	 */
	public abstract boolean checkValidity(List<TankerEvent> events);

	/**
	 * Checks whether this Plan has any more Actions to execute. Plans which fail their
	 * preconditions should return true here
	 * @return true if this Plan has no more Actions, false otherwise
	 */
	public boolean isComplete() {
		return actionQueue.isEmpty();
	}

	/**
	 * Pops the next Action from the top of this Plan's queue. In the case of MoveToPositionAction,
	 * the Action will not be popped until the Tanker has moved to the target Position.
	 * @return the next Action for the agent to execute
	 */
	public Action getNextAction() {
		Action nextAction = actionQueue.peek();

		while (nextAction instanceof MoveToPositionAction) {
			if (((MoveToPositionAction) nextAction).getPosition().equals(tanker.getAbsolutePosition())) {
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

}
