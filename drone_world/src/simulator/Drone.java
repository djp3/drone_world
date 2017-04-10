package simulator;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import simulator.enums.DroneState;
import simulator.enums.PersonState;
import simulator.interfaces.DroneController;

public class Drone implements Comparable<Drone>{
	
	//This is the controller that makes decisions for this drone
	private DroneController controller;
	
	// Id to identify the drone by e.g., "000001"
	private String id;
	//This is the human identifiable name of the individual drone, e.g, "Hopper"
	private String name;
	//This is the name of a company that might have several Drones, e.g., "Patterson Drone Inc."
	private String companyName;
	
	private Place start;
	private Position position;
	private Place destination;
	
	//The list of places that passengers are told the drone is going to - effects their boarding
	private Set<String> manifest;
	
	private DroneState state;
	
	
	// Time since starting the load of the last passenger
	private long embarkingStart;
	// How long it takes to load a passenger in milliseconds
	private int embarkingDuration;
	// How many people can embark at once
	private int embarkingCapacity;
	private Set<Person> embarkers;
	
	private long disembarkingStart;
	private int disembarkingDuration;
	private int disembarkingCapacity;
	private Set<Person> disembarkers;
	
	//How many milliseconds it takes to lift to cruising altitude
	private long ascensionTime;
	//How many milliseconds it takes to lower from to cruising altitude
	private long descensionTime;
	
	//Time of start of transit
	private long transitStart;
	private long transitEnd;
	
	// between 0.0 and 1.0 with 1.0 full charged
	private double charge;
	
	// Percentage of charged gained per second while recharging
	private double rechargeRate;
	
	// Percentage of charge lost per meter
	private double dischargeRate;
	
	// How many passengers can this drone carry?
	private int capacity;
	private Set<Person> passengers;
	
	// meters per second;
	private double speed;
	
	
	public DroneController getController() {
		return controller;
	}

	void setController(DroneController controller) {
		this.controller = controller;
	}

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getCompanyName() {
		return companyName;
	}

	void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Place getStart() {
		return start;
	}
	
	void setStart(Place start) {
		this.start = start;
	}
	
	public Position getPosition(){
		return position;
	}
	
	void setPosition(Position position){
		this.position = position;
	}

	public Place getDestination() {
		return destination;
	}
	
	void setDestination(Place place){
		destination = place;
	}

	public Set<String> getManifest() {
		return manifest;
	}

	void setManifest(Set<String> manifest) {
		this.manifest = manifest;
	}

	public DroneState getState(){
		return this.state;
	}

	void setState(DroneState state) {
		this.state = state;
	}

	public long getEmbarkingStart() {
		return embarkingStart;
	}

	void setEmbarkingStart(long time) {
		this.embarkingStart= time;
	}

	public int getEmbarkingDuration() {
		return embarkingDuration;
	}

	public int getEmbarkingCapacity() {
		return embarkingCapacity;
	}

	public Set<Person> getEmbarkers(){
		return embarkers;
	}

	public long getDisembarkingStart() {
		return disembarkingStart;
	}

	void setDisembarkingStart(long disembarkingStart) {
		this.disembarkingStart = disembarkingStart;
	}

	public int getDisembarkingDuration() {
		return disembarkingDuration;
	}

	int getDisembarkingCapacity() {
		return disembarkingCapacity;
	}

	public Set<Person> getDisembarkers(){
		return disembarkers;
	}

	public long getAscensionTime() {
		return this.ascensionTime;
	}

	public long getDescensionTime() {
		return this.descensionTime;
	}

	public long getTransitStart() {
		return transitStart;
	}

	void setTransitStart(long transitStart) {
		this.transitStart = transitStart;
	}

	public long getTransitEnd() {
		return transitEnd;
	}

	void setTransitEnd(long transitEnd) {
		this.transitEnd = transitEnd;
	}

	public double getCharge() {
		return charge;
	}

	void setCharge(double charge) {
		this.charge = charge;
	}

	public double getRechargeRate() {
		return rechargeRate;
	}

