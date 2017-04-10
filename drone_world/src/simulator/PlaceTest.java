package simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PlaceTest {

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
	public void testBasicConstructor() {
		String name = "Timbuktu";
		Position position = new Position(10.0,20.0,0.0);
		Place place = new Place(name, position);
		assertEquals(name,place.getName());
		assertEquals(position, place.getPosition());
		assertEquals(0, place.getWaitingToEmbark().size());
		
		Place place2 = new Place(place);
		assertEquals(name,place2.getName());
		assertEquals(position, place2.getPosition());
		assertEquals(0, place2.getWaitingToEmbark().size());
		
		place.getWaitingToEmbark().add(PersonTest.makeDummyPerson());
		place2 = new Place(place);
		assertEquals(name,place2.getName());
		assertEquals(position, place2.getPosition());
		assertEquals(1, place2.getWaitingToEmbark().size());
	}
	
	@Test
	public void testEquals() {
		String name = "Timbuktu";
		Position position = new Position(10.0,20.0,0.0);
		Place place1 = new Place(name, position);
		Place place2 = new Place(name, position);
		assertTrue(place1.equals(place1));
		assertTrue(place1.compareTo(place1) == 0);
		assertTrue(!place1.equals(null));
		assertTrue(place1.compareTo(null) == 1);
		assertTrue(!place1.equals("String"));
		
		assertEquals(place1,place2);
		assertTrue(place1.hashCode() == place2.hashCode());
		assertTrue(place1.equals(place2));
		assertTrue(place1.compareTo(place2) == 0);
		assertTrue(place2.compareTo(place1) == 0);
		
		place1 = new Place(null, position);
		place2 = new Place(null, position);
		assertEquals(place1,place2);
		assertTrue(place1.hashCode() == place2.hashCode());
		assertTrue(place1.compareTo(place2) == 0);
		
		place1 = new Place(null, position);
		place2 = new Place(name, position);
		assertTrue(!place1.equals(place2));
		assertTrue(place1.hashCode() != place2.hashCode());
		assertTrue(place1.compareTo(place2) < 0);
		assertTrue(place2.compareTo(place1) > 0);
		
		place1 = new Place(name+"foo", position);
		place2 = new Place(name, position);
		assertTrue(!place1.equals(place2));
		assertTrue(place1.hashCode() != place2.hashCode());
		assertTrue(place1.compareTo(place2) > 0);
		assertTrue(place2.compareTo(place1) < 0);
		
		place1 = new Place(name, null);
		place2 = new Place(name, null);
		assertTrue(place1.equals(place2));
		assertTrue(place1.hashCode() == place2.hashCode());
		assertTrue(place1.compareTo(place2) == 0);
		assertTrue(place2.compareTo(place1) == 0);
		
		place1 = new Place(name, null);
		place2 = new Place(name, position);
		assertTrue(!place1.equals(place2));
		assertTrue(place1.compareTo(place2) < 0);
		assertTrue(place2.compareTo(place1) > 0);
		assertTrue(place1.hashCode() != place2.hashCode());
		
		place1 = new Place(name, position);
		place2 = new Place(name, new Position(-position.getLatitude(),position.getLongitude(),0));
		assertTrue(!place1.equals(place2));
		assertTrue(place1.hashCode() != place2.hashCode());
		assertTrue(place1.compareTo(place2) < 0);
		assertTrue(place2.compareTo(place1) > 0);
		
		place1 = new Place(name, position);
		place2 = new Place(name, position);
		place1.getWaitingToEmbark().add(PersonTest.makeDummyPerson());
		assertTrue(!place1.equals(place2));
		assertTrue(place1.hashCode() != place2.hashCode());
	}
}
