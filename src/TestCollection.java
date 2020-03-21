import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.uwm.cs351.Film;
import edu.uwm.cs351.Film.Collection;
import junit.framework.TestCase;

public class TestCollection extends TestCase {
	private Comparator<Film> byTitle = new Comparator<Film> () {
		@Override
		public int compare(Film f1, Film f2) {
			return f1.getTitle().compareTo(f2.getTitle());
		}
	};
	private Comparator<Film> byDirector = new Comparator<Film> () {
		@Override
		public int compare(Film f1, Film f2) {
			return f1.getDirector().compareTo(f2.getDirector());
		}
	};
	private Comparator<Film> byYear = new Comparator<Film> () {
		@Override
		public int compare(Film f1, Film f2) {
			return f1.getYear()-f2.getYear();
		}
	};
	
	protected void assertException(Class<? extends Throwable> c, Runnable r) {
		try {
			r.run();
			assertFalse("Exception should have been thrown",true);
		} catch (RuntimeException ex) {
			assertTrue("should throw exception of " + c + ", not of " + ex.getClass(), c.isInstance(ex));
		}
	}
	
	Film f1, f2, f3, f4, f5;
	Collection c1, c2;
	
	public void setUp() {
		f1 = new Film("The Shape of Water", "Guillermo Del Toro", "Drama/Fantasy", 2017);
		f2 = new Film("The Fifth Element", "Luc Besson", "Science Fiction", 1997);
		f3 = new Film("The Return of the King", "Peter Jackson", "Fantasy/Adventure", 2003);
		f4 = new Film("Parasite", "Bong Joon Ho", "Drama/Thriller", 2019);
		f5 = new Film("Bringing Up Baby", "Howard Hawks", "Comedy", 1938);
		c1 = new Collection(byTitle);
	}
	
	//test collection add/remove
	
	public void test00(){
		assertException(IllegalArgumentException.class, () -> c2 = new Collection(null));
		assertEquals(0, c1.size());
		c1.add(f1);
		c1.add(f4);
		c1.add(f5);
		assertEquals(3, c1.size());
		assertEquals("Bringing Up Baby, Parasite, The Shape of Water", c1.toString());
	}
	
	public void test01(){
		assertEquals(0, c1.size());
		c1.add(f1);
		c1.add(f4);
		c1.add(f5);
		assertEquals(3, c1.size());
		assertEquals("Bringing Up Baby, Parasite, The Shape of Water", c1.toString());
		c1.remove(f5);
		assertEquals(2, c1.size());
		assertEquals("Parasite, The Shape of Water", c1.toString());
		c1.remove(f1);
		assertEquals(1, c1.size());
		assertEquals("Parasite", c1.toString());
		c1.remove(f4);
		assertEquals(0, c1.size());
		assertEquals("", c1.toString());
	}
	
	public void test02() {
		c1.add(f4);
		assertEquals(1, c1.size());
		c2 = new Collection(byYear);
		assertException(IllegalArgumentException.class, () -> c2.add(f4));
		assertEquals(0, c2.size());
		c1.remove(f4);
		c2.add(f4);
		assertEquals(1, c2.size());
		assertEquals(0, c1.size());
	}
	
	public void test03(){
		assertEquals(0, c1.size());
		c1.add(f1);
		c1.add(f4);
		c1.add(f5);
		assertEquals(3, c1.size());
		assertEquals("Bringing Up Baby, Parasite, The Shape of Water", c1.toString());
		c1.remove(f4);
		assertEquals(2, c1.size());
		assertEquals("Bringing Up Baby, The Shape of Water", c1.toString());
	}
	
	//test iterator methods
	
	public void test04() {
		Iterator<Film> it = c1.iterator();
		assertFalse(it.hasNext());
	}
	
	public void test05() {
		c1.add(f3);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
	}
	
	public void test06() {
		c1.add(f3);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		assertSame(f3, it.next());
	}
	
	public void test07() {
		c1.add(f3);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		assertSame(f3, it.next());
		assertException(NoSuchElementException.class, () -> it.next());
	}
	
	public void test08() {
		Iterator<Film> it = c1.iterator();
		c1.add(f3);
		assertException(ConcurrentModificationException.class, () -> it.hasNext());
	}
	
	public void test09() {
		c1.add(f3);
		c1.add(f4);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		assertSame(f4, it.next());
		c1.remove(f3);
		assertException(ConcurrentModificationException.class, () -> it.hasNext());
	}
	
	public void test10() {
		c1.add(f3);
		c1.add(f4);
		c1.add(f1);
		c1.add(f2);
		c1.add(f5);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		assertSame(f5, it.next());
		c1.sort(byYear);
		assertException(ConcurrentModificationException.class, () -> it.hasNext());
	}
	
	public void test11() {
		c1.add(f1);
		c1.add(f2);
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		it.next();
		it.remove();
		assertTrue(it.hasNext());
	}
	
	public void test12() {
		c1.add(f1);
		c1.add(f2);
		c1.add(f3);
		c1.add(f4);
		c1.add(f5);
		//in order: f5, f4, f2, f3, f1
		Iterator<Film> it = c1.iterator();
		assertTrue(it.hasNext());
		assertSame(f5, it.next());
		assertSame(f4, it.next());
		assertSame(f2, it.next());
		it.remove();
		assertException(IllegalStateException.class, () -> it.remove());
		assertSame(f3, it.next());
		it.remove();
		assertSame(f1, it.next());
	}
	
	//test sort method
	
	public void test13() {
		c1.add(f1);
		c1.add(f2);
		c1.add(f3);
		c1.add(f4);
		c1.add(f5);
		assertEquals("Bringing Up Baby, Parasite, The Fifth Element, The Return of the King, The Shape of Water", c1.toString());
		assertException(IllegalArgumentException.class, () -> c1.sort(null));
		c1.sort(byDirector);
		assertEquals("Parasite, The Shape of Water, Bringing Up Baby, The Fifth Element, The Return of the King", c1.toString());
		c1.remove(f1);
		assertEquals("Parasite, Bringing Up Baby, The Fifth Element, The Return of the King", c1.toString());
		c1.add(f1);
		assertEquals("Parasite, The Shape of Water, Bringing Up Baby, The Fifth Element, The Return of the King", c1.toString());
	}
	
}