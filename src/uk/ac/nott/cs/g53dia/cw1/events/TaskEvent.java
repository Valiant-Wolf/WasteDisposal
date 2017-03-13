package uk.ac.nott.cs.g53dia.cw1.events;

import uk.ac.nott.cs.g53dia.cw1.Position;

public class TaskEvent extends TankerEvent {

	private final Position position;
	private final int amount;

	public TaskEvent(Position station, int amount) {
		this.position = station;
		this.amount = amount;
	}

	public Position getPosition() {
		return position;
	}

	public int getAmount() {
		return amount;
	}
}
