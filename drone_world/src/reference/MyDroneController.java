package reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
		
		//Find out where the drone currently is (idling at it's last destination)
		Place x = drone.getDestination();
		
		//Get all the possible places to go
		TreeSet<Place> places = getSimulator().getPlaces();
		places.remove(x);
		
		List<Place> listOfPlaces = new ArrayList<Place>();
		listOfPlaces.addAll(places);
			
		Random r = getSimulator().getSimulationController().getRandom();
		Integer nextRandom = r.nextInt(listOfPlaces.size());
			
		// Pick a random destination
		Place placeToGoTo = listOfPlaces.get(nextRandom);
			
		// Tell the passengers where the drone is going
		getSimulator().setDroneManifest(drone, placeToGoTo.getName());
			
		// Send the drone to it's location
		getSimulator().routeDrone(drone, placeToGoTo.getName());
	}

}
