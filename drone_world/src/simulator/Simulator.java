package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import reference.MyDroneController;
import reference.MySimulationController;
import simulator.enums.DroneState;
import simulator.enums.PersonState;
import simulator.interfaces.DroneController;
import simulator.interfaces.SimulationController;
import visualization.Visualizer;

public class Simulator {
	public static final int MAX_DRONES_PER_CONTROLLER = 5;
	public static final int DRONE_MAX_CAPACITY = 1;
	
	private static final boolean DRONE_CAPACITY_VARIES = false;
	private static boolean DRONES_RUN_OUT_OF_CHARGE = false;
	
	public static final int MAX_PEOPLE = 100;
	
	public static final int MAX_LOCATIONS = 10;
	
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

	//How many simulation loops in which drones have not been busy
	private int notBusyCount=0;

	static final int ONE_SECOND = 1000;

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
		
		setQuitting(false);
		setSimulationEnded(false);
		
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
		
		//Tell the drones we are starting
		{
			//Shuffle drones so that different drones get random priority on each round
			//Shuffling manually to make sure that we only use a managed random number generator for consistency
			ArrayList<Drone> shuffledDrones = new ArrayList<Drone>();
			shuffledDrones.addAll(drones);
			for(int j = 0 ; j < shuffledDrones.size(); j++){
				Drone drone = shuffledDrones.get(j);
				Drone cloneDrone = new Drone(drone);
				drone.getController().droneSimulationStart(cloneDrone);
				if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
					drone.quarantine();
				}
			}
		}
		
		//The main loop
		while(!isQuitting() && !isSimulationEnded()){
			setSimulationEnded(true);//If it hasn't ended then it needs to be unset
			
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
				Drone cloneDrone = new Drone(drone);
				switch (drone.getState()){
					case BEGIN:{
						setSimulationEnded(false);
						drone.getController().droneEmbarkingStart(cloneDrone);
						if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
							drone.quarantine();
						}
						else{
							drone.setState(DroneState.EMBARKING);
						}
					}
					break;
					case EMBARKING:{
						setSimulationEnded(false);
						//Check to see if the passengers have had enough time to get onboard 
						if((clockTick - drone.getEmbarkingStart()) > drone.getEmbarkingDuration()){
							
							boolean embarkingSome = (drone.getEmbarkers().size() > 0);
							for(Person person:drone.getEmbarkers()){
								if(!drone.getEmbarkers().remove(person)){
									throw new RuntimeException("Why didn't the person get on board?");
								}
								person.setState(PersonState.IN_DRONE);
								if(!drone.getPassengers().add(person)){
									throw new RuntimeException("Why didn't the person get become a passenger?");
								}
							}
							
							if(embarkingSome){
								drone.getController().droneEmbarkingAGroupEnd(cloneDrone);
								if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
									drone.quarantine();
								}
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
										drone.getController().droneEmbarkingAGroupStart(cloneDrone);
										if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
											drone.quarantine();
										}
									}
								}
							}
						}
					}
					break;
					case ASCENDING:{
						setSimulationEnded(false);
						long timeToGo = drone.getTransitStart() - clockTick;
						if(timeToGo > 0){
							double percentage = timeToGo/(0.0+drone.getAscensionTime());
							double currentHeight = TRANSIT_HEIGHT - percentage*TRANSIT_HEIGHT;
							drone.getPosition().setHeight(currentHeight);
						}
						else{
							drone.getController().droneAscendingEnd(cloneDrone);
							if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
								drone.quarantine();
							}
							else{
								drone.setState(DroneState.IN_TRANSIT);
								cloneDrone = new Drone(drone);
								drone.getController().droneTransitingStart(cloneDrone);
								if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
									drone.quarantine();
								}
							}
						}
					}
					break;
					case EXPLODING:{
						setSimulationEnded(false);
						
						for(Person p: drone.getPassengers()){
							p.setState(PersonState.DYING);
						}
						
						long timeToGo = drone.getTransitEnd() - clockTick;
						if(timeToGo > 0){
							double percentage = timeToGo/(0.0+drone.getDescensionTime());
							double currentHeight = percentage*TRANSIT_HEIGHT;
							drone.getPosition().setHeight(currentHeight);
						}
						else{
							drone.setState(DroneState.DYING);
						}
					}
					break;
					case IN_TRANSIT:{
						setSimulationEnded(false);
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
						double dischargeRate = drone.getDischargeRate();
						charge = charge - (metersPerTick*dischargeRate); 
						if(charge < 0.0 ){
							charge = 0.0;
						}
						drone.setCharge(charge);
						
						if((charge <= 0.0 )&&(DRONES_RUN_OUT_OF_CHARGE)){
							drone.setState(DroneState.EXPLODING);
							drone.setTransitEnd(clockTick+(drone.getDescensionTime()/2));
							drone.getController().droneExploding(cloneDrone);
							if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
								drone.quarantine();
							}
						}
						else{
							//What percentage of the way there are we?
							double percentage = 1.0 - ((metersToGoal-metersPerTick)/metersForTrip);
							if(percentage > 1.0){
								//This could happen if the drone was rerouted in transit to a further destination
								percentage = 1.0; 
								
							}
						
							//Close enough to call it an arrival
							if(metersPerTick >= metersToGoal){
								drone.getPosition().setLatitude(drone.getDestination().getPosition().getLatitude());
								drone.getPosition().setLongitude(drone.getDestination().getPosition().getLongitude());
								drone.getPosition().setHeight(TRANSIT_HEIGHT+drone.getDestination().getPosition().getHeight());
							
								//Arrival
								drone.setTransitEnd(clockTick+drone.getDescensionTime());
								drone.getController().droneTransitingEnd(cloneDrone);
								if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
									drone.quarantine();
								}
								else{
									drone.setState(DroneState.DESCENDING);
									cloneDrone = new Drone(drone);
									drone.getController().droneDescendingStart(cloneDrone);
									if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
										drone.quarantine();
									}
								}
							}
							else{
								// This is going to screw up if a drone is rerouted in transit because it needs to interpolate between the drone's
								// current position and the destination, not the drone's starting point
								Position a = drone.getStart().getPosition();
								Position b = drone.getDestination().getPosition();
								double latitude = (b.getLatitude()-a.getLatitude())*percentage+a.getLatitude();
								double longitude = (b.getLongitude()-a.getLongitude())*percentage+a.getLongitude();
								double height = (b.getHeight()-a.getHeight())*percentage+a.getHeight() + TRANSIT_HEIGHT;
						
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
							drone.getController().droneTransiting(cloneDrone, 1.0-(metersToGoal/metersForTrip));
							if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
								drone.quarantine();
							}
						}
					}
					break;
					case DESCENDING:{
						setSimulationEnded(false);
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
						setSimulationEnded(false);
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
								drone.getController().droneDisembarkingGroupEnd(cloneDrone);
								if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
									drone.quarantine();
								}
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
										if(!drone.getPassengers().remove(person)){
											throw new RuntimeException("Why didn't the person get removed?");
										}
										person.setState(PersonState.DISEMBARKING);
										drone.getDisembarkers().add(person);
									}
									drone.getController().droneDisembarkingGroupStart(cloneDrone);
									if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
										drone.quarantine();
									}
									drone.setDisembarkingStart(clockTick);
								}
							}
						}
					}
					break;
					case RECHARGING:{
						setSimulationEnded(false);
						
						//If the controller has told the drone to leave
						if(!drone.getStart().equals(drone.getDestination())){
							drone.getController().droneDoneRecharging(cloneDrone);
							if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
								drone.quarantine();
							}
							else{
								drone.setState(DroneState.BEGIN);
							}
						}
						else{
							double chargeDelta = (SIMULATION_SPEED/1000.0) * drone.getRechargeRate() ;
							if(drone.getCharge()+ chargeDelta > 1.0){
								drone.setCharge(1.0);
								drone.getController().droneDoneRecharging(cloneDrone);
								if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
									drone.quarantine();
								}
								else{
									drone.setState(DroneState.IDLING);
								}
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
									drone.getController().droneRecharging(cloneDrone,drone.getCharge());
									if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
										drone.quarantine();
									}
								}
							}
						}
					}
					break;
					case IDLING:{
						setSimulationEnded(false);
						
						for(Person p: drone.getPassengers()){
							p.setState(PersonState.IN_DRONE);
						}
						
						if(!drone.getStart().equals(drone.getDestination())){
							drone.setState(DroneState.BEGIN);
						}
						else{
							drone.getController().droneIdling(cloneDrone);
							if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
								drone.quarantine();
							}
						}
					}
					break;
					case DYING:{
						setSimulationEnded(false);
						for(Person p: drone.getPassengers()){
							p.setState(PersonState.DEAD);
						}
						drone.setState(DroneState.DEAD);
					}
					break;
					case DEAD:{
						//simulationEnded = false;
						//If all drones explode the simulation is ended
					}
					break;
					case QUARANTINED:{
						//simulationEnded = false;
						//If all drones are quarantined by the simulator, the simulation is ended
					}
					break;
					case IGNORED:{
					}
					break;
					default:
						throw new IllegalArgumentException("Unhandled Drone State: "+drone.getState());
				}
			}
			
			//Check to see if all passengers are delivered
			boolean allDone = true;
			boolean someWaiting = false;
			for(Person p: people){
				if((!p.getState().equals(PersonState.ARRIVED))&&(!p.getState().equals(PersonState.DEAD)) && (!p.getState().equals(PersonState.QUARANTINED))){
					allDone = false;
				}
				if(!someWaiting && p.getState().equals(PersonState.WAITING)){
					someWaiting = true;
				}
			}
			if(allDone){
				setSimulationEnded(true);
				System.out.println("Simulation ended with all passengers delivered at time "+clockTick);
			}
			
			//If there is no one waiting ignore the drones that aren't carrying passengers
			for(Drone d: drones){
				if(!someWaiting){
					if(d.getEmbarkers().size() == 0){
						if(d.getPassengers().size() == 0){
							if(d.getDisembarkers().size() == 0){
								d.setState(DroneState.IGNORED);
							}
						}
					}
				}
			}
			
			//Check to see if the drones are making progress
			boolean dronesBusy = false;
			for(Drone d: drones){
				if(d.getState() != DroneState.IDLING){
					if(d.getState() != DroneState.DEAD){
						if(d.getState() != DroneState.QUARANTINED){
							if(d.getState() != DroneState.IGNORED){
								dronesBusy= true;
							}
						}
					}
				}
			}
			//If not quarantine one
			if(dronesBusy){
				notBusyCount = 0;
			}
			else{
				notBusyCount++;
			}
			//We are checking for 10 so that 9 calls to a drone's idling call back must result in no action
			//  (The random drone controller would occasionally break this when it was 2 by choosing the same destination
			//  as the current location)
			if(notBusyCount > 10){  
				notBusyCount = 0;
				//Shuffle drones so that different drones get random priority on each round
				//Shuffling manually to make sure that we only use a managed random number generator for consistency
				ArrayList<Drone> shuffledDrones2 = new ArrayList<Drone>();
				shuffledDrones2.addAll(drones);
				for(int j = 0 ; j < shuffledDrones2.size(); j++){
					int swapIndex = simulationController.getRandom().nextInt(shuffledDrones2.size());
					Drone foo = shuffledDrones2.get(j);
					shuffledDrones2.set(j,shuffledDrones2.get(swapIndex));
					shuffledDrones2.set(swapIndex,foo);
				}
				for(Drone d: shuffledDrones2){
					if(d.getState().equals(DroneState.IDLING)){
						d.quarantine();
						break;
					}
				}
			}
			
			/* Put an end to our misery */
			if(getClockTick() > getSimulationController().getSimulationEndTime()){
				setSimulationEnded(true);
				System.out.println("Out of time");
			}
		}
		
		//Tell the drones we are ending
		{
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
			for(int j = 0 ; j < shuffledDrones.size(); j++){
				Drone d = shuffledDrones.get(j);
				Drone cloneDrone = new Drone(d);
				d.getController().droneSimulationEnd(cloneDrone);
				if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
					d.quarantine();
				}
			}
		}
	}


	
	public void end(String reason){
		System.out.println("Simulation ending");
		if(reason != null){
			System.out.println("\t"+reason);
		}
		
		setQuitting(true);
		setSimulationEnded(true);
	}


	private void droneStartRecharging(Drone drone) {
		Drone cloneDrone = new Drone(drone);
		drone.getController().droneDisembarkingEnd(cloneDrone);
		if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
			drone.quarantine();
		}
		else{
			drone.setState(DroneState.RECHARGING);
			cloneDrone = new Drone(drone);
			drone.getController().droneRechargingStart(cloneDrone);
			if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
				drone.quarantine();
			}
		}
	}



	private void droneTakeOff(Drone drone){
		Drone cloneDrone = new Drone(drone);
		drone.getController().droneEmbarkingEnd(cloneDrone);
		if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
			drone.quarantine();
		}
		else{
			drone.setState(DroneState.ASCENDING);
			
			cloneDrone = new Drone(drone);
			drone.getController().droneAscendingStart(cloneDrone);
			if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
				drone.quarantine();
			}
			else{
				drone.setTransitStart(clockTick+drone.getAscensionTime());
			}
		}
	}
	
	private void droneLand(Drone drone) {
		Drone cloneDrone = new Drone(drone);
		drone.getController().droneDescendingEnd(cloneDrone);
		if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
			drone.quarantine();
		}
		else{
			drone.setStart(drone.getDestination());
			drone.setState(DroneState.DISEMBARKING);
			
			cloneDrone = new Drone(drone);
			drone.getController().droneDisembarkingStart(cloneDrone);
			if(cloneDrone.getState().equals(DroneState.QUARANTINED)){
				drone.quarantine();
			}
			else{
				//Make sure that disembarking starts by setting the last disembark time to before the simulation started
				drone.setDisembarkingStart(-drone.getDisembarkingDuration());
			}
		}
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
	
	
	public boolean isSimulationEnded() {
		return simulationEnded;
	}

	void setSimulationEnded(boolean simulationEnded) {
		this.simulationEnded = simulationEnded;
	}

	public boolean isQuitting() {
		return quitting;
	}


	void setQuitting(boolean quitting) {
		this.quitting = quitting;
	}




	/******************************************************************/
	/* Set up the simulation */

	private static Set<Place> loadPlaces(SimulationController simulationController) {
		// Start with 10 basic locations
		Set<Place> ret = new TreeSet<Place>();
		ret.add(new Place("Winter Hall",new Position(34.448868,-119.6629439,0)));
		ret.add(new Place("SBCC",new Position(34.4060661,-119.69755,0)));
		
		ret.add(new Place("Show Grounds",new Position(34.4300057,-119.7363983,0)));
		ret.add(new Place("Santa Barbara Bowl",new Position(34.4351155,-119.6935015,0)));
		ret.add(new Place("Reservoir", new Position(34.4537095,-119.7277611,0)));
		
		ret.add(new Place("Trader Joe's",new Position(34.4392777,-119.7293757,0)));
		ret.add(new Place("Canyon Park",new Position(34.457205,-119.781077,0)));
		ret.add(new Place("Green Houses",new Position(34.461478,-119.788307,0)));
		ret.add(new Place("In-N-Out",new Position(34.442533,-119.790726,0)));
		ret.add(new Place("McDonald's",new Position(34.441046,-119.753056,0)));
		ret.add(new Place("Courthouse",new Position(34.424117,-119.701909,0)));
		ret.add(new Place("Princess Cruise",new Position(34.396725,-119.683566,0)));
		ret.add(new Place("Mother Stearn's Candy",new Position(34.4097893,-119.6855427,0)));
		
		ret.add(new Place("Doctor Evil's Sub",new Position(34.395299,-119.658715,0)));
		ret.add(new Place("Dog Beach",new Position(34.4026544,-119.7426834,0)));
		ret.add(new Place("Beach Cabana",new Position(34.412831,-119.775672,0)));
		
		
		//Figure out the extents of the locations
		double maxLat = -1.0*Double.MAX_VALUE;
		double maxLong = -1.0*Double.MAX_VALUE;
		double minLat = Double.MAX_VALUE;
		double minLong = Double.MAX_VALUE;
		for(Place p: ret){
			if(p.getPosition().getLatitude() > maxLat){
				maxLat = p.getPosition().getLatitude();
			}
			if(p.getPosition().getLatitude() < minLat){
				minLat = p.getPosition().getLatitude();
			}
			if(p.getPosition().getLongitude() > maxLong){
				maxLong = p.getPosition().getLongitude();
			}
			if(p.getPosition().getLongitude() < minLong){
				minLong = p.getPosition().getLongitude();
			}
		}
		
		//Reduce the number of locations if necessary
		while(ret.size() > MAX_LOCATIONS){
			ret.remove(ret.iterator().next());
		}
		
		//Increase the number of locations if necesary
		while(ret.size() < MAX_LOCATIONS){
			double lat = simulationController.getRandom().nextDouble();
			lat *= (maxLat-minLat);
			lat += minLat;
			double longi = simulationController.getRandom().nextDouble();
			longi *= (maxLong-minLong);
			longi += minLong;
			ret.add(new Place(""+ret.size(),new Position(lat,longi,0)));
		}
		
		
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
			
			Drone drone;
			if(DRONE_CAPACITY_VARIES){
				int capacity = MAX_DRONES_PER_CONTROLLER - i;
				if(i > DRONE_MAX_CAPACITY){
					i = i % (DRONE_MAX_CAPACITY-1);
					i++;
				}
				drone = new Drone(controller,thePlace,thePlace,capacity);
			}
			else{
				drone = new Drone(controller,thePlace,thePlace,DRONE_MAX_CAPACITY);
			}
			drone.setState(DroneState.IDLING);
			ret.add(drone);
		}
		
		return (ret);
	}

	private static Set<Person> loadPeople(Random random,Set<Place> places) {
		ArrayList<Place> randomizePlaces = new ArrayList<Place>();
		randomizePlaces.addAll(places);
		
		String[] namesFirst = { "Jason", "Reilly", "Emma", "Kalie", "Hannah", "Rebecca", "Kevin", "Sophia", "David", "Tanner", "Mo", "Ryley", "Dante", "Sam", "Maya", "Dempsey", "Ben", "Heather", "M'kya", "Kaylee" };
		List<String> randomizeFirst = Arrays.asList(namesFirst);
		
		String[] namesLast = { "Campbell", "Cole", "Donelson", "Drown", "Fisk", "Frink", "Gao", "Gigliotti", "Kyle", "Leslie", "Mahjoub", "Oroku", "Polesselli", "Reep", "Rouillard", "Salazar", "Thomas", "Totten", "Williams", "Yoon" };
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
		Set<Place> places = Simulator.loadPlaces(simController);
		
		//Generate the drones
		Set<Drone> drones = new TreeSet<Drone>();
		
		//Add each companies drones here
		drones.addAll(loadDrones(places,new DroneControllerSafetyWrapper(new GreedyController(),simController.shouldQuarantineDrones()))); //Professor's Controller
		drones.addAll(loadDrones(places,new DroneControllerSafetyWrapper(new PromiscuousController(),simController.shouldQuarantineDrones()))); //Professor's Controller
		drones.addAll(loadDrones(places,new DroneControllerSafetyWrapper(new RandomDroneController(),simController.shouldQuarantineDrones()))); //Professor's Controller
		drones.addAll(loadDrones(places,new DroneControllerSafetyWrapper(new MyDroneController(),simController.shouldQuarantineDrones()))); //Student's Controller
		
		//Generate people
		Set<Person> people = loadPeople(simController.getRandom(),places);
		
		//Build simulator
		Simulator simulator = new Simulator(simController,people,places,drones);
		
		//Attach simulation to a visualizer
		Visualizer visualization = new Visualizer(simulator,people,places,drones);
		
		//Start it up
		visualization.launch();
		
		calculateWinners(people,drones, simulator.getClockTick());
		
		//Shut it down
		visualization.stop(true);
		
	}




	private static void calculateWinners(Set<Person> people,Set<Drone> drones, Long timeElapsed) {
		//Aggregate scores
		Set<Person> waiting = new HashSet<Person>();
		HashMap<String, Pair<Integer,Long>> delivered = new HashMap<String,Pair<Integer,Long>>();
		HashMap<String, Pair<Integer,Long>> dead = new HashMap<String,Pair<Integer,Long>>();
		HashMap<String, Pair<Integer,Long>> quarantined = new HashMap<String,Pair<Integer,Long>>();
		HashMap<String, Pair<Integer,Long>> total = new HashMap<String,Pair<Integer,Long>>();
		for(Person p: people){
			if(p.getState().equals(PersonState.WAITING)){
				waiting.add(p);
			}
			if(p.getState().equals(PersonState.ARRIVED)){
				delivered.merge(p.deliveryCompany,new Pair<Integer,Long>(1,p.getEndTransitTime()-p.getStartTransitTime()),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
				total.merge(p.deliveryCompany,new Pair<Integer,Long>(1,p.getEndTransitTime()-p.getStartTransitTime()),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
			}
			if(p.getState().equals(PersonState.QUARANTINED)){
				quarantined.merge(p.deliveryCompany,new Pair<Integer,Long>(-1,0L),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
				total.merge(p.deliveryCompany,new Pair<Integer,Long>(-1,0L),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
			}
			if(p.getState().equals(PersonState.DEAD)){
				dead.merge(p.deliveryCompany,new Pair<Integer,Long>(-1,0L),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
				total.merge(p.deliveryCompany,new Pair<Integer,Long>(-1,0L),(v1,v2) ->{return (new Pair<Integer,Long>(v1.getKey()+v2.getKey(),v1.getValue()+v2.getValue()));});
			}
		}
		
		//Output all results
		System.out.println("All Results:");
		System.out.println("\t Time elapsed:"+timeElapsed);
		if(waiting.size() == 0){
			System.out.println("\tAll passengers delivered");
		}
		else{
			System.out.println("\t"+waiting.size()+" passengers never picked up");
		}
		for(Entry<String, Pair<Integer, Long>> p:total.entrySet()){
			int deliveredNum = 0;
			if(delivered.size() > 0){
				if(delivered.get(p.getKey()) != null){
					deliveredNum += delivered.get(p.getKey()).getKey();
				}
			}
			
			int quarantinedNum = 0;
			if(quarantined.size() > 0 ){
				if(quarantined.get(p.getKey())!= null){
					quarantinedNum += (-1*quarantined.get(p.getKey()).getKey());
				}
			}
			
			int deadNum = 0;
			if(dead.size() > 0 ){
				if(dead.get(p.getKey())!= null){
					deadNum += (-1*dead.get(p.getKey()).getKey());
				}
			}
			
			System.out.println("\t"+p.getKey()+" delivered "+deliveredNum+" passengers in a total time of "+p.getValue().getValue()+ ", killed "+deadNum+", quarantined "+quarantinedNum);
		}
		
		System.out.println("\nOrdered Results:");
		while(total.size() > 0){
		
			//Figure out the highest score
			int max = Integer.MIN_VALUE;
			for(Entry<String, Pair<Integer, Long>> p:total.entrySet()){
				if(p.getValue().getKey() > max){
					max = p.getValue().getKey();
				}
			}
			long minTime = Long.MAX_VALUE;
			for(Entry<String, Pair<Integer, Long>> p:total.entrySet()){
				if(p.getValue().getKey() == max){
					if(minTime > p.getValue().getValue()){
						minTime = p.getValue().getValue();
					}
				}
			}
			
			//See who has the highest score
			Map<String,Long> winners = new HashMap<String,Long>();
			for(Entry<String, Pair<Integer, Long>> p:total.entrySet()){
				if((p.getValue().getKey() == max) && (minTime == p.getValue().getValue())){
					winners.put(p.getKey(),p.getValue().getValue());
				}
			}
			
		
			//Output everyone who is a winner
			for(Entry<String, Long> p:winners.entrySet()){
				int deliveredNum = 0;
				if(delivered.size() > 0){
					if(delivered.get(p.getKey()) != null){
						deliveredNum = delivered.get(p.getKey()).getKey();
					}
				}
				
				int quarantinedNum = 0;
				if(quarantined.size() > 0 ){
					if(quarantined.get(p.getKey())!= null){
						quarantinedNum += (-1*quarantined.get(p.getKey()).getKey());
					}
				}
				
				int deadNum = 0;
				if(dead.size() > 0 ){
					if(dead.get(p.getKey())!= null){
						deadNum = (-1*dead.get(p.getKey()).getKey());
					}
				}
				
				System.out.println("\t"+p.getKey()+" delivered "+deliveredNum+" passengers in a total time of "+minTime+ ", killed "+deadNum+", quarantined "+quarantinedNum);
			}
			
			for(Entry<String, Long> x: winners.entrySet()){
				total.remove(x.getKey());
			}
		}
		
		// Announced if any drones were quarantined
		HashMap<String, Integer> quarantinedCompanies = new HashMap<String,Integer>();
		for(Drone d:drones){
			if(d.getState().equals(DroneState.QUARANTINED)){
				quarantinedCompanies.merge(d.getCompanyName(),1,(v1,v2) ->{return (v1+v2);});
			}
		}
		
		if(quarantinedCompanies.size()> 0){
			System.out.println("\nCompanies with Quarantined Drones:");
			for(Entry<String, Integer> x: quarantinedCompanies.entrySet()){
				System.out.println("\t"+x.getKey()+" with "+x.getValue()+" quarantined drones");
			}
		}
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
