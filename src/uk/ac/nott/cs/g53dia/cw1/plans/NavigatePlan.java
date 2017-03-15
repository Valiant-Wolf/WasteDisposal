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

		// If the Tanker is critically low on fuel, invalidate this plan
		for (TankerEvent event : events) {
			if (event instanceof CriticalFuelEvent) return;
		}


		Position tankerPosition = tanker.getAbsolutePosition();
		int remainingFuel = tanker.getFuelLevel();

		// Find the nearest Task to the Tanker's current Position regardless of range
		Position nearestTask = Position.getNearest(tankerPosition, tanker.getCellsAdjacent(MyTanker.CELL_TASK)).position;
		// If there are no discovered, incomplete tasks, invalidate this plan
		if (nearestTask == null) return;

		/*
		 * The following code is a modified implementation of the A* search algorithm, where the
		 * vertices are the known Pumps, the path cost is the distance between Pumps, and the
		 * heuristic is the distance to the Tanker. The search is performed in reverse from the Task
		 * to the Tanker. The initial state and finishing conditions of the search have been changed
		 * to suit the environment and differences are commented where appropriate
		 */

		LinkedList<AStarNode> openList = new LinkedList<>();
		Set<AStarNode> closedSet = new HashSet<>();

		// Instead of starting with a single vertex in the open list, start with all Pumps in range of the closest Task
		//noinspection unchecked
		Set<PumpPosition> startPositions = (Set<PumpPosition>) tanker.getCellsInRange(MyTanker.CELL_PUMP, nearestTask, Tanker.MAX_FUEL/2);

		for (PumpPosition startPosition : startPositions) {
			int distanceFromStart = nearestTask.distanceTo(startPosition);
			int distanceToFinish = tankerPosition.distanceTo(startPosition);
			openList.add(new AStarNode(startPosition, distanceFromStart, distanceToFinish, null));
		}

		// Keep track of the last node in the path
		AStarNode lastNode;

		while (true) {
			// If no path can be found to the nearest Task, invalidate this plan
			if (openList.isEmpty()) return;

			// Sort the list of open vertices in ascending order
			openList.sort(Comparator.comparingInt(AStarNode::getPriority));

			// If the current best vertex is within fuel range of the Tanker, a path has been found
			AStarNode current = openList.getFirst();
			if (current.distanceToFinish <= remainingFuel) {
				lastNode = current;
				break;
			}

			openList.remove(current);
			closedSet.add(current);

			for (PumpPosition.MapEntry neighbour : current.position.getConnectedNodes()) {
				// Create a dummy node for checking equality in the open and closed lists
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

		// Unwind the path found using A* and add Actions to the queue to move and refuel the Tanker
		while (lastNode != null) {
			actionQueue.add(new MoveToPositionAction(lastNode.position));
			actionQueue.add(new RefuelAction());
			lastNode = lastNode.prevNode;
		}
	}

	@Override
	public boolean checkValidity(List<TankerEvent> events) {
		for (TankerEvent event : events) {
			// Critical fuel and new Tasks immediately invalidate this plan
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
