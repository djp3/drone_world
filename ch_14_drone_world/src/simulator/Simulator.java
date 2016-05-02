package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import reference.GreedyController;
import reference.MyDroneController;
import reference.MySimulationController;
import reference.PromiscuousController;
import reference.RandomDroneController;
import reference.TestController;
import simulator.enums.DroneState;
import simulator.enums.PersonState;
import simulator.interfaces.DroneController;
import simulator.interfaces.SimulationController;
import visualization.DroneWorld;

public class Simulator {
	
	public static final int MAX_DRONES_PER_CONTROLLER = 5;
	private static final int DRONE_CAPACITY = 1;
	public static final int MAX_PEOPLE = 100;
	
	private static final long SIMULATION_SPEED = 100;
	private static final boolean PEOPLE_ALWAYS_BOARD_DRONE = false;
	private static final boolean PEOPLE_ALWAYS_DISEMBARK_DRONE = false;
	private static final int TRANSIT_HEIGHT = 2;
	
	private SimulationController simulationController;
	
	//Simulator time in milliseconds
	private long clockTick;
	
	//The reference set of objects in the simulator
	private Set<Drone> drones;
	private Set<Person> people;
	private Set<Place> places;
	
	//Flags to end the simulation
	private boolean simulationEnded;
	private boolean quitting;

	public Simulator(SimulationController simulationController,Collection<Person> people,Collection<Place> places,Collection<Drone> drones){
		
		if(simulationController == null){
			throw new IllegalArgumentException("Can't give me a null simulation controller");
		}
		
		this.simulationController = simulationController;
		
		this.people = new TreeSet<Person>();
		if(people != null){
			for(Person p: people){
				this.people.add(p);
			}
		}
		
		this.places = new TreeSet<Place>();
		if(places != null){
			for(Place p: places){
				this.places.add(p);
			}
		}
		
		this.drones = new TreeSet<Drone>();
		if(drones != null){
			for(Drone d: drones){
				this.drones.add(d);
				d.getController().setSimulator(this);
			}
		}
	}




