package reference;

import java.util.TreeSet;

import simulator.Drone;
import simulator.Place;

/**
 * This is the class that students should work with to create their drone controller
 * Rename this class to MyDroneControllerXXXXXXX where XXXXXX is the team name
 * 
 */
public class MyDroneController extends DroneControllerSkeleton {
	
	
	@Override
	public String getNextDroneName() {
		return "Generic Drone #"+incrementDroneCounter();
	}
	
	@Override
	public String getCompanyName() {
		return "Generic Co.";
	}
	
	@Override
	public void droneIdling(Drone drone) {
		// Notify the parent class of the current event
		super.droneIdling(drone);
		
		Place whereIsTheDrone = drone.getDestination();
		
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(whereIsTheDrone);
		
		if(places.size() > 0) {	
			// Tell the passengers where the drone is going
			getSimulator().setDroneManifest(drone, places.first().getName());
			// Send the drone to it's location
			getSimulator().routeDrone(drone, places.first().getName());
		}	
	}

}
