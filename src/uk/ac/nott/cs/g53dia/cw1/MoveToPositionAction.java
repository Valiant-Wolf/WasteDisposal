package uk.ac.nott.cs.g53dia.cw1;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.ActionFailedException;
import uk.ac.nott.cs.g53dia.library.Environment;
import uk.ac.nott.cs.g53dia.library.Tanker;

public class MoveToPositionAction implements Action {

	private Position position;

	public MoveToPositionAction(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public void execute(Environment env, Tanker tanker) throws ActionFailedException {
		throw new UnsupportedOperationException();
	}
}
