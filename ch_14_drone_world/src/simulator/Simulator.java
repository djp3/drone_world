package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import reference.MyController;
import reference.MyNewController;
import visualization.DroneWorld;

public class Simulator {
	
	public static final int MAX_DRONES = 1;
	public static final int MAX_PEOPLE = 6;
	private static final long SIMULATION_SPEED = 100;
	private static final boolean PEOPLE_ALWAYS_BOARD_DRONE = false;
	private static final boolean PEOPLE_ALWAYS_DISEMBARK_DRONE = false;
	private static final int DELIVER_PERSON_SCORE = 100;
	private static final int TRANSIT_HEIGHT = 2;
	private static Random random = new Random(10);
	
	//Simulator time in milliseconds
	private long clockTick;
	
	private int score;
	
	private Set<Drone> drones;
	private Set<Person> people;
	private Controller controller;
	private Set<Place> places;
	private boolean simulationEnded;
	private boolean quitting;

	public Simulator(Collection<Person> people,Collection<Place> places,Collection<Drone> drones,Controller controller){
		
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
			}
		}
		
		this.controller = controller;
		controller.setSimulator(this);
	}


	private void drawScore(){
		//System.out.println("Score: "+score);
	}
	
	private void drawTime(){
		//System.out.println("Time: "+clockTick);
	}
	
	
	
	private void drawMap() {
		/*
		System.out.println("Places:");
		for(Place place: this.places){
			System.out.print("\t"+place.getName()+" ");
			for(int i=0;i< place.getWaitingToEmbark().size();i++){
				System.out.print("*");
			}
			System.out.println("");
		}*/
		
	}


	public void end(String reason){
		System.out.println("Simulation ending");
		if(reason != null){
			System.out.println("\t"+reason);
		}
		quitting = true;
	}

	public void start() {
		quitting = false;
		simulationEnded = false;
		
		score = 0;
		clockTick = -SIMULATION_SPEED;
		long previousTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();
		long waitTime = SIMULATION_SPEED/10;
		
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
			drawScore();
			drawTime();
			drawMap();
			for(Drone drone:drones){
				switch (drone.getState()){
					case BEGIN:{
						simulationEnded = false;
						controller.droneEmbarkingStart(new Drone(drone));
						drone.setState(DroneState.EMBARKING);
					}
					break;
					case EMBARKING:{
						simulationEnded = false;
						//Check to see if the passengers have had enough time to get onboard 
						if((clockTick - drone.getEmbarkingStart()) > drone.getEmbarkingDuration()){
							
							boolean embarkingSome = (drone.getEmbarkers().size() > 0);
							for(Person person:drone.getEmbarkers()){
								drone.getEmbarkers().remove(person);
								person.setState(PersonState.IN_DRONE);
								drone.getPassengers().add(person);
							}
							
							if(embarkingSome){
								controller.droneEmbarkingAGroupEnd(new Drone(drone));
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
									if((person.getDestination().equals(drone.getDestination().getName())) || (PEOPLE_ALWAYS_BOARD_DRONE)){
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
											drone.getStart().getWaitingToEmbark().remove(loadMe);
											loadMe.setState(PersonState.EMBARKING);
											drone.getEmbarkers().add(loadMe);
										}
										controller.droneEmbarkingAGroupStart(new Drone(drone));
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
							double percentage = timeToGo/(0.0+drone.getAscentionTime());
							double currentHeight = TRANSIT_HEIGHT - percentage*TRANSIT_HEIGHT;
							drone.getPosition().setHeight(currentHeight);
						}
						else{
							controller.droneAscendingEnd(new Drone(drone));
							drone.setState(DroneState.IN_TRANSIT);
							controller.droneTransitingStart(new Drone(drone));
						}
					}
					break;
					case IN_TRANSIT:{
						simulationEnded = false;
						double metersToGoal = DistanceCalculator.distance(drone.getStart().getPosition().getLatitude(),drone.getStart().getPosition().getLongitude(),drone.getDestination().getPosition().getLatitude(),drone.getDestination().getPosition().getLongitude());
						if(metersToGoal <= 0){
							metersToGoal = 1;
						}
						double speed = drone.getSpeed();
						double totalTimeInSeconds = metersToGoal/speed;
						double totalTimeInMilliSeconds = totalTimeInSeconds *1000;
					
						double start = drone.getTransitStart();
						double duration = clockTick - start;
						double percentage = duration/totalTimeInMilliSeconds;
						Position a = drone.getStart().getPosition();
						Position b = drone.getDestination().getPosition();
						double latitude = (b.getLatitude()-a.getLatitude())*percentage+a.getLatitude();
						double longitude = (b.getLongitude()-a.getLongitude())*percentage+a.getLongitude();
						double height = (b.getHeight()-a.getHeight())*percentage+a.getHeight() + TRANSIT_HEIGHT;
						drone.getPosition().setLatitude(latitude);
						drone.getPosition().setLongitude(longitude);
						drone.getPosition().setHeight(height);
						if(percentage >= 1.0){
							drone.setTransitEnd(clockTick+drone.getDescentionTime());
							drone.setCharge(0.0);
							controller.droneTransitingEnd(new Drone(drone));
							drone.setState(DroneState.DESCENDING);
							controller.droneDescendingStart(new Drone(drone));
						}
					}
					break;
					case DESCENDING:{
						simulationEnded = false;
						long timeToGo = drone.getTransitEnd() - clockTick;
						if(timeToGo > 0){
							double percentage = timeToGo/(0.0+drone.getDescentionTime());
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
								person.setPosition(new Position(drone.getDestination().getPosition()));
								personArrived(person);
								//Do something with person after they arrived
								//drone.getDestination().getWaitingToEmbark().add(person);
							}
							if(disembarkingSome){
								controller.droneDisembarkingGroupEnd(new Drone(drone));
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
										drone.getPassengers().remove(person);
										person.setState(PersonState.DISEMBARKING);
										drone.getDisembarkers().add(person);
									}
									controller.droneDisembarkingGroupStart(new Drone(drone));
									drone.setDisembarkingStart(clockTick);
								}
							}
						}
					}
					break;
					case RECHARGING:{
						simulationEnded = false;
						long duration = clockTick - drone.getRechargeStartTime();
						double percent = duration/drone.getRechargeRate();
						if(percent > 1.0){
							percent = 1.0;
							drone.setCharge(percent);
							controller.droneDoneRecharging(new Drone(drone));
							drone.setState(DroneState.IDLING);
						}
						else{
							boolean alert = false;
							double[] breaks = {0.3,0.6,0.9,1.0};
							for(int i = 0; i < breaks.length; i++){
								if((drone.getCharge() < breaks[i]) && (percent >= breaks[i])){
									alert = true;
								}
							}
							drone.setCharge(percent);
							if(alert){
								controller.droneRecharging(new Drone(drone),percent);
							}
						}
					}
					break;
					case IDLING:{
						simulationEnded = false;
						controller.droneIdling(new Drone(drone));
						if(!drone.getStart().equals(drone.getDestination())){
							drone.setState(DroneState.BEGIN);
						}
					}
					break;
					default:
						throw new IllegalArgumentException("Unhandled Drone State: "+drone.getState());
				}
			}
		}
	}




	private void droneDisembarkingGroupStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}



	private void droneStartRecharging(Drone drone) {
		controller.droneDisembarkingEnd(new Drone(drone));
		drone.setState(DroneState.RECHARGING);
		controller.droneRechargingStart(new Drone(drone));
		drone.setRechargeStartTime(clockTick);
	}


	private void personArrived(Person person) {
		score += DELIVER_PERSON_SCORE;
	}


	private void droneTakeOff(Drone drone) {
		controller.droneEmbarkingEnd(new Drone(drone));
		drone.setState(DroneState.ASCENDING);
		controller.droneAscendingStart(new Drone(drone));
		drone.setTransitStart(clockTick+drone.getAscentionTime());
	}
	
	private void droneLand(Drone drone) {
		controller.droneDescendingEnd(new Drone(drone));
		drone.setStart(drone.getDestination());
		drone.setState(DroneState.DISEMBARKING);
		controller.droneDisembarkingStart(new Drone(drone));
		//Make sure that disembarking starts by setting the last disembark time to before the simulation started
		drone.setDisembarkingStart(-drone.getDisembarkingDuration());
	}
	
	public TreeSet<Place> getPlaces(){
		TreeSet<Place> ret = new TreeSet<Place>();
		if(places != null){
			for(Place p: places){
				ret.add(new Place(p));
			}
		}
		return ret;
	};
	
	public TreeSet<Drone> getDrones(){
		TreeSet<Drone> ret = new TreeSet<Drone>();
		if(drones != null){
			for(Drone d: drones){
				ret.add(new Drone(d));
			}
		}
		return ret;
	};
	

	private static Set<Place> loadPlaces() {
		Set<Place> ret = new TreeSet<Place>();
		ret.add(new Place("Winter Hall",new Position(34.448868,-119.6629439,0)));
		ret.add(new Place("SBCC",new Position(34.4060661,-119.69755,0)));
		ret.add(new Place("Show Grounds",new Position(34.4300057,-119.7363983,0)));
		//ret.add(new Place("Dining Commons",new Position(34.4495896,-119.6602979)));
		//ret.add(new Place("Page Hall", new Position(34.4517064,-119.6623229)));
		return ret;
	}

	private static Set<Drone> loadDrones(Set<Place> places) {
		ArrayList<Place> randomize = new ArrayList<Place>();
		randomize.addAll(places);
		
		TreeSet<Drone> ret = new TreeSet<Drone>();
		
		for(int i = 0; i < MAX_DRONES ; i++){
			int start = random.nextInt(randomize.size());
			int end = random.nextInt(randomize.size());
			while((start == end) && (randomize.size() > 1)){
				end = random.nextInt(randomize.size());
			}
			Place startPlace = randomize.get(start);
			Place endPlace = randomize.get(end);
			Drone drone = new Drone(startPlace,endPlace,1);
			drone.setState(DroneState.BEGIN);
			ret.add(drone);
		}
		
		return (ret);
	}

	private static Set<Person> loadPeople(Set<Place> places) {
		ArrayList<Place> randomizePlaces = new ArrayList<Place>();
		randomizePlaces.addAll(places);
		
		String[] namesFirst = {"Matthew", "Bethany", "Christian" , "Parker", "Jonathan" , "David", "Samuel" , "Jared", "Ryan", "Kyle", "Kathryn", "Devon", "Xinyu", "Bryan" , "Mark", "James" };
		List<String> randomizeFirst = Arrays.asList(namesFirst);
		
		String[] namesLast = { "Miller", "Le", "Alvo", "Leach", "Skidanov", "Spindler", "McCollum",	 "Wilkens",	 "Kleinberg",	 "Beall", "Hansen", "Mohrhoff",	 "Wear", "Coffman",	 "Yu", "Miner", "Carlson","Solum"};
		List<String> randomizeLast = Arrays.asList(namesLast);
		
		Set<Person> ret = new TreeSet<Person>();
		
		for(int i = 0; i < MAX_PEOPLE ; i++){
			Collections.shuffle(randomizePlaces);
			int start = random.nextInt(randomizePlaces.size());
			int end = random.nextInt(randomizePlaces.size());
			while((start == end) &&(randomizePlaces.size() > 1)){
				end = random.nextInt(randomizePlaces.size());
			}
			int first = random.nextInt(randomizeFirst.size());
			int last = random.nextInt(randomizeLast.size());
			
			Person person = new Person(""+i,randomizeFirst.get(first)+" "+randomizeLast.get(last),randomizePlaces.get(start).getName(),randomizePlaces.get(start).getPosition(),randomizePlaces.get(end).getName());
			person.setState(PersonState.WAITING);
			randomizePlaces.get(start).getWaitingToEmbark().add(person);
			ret.add(person);
		}
		
		return ret;
	}

	public static void main(String[] args) {
		Set<Place> places = Simulator.loadPlaces();
		Set<Drone> drones = loadDrones(places);
		Set<Person> people = loadPeople(places);
		Simulator simulator = new Simulator(people,places,drones,new MyNewController());
		DroneWorld visualization = new DroneWorld(simulator,people,places,drones);
		visualization.launch();
	}

	
	//These are the things that the controller is supposed to be able to call

	public void redirectDrone(Drone drone, Place place) {
		for(Drone d:drones){
			if(d.getId().equals(drone.getId())){
				d.setDestination(place);
			}
		}
	}



}