	void setRechargeRate(double rechargeRate) {
		this.rechargeRate = rechargeRate;
	}
	
	public double getDischargeRate() {
		return dischargeRate;
	}

	void setDischargeRate(double dischargeRate) {
		this.dischargeRate = dischargeRate;
	}

	public int getCapacity() {
		return capacity;
	}

	void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Set<Person> getPassengers(){
		return passengers;
	}

	public double getSpeed(){
		return speed;
	}
	
	void setSpeed(double speed) {
		this.speed = speed;
	}
	
	void quarantine(){
		System.out.println("Drone quarantined. "+this.getCompanyName()+" "+this.getName());
		setState(DroneState.QUARANTINED);
		for(Person p:this.getPassengers()){
			p.setState(PersonState.QUARANTINED);
		}
	}
	

	public Drone(DroneController controller, Place start,Place destination,int capacity) {
		
		this.controller = controller;
		
		// Make up a unique id
		this.id = UUID.randomUUID().toString();
		
		//Ask controller for drone and company name
		this.name = this.controller.getNextDroneName();
		if(this.name == null){
			throw new RuntimeException("Controller: \""+this.controller.getClass()+"\" returned null to getNextDroneName call");
		}
		this.companyName = this.controller.getCompanyName();
		if(this.companyName == null){
			throw new RuntimeException("Controller: \""+this.controller.getClass()+"\" returned null to getCompanyName call");
		}
		
		//Set defaults
		speed = 100.0;
		charge = 1.0;
		rechargeRate = 0.10;
		dischargeRate = 0.0002;
		
		embarkingDuration= 20*Simulator.ONE_SECOND;
		embarkingCapacity = 1;
		this.embarkers = new HashSet<Person>();
		
		disembarkingDuration = 20*Simulator.ONE_SECOND;
		disembarkingCapacity = 1;
		this.disembarkers = new HashSet<Person>();
		
		ascensionTime = 60*Simulator.ONE_SECOND;
		descensionTime = 60*Simulator.ONE_SECOND;
		
		this.start = start;
		this.position = start.getPosition();
		this.destination = destination;
		this.manifest = new TreeSet<String>();
		
		if(capacity < 1){
			throw new IllegalArgumentException("Drones must be able to carry 1 or greater");
		}
		
		this.capacity = capacity;
		this.passengers = new TreeSet<Person>();
		
	}
	