	public void start(){
		quitting = false;
		simulationEnded = false;
		
		clockTick = -SIMULATION_SPEED;
		long previousTime;
		long currentTime = System.currentTimeMillis();
		
		//Set up the speed of the simulation
		long factor = simulationController.simulatorSpeed();
		if(factor <= 0){
			factor = 1;
		}
		else if (factor > SIMULATION_SPEED){
			factor = SIMULATION_SPEED;
		}
		long waitTime = SIMULATION_SPEED/factor;
		
		//The main loop
		while(!quitting && !simulationEnded){
			simulationEnded = true;
			
			previousTime = currentTime;
			currentTime = System.currentTimeMillis();
			
			while((currentTime - previousTime) < waitTime){
				try {
					Thread.sleep(waitTime - (currentTime-previousTime));
				} catch (InterruptedException e) {
				}
				currentTime = System.currentTimeMillis();
			}
			
			clockTick += SIMULATION_SPEED;
			
			//Shuffle drones so that different drones get random priority on each round
			//Shuffling manually to make sure that we only use a managed random number generator for consistency
			ArrayList<Drone> shuffledDrones = new ArrayList<Drone>();
			shuffledDrones.addAll(drones);
			for(int j = 0 ; j < shuffledDrones.size(); j++){
				int swapIndex = simulationController.getRandom().nextInt(shuffledDrones.size());
				Drone foo = shuffledDrones.get(j);
				shuffledDrones.set(j,shuffledDrones.get(swapIndex));
				shuffledDrones.set(swapIndex,foo);
			}
			
			for(Drone drone:shuffledDrones){
				switch (drone.getState()){
					case BEGIN:{
						simulationEnded = false;
						drone.getController().droneEmbarkingStart(new Drone(drone));
						drone.setState(DroneState.EMBARKING);
					}
					break;
					case EMBARKING:{
						simulationEnded = false;
						//Check to see if the passengers have had enough time to get onboard 
						if((clockTick - drone.getEmbarkingStart()) > drone.getEmbarkingDuration()){
							
							boolean embarkingSome = (drone.getEmbarkers().size() > 0);
							for(Person person:drone.getEmbarkers()){
								if(!drone.getEmbarkers().remove(person)){
									throw new RuntimeException("Whey didn't the person get on board?");
								}
								person.setState(PersonState.IN_DRONE);
								if(!drone.getPassengers().add(person)){
									throw new RuntimeException("Whey didn't the person get become a passenger?");
								}
							}
							
							if(embarkingSome){
								drone.getController().droneEmbarkingAGroupEnd(new Drone(drone));
							}
							
							// If the drone is full then it takes off
							if(drone.getPassengers().size() == drone.getCapacity()){
								droneTakeOff(drone);
							}
							else if(drone.getPassengers().size() > drone.getCapacity()){
								throw new IllegalArgumentException("Somehow we overloaded the drone"+drone);
							}
							else{
								//Figure out who is still waiting to board this drone 
								LinkedList<Person> waiting = new LinkedList<Person>();
								for(Person person: drone.getStart().getWaitingToEmbark()){
									if((drone.getManifest().contains(person.getDestination())) || (PEOPLE_ALWAYS_BOARD_DRONE)){
										waiting.add(person);
									}
								}
								//If no one is waiting then the drone takes off
								if(waiting.size() == 0){
									droneTakeOff(drone);
								}
								else{
									// Figure out how many people to load
									int nextEmbarkGroupSize = drone.getEmbarkingCapacity();
									int remainingCapacity = drone.getCapacity() - drone.getPassengers().size();
									if(remainingCapacity < nextEmbarkGroupSize){
										nextEmbarkGroupSize = remainingCapacity;
									}
									if(waiting.size() < nextEmbarkGroupSize){
										nextEmbarkGroupSize = waiting.size();
									}
									if(nextEmbarkGroupSize == 0){
										throw new IllegalArgumentException("We should have already accounted for all cases where this is 0");
									}
									else{
										drone.setEmbarkingStart(clockTick);
										for(int i = 0; i < nextEmbarkGroupSize;i++){
											Person loadMe = waiting.remove();
											//Remove them from the place
											if(!drone.getStart().getWaitingToEmbark().remove(loadMe)){
												throw new RuntimeException("Why didn't the person embark?");
											}
											loadMe.setDeliveryCompany(drone.getCompanyName());
											loadMe.setStartTransitTime(clockTick);
											loadMe.setState(PersonState.EMBARKING);
											drone.getEmbarkers().add(loadMe);
										}
										drone.getController().droneEmbarkingAGroupStart(new Drone(drone));
									}
								}
							}
						}
					}
					break;
					case ASCENDING:{
						simulationEnded = false;
						long timeToGo = drone.getTransitStart() - clockTick;
						if(timeToGo > 0){
							double percentage = timeToGo/(0.0+drone.getAscensionTime());
							double currentHeight = TRANSIT_HEIGHT - percentage*TRANSIT_HEIGHT;
							drone.getPosition().setHeight(currentHeight);
						}
						else{
							drone.getController().droneAscendingEnd(new Drone(drone));
							drone.setState(DroneState.IN_TRANSIT);
							drone.getController().droneTransitingStart(new Drone(drone));
						}
					}
					break;
					case IN_TRANSIT:{
						simulationEnded = false;
						//How far the drone has to go from it's current position to it's destination
						double metersToGoal = DistanceCalculator.distance(drone.getPosition().getLatitude(),drone.getPosition().getLongitude(),drone.getDestination().getPosition().getLatitude(),drone.getDestination().getPosition().getLongitude());
						//How far the drone had to go from it's original destination at launch to it's current destination (It's destination might have changed)
						double metersForTrip = DistanceCalculator.distance(drone.getStart().getPosition().getLatitude(),drone.getStart().getPosition().getLongitude(),drone.getDestination().getPosition().getLatitude(),drone.getDestination().getPosition().getLongitude());
						if(metersToGoal <= 0){
							metersToGoal = 1;
						}
						double speed = drone.getSpeed();
						
						//How long it should take for the drone to get to the destination
						//double totalTimeInSeconds = metersToGoal/speed;
						//double totalTimeInMilliSeconds = totalTimeInSeconds *1000;
					
						//Time since the drone started this transit (it may have been redirected enroute)
						//double start = drone.getTransitStart();
						//double duration = clockTick - start;
						
						//Move the drone forward 
						double metersPerTick = speed *(SIMULATION_SPEED /1000.0);
						
						//Deduct charge
						double charge = drone.getCharge();
						//Percent per meter
						double dischargeRate = 0.0002;
						charge = charge - (metersPerTick*dischargeRate); 
						if(charge < 0.0 ){
							charge = 0.0;
						}
						drone.setCharge(charge);
						
						// What percentage of the distance to goal was covered in the last simulation tick
						double percentage = metersPerTick/metersToGoal;
						
						//Close enough to call it an arrival
						if(metersPerTick >= metersToGoal){
							drone.getPosition().setLatitude(drone.getDestination().getPosition().getLatitude());
							drone.getPosition().setLongitude(drone.getDestination().getPosition().getLongitude());
							drone.getPosition().setHeight(drone.getDestination().getPosition().getHeight());
							
							//Arrival
							drone.setTransitEnd(clockTick+drone.getDescensionTime());
							drone.getController().droneTransitingEnd(new Drone(drone));
							drone.setState(DroneState.DESCENDING);
							drone.getController().droneDescendingStart(new Drone(drone));
						}
						else{
							Position a = drone.getPosition();
							Position b = drone.getDestination().getPosition();
							double latitude = (b.getLatitude()-a.getLatitude())*percentage+a.getLatitude();
							double longitude = (b.getLongitude()-a.getLongitude())*percentage+a.getLongitude();
							double height = (b.getHeight()-a.getHeight())*percentage+ TRANSIT_HEIGHT;
						
							drone.getPosition().setLatitude(latitude);
							drone.getPosition().setLongitude(longitude);
							drone.getPosition().setHeight(height);
						}
						if(drone.getEmbarkers().size() != 0){
							throw new IllegalStateException("Simulator Error:There shouldn't be anyone embarking if we are in transit");
						}
						if(drone.getDisembarkers().size() != 0){
							throw new IllegalStateException("Simulator Error:There shouldn't be anyone disembarking if we are in transit");
						}
						for(Person p: drone.getPassengers()){
							p.setPosition(drone.getPosition());
						}
						
						/* Call back to controller */
						drone.getController().droneTransiting(new Drone(drone), 1.0-(metersToGoal/metersForTrip));
					}
					break;
					case DESCENDING:{
						simulationEnded = false;
						long timeToGo = drone.getTransitEnd() - clockTick;
						if(timeToGo > 0){
							double percentage = timeToGo/(0.0+drone.getDescensionTime());
							double currentHeight = percentage*TRANSIT_HEIGHT;
							drone.getPosition().setHeight(currentHeight);
						}
						else{
							droneLand(drone);
						}
					}
					break;
					case DISEMBARKING:{
						simulationEnded = false;
						//If we are done with the last set of disembarkers
						if((clockTick - drone.getDisembarkingStart()) > drone.getDisembarkingDuration()){
							boolean disembarkingSome = (drone.getDisembarkers().size() > 0);
							for(Person person:drone.getDisembarkers()){
								drone.getDisembarkers().remove(person);
								person.setState(PersonState.ARRIVED);
								person.setEndTransitTime(clockTick);
								person.setPosition(new Position(drone.getDestination().getPosition()));
								//Do something with person after they arrived
								//drone.getDestination().getWaitingToEmbark().add(person);
							}
							if(disembarkingSome){
								drone.getController().droneDisembarkingGroupEnd(new Drone(drone));
							}
							//Find all the people who still want to disembark
							LinkedList<Person> waiting = new LinkedList<Person>();
							for(Person person:drone.getPassengers()){
								if((person.getDestination().equals(drone.getDestination().getName())) || (PEOPLE_ALWAYS_DISEMBARK_DRONE)){
									waiting.add(person);
								}
							}
							if(waiting.size() == 0){
								droneStartRecharging(drone);
							}
							else{
								// Figure out how many people to unload
								int nextDisembarkGroupSize = drone.getDisembarkingCapacity();
								if(waiting.size() < nextDisembarkGroupSize){
									nextDisembarkGroupSize = waiting.size();
								}
								if(nextDisembarkGroupSize == 0){
									throw new IllegalArgumentException("We should have already accounted for all cases where this is 0");
								}
								else{
									for(int i =0; i< nextDisembarkGroupSize; i++){
										Person person = waiting.remove();
										/*
										Person p = drone.getPassengers().iterator().next();
										System.out.println(p.toString());
											System.out.println("\t\t"+p.hashCode());
										System.out.println(person.toString());
											System.out.println("\t\t"+person.hashCode());
										if(person.equals(p)){
											System.out.println("They are equal");
										}
										else{
											System.out.println("They are not equal");
										}
										Set<Person> passengers = drone.getPassengers();
										for(Person p2: passengers){
											System.out.println("\t"+p2.toString());
											System.out.println("\t\t"+p2.hashCode());
										}
										*/
										if(!drone.getPassengers().remove(person)){
											throw new RuntimeException("Why didn't the person get removed?");
										}
										person.setState(PersonState.DISEMBARKING);
										drone.getDisembarkers().add(person);
									}
									drone.getController().droneDisembarkingGroupStart(new Drone(drone));
									drone.setDisembarkingStart(clockTick);
								}
							}
						}
					}
					break;
					case RECHARGING:{
						simulationEnded = false;
						
						//If the controller has told the drone to leave
						if(!drone.getStart().equals(drone.getDestination())){
							drone.getController().droneDoneRecharging(new Drone(drone));
							drone.setState(DroneState.BEGIN);
						}
						else{
							double chargeDelta = (SIMULATION_SPEED/1000.0) * drone.getRechargeRate() ;
							if(drone.getCharge()+ chargeDelta > 1.0){
								drone.setCharge(1.0);
								drone.getController().droneDoneRecharging(new Drone(drone));
								drone.setState(DroneState.IDLING);
							}
							else{
								boolean alert = false;
								double[] breaks = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
								for(int i = 0; i < breaks.length; i++){
									if((drone.getCharge() < breaks[i]) && ((drone.getCharge()+chargeDelta) >= breaks[i])){
										alert = true;
									}
								}
								drone.setCharge(drone.getCharge()+chargeDelta);
								if(alert){
									drone.getController().droneRecharging(new Drone(drone),drone.getCharge());
								}
							}
						}
					}
					break;
					case IDLING:{
						simulationEnded = false;
						
						for(Person p: drone.getPassengers()){
							p.setState(PersonState.IN_DRONE);
						}
						
						if(!drone.getStart().equals(drone.getDestination())){
							drone.setState(DroneState.BEGIN);
						}
						else{
							drone.getController().droneIdling(new Drone(drone));
						}
					}
					break;
					default:
						throw new IllegalArgumentException("Unhandled Drone State: "+drone.getState());
				}
			}
			//Check to see if all passengers are delivered
			boolean allDone = true;
			for(Person p: people){
				if(!p.getState().equals(PersonState.ARRIVED)){
					allDone = false;
				}
			}
			if(allDone){
				simulationEnded = true;
				System.out.println("Simulation ended with all passengers delivered at time "+clockTick);
			}
		}
	}
	
