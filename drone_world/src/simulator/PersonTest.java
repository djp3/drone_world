package simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulator.enums.PersonState;

public class PersonTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	public static Person makeDummyPerson(){
		Place capeTown = new Place("Cape Town",new Position(-33.9249,18.4241,0));
		Place paris = new Place("Paris",new Position(48.8566,2.3522,0));
		return new Person("01","Doug","Paris",paris.getPosition(),capeTown.getName(),PersonState.WAITING);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void test() {
		Place capeTown = new Place("Cape Town",new Position(-33.9249,18.4241,0.0));
		Place paris = new Place("Paris",new Position(48.8566,2.3522,0.0));
		Person doug = new Person("01","Doug",paris.getName(),paris.getPosition(),capeTown.getName(),PersonState.WAITING);
		assertTrue(doug.getName().equals("Doug"));
		assertTrue(doug.getStart().equals(paris.getName()));
		assertTrue(doug.getPosition().equals(paris.getPosition()));
		assertTrue(doug.getDestination().equals(capeTown.getName()));
		
		Person sarah = new Person(doug);
		assertTrue(sarah != doug);
		assertEquals(sarah,doug);
		assertTrue(sarah.hashCode() == doug.hashCode());
		assertTrue(sarah.compareTo(doug) == 0);
	}
	
	@Test
	public void testEquals() {
		Place capeTown = new Place("Cape Town",new Position(-33.9249,18.4241,0.0));
		Place paris = new Place("Paris",new Position(48.8566,2.3522,0.0));
		Person doug = new Person("01","Doug",paris.getName(),paris.getPosition(),capeTown.getName(),PersonState.WAITING);
		assertTrue(doug.getId().equals("01"));
		assertTrue(doug.getName().equals("Doug"));
		assertTrue(doug.getStart().equals(paris.getName()));
		assertTrue(doug.getPosition().equals(paris.getPosition()));
		assertTrue(doug.getDestination().equals(capeTown.getName()));
		
		/*
		Person sarah = new Person(doug);
		assertTrue(doug.equals(doug));
		assertTrue(doug.hashCode() == sarah.hashCode());
		assertTrue(!doug.equals(null));
		assertTrue(doug.compareTo(null) == 1);
		assertTrue(!doug.equals("Stringy McStringalot"));
		assertTrue(doug.compareTo(doug) == 0);
		
		doug = new Person(null,"Doug",paris.getName(),paris.getPosition(),capeTown.getName());
		sarah = new Person(null,"Doug",null,paris.getPosition(),capeTown.getName());
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		assertTrue(doug.compareTo(sarah) == 0);
		
		doug = new Person(null,"Doug",paris.getName(),paris.getPosition(),capeTown.getName());
		sarah = new Person("01","Doug",null,paris.getPosition(),capeTown.getName());
		assertTrue(!doug.equals(sarah));
		assertTrue(!sarah.equals(doug));
		assertTrue(doug.hashCode() != sarah.hashCode());
		assertTrue(doug.compareTo(sarah) < 0);
		assertTrue(sarah.compareTo(doug) > 0);
		
		doug = new Person("01","Doug",paris,capeTown);
		sarah = new Person("02","Doug",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		assertTrue(doug.compareTo(sarah) < 0);
		assertTrue(sarah.compareTo(doug) > 0);
		
		doug = new Person("01","Doug",paris,null);
		sarah = new Person("01","Doug",paris,null);
		assertTrue(doug.equals(sarah));
		assertTrue(doug.hashCode() == sarah.hashCode());
		assertTrue(doug.compareTo(sarah) == 0);
		
		doug = new Person("01","Doug",paris,null);
		sarah = new Person("01","Doug",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		assertTrue(doug.compareTo(sarah) == 0);
		
		doug = new Person("01","Doug",paris,paris);
		sarah = new Person("01","Doug",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		
		doug = new Person("01",null,paris,capeTown);
		sarah = new Person("01",null,paris,capeTown);
		assertTrue(doug.equals(sarah));
		assertTrue(doug.hashCode() == sarah.hashCode());
		
		doug = new Person("01",null,paris,capeTown);
		sarah = new Person("01","Sarah",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		
		doug = new Person("01","Doug",paris,capeTown);
		sarah = new Person("01","Sarah",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		
		doug = new Person("01","Doug",null,capeTown);
		sarah = new Person("01","Doug",null,capeTown);
		assertTrue(doug.equals(sarah));
		assertTrue(doug.hashCode() == sarah.hashCode());
		
		doug = new Person("01","Doug",null,capeTown);
		sarah = new Person("01","Doug",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		
		doug = new Person("01","Doug",capeTown,capeTown);
		sarah = new Person("01","Doug",paris,capeTown);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		
		doug = new Person("01","Doug",paris,capeTown);
		sarah = new Person("01","Doug",paris,capeTown);
		doug.setState(PersonState.ARRIVED);
		sarah.setState(PersonState.DISEMBARKING);
		assertTrue(!doug.equals(sarah));
		assertTrue(doug.hashCode() != sarah.hashCode());
		*/
	}

}
