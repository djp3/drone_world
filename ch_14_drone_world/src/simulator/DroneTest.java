package simulator;

import static org.junit.Assert.*;

import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import reference.DroneControllerSkeleton;

public class DroneTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String name = "Timbuktu";
		Position position = new Position(10.0,20.0,0.0);
		Place start = new Place(name, position);
		
		name = "Timbuk3";
		position = new Position(15.0,25.0,0.0);
		Place destination = new Place(name, position);
		
		Drone d1 = null;
		d1 = new Drone(new DroneControllerSkeleton(),start,destination,1);
		
		Drone d2 = null;
		d2 = new Drone(d1);
		
		assertEquals(d1,d1);
		assertTrue(d1.hashCode() == d1.hashCode());
		assertTrue(d1.compareTo(d1) == 0);
		
		assertTrue(!d1.equals(null));
		assertTrue(!d1.equals("String"));
		
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		/* Make sure the manifest is accounted for */
		TreeSet<String> treeSet = new TreeSet<String>();
		treeSet.add(name);
		d2.setManifest(treeSet);
		assertTrue(!d1.equals(d2));
		assertTrue(d1.hashCode() != d2.hashCode());
		
		treeSet = new TreeSet<String>();
		treeSet.add(name);
		d1.setManifest(treeSet);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
	}

}
