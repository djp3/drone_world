package simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import reference.DroneControllerSkeleton;
import reference.MySimulationController;
import simulator.enums.DroneState;
import simulator.enums.PersonState;
import simulator.interfaces.DroneController;

public class SimulatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	
	
	//A class to control the parameters of a test simulation
	static public class SimulationTestParameters{
		int maxDronesPerController;
		int droneCapacity;
		int maxPeople;
		
		int droneCounter = 0;
		
		public SimulationTestParameters(int maxDronesPerController, int droneCapacity, int maxPeople) {
			this.maxDronesPerController = maxDronesPerController;
			this.droneCapacity = droneCapacity;
			this.maxPeople = maxPeople;
		}

		public void incrementCounter(){
			droneCounter++;
		}
		
		void setDroneCounter(int x){
			droneCounter = x;
		}
	}
	

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	//Helper functions
	private static Set<Place> loadTestPlaces() {
		Set<Place> ret = new TreeSet<Place>();
		ret.add(new Place("Winter Hall",new Position(34.448868,-119.6629439,0)));
		ret.add(new Place("SBCC",new Position(34.4060661,-119.69755,0)));
		
		ret.add(new Place("Show Grounds",new Position(34.4300057,-119.7363983,0)));
		ret.add(new Place("Santa Barbara Bowl",new Position(34.4351155,-119.6935015,0)));
		ret.add(new Place("Reservoir", new Position(34.4537095,-119.7277611,0)));
		
		ret.add(new Place("Trader Joe's",new Position(34.4392777,-119.7293757,0)));
		ret.add(new Place("Water Tower",new Position(34.4677583,-119.7480575,0)));
		ret.add(new Place("Mother Stearn's Candy",new Position(34.4097893,-119.6855427,0)));
		
		ret.add(new Place("Doctor Evil's Sub",new Position(34.3979696,-119.6640514,0)));
		ret.add(new Place("Dog Beach",new Position(34.4026544,-119.7426834,0)));
		
		return ret;
	}

	private static Set<Drone> loadTestDrones(SimulationTestParameters params, Set<Place> places,DroneController controller) {
		
		if((places == null) || (places.size() == 0)){
			throw new IllegalArgumentException("Places is badly formed");
		}
		
		if(controller == null){
			throw new IllegalArgumentException("Please supply a valid controller");
		}
		
		TreeSet<Drone> ret = new TreeSet<Drone>();
		for(int i = 0; i < params.maxDronesPerController ; i++){
			//Start all drones at the same spot
			Place thePlace = places.iterator().next();
			Drone drone = new Drone(controller,thePlace,thePlace,params.droneCapacity);
			drone.setState(DroneState.IDLING);
			ret.add(drone);
		}
		
		return (ret);
	}

	private static Set<Person> loadTestPeople(SimulationTestParameters params, Random random,Set<Place> places) {
		ArrayList<Place> randomizePlaces = new ArrayList<Place>();
		randomizePlaces.addAll(places);
		
		String[] namesFirst = {"Matthew", "Bethany", "Christian" , "Parker", "Jonathan" , "David", "Samuel" , "Jared", "Ryan", "Kyle", "Kathryn", "Devon", "Xinyu", "Bryan" , "Mark", "James" };
		List<String> randomizeFirst = Arrays.asList(namesFirst);
		
		String[] namesLast = { "Miller", "Le", "Alvo", "Leach", "Skidanov", "Spindler", "McCollum",	 "Wilkens",	 "Kleinberg",	 "Beall", "Hansen", "Mohrhoff",	 "Wear", "Coffman",	 "Yu", "Miner", "Carlson","Solum"};
		List<String> randomizeLast = Arrays.asList(namesLast);
		
		Set<Person> ret = new TreeSet<Person>();
		for(int i = 0; i < params.maxPeople ; i++){
			//Shuffling manually to make sure that we only use my random number generator for consistency
			for(int j = 0 ; j < randomizePlaces.size(); j++){
				int swapIndex = random.nextInt(randomizePlaces.size());
				Place foo = randomizePlaces.get(j);
				randomizePlaces.set(j,randomizePlaces.get(swapIndex));
				randomizePlaces.set(swapIndex,foo);
			}
			int start = random.nextInt(randomizePlaces.size());
			int end = random.nextInt(randomizePlaces.size());
			while((start == end) &&(randomizePlaces.size() > 1)){
				end = random.nextInt(randomizePlaces.size());
			}
			int first = random.nextInt(randomizeFirst.size());
			int last = random.nextInt(randomizeLast.size());
			
			Person person = new Person(""+i,randomizeFirst.get(first)+" "+randomizeLast.get(last),randomizePlaces.get(start).getName(),randomizePlaces.get(start).getPosition(),randomizePlaces.get(end).getName(),PersonState.WAITING);
			randomizePlaces.get(start).getWaitingToEmbark().add(person);
			ret.add(person);
		}
		
		return ret;
	}
	

	// Create a class to test the simulator
	static class TestManifest_SimulationController extends MySimulationController {

		@Override
		public boolean isHighResolution() {
			return false;
		}

		@Override
		public long simulatorSpeed() {
			return 100;
		}
		
	}

	// Create a class to test the drones
	static class TestManifest_DroneController extends DroneControllerSkeleton {
		
		Map<String,Drone> passengerIdleProcessed = Collections.synchronizedMap(new TreeMap<String,Drone>());
		Map<String,Drone> passengerPickups = Collections.synchronizedMap(new TreeMap<String,Drone>());
		Map<String,Drone> passengerNonPickups = Collections.synchronizedMap(new TreeMap<String,Drone>());

		private SimulationTestParameters params;

		TestManifest_DroneController(SimulationTestParameters params) {
			super();
			this.params = params;
		}

		@Override
		public void droneAscendingStart(Drone drone) {
			if(drone.getPassengers().size() != 0){
				passengerPickups.put(drone.getName(), drone);
			}
			else{
				passengerNonPickups.put(drone.getName(), drone);
			}
		}

		@Override
		public void droneIdling(Drone drone) {
			// Only pickup passengers once
			if ((!passengerPickups.containsKey(drone.getName())) && (!passengerNonPickups.containsKey(drone.getName())) && (!passengerIdleProcessed.containsKey(drone.getName()))){
				Simulator s = getSimulator();

				// Set manifest to all places
				Set<String> places = new TreeSet<String>();
				for (Place p : s.getPlaces()) {
					places.add(p.getName());
				}
				s.setDroneManifest(drone, places);

				// Figure out where this drone should go
				ArrayList<String> placeList = new ArrayList<String>();
				placeList.addAll(places);
				// Remove current place
				placeList.remove(drone.getStart().getName());

				// Use name to pick the next place so drone's go different
				// places
				int index = Integer.parseInt(drone.getName());

				s.routeDrone(drone, placeList.get(index));
				passengerIdleProcessed.put(drone.getName(), drone);
			}
		}

		@Override
		public String getNextDroneName() {
			String answer = "" + params.droneCounter;
			params.incrementCounter();
			return answer;
		}

		@Override
		public String getCompanyName() {
			return "Test Controller";
		}

	}
	
	

	@Test
	//This test is to make sure that a drone that sets it's manifest as everywhere correctly picks up passengers regardless of where it says it's going
	public void testManifest() {
		//Set up the simulation parameters
		int maxDronesPerController = 5;
		int droneCapacity = 1;
		int maxPeople = 100;
		SimulationTestParameters simParams = new SimulationTestParameters(maxDronesPerController,droneCapacity,maxPeople);
		
		//Make a simulation controller
		MySimulationController simController = new TestManifest_SimulationController();
		
		//Generate the places
		Set<Place> places = loadTestPlaces();
		
		//Generate the drones
		Set<Drone> drones = new TreeSet<Drone>();
		TestManifest_DroneController controller = new TestManifest_DroneController(simParams);
		drones.addAll(loadTestDrones(simParams,places,controller));
		
		//Generate people
		Set<Person> people = loadTestPeople(simParams,simController.getRandom(),places);
		
		//Build simulator
		Simulator simulator = new Simulator(simController,people,places,drones);
		
		//Start it up
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				simulator.start();
			}
		});
		thread.start();
		
		long start = System.currentTimeMillis();
		boolean pickupComplete = false;
		while(!pickupComplete){
			//Check if all drones have been serviced at idle
			if(controller.passengerIdleProcessed.size() == maxDronesPerController){
				pickupComplete = true;
			}
			else{
				if((System.currentTimeMillis() - start)> 1000){
					fail("Picking up passengers took too long");
				}
				else{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
		//Check if all drones have lifted off
		
		start = System.currentTimeMillis();
		boolean ascensionStarted = false;
		while(!ascensionStarted){
			if((controller.passengerPickups.size() + controller.passengerNonPickups.size()) == maxDronesPerController){
				ascensionStarted = true;
			}
			else{
				if((System.currentTimeMillis() - start)> 1000){
					fail("Getting all drones to ascend took too long");
				}
				else{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
		assertEquals(0,controller.passengerNonPickups.size());
		assertEquals(maxDronesPerController,controller.passengerPickups.size());
		
		simulator.end("Test complete");
		
		while(thread.isAlive()){
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

}
