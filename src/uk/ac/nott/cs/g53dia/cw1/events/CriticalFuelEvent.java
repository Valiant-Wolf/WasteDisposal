package uk.ac.nott.cs.g53dia.cw1.events;

public class CriticalFuelEvent extends TankerEvent {

	private final int remainingFuel;

	public CriticalFuelEvent(int remainingFuel) {
		this.remainingFuel = remainingFuel;
	}

	@Override
	public int getPriority() {
		return 5;
	}

	public int getRemainingFuel() {
		return remainingFuel;
	}
}
