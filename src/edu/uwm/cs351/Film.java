package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Film contains a title, director, genre, and year. A film belongs to a specific collection or none.
 *
 */
public class Film {
	private String title;
	private String director;
	private String genre;
	private int year;
	private Collection c;

	private Film prev, next;

	public Film(String t, String d, String g, int y) {
		if(t == null || d == null || g == null) throw new IllegalArgumentException("cannot have null title, director, or genre");
		if(y < 1890) throw new IllegalArgumentException("Cannot have a date before 1890--films were not produced then");
		title = t;
		director = d;
		genre = g;
		year = y;
	}

	public String getTitle() {
		return title;
	}

	public String getDirector() {
		return director;
	}

	public String getGenre() {
		return genre;
	}

	public int getYear() {
		return year;
	}

	/**
	 * A sorted doubly linked endogenous list of films in a collection.
	 * 
	 */
	public static class Collection extends AbstractCollection<Film>{

		private Film head, tail;
		private int manyFilms, version;
		private Comparator<Film> comp;

		private boolean report(String s) {
			System.out.println("Invariant failure: " + s);
			return false;
		}

		private boolean wellFormed() {
			if(head == null && tail != null) return report ("tail is not null");
			if(tail == null && head != null) return report ("head is not null");
			if(tail != null && tail.next != null) return report("Tail not at end of list!");
			if(head != null && head.prev != null) return report("Head not at beginning of list!");
			if(comp == null) return report("Comparator is null");

			boolean foundT = false;
			int count = 0;
			for(Film f = head; f != null; f = f.next) {
				if(f.next != null && f.next.prev != f) return report("improper links");
				++count;
				if(f == tail) foundT = true;
			}
			if(!foundT && tail != null) return report("Tail in wrong list");
			if(count != manyFilms) return report("manyFilms is wrong");
			return true;
		}

		/**
		 * Constructor for a collection
		 * @param c, a comparator for Film objects, must not be null
		 * @throws IllegalArgumentException if comparator is null
		 */
		public Collection(Comparator<Film> c) {
			if(c == null) throw new IllegalArgumentException("Comparator is null");
			comp = c;
			assert wellFormed():"Invariant failed at end of constructor";
		}

		@Override
		public int size() {
			assert wellFormed():"Invariant failed at beginning of size";
			return manyFilms;
		}

		/**
		 * New film is inserted in the list based on the value returned by the comparator. 
		 * @param f Film to be added.
		 * @throws IllegalArgumentException if f is null or already in a collection
		 * @return true if added
		 */
		public boolean add(Film f) {
			assert wellFormed():"Invariant failed at beginning of add";
			if(f == null) throw new IllegalArgumentException("Cannot add a null");
			if(f.c != null || f.next != null || f.prev != null) throw new IllegalArgumentException("Film already in a collection");

			if(head == null) head = tail = f;
			else {
				Film current = tail;
				while(current != null && comp.compare(current, f) > 0)
					current = current.prev;

				if(current == null) {
					f.next = head;
					head = head.prev = f;
				}else {
					f.prev = current;
					f.next = current.next;
					current.next = f;
					if(f.next == null) tail = f;
					else f.next.prev = f;
				}
			}
			f.c = this;
			++version;
			++manyFilms;
			assert wellFormed():"Invariant failed at end of addd";
			return true;
		}

		/**
		 * Update the comparator for the collection and resort the collection.
		 * @param c, the new comparator, must not be null
		 * @throws IllegalArgumentException if comparator is null
		 */
		public void sort(Comparator<Film> c) {
			assert wellFormed():"Invariant failed at beginning of sort";
			if(c == null) throw new IllegalArgumentException("Comparator is null");
			comp = c;
			if(manyFilms < 2) return;

			boolean swapped = false;
			do {
				swapped = false;
				Film current = head;
				while(current.next != null) {
					if(comp.compare(current, current.next) > 0) {
						Film next = current.next;
						if (next.next != null) next.next.prev = current;
						if (current.prev != null) current.prev.next = next;
						/*
						 * Given bug:
						 * 
						 * current.next = next.next;
						 * current.prev = next;
						 * next.next = current;
						 * next.prev = current.prev;
						 * 
						 * Solution:
						 */
						current.next = next.next;
						next.prev = current.prev;
						next.next = current;
						current.prev = next;

						if (current == head) head = next;
						if (next == tail) tail = current;
						swapped = true;
					}
					else current = current.next;
				}
			}while (swapped);

			++version;
			assert wellFormed():"Invariant failed at end of sort";
		}

		/**
		 * Removes the given film if it is part of the collection.
		 * @param f, a film to be removed
		 * @return true if removed
		 */
		public boolean remove(Film f) {
			assert wellFormed():"Invariant failed at beginning of remove";
			if(f == null || f.c != this) return false;
			if(f.next != null) f.next.prev = f.prev;
			else tail = tail.prev;
			if(f.prev != null) f.prev.next = f.next;
			else head = head.next;
			f.next = f.prev = null;
			f.c = null;
			++version;
			--manyFilms;
			assert wellFormed():"Invariant failed at end of remove";
			return true;
		}

		@Override
		public String toString(){
			assert wellFormed():"Invariant failed at beginning of toString";
			StringBuilder sb = new StringBuilder();
			for(Film f = head; f != null; f = f.next) {
				sb.append(f.getTitle());
				if(f.next != null) sb.append(", ");
			}

			return sb.toString();
		}

		@Override
		public Iterator<Film> iterator() {
			assert wellFormed():"Invariant failed at beginning of iterator";
			return new FilmIterator();
		}

		/**
		 * An iterator for a collection of films
		 *
		 */
		private class FilmIterator implements Iterator<Film>{

			private Film cursor = null;
			private int myVersion = version;
			private boolean canRemove = false;

			private boolean wellFormed() {
				if(!Collection.this.wellFormed()) return report("Collection invariant fails");
				if(myVersion != version) return true;
				if(cursor == null && canRemove) return report("cursor is null but can remove");
				boolean foundC = false;
				for(Film f = head; f != null; f = f.next) {
					if(f == cursor) foundC = true;
				}
				if(cursor != null && !foundC) return report("cursor not in this collection");
				return true;
			}

			@Override
			public boolean hasNext() {
				assert wellFormed(): "Invariant failed at beginning of hasNext";
				if(myVersion != version) throw new ConcurrentModificationException("stale iterator");
				return cursor != tail;
			}

			@Override
			public Film next() {
				assert wellFormed(): "Invariant failed at beginning of next";
				if(myVersion != version) throw new ConcurrentModificationException("stale iterator");
				if(!hasNext()) throw new NoSuchElementException("No more!");
				canRemove = true;
				if(cursor == null) cursor = head;
				else cursor = cursor.next;
				assert wellFormed(): "Invariant failed at end of next";
				return cursor;
			}

			@Override
			public void remove() {
				assert wellFormed(): "Invariant failed at beginning of remove";
				if(myVersion != version) throw new ConcurrentModificationException("stale iterator");
				if(!canRemove) throw new IllegalStateException("next has not been called");

				/* Given bug:
				 * 
				 * cursor = cursor.next;
				 * Collection.this.remove(cursor.prev);
				 * 
				 * Solution:
				 */
				Film cur = cursor.prev;
				Collection.this.remove(cursor);
				cursor = cur;
				canRemove = false;
				++myVersion;
				assert wellFormed(): "Invariant failed at end of remove";
			}

		}

	}
}