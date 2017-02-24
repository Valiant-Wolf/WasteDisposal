package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class DualRunnerList<E> {

	private RunnerList<E> xRunner = new RunnerList<>();
	private RunnerList<E> yRunner = new RunnerList<>();

	public void moveTo(int xPos, int yPos) {
		xRunner.moveTo(xPos);
		yRunner.moveTo(yPos);
	}

	public void add(int xKey, int yKey, E value) {
		xRunner.add(xKey, value);
		yRunner.add(yKey, value);
	}

	public void addAll(Collection<DualEntry> entries) {
		LinkedList<RunnerList.Entry<E>> xEntries = new LinkedList<>();
		LinkedList<RunnerList.Entry<E>> yEntries = new LinkedList<>();

		for (DualEntry entry : entries) {
			xEntries.add(new RunnerList.Entry<>(entry.xKey, entry.value));
			yEntries.add(new RunnerList.Entry<>(entry.yKey, entry.value));
		}

		xRunner.addAll(xEntries);
		yRunner.addAll(yEntries);
	}

	public Set<E> getAdjacent() {
		Set<E> adjacent = new HashSet<>();
		adjacent.addAll(xRunner.getAdjacent());
		adjacent.addAll(yRunner.getAdjacent());
		return adjacent;
	}

	public class DualEntry {

		int xKey;
		int yKey;
		E value;

		DualEntry(int xKey, int yKey, E value) {
			this.xKey = xKey;
			this.yKey = yKey;
			this.value = value;
		}
	}

}
