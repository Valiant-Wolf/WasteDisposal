package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

class DualRunnerList<E> {

	private final RunnerList<E> xRunner = new RunnerList<>();
	private final RunnerList<E> yRunner = new RunnerList<>();

	void moveTo(int xPos, int yPos) {
		xRunner.moveTo(xPos);
		yRunner.moveTo(yPos);
	}

	public void add(int xKey, int yKey, E value) {
		xRunner.add(xKey, value);
		yRunner.add(yKey, value);
	}

	void addAll(Collection<DualEntry<E>> entries) {
		LinkedList<RunnerList.Entry<E>> xEntries = new LinkedList<>();
		LinkedList<RunnerList.Entry<E>> yEntries = new LinkedList<>();

		for (DualEntry<E> entry : entries) {
			xEntries.add(new RunnerList.Entry<>(entry.xKey, entry.value));
			yEntries.add(new RunnerList.Entry<>(entry.yKey, entry.value));
		}

		xRunner.addAll(xEntries);
		yRunner.addAll(yEntries);
	}

	Set<E> getAdjacent() {
		Set<E> adjacent = new HashSet<>();
		adjacent.addAll(xRunner.getAdjacent());
		adjacent.addAll(yRunner.getAdjacent());
		return adjacent;
	}

	static class DualEntry<V> {

		final int xKey;
		final int yKey;
		final V value;

		DualEntry(int xKey, int yKey, V value) {
			this.xKey = xKey;
			this.yKey = yKey;
			this.value = value;
		}
	}

}
