package simulator;

import java.util.HashSet;
import java.util.Set;

public class Drone implements Comparable<Drone>{
	
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60*ONE_SECOND;
	private static final int ONE_HOUR = 60*ONE_MINUTE;
	
	// Name to identify the drone by
	private String id;
	private String name;
	private Place start;
	private Position position;
	private Place destination;
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
	private long ascentionTime;
	//How many milliseconds it takes to lower from to cruising altitude
	private long descentionTime;
	
	//Time of start of transit
	private long transitStart;
	private long transitEnd;
	
	// between 0.0 and 1.0 with 1.0 full charged
	private double charge;
	// time of start of recharging
	private long rechargeStart;
	// How many milliseconds until a full charge;
	private double rechargeRate;
	
	// How many passengers can this drone carry?
	int capacity;
	Set<Person> passengers;
	
	// meters per second;
	private double speed;

	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Place getStart() {
		return start;
	}
	
	public void setStart(Place start) {
		this.start = start;
	}
	
	public Position getPosition(){
		return position;
	}

	Place getDestination() {
		return destination;
	}
	
	void setDestination(Place place){
		destination = new Place(place);
	}

	public DroneState getState(){
		return this.state;
	}

	void setState(DroneState state) {
		this.state = state;
	}

	long getEmbarkingStart() {
		return embarkingStart;
	}

	void setEmbarkingStart(long time) {
		this.embarkingStart= time;
	}

	int getEmbarkingDuration() {
		return embarkingDuration;
	}

	int getEmbarkingCapacity() {
		return embarkingCapacity;
	}

	public Set<Person> getEmbarkers(){
		return embarkers;
	}

	long getDisembarkingStart() {
		return disembarkingStart;
	}

	void setDisembarkingStart(long disembarkingStart) {
		this.disembarkingStart = disembarkingStart;
	}

	int getDisembarkingDuration() {
		return disembarkingDuration;
	}

	int getDisembarkingCapacity() {
		return disembarkingCapacity;
	}

	public Set<Person> getDisembarkers(){
		return disembarkers;
	}

	long getAscentionTime() {
		return this.ascentionTime;
	}

	long getDescentionTime() {
		return this.descentionTime;
	}

	long getTransitStart() {
		return transitStart;
	}

	void setTransitStart(long transitStart) {
		this.transitStart = transitStart;
	}

	long getTransitEnd() {
		return transitEnd;
	}

	void setTransitEnd(long transitEnd) {
		this.transitEnd = transitEnd;
	}

	long getRechargeStartTime() {
		return rechargeStart;
	}

	void setRechargeStartTime(long rechargeStart) {
		this.rechargeStart = rechargeStart;
	}

	double getCharge() {
		return charge;
	}

	void setCharge(double charge) {
		this.charge = charge;
	}

	double getRechargeRate() {
		return rechargeRate;
	}

	void setRechargeRate(double rechargeRate) {
		this.rechargeRate = rechargeRate;
	}

	int getCapacity() {
		return capacity;
	}

	void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Set<Person> getPassengers(){
		return passengers;
	}

	double getSpeed(){
		return speed;
	}

	public Drone(Place start,Place destination,int capacity) {
		this.id = "#"+System.currentTimeMillis();
		speed = 100.0;
		charge = 1.0;
		rechargeRate = ONE_MINUTE;
		
		embarkingDuration= 20*ONE_SECOND;
		embarkingCapacity = 1;
		this.embarkers = new HashSet<Person>();
		
		disembarkingDuration = 20*ONE_SECOND;
		disembarkingCapacity = 1;
		this.disembarkers = new HashSet<Person>();
		
		ascentionTime = 60*ONE_SECOND;
		descentionTime = 60*ONE_SECOND;
		
		this.start = start;
		this.position = start.getPosition();
		this.destination = destination;
		
		if(capacity < 1){
			throw new IllegalArgumentException("Drones must be able to carry 1 or greater");
		}
		
		this.capacity = capacity;
		this.passengers = new HashSet<Person>();
		
	}
	
	public Drone(Drone drone){
		this.id = drone.getId();
		this.start = new Place(drone.getStart());
		this.destination = new Place(drone.getDestination());
		this.state = drone.getState();
		
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
		
		this.ascentionTime = drone.getAscentionTime();
		this.descentionTime = drone.getDescentionTime();
		
		this.transitStart = drone.getTransitStart();
		this.transitEnd = drone.getTransitEnd();
		
		this.charge = drone.getCharge();
		this.rechargeStart = drone.getRechargeStartTime();
		this.rechargeRate = drone.getRechargeRate();
		
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
		
		this.speed = drone.getSpeed();
		
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ascentionTime ^ (ascentionTime >>> 32));
		result = prime * result + capacity;
		long temp;
		temp = Double.doubleToLongBits(charge);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (descentionTime ^ (descentionTime >>> 32));
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((disembarkers == null) ? 0 : disembarkers.hashCode());
		result = prime * result + disembarkingCapacity;
		result = prime * result + disembarkingDuration;
		result = prime * result + (int) (disembarkingStart ^ (disembarkingStart >>> 32));
		result = prime * result + ((embarkers == null) ? 0 : embarkers.hashCode());
		result = prime * result + embarkingCapacity;
		result = prime * result + embarkingDuration;
		result = prime * result + (int) (embarkingStart ^ (embarkingStart >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((passengers == null) ? 0 : passengers.hashCode());
		temp = Double.doubleToLongBits(rechargeRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (rechargeStart ^ (rechargeStart >>> 32));
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
		if (ascentionTime != other.ascentionTime)
			return false;
		if (capacity != other.capacity)
			return false;
		if (Double.doubleToLongBits(charge) != Double.doubleToLongBits(other.charge))
			return false;
		if (descentionTime != other.descentionTime)
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
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
		if (passengers == null) {
			if (other.passengers != null)
				return false;
		} else if (!passengers.equals(other.passengers))
			return false;
		if (Double.doubleToLongBits(rechargeRate) != Double.doubleToLongBits(other.rechargeRate))
			return false;
		if (rechargeStart != other.rechargeStart)
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
