package uk.ac.nott.cs.g53dia.cw1.events;

import uk.ac.nott.cs.g53dia.cw1.Position;

public class WellEvent extends TankerEvent {

	private final Position position;

	public WellEvent(Position position) {
		this.position = position;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	public Position getPosition() {
		return position;
	}
}
