package uk.ac.nott.cs.g53dia.cw1;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class RunnerList<E> {

	private Entry<E> next = null;
	private Entry<E> previous = null;
	private Stack<Bookmark> bookmarks = new Stack<>();
	private int position = 0;

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
		 this.position = position;
	}

	void add(int key, E value) {
		pushBookmark();

		moveTo(key);
		Entry<E> newEntry = new Entry<>(key, value, next, previous);
		if (next != null) next.previous = newEntry;
		if (previous != null) previous.next = newEntry;

		popBookmark(newEntry);
	}

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

	private void pushBookmark() {
		bookmarks.push(new Bookmark());
	}

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
