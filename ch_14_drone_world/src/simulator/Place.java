package simulator;

import java.util.LinkedList;

public class Place implements Comparable<Place>{
	
	private String name;
	private Position position;
	
	private LinkedList<Person> waitingToEmbark;
	
	public String getName() {
		return name;
	}

	public Position getPosition(){
		return new Position(position);
	}

	public LinkedList<Person> getWaitingToEmbark() {
		return waitingToEmbark;
	}

	public Place(String name, Position position){
		this.name = name;
		if(position == null){
			this.position = null;
		}
		else{
			this.position = new Position(position);
		}
		this.waitingToEmbark = new LinkedList<Person>();
	}
	
	public Place(Place place){
		this.name = place.getName();
		this.position = new Position(place.getPosition());
		this.waitingToEmbark = new LinkedList<Person>();
		for(Person p: place.getWaitingToEmbark()){
			this.waitingToEmbark.add(new Person(p));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((waitingToEmbark == null) ? 0 : waitingToEmbark.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Place))
			return false;
		Place other = (Place) obj;
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
		if (waitingToEmbark == null) {
			if (other.waitingToEmbark != null)
				return false;
		} else if (!waitingToEmbark.equals(other.waitingToEmbark))
			return false;
		return true;
	}

	@Override
	public int compareTo(Place other) {
		if (this.equals(other)){
			return 0;
		}
		
		if (other == null){
			return 1;
		}
		
		if (this.getName() == null) {
			if (other.getName() != null){
				return -1;
			}
		} 
		else if(other.getName() == null){
			return 1;
		}
		
		int c = this.getName().compareTo(other.getName());
		if(c != 0){
			return c;
		}
		else{
			if (this.getPosition() == null) {
				if (other.getPosition() != null){
					return -1;
				}
				else{
					return 0; //For now you can't get here because they are already found to be equal
				}
			}
			else{
				return this.getPosition().compareTo(other.getPosition());
			}
		}
	}
	
	
}

