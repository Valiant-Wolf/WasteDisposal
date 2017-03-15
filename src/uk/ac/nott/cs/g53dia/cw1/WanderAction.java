package uk.ac.nott.cs.g53dia.cw1;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.Tanker;

/**
 * Stand-in Action to move the Tanker pseudo-randomly. Used in conjunction with
 * MyTanker.wanderAway() or MyTanker.wanderRandomly()
 */
public class WanderAction implements Action {

	/**
	 * UNSUPPORTED - this Action is not to be used in the Environment and acts only as an
	 * intermediate for MyTanker and its Plans
	 * @param env The Environment that the Tanker inhabits.
	 * @param tanker The Tanker trying to perform this action.
	 */
	@Override
	public void execute(Environment env, Tanker tanker) {
		throw new UnsupportedOperationException();
	}
}
