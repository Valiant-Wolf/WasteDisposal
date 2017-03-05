package uk.ac.nott.cs.g53dia.cw1.events;

import uk.ac.nott.cs.g53dia.cw1.Position;

public class PumpEvent extends TankerEvent {

	private final Position position;

	public PumpEvent(Position pump) {
		this.position = pump;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	public Position getPosition() {
		return position;
	}
}
