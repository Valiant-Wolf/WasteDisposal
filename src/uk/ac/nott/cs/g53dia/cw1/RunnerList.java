package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Stores an ordered, keyed list and provides operations to remove entries or retrieve all entries
 * in a given range
 * @param <E> the type of the values stored
 */
public class RunnerList<E> {

	private Entry<E> next = null;
	private Entry<E> previous = null;
	private Stack<Bookmark> bookmarks = new Stack<>();
	private int position = 0;

	/**
	 * Moves the iteration head forwards until the next entry has the given key or higher
	 * @param position the key to move up to
	 */
	private void walkForwardTo(int position) {
		boolean nextNull = false;
		boolean moved = false;

		while (next != null && next.key < position) {
			moved = true;
			if (next.next == null) {
				previous = next;
				nextNull = true;
			}
			next = next.next;
		}

		if (!nextNull && moved) previous = next.previous;
	}

	/**
	 * Moves the iteration head backwards until the next entry has the given key or higher
	 * @param position the key to move down past
	 */
	private void walkBackwardTo(int position) {
		boolean prevNull = false;
		boolean moved = false;

		while (previous != null && previous.key >= position) {
			moved = true;
			if (previous.previous == null) {
				next = previous;
				prevNull = true;
			}
			previous = previous.previous;
		}

		if (!prevNull && moved) next = previous.next;
	}

	/**
	 * Positions the iteration head at the given key, such that all entries with the given key are
	 * ahead of it
	 * @param position the key to move to
	 */
	void moveTo(int position) {
		 walkForwardTo(position);
		 walkBackwardTo(position);
		 this.position = position;
	}

	/**
	 * Adds an entry to the correct position in the list with the given key. Duplicate keys are
	 * permitted
	 * @param key the position of the entry in the list
	 * @param value the value of the entry
	 */
	void add(int key, E value) {
		pushBookmark();

		moveTo(key);
		Entry<E> newEntry = new Entry<>(key, value, next, previous);
		if (next != null) next.previous = newEntry;
		if (previous != null) previous.next = newEntry;

		popBookmark(newEntry);
	}

	/**
	 * Removes all entries with the given key and value. Comparisons are made with the value's
	 * equals() method.
	 * @param key the key to remove entries at
	 * @param value the value of the entries to remove
	 * @return true if any number of entries were removed, false otherwise
	 */
	boolean remove(int key, E value) {
		pushBookmark();

		moveTo(key);
		Entry<E> runner = next;
		boolean removed = false;

		while (runner != null && runner.key == key) {
			if (runner.value.equals(value)) {
				removeFromBookmarks(runner);
				if (runner.previous != null) runner.previous.next = runner.next;
				if (runner.next != null) runner.next.previous = runner.previous;
				removed = true;
			}
			runner = runner.next;
		}

		popBookmark(null);
		return removed;
	}

	/**
	 * Adds all of the provided entries to the list in their correct positions
	 * @param entries the entries to add
	 */
	void addAll(List<Entry<E>> entries) {
		if (entries.size() == 0) return;

		Collections.sort(entries);

		pushBookmark();

		moveTo(entries.get(0).key);
		for (Entry<E> entry : entries) {
			walkForwardTo(entry.key);
			entry.next = next;
			entry.previous = previous;
			if (next != null) next.previous = entry;
			if (previous != null) previous.next = entry;
			next = entry;
		}

		popBookmark(next);
	}

	/**
	 * Retrieves all entries with a key surrounding or equal to the current position. If there are
	 * entries at keys 1, 2, 4, 5, 7 and the current position is 4, all entries with a key of 2, 4
	 * or 5 will be returned
	 * @return the entries adjacent to the current position
	 */
	List<E> getAdjacent() {
		LinkedList<E> list = new LinkedList<>();

		int key;
		Entry<E> runner;

		if (previous != null) {
			key = previous.key;
			runner = previous;
			while (runner != null && runner.key == key) {
				list.add(runner.value);
				runner = runner.previous;
			}
		}
		if (next != null) {
			key = next.key;
			runner = next;
			while (runner != null && runner.key == key) {
				list.add(runner.value);
				runner = runner.next;
			}

			if (runner != null && runner.previous != null && runner.previous.key == position) {
				key = runner.key;
				while (runner != null && runner.key == key) {
					list.add(runner.value);
					runner = runner.next;
				}
			}
		}

		return list;
	}

	/**
	 * Retrieves all entries with a key within a certain distance from a centre position. All
	 * entries with a key such that (centre - radius <= key <= centre + radius) will be returned
	 * @param centre the centre of the search
	 * @param radius the radius of the search
	 * @return all entries with a key within radius of the centre
	 */
	List<E> getAllInRange(int centre, int radius) {
		LinkedList<E> list = new LinkedList<>();

		pushBookmark();
		moveTo(centre - radius);

		int maxKey = centre + radius;
		Entry<E> runner = next;

		while (runner != null && runner.key <= maxKey) {
			list.add(runner.value);
			runner = runner.next;
		}

		popBookmark(null);
		return list;
	}

	/**
	 * Stores the current position of the iteration head for recall
	 */
	private void pushBookmark() {
		bookmarks.push(new Bookmark());
	}

	/**
	 * Recalls the most recently stored iteration head
	 * @param fallbackNext the new Entry to use if the list was empty on the last push call
	 */
	private void popBookmark(Entry<E> fallbackNext) {
		Bookmark bookmark = bookmarks.pop();
		if (bookmark.isNext) {
			next = bookmark.entry;
			previous = next.previous;
		} else {
			previous = bookmark.entry;
			next = (previous != null ? previous.next : fallbackNext);
		}
		moveTo(bookmark.pos);
	}

	/**
	 * Removes the given Entry from all bookmarks to facilitate correct removal
	 * @param entry the Entry to remove
	 */
	private void removeFromBookmarks(Entry<E> entry) {
		for (Bookmark bookmark : bookmarks) {
			if (entry.equals(bookmark.entry)) {
				if (bookmark.isNext) {
					if (entry.next != null) {
						bookmark.entry = entry.next;
					} else {
						bookmark.entry = entry.previous;
						bookmark.isNext = false;
					}

				} else {
					if (entry.previous != null) {
						bookmark.entry = entry.previous;
					} else if (entry.previous == null && entry.next == null) {
						bookmark.entry = null;
					} else {
						bookmark.entry = entry.next;
						bookmark.isNext = true;
					}
				}
			}
		}
	}

	/**
	 * Represents an entry in a RunnerList with a key and a value
	 * @param <V> the type of the value
	 */
	public static class Entry<V> implements Comparable<Entry> {

		final int key;
		final V value;
		Entry<V> next;
		Entry<V> previous;

		Entry(int key, V value) {
			this(key, value, null, null);
		}

		Entry(int key, V value, Entry<V> next, Entry<V> previous) {
			this.key = key;
			this.value = value;
			this.next = next;
			this.previous = previous;
		}

		@Override
		public int compareTo(Entry that) {
			return this.key - that.key;
		}

	}

	/**
	 * Represents a stored iteration position
	 */
	private class Bookmark {

		Entry<E> entry;
		boolean isNext;
		int pos;

		Bookmark() {
			isNext = next != null;
			entry = (isNext ? next : previous);
			pos = position;
		}

	}

}
