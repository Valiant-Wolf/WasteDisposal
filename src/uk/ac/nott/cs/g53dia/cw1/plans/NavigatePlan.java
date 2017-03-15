package uk.ac.nott.cs.g53dia.cw1.plans;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.nott.cs.g53dia.cw1.MoveToPositionAction;
import uk.ac.nott.cs.g53dia.cw1.MyTanker;
import uk.ac.nott.cs.g53dia.cw1.Position;
import uk.ac.nott.cs.g53dia.cw1.PumpPosition;
import uk.ac.nott.cs.g53dia.cw1.events.CriticalFuelEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TankerEvent;
import uk.ac.nott.cs.g53dia.cw1.events.TaskEvent;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Tanker;

public class NavigatePlan extends TankerPlan {
	public NavigatePlan(MyTanker tanker, List<TankerEvent> events) {
		super(tanker, events);

		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}

		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();
		Position nearestTask = Position.getNearest(tankerPosition, tanker.getCellsAdjacent(MyTanker.CELL_TASK)).position;

		if (nearestTask == null) return;

		// A* SEARCH TIME!

		LinkedList<AStarNode> openList = new LinkedList<>();
		Set<AStarNode> closedSet = new HashSet<>();

		//noinspection unchecked
		Set<PumpPosition> startPositions = (Set<PumpPosition>) tanker.getCellsInRange(MyTanker.CELL_PUMP, nearestTask, Tanker.MAX_FUEL/2);

		for (PumpPosition startPosition : startPositions) {
			int distanceFromStart = nearestTask.distanceTo(startPosition);
			int distanceToFinish = tankerPosition.distanceTo(startPosition);
			openList.add(new AStarNode(startPosition, distanceFromStart, distanceToFinish, null));
		}

		AStarNode lastNode;

		while (true) {
			if (openList.isEmpty()) return;
			openList.sort(Comparator.comparingInt(AStarNode::getPriority));

			AStarNode current = openList.getFirst();
			if (current.distanceToFinish <= remainingFuel) {
				lastNode = current;
				break;
			}

			openList.remove(current);
			closedSet.add(current);

			for (PumpPosition.MapEntry neighbour : current.position.getConnectedNodes()) {
				AStarNode dummyNode = new AStarNode(neighbour.position, 0, 0, null);
				if (closedSet.contains(dummyNode)) continue;

				int distanceFromStart = current.distanceFromStart + neighbour.distance;

				if (!openList.contains(dummyNode)) {
					int distanceToFinish = tankerPosition.distanceTo(neighbour.position);
					openList.add(new AStarNode(neighbour.position, distanceFromStart, distanceToFinish, current));
					continue;
				}

				AStarNode existingNode = null;
				for (AStarNode node : openList) {
					if (node.equals(dummyNode)) {
						existingNode = node;
						break;
					}
				}
				if (existingNode == null) throw new IllegalStateException();
				if (existingNode.distanceFromStart <= distanceFromStart) continue;

				existingNode.distanceFromStart = distanceFromStart;
				existingNode.prevNode = current;
			}
		}

		while (lastNode != null) {
			actionQueue.add(new MoveToPositionAction(lastNode.position));
			actionQueue.add(new RefuelAction());
			lastNode = lastNode.prevNode;
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return false;
			if (event instanceof TaskEvent) return false;
		}
		return true;
	}

	private static class AStarNode {

		int distanceFromStart;
		final int distanceToFinish;
		PumpPosition position;
		AStarNode prevNode;

		AStarNode(PumpPosition position, int distanceFromStart, int distanceToFinish, AStarNode prevNode) {
			this.distanceFromStart = distanceFromStart;
			this.distanceToFinish = distanceToFinish;
			this.position = position;
			this.prevNode = prevNode;
		}

		int getPriority() {
			return distanceFromStart + distanceToFinish;
		}

		@Override
		public boolean equals(Object that) {
			return that instanceof AStarNode && this.position.equals(((AStarNode) that).position);
		}

		@Override
		public int hashCode() {
			return position.hashCode();
		}

	}
}
