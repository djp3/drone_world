package simulator;

import java.io.IOException;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;

import simulator.enums.PersonState;

public class Person implements Comparable<Person>, Savable{
	
	String id;
	String name;
	//Name of place where person is starting
	String start;
	//Name of place where person is going
	String destination;
	//The current position of the Person that is used to render them
	Position position;
	PersonState state;
	//When the Person began and ended their trip
	Long startTransitTime;
	Long endTransitTime;
	
	// The company that picked up this person
	String deliveryCompany;
	

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
	

	public String getDestination() {
		return destination;
	}

	void setDestination(String destination) {
		this.destination = destination;
	}
	
	
	public String getStart() {
		return start;
	}

	void setStart(String start) {
		this.start = start;
	}
	

	void setPosition(Position position){
		this.position = position;
	}
	
	public Position getPosition(){
		return new Position(position);
	}
	
	
	public PersonState getState() {
		return state;
	}

	void setState(PersonState newState){
		this.state = newState;
	}
	

	public Long getStartTransitTime() {
		return startTransitTime;
	}

	
	void setStartTransitTime(Long startTransitTime) {
		this.startTransitTime = startTransitTime;
	}

	public Long getEndTransitTime() {
		return endTransitTime;
	}

	void setEndTransitTime(Long endTransitTime) {
		this.endTransitTime = endTransitTime;
	}

	public String getDeliveryCompany() {
		return deliveryCompany;
	}

	void setDeliveryCompany(String deliveryCompany) {
		this.deliveryCompany = deliveryCompany;
	}

	public Person(String id,String name, String start,Position currentLocation,String destination,PersonState state) {
		this.id = id;
		this.name = name;
		this.start = start ;
		this.position = new Position(currentLocation);
		this.destination = destination;
		this.state = state;
		this.startTransitTime = 0L;
		this.endTransitTime = 0L;
		this.deliveryCompany = null;
	}
	
	public Person(Person person){
		this.setId(person.getId());
		this.setName(person.getName());
		this.setStart(person.getStart());
		this.setPosition(new Position(person.getPosition()));
		this.setDestination(person.getDestination());
		this.setState(person.getState());
		this.setDeliveryCompany(person.getDeliveryCompany());
		this.setStartTransitTime(person.getStartTransitTime());
		this.setEndTransitTime(person.getEndTransitTime());
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliveryCompany == null) ? 0 : deliveryCompany.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((endTransitTime == null) ? 0 : endTransitTime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((startTransitTime == null) ? 0 : startTransitTime.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Person))
			return false;
		Person other = (Person) obj;
		if (deliveryCompany == null) {
			if (other.deliveryCompany != null)
				return false;
		} else if (!deliveryCompany.equals(other.deliveryCompany))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (endTransitTime == null) {
			if (other.endTransitTime != null)
				return false;
		} else if (!endTransitTime.equals(other.endTransitTime))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (startTransitTime == null) {
			if (other.startTransitTime != null)
				return false;
		} else if (!startTransitTime.equals(other.startTransitTime))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public int compareTo(Person other) {
		if (this == other)
			return 0;
		if (other == null)
			return -11;
		
		if (name == null) {
			if (other.name != null)
				return -1;
		} else if (!name.equals(other.name))
			return name.compareTo(other.name);
		
		if (destination == null) {
			if (other.destination != null)
				return 1;
		} else if (!destination.equals(other.destination))
			return destination.compareTo(other.destination);
		
		if (id == null) {
			if (other.id != null)
				return 1;
		} else if (!id.equals(other.id))
			return id.compareTo(other.id);
		
		if (position == null) {
			if (other.position != null)
				return 1;
		} else if (!position.equals(other.position))
			return position.compareTo(other.position);
		
		if (start == null) {
			if (other.start != null)
				return 1;
		} else if (!start.equals(other.start))
			return start.compareTo(other.start);
		
		if (deliveryCompany == null) {
			if (other.deliveryCompany != null)
				return 1;
		} else if (!deliveryCompany.equals(other.deliveryCompany))
			return deliveryCompany.compareTo(other.deliveryCompany);
		
		if (state != other.state)
			return state.compareTo(other.state);
		
		if (startTransitTime == null) {
			if (other.startTransitTime != null)
				return 1;
		} else if (!startTransitTime.equals(other.startTransitTime))
			return Long.compare(startTransitTime,other.startTransitTime);
		
		if (endTransitTime == null) {
			if (other.endTransitTime != null)
				return 1;
		} else if (!endTransitTime.equals(other.endTransitTime))
			return Long.compare(endTransitTime,other.endTransitTime);
		
		return 0;
	}
	
	public String toString(){
		StringBuffer out = new StringBuffer();
		out.append("id: "+id+",");
		out.append("name: "+name+",");
		out.append("start: "+start+",");
		out.append("destination: "+destination+",");
		out.append("position: "+position.toString()+",");
		out.append("state: "+state.toString()+",");
		out.append("delivery company: "+deliveryCompany+",");
		out.append("start transit time : "+startTransitTime+",");
		out.append("end transit time : "+endTransitTime);
		return out.toString();
	}

	@Override
	public void read(JmeImporter arg0) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void write(JmeExporter arg0) throws IOException {
		throw new RuntimeException("Not implemented");
	}
	
	

}
