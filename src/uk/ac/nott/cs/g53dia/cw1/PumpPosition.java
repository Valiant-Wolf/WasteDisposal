package uk.ac.nott.cs.g53dia.cw1;

import java.util.LinkedList;
import java.util.List;

import uk.ac.nott.cs.g53dia.library.Tanker;

public class PumpPosition extends Position {

	private List<MapEntry> connectedNodes = new LinkedList<>();

	public PumpPosition(Position position) {
		super(position.x, position.y);
	}

	public void addPump(PumpPosition position) {
		int distance = this.distanceTo(position);
		if (distance > Tanker.MAX_FUEL) return;

		connectedNodes.add(new MapEntry(position, distance));
	}

	public List<MapEntry> getConnectedNodes() {
		return connectedNodes;
	}

	public static class MapEntry {

		public PumpPosition position;
		public int distance;

		private MapEntry(PumpPosition position, int distance) {
			this.position = position;
			this.distance = distance;
		}
	}

}
