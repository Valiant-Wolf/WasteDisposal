package uk.ac.nott.cs.g53dia.cw1;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.Tanker;

/**
 * Stand-in Action to move the Tanker towards a Position. Used in conjunction with
 * MyTanker.moveTowardsPosition()
 */
public class MoveToPositionAction implements Action {

	/**
	 * The Position the Tanker should move towards
	 */
	private Position position;

	/**
	 * Constructs a new instance with the given target Position
	 * @param position the Position the Tanker should move towards
	 */
	public MoveToPositionAction(Position position) {
		if (position == null) throw new IllegalArgumentException();

		this.position = position;
	}

	/**
	 * Returns the target Position of this Action
	 * @return the Position the Tanker should move towards
	 */
	public Position getPosition() {
		return position;
	}

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