	public void end(String reason){
		System.out.println("Simulation ending");
		if(reason != null){
			System.out.println("\t"+reason);
		}
		quitting = true;
	}


	private void droneStartRecharging(Drone drone) {
		drone.getController().droneDisembarkingEnd(new Drone(drone));
		drone.setState(DroneState.RECHARGING);
		drone.getController().droneRechargingStart(new Drone(drone));
	}



	private void droneTakeOff(Drone drone){
		drone.getController().droneEmbarkingEnd(new Drone(drone));
		drone.setState(DroneState.ASCENDING);
		drone.getController().droneAscendingStart(new Drone(drone));
		drone.setTransitStart(clockTick+drone.getAscensionTime());
	}
	
	private void droneLand(Drone drone) {
		drone.getController().droneDescendingEnd(new Drone(drone));
		drone.setStart(drone.getDestination());
		drone.setState(DroneState.DISEMBARKING);
		drone.getController().droneDisembarkingStart(new Drone(drone));
		//Make sure that disembarking starts by setting the last disembark time to before the simulation started
		drone.setDisembarkingStart(-drone.getDisembarkingDuration());
	}
	
	public boolean isHighResolution(){
		return this.simulationController.isHighResolution();
	}
	
	/**
	 * Returns a copy of all the places in the simulation
	 * @return
	 */
	public TreeSet<Place> getPlaces(){
		TreeSet<Place> ret = new TreeSet<Place>();
		if(places != null){
			for(Place p: places){
				ret.add(new Place(p));
			}
		}
		return ret;
	};
	
