package uk.ac.nott.cs.g53dia.cw1.events;

import uk.ac.nott.cs.g53dia.cw1.Position;

public class StationTaskEvent extends TankerEvent {

	private final Position position;
	private final int amount;

	public StationTaskEvent(Position station, int amount) {
		this.position = station;
		this.amount = amount;
	}

	@Override
	public int getPriority() {
		return 2;
	}

	public Position getPosition() {
		return position;
	}

	public int getAmount() {
		return amount;
	}
}
