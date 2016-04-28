package simulator;

import java.util.LinkedList;
import java.util.ListIterator;

public class Place implements Comparable<Place>{
	
	private String name;
	private Position position;
	
	private LinkedList<Person> waitingToEmbark;
	
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}


	public Position getPosition(){
		return new Position(position);
	}
	
	void setPosition(Position position) {
		this.position = position;
	}

	public LinkedList<Person> getWaitingToEmbark() {
		return waitingToEmbark;
	}

	void setWaitingToEmbark(LinkedList<Person> waitingToEmbark) {
		this.waitingToEmbark = waitingToEmbark;
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
		this.setName(place.getName());
		this.setPosition(new Position(place.getPosition()));
		this.setWaitingToEmbark(new LinkedList<Person>());
		for(Person p: place.getWaitingToEmbark()){
			this.getWaitingToEmbark().add(new Person(p));
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
		if (this == other)
			return 0;
		if (other == null)
			return 1;
		
		if (name == null) {
			if (other.name != null)
				return -1;
		} else if (other.name == null){
			return 1;
		} else if (!name.equals(other.name))
			return name.compareTo(other.name);
		
		if (position == null) {
			if (other.position != null)
				return -1;
		} else if (!position.equals(other.position))
			return position.compareTo(other.position);
		
		if (waitingToEmbark == null) {
			if (other.waitingToEmbark != null)
				return -1;
		} else if (!waitingToEmbark.equals(other.waitingToEmbark)) {
			ListIterator<Person> a = waitingToEmbark.listIterator();
			ListIterator<Person> b = other.waitingToEmbark.listIterator();
			while(a.hasNext() && b.hasNext()){
				int c = a.next().compareTo(b.next());
				if(c != 0){
					return c;
				}
			}
			if(a.hasNext()){
				return 1;
			}
			if(b.hasNext()){
				return -1;
			}
		}
		
		return 0;
	}
	
}

