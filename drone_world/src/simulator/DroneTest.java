package simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import reference.DroneControllerSkeleton;
import simulator.enums.DroneState;
import simulator.enums.PersonState;
import simulator.interfaces.DroneController;

public class DroneTest {
	
	Random r = new Random(0);

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
	public void testDegenerate() {
		String name = "Timbuktu";
		Position position = new Position(10.0,20.0,0.0);
		Place start = new Place(name, position);
		
		name = "Timbuk3";
		position = new Position(15.0,25.0,0.0);
		Place destination = new Place(name, position);
		
		try {
			new Drone(new DroneControllerSkeleton(),start,destination,0);
			fail("Capacity checkshould throw an exception");
		} catch(IllegalArgumentException e) {
			//capacity must be >= 0
		}
		
		try {
			new Drone(null);
			fail("Shouldn't be able to copy a null drone");
		} catch(IllegalArgumentException e) {
			//capacity must be >= 0
		}
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
		Person p = new Person("id", "name", start, position, destination, PersonState.IN_DRONE);
		Set<Person> passengers = d1.getPassengers();
		passengers.add(p);
		
		Drone d2 = null;
		d2 = new Drone(d1);
		
		assertEquals(d1,d1);
		assertTrue(d1.hashCode() == d1.hashCode());
		assertTrue(d1.compareTo(d1) == 0);
		assertTrue(d1.compareTo(null) == 1);
		
		String id = UUID.randomUUID().toString();
		d1.setId(null);
		d2.setId(id);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		assertTrue(d1.compareTo(d2) < 0);
		d2.setId(null);
		assertTrue(d1.compareTo(d2) == 0);
		d1.setId(id);
		assertTrue(d1.compareTo(d2) > 0);
		d2.setId(id);
		assertTrue(d1.compareTo(d2) == 0);
		
		String name1 = d1.getName();
		String name2 = d2.getName();
		d1.setName("a");
		d2.setName("b");
		assertTrue(d1.compareTo(d2) == 0);
		d1.setName(name1);
		d2.setName(name2);
		
		
		assertTrue(!d1.equals(null));
		assertTrue(!d1.equals("String"));
		
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		/* Make sure the manifest is accounted for */
		TreeSet<Place> treeSet = new TreeSet<Place>();
		treeSet.add(destination);
		d2.setManifest(treeSet);
		assertTrue(!d1.equals(d2));
		assertTrue(d1.hashCode() != d2.hashCode());
		
		treeSet = new TreeSet<Place>();
		treeSet.add(destination);
		d1.setManifest(treeSet);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		int x = r.nextInt();
		d1.setAscensionTime(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setAscensionTime(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		x = r.nextInt();
		d1.setCapacity(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setCapacity(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		double y = r.nextDouble();
		d1.setCharge(y);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setCharge(y);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		String z = UUID.randomUUID().toString();
		d1.setCompanyName(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setCompanyName(null);
		d2.setCompanyName(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setCompanyName(z);
		d1.setCompanyName(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setCompanyName(null);
		d2.setCompanyName(null);
		assertTrue(d1.equals(d2));
		assertTrue((d1.hashCode() == d2.hashCode()));
		d1.setCompanyName(z);
		d2.setCompanyName(z);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		DroneController c1 = d1.getController();
		DroneController c2 = new DroneControllerSkeletonTester();
		d1.setController(null);
		d2.setController(c2);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setController(c2);
		d2.setController(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setController(null);
		d2.setController(null);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		d1.setController(c1);
		d2.setController(c1);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		long b = r.nextLong();
		d1.setDescensionTime(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setDescensionTime(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Place place = d1.getDestination();
		d1.setDestination(null);
		d2.setDestination(place);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setDestination(place);
		d2.setDestination(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setDestination(null);
		d2.setDestination(null);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		d1.setDestination(place);
		d2.setDestination(place);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		place = d1.getStart();
		d1.setStart(null);
		d2.setStart(place);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setStart(place);
		d2.setStart(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setStart(null);
		d2.setStart(null);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		d1.setStart(place);
		d2.setStart(place);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		y = r.nextDouble();
		d1.setDischargeRate(y);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setDischargeRate(y);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Set<Person> disembarkers = d1.getDisembarkers();
		disembarkers.add(p);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.getDisembarkers().addAll(disembarkers);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		x = r.nextInt();
		d1.setDisembarkingCapacity(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setDisembarkingCapacity(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		x = r.nextInt();
		d1.setDisembarkingDuration(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setDisembarkingDuration(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setDisembarkingStart(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setDisembarkingStart(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Set<Person> embarkers = d1.getEmbarkers();
		embarkers.add(p);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.getEmbarkers().addAll(embarkers);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		x = r.nextInt();
		d1.setEmbarkingCapacity(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setEmbarkingCapacity(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		x = r.nextInt();
		d1.setEmbarkingDuration(x);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setEmbarkingDuration(x);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setEmbarkingStart(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setEmbarkingStart(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		z = UUID.randomUUID().toString();
		d1.setId(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setId(null);
		d2.setId(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setId(z);
		d1.setId(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setId(null);
		d2.setId(null);
		assertTrue(d1.equals(d2));
		assertTrue((d1.hashCode() == d2.hashCode()));
		d1.setId(z);
		d2.setId(z);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		z = UUID.randomUUID().toString();
		d1.setName(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setName(null);
		d2.setName(z);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setName(z);
		d1.setName(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setName(null);
		d2.setName(null);
		assertTrue(d1.equals(d2));
		assertTrue((d1.hashCode() == d2.hashCode()));
		d1.setName(z);
		d2.setName(z);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Set<Place> m = d1.getManifest();
		m.clear();
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.getManifest().addAll(d2.getManifest());
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Set<Person> ps = d1.getPassengers();
		ps.clear();
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.getPassengers().addAll(d2.getPassengers());
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		Position pos = new Position(r.nextDouble(), r.nextDouble(), r.nextDouble());
		d1.setPosition(null);
		d2.setPosition(pos);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setPosition(pos);
		d2.setPosition(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setPosition(null);
		d2.setPosition(null);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		d1.setPosition(pos);
		d2.setPosition(pos);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setRechargeRate(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setRechargeRate(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setSpeed(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setSpeed(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setTransitStart(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setTransitStart(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		b = r.nextLong();
		d1.setTransitEnd(b);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d2.setTransitEnd(b);
		assertTrue(d1.equals(d2));
		assertTrue(d1.hashCode() == d2.hashCode());
		
		DroneState s = d1.getState();
		d1.setState(null);
		d2.setState(s);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setState(s);
		d2.setState(null);
		assertTrue(!d1.equals(d2));
		assertTrue(!(d1.hashCode() == d2.hashCode()));
		d1.setState(null);
		d2.setState(null);
		assertTrue(d1.equals(d2));
		assertTrue((d1.hashCode() == d2.hashCode()));
		d1.setState(s);
		d2.setState(s);
		assertTrue(d1.equals(d2));
		assertTrue((d1.hashCode() == d2.hashCode()));
	}
	
	@Test
	public void testCompareTo() {
		String name = "Timbuktu";
	}
	
	@Test
	public void testQuarantine() {
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
		
		
		assertTrue(!d1.getState().equals(DroneState.QUARANTINED));
		assertTrue(!d2.getState().equals(DroneState.QUARANTINED));
		
		Person p = new Person("id", "name", start, position, destination, PersonState.IN_DRONE);
		Set<Person> passengers = d1.getPassengers();
		passengers.add(p);
		for(Person passenger:passengers) {
			assertTrue(!passenger.getState().equals(PersonState.QUARANTINED));
		}
		d1.quarantine();
		for(Person passenger:passengers) {
			assertTrue(passenger.getState().equals(PersonState.QUARANTINED));
		}
	}
	
	class DroneControllerSkeletonTester implements DroneController{

		@Override
		public void setSimulator(Simulator simulator) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getNextDroneName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getCompanyName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void droneSimulationStart(Drone d) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneSimulationEnd(Drone d) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneBehavingBadly(Drone d) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneEmbarkingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneEmbarkingAGroupStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneEmbarkingAGroupEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneEmbarkingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneAscendingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneAscendingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneTransitingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneTransiting(Drone drone, double percent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneTransitingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneExploding(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneHasDied(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDescendingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDescendingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDisembarkingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDisembarkingGroupStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDisembarkingGroupEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneDisembarkingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneRechargingStart(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneRecharging(Drone drone, double percent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneRechargingEnd(Drone drone) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void droneIdling(Drone drone) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
