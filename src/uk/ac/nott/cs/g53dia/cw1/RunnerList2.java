package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Stores two ordered, keyed lists to facilitate retrieval of all entries within a given 2D range
 * @param <E> the type of the values stored
 */
public class RunnerList2<E> {

	private final RunnerList<E> xRunner = new RunnerList<>();
	private final RunnerList<E> yRunner = new RunnerList<>();

	/**
	 * Moves the iteration head of the two internal lists
	 * @param xPos the x position to move to
	 * @param yPos the y position to move to
	 */
	void moveTo(int xPos, int yPos) {
		xRunner.moveTo(xPos);
		yRunner.moveTo(yPos);
	}

	/**
	 * Adds an entry with given x and y keys
	 * @param xKey the x key of the entry
	 * @param yKey the y key of the entry
	 * @param value the value of the entry
	 */
	public void add(int xKey, int yKey, E value) {
		xRunner.add(xKey, value);
		yRunner.add(yKey, value);
	}

	/**
	 * Removes a specific entry from this list with keys and value equal to the passed Entry
	 * @param entry the entry to remove
	 * @return true if the removal was successful
	 */
	public boolean remove(Entry<E> entry) {
		boolean result = xRunner.remove(entry.xKey, entry.value);
		result &= yRunner.remove(entry.yKey, entry.value);
		return result;
	}

	/**
	 * Removes all entries with the given x and y keys
	 * @param x the x key of the entries to remove
	 * @param y the y key of the entries to remove
	 * @return true if the removal was successful
	 */
	public boolean removeAt(int x, int y) {
		Set<E> values = getAllInRange(x, y, 0);
		if (values.isEmpty()) return false;

		//noinspection unchecked
		E value = ((E[]) values.toArray())[0];
		return remove(new Entry<>(x, y, value));
	}

	/**
	 * Adds all of the given entries to the list in their correct positions
	 * @param entries the entries to add
	 */
	void addAll(Collection<Entry<E>> entries) {
		LinkedList<RunnerList.Entry<E>> xEntries = new LinkedList<>();
		LinkedList<RunnerList.Entry<E>> yEntries = new LinkedList<>();

		for (Entry<E> entry : entries) {
			xEntries.add(new RunnerList.Entry<>(entry.xKey, entry.value));
			yEntries.add(new RunnerList.Entry<>(entry.yKey, entry.value));
		}

		xRunner.addAll(xEntries);
		yRunner.addAll(yEntries);
	}

	/**
	 * Retrieves all entries with a key surrounding or equal to the current position in either x or
	 * y
	 * @return the entries adjacent to the current position
	 */
	Set<E> getAdjacent() {
		Set<E> adjacent = new HashSet<>();
		adjacent.addAll(xRunner.getAdjacent());
		adjacent.addAll(yRunner.getAdjacent());
		return adjacent;
	}

	/**
	 * Retrieves all entries with keys within a certain distance from a centre position in both x
	 * and y
	 * @param xCentre the centre of the search in x
	 * @param yCentre the centre of the search in y
	 * @param radius the radius of the search
	 * @return all entries with keys within radius of the centres
	 */
	Set<E> getAllInRange(int xCentre, int yCentre, int radius) {
		Set<E> inRange = new HashSet<>();
		inRange.addAll(xRunner.getAllInRange(xCentre, radius));
		inRange.retainAll(yRunner.getAllInRange(yCentre, radius));
		return inRange;
	}

	/**
	 * Represents an Entry in a RunnerList2 with two keys and a value
	 * @param <V> the type of the value
	 */
	static class Entry<V> {

		final int xKey;
		final int yKey;
		final V value;

		Entry(int xKey, int yKey, V value) {
			this.xKey = xKey;
			this.yKey = yKey;
			this.value = value;
		}
	}

}
