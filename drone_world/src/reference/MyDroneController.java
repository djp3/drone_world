package reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import simulator.Drone;
import simulator.Place;

/**
 * This is the class that students should work with to create their drone controller
 * Rename this class to MyDroneControllerXXXXXXX where XXXXXX is the team name
 * 
 */
public class MyDroneController extends DroneControllerSkeleton {
	
	/**
	 * Use this random number generator to get consistent random numbers 
	 * @return a random number generator that is seeded by the Simulator
	 */
	public Random getRandom() {
		return getSimulator().getSimulationController().getRandom();
	}
	

	@Override
	// See comment in parent class
	public String getNextDroneName() {
		int i = incrementDroneCounter();
		if(i == 1 ) {
			return "Polaris"; //The camera follows one of the drones that have "Polaris" in it's name
		}
		else {
			return "Generic Drone #"+incrementDroneCounter();
		}
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
		
		//Get all the possible places to go
		Set<Place> setOfPlaces = getSimulator().getPlaces();
		
		//Find out where the drone currently is (idling at it's last destination)
		Place currentPlace = drone.getDestination();
		
		//Remove the current place from the set of all Places
		setOfPlaces.remove(currentPlace);
		
		// Pick one of the destinations
		List<Place> listOfPlaces = new ArrayList<Place>(setOfPlaces);
		if(listOfPlaces.size() > 0) {
			Place placeToGoTo = listOfPlaces.get(0);
			// Tell the passengers where the drone is going
			getSimulator().setDroneManifest(drone, placeToGoTo);
			
			// Send the drone to it's location
			getSimulator().routeDrone(drone, placeToGoTo);
		}
	}
}