	public Drone(Drone drone){
		if(drone == null){
			throw new IllegalArgumentException("Can't copy construct null");
		}
		
		this.setController(drone.getController());
		
		this.setId(drone.getId());
		this.setName(drone.getName());
		this.setCompanyName(drone.getCompanyName());
		this.setStart(new Place(drone.getStart()));
		this.setPosition(new Position(drone.getPosition()));
		this.setDestination(new Place(drone.getDestination()));
		this.setManifest(new TreeSet<String>());
		this.getManifest().addAll(drone.getManifest());
		this.setState(drone.getState());
		
		this.embarkingStart = drone.getEmbarkingStart();
		this.embarkingDuration = drone.getEmbarkingDuration();
		this.embarkingCapacity = drone.getEmbarkingCapacity();
		if(drone.getEmbarkers() == null){
			this.embarkers = null;
		}
		else{
			this.embarkers = new HashSet<Person>();
			for(Person p: drone.getEmbarkers()){
				this.embarkers.add(p);
			}
		}
		
		this.disembarkingStart = drone.getDisembarkingStart();
		this.disembarkingDuration = drone.getDisembarkingDuration();
		this.disembarkingCapacity = drone.getDisembarkingCapacity();
		if(drone.getDisembarkers() == null){
			this.disembarkers = null;
		}
		else{
			this.disembarkers = new HashSet<Person>();
			for(Person p: drone.getDisembarkers()){
				this.disembarkers.add(p);
			}
		}
		
		this.ascensionTime = drone.getAscensionTime();
		this.descensionTime = drone.getDescensionTime();
		
		this.transitStart = drone.getTransitStart();
		this.transitEnd = drone.getTransitEnd();
		
		this.setCharge(drone.getCharge());
		this.setRechargeRate(drone.getRechargeRate());
		this.setDischargeRate(drone.getDischargeRate());
		
		this.capacity = drone.getCapacity();
		
		if(drone.getPassengers() == null){
			this.passengers = null;
		}
		else{
			this.passengers = new HashSet<Person>();
			for(Person p: drone.getPassengers()){
				this.passengers.add(p);
			}
		}
		
		this.setSpeed(drone.getSpeed());
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ascensionTime ^ (ascensionTime >>> 32));
		result = prime * result + capacity;
		long temp;
		temp = Double.doubleToLongBits(charge);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((companyName == null) ? 0 : companyName.hashCode());
		result = prime * result + ((controller == null) ? 0 : controller.hashCode());
		result = prime * result + (int) (descensionTime ^ (descensionTime >>> 32));
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		temp = Double.doubleToLongBits(dischargeRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((disembarkers == null) ? 0 : disembarkers.hashCode());
		result = prime * result + disembarkingCapacity;
		result = prime * result + disembarkingDuration;
		result = prime * result + (int) (disembarkingStart ^ (disembarkingStart >>> 32));
		result = prime * result + ((embarkers == null) ? 0 : embarkers.hashCode());
		result = prime * result + embarkingCapacity;
		result = prime * result + embarkingDuration;
		result = prime * result + (int) (embarkingStart ^ (embarkingStart >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((manifest == null) ? 0 : manifest.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((passengers == null) ? 0 : passengers.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		temp = Double.doubleToLongBits(rechargeRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(speed);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + (int) (transitEnd ^ (transitEnd >>> 32));
		result = prime * result + (int) (transitStart ^ (transitStart >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Drone))
			return false;
		Drone other = (Drone) obj;
		if (ascensionTime != other.ascensionTime)
			return false;
		if (capacity != other.capacity)
			return false;
		if (Double.doubleToLongBits(charge) != Double.doubleToLongBits(other.charge))
			return false;
		if (companyName == null) {
			if (other.companyName != null)
				return false;
		} else if (!companyName.equals(other.companyName))
			return false;
		if (controller == null) {
			if (other.controller != null)
				return false;
		} else if (!controller.equals(other.controller))
			return false;
		if (descensionTime != other.descensionTime)
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (Double.doubleToLongBits(dischargeRate) != Double.doubleToLongBits(other.dischargeRate))
			return false;
		if (disembarkers == null) {
			if (other.disembarkers != null)
				return false;
		} else if (!disembarkers.equals(other.disembarkers))
			return false;
		if (disembarkingCapacity != other.disembarkingCapacity)
			return false;
		if (disembarkingDuration != other.disembarkingDuration)
			return false;
		if (disembarkingStart != other.disembarkingStart)
			return false;
		if (embarkers == null) {
			if (other.embarkers != null)
				return false;
		} else if (!embarkers.equals(other.embarkers))
			return false;
		if (embarkingCapacity != other.embarkingCapacity)
			return false;
		if (embarkingDuration != other.embarkingDuration)
			return false;
		if (embarkingStart != other.embarkingStart)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (manifest == null) {
			if (other.manifest != null)
				return false;
		} else if (!manifest.equals(other.manifest))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (passengers == null) {
			if (other.passengers != null)
				return false;
		} else if (!passengers.equals(other.passengers))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (Double.doubleToLongBits(rechargeRate) != Double.doubleToLongBits(other.rechargeRate))
			return false;
		if (Double.doubleToLongBits(speed) != Double.doubleToLongBits(other.speed))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (state != other.state)
			return false;
		if (transitEnd != other.transitEnd)
			return false;
		if (transitStart != other.transitStart)
			return false;
		return true;
	}

	@Override
	public int compareTo(Drone other) {
		if (this.equals(other)){
			return 0;
		}
		
		if (other == null){
			return 1;
		}
		
		if (this.getId() == null) {
			if (other.getId() != null){
				return -1;
			}
			else{
				return 0;
			}
		} 
		else if(other.getId() == null){
			return 1;
		}
		
		return(this.getId().compareTo(other.getId()));
	}


}
