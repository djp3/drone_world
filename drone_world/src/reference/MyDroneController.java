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
	
	
	private TreeSet<Place> places = null;

	@Override
	// See comment in parent class
	public String getNextDroneName() {
		return "Generic Drone #"+incrementDroneCounter();
	}
	
	@Override
	//See comment in parent class
	public String getCompanyName() {
		return "Generic Co.";
	}
	
	
	@Override
	//This is an example of what do to when the simulator tells your controller that a drone is idling
	public void droneIdling(Drone drone) {
		// Notify the parent class of the current event
		super.droneIdling(drone);
		
		//See if we already know of places
		if((places == null) || (places.size() == 0)) {
			//Get all the possible places to go
			places = getSimulator().getPlaces();
		}
		
		//Find out where the drone currently is (idling at it's last destination)
		Place x = drone.getDestination();
		places.remove(x);
		
		// Pick a random destination
		Place placeToGoTo = places.pollFirst();
		
		if(placeToGoTo != null) {
			// Tell the passengers where the drone is going
			getSimulator().setDroneManifest(drone, placeToGoTo);
			
			// Send the drone to it's location
			getSimulator().routeDrone(drone, placeToGoTo);
		}
	}

}
