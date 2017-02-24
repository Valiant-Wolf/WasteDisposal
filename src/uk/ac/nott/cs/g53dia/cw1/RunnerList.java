package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class RunnerList<E> {

	private Entry<E> next = null;
	private Entry<E> previous = null;

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

	void moveTo(int position) {
		 walkForwardTo(position);
		 walkBackwardTo(position);
	}

	void add(int key, E value) {
		boolean bookmarkNext = next != null;
		Entry<E> bookmark = (bookmarkNext ? next : previous);

		moveTo(key);
		Entry<E> newEntry = new Entry<>(key, value, next, previous);
		if (next != null) next.previous = newEntry;
		if (previous != null) previous.next = newEntry;

		if (bookmarkNext) {
			next = bookmark;
			previous = bookmark.previous;
		} else {
			previous = bookmark;
			next = bookmark.next;
		}
	}

	void addAll(List<Entry<E>> entries) {
		if (entries.size() == 0) return;

		Collections.sort(entries);

		boolean bookmarkNext = next != null;
		Entry<E> bookmark = (bookmarkNext ? next : previous);

		moveTo(entries.get(0).key);
		for (Entry<E> entry : entries) {
			walkForwardTo(entry.key);
			entry.next = next;
			entry.previous = previous;
			if (next != null) next.previous = entry;
			if (previous != null) previous.next = entry;
		}

		if (bookmarkNext) {
			next = bookmark;
			previous = bookmark.previous;
		} else {
			previous = bookmark;
			next = (bookmark != null ? bookmark.next : entries.get(0));
		}
	}

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
		}

		return list;
	}

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
			return that.key - this.key;
		}

	}

}