	/**
	 * Returns a copy of all the drones in the simulation
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public TreeSet<Drone> getDrones(){ 
		TreeSet<Drone> ret = new TreeSet<Drone>();
		if(drones != null){
			for(Drone d: drones){
				ret.add(new Drone(d));
			}
		}
		return ret;
	};
	
	/**
	 * Returns a copy of all the people in the simulation
	 * @return
	 */
	public TreeSet<Person> getPeople(){
		TreeSet<Person> ret = new TreeSet<Person>();
		if(people != null){
			for(Person p: people){
				ret.add(new Person(p));
			}
		}
		return ret;
	};
	
	

	/**
	 * 
	 * @return the current simulated clock time in milliseconds
	 */
	public long getClockTick() {
		return clockTick;
	}
	
	public SimulationController getSimulationController(){
		return this.simulationController;
	}

	/******************************************************************/
	/* Set up the simulation */

	private static Set<Place> loadPlaces() {
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

	private static Set<Drone> loadDrones(Set<Place> places,DroneController controller) {
		
		if((places == null) || (places.size() == 0)){
			throw new IllegalArgumentException("Places is badly formed");
		}
		
		if(controller == null){
			throw new IllegalArgumentException("Please supply a valid controller");
		}
		
		TreeSet<Drone> ret = new TreeSet<Drone>();
		for(int i = 0; i < MAX_DRONES_PER_CONTROLLER ; i++){
			//Start all drones at the same spot
			Place thePlace = places.iterator().next();
			Drone drone = new Drone(controller,thePlace,thePlace,DRONE_CAPACITY);
			drone.setState(DroneState.IDLING);
			ret.add(drone);
		}
		
		return (ret);
	}

	private static Set<Person> loadPeople(Random random,Set<Place> places) {
		ArrayList<Place> randomizePlaces = new ArrayList<Place>();
		randomizePlaces.addAll(places);
		
		String[] namesFirst = {"Matthew", "Bethany", "Christian" , "Parker", "Jonathan" , "David", "Samuel" , "Jared", "Ryan", "Kyle", "Kathryn", "Devon", "Xinyu", "Bryan" , "Mark", "James" };
		List<String> randomizeFirst = Arrays.asList(namesFirst);
		
		String[] namesLast = { "Miller", "Le", "Alvo", "Leach", "Skidanov", "Spindler", "McCollum",	 "Wilkens",	 "Kleinberg",	 "Beall", "Hansen", "Mohrhoff",	 "Wear", "Coffman",	 "Yu", "Miner", "Carlson","Solum"};
		List<String> randomizeLast = Arrays.asList(namesLast);
		
		Set<Person> ret = new TreeSet<Person>();
		for(int i = 0; i < MAX_PEOPLE ; i++){
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

	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		//Make a simulation controller
		MySimulationController simController = new MySimulationController();
		
		//Generate the places
		Set<Place> places = Simulator.loadPlaces();
		
		//Generate the drones
		Set<Drone> drones = new TreeSet<Drone>();
		//Add each companies drones here
		//drones.addAll(loadDrones(places,new RandomDroneController())); //Professor's Controller
		//drones.addAll(loadDrones(places,new PromiscuousController())); //Professor's Controller
		//drones.addAll(loadDrones(places,new GreedyController())); //Professor's Controller
		
		//drones.addAll(loadDrones(places,new MyDroneController())); //Student's Controller
		drones.addAll(loadDrones(places,new TestController())); //Student's Controller
		
		//Generate people
		Set<Person> people = loadPeople(simController.getRandom(),places);
		
		//Build simulator
		Simulator simulator = new Simulator(simController,people,places,drones);
		
		//Attach simulation to a visualizer
		DroneWorld visualization = new DroneWorld(simulator,people,places,drones);
		
		//Start it up
		visualization.launch();
	}

	
	/********************************************************************************/
	//These are the things that the student controller is supposed to be able to call
	
	/**
	 * MyController derivative classes call this function to send a drone to a new destination
	 * @param drone
	 * @param place
	 */
	public void routeDrone(Drone drone, Place place) {
		routeDrone(drone,place.getName());
	}
	
	/**
	 * Convenience method to refer to a place by a string
	 */
	public void routeDrone(Drone drone, String place) {
		boolean success = false;
		for(Drone d:drones){
			if(d.getId().equals(drone.getId())){
				for(Place p: places){
					if(p.getName().equals(place)){
						d.setDestination(p);
						success = true;
					}
				}
			}
		}
		if(!success){
			throw new IllegalArgumentException("Unable to find a drone with id:"+drone.getId()+" and/or a place called:"+place);
		}
	}
	
	/**
	 * MyController derivative classes call this function to announce the places they intend to go.
	 * Passengers that are going to these locations will board the drone.  Note this is different than where the drone is actually going.
	 * @param drone
	 * @param placeManifest, a set of place names that you want to tell the passengers you intend to go to in case you have to make a stop on the way
	 */
	public void setDroneManifest(Drone drone, Set<String> placeManifest) {
		boolean success = false;
		Set<String> validatedManifest = new TreeSet<String>();
		
		if(drone != null){
			if((placeManifest == null) || (placeManifest.size() == 0)){
				drone.setManifest(validatedManifest);
				success = true;
			}
			else{
				for(Place p: places){
					if(placeManifest.contains(p.getName())){
						validatedManifest.add(p.getName());
					}
				}
				for(Drone d:drones){
					if(d.getId().equals(drone.getId())){
						d.setManifest(validatedManifest);
						success = true;
					}
				}	
			}
		}
		if(!success){
			throw new IllegalArgumentException("Unable to find a drone with id:"+drone.getId());
		}
	}
	
	/**
	 * This is just an overloaded version of the set manifest in case the concept of provided multiple destinations is too hard for people to get
	 * @param drone
	 * @param placeManifest, where you tell the passengers you are going
	 */
	public void setDroneManifest(Drone drone, String placeManifest) {
		TreeSet<String> helper = new TreeSet<String>();
		helper.add(placeManifest);
		setDroneManifest(drone,helper);
	}

}
