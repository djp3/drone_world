package reference;

import java.util.ArrayList;
import java.util.Collections;

import simulator.Drone;
import simulator.Place;

public class MyNewController extends MyController {

	public MyNewController() {
	}

	@Override
	public void droneIdling(Drone drone) {
		super.droneIdling(drone);
		
		ArrayList<Place> places = new ArrayList<Place>();
		places.addAll(simulator.getPlaces());
		Collections.shuffle(places);

		simulator.redirectDrone(drone, places.get(0));
		System.out.println(" Sent the drone to :"+places.get(0).getName());

	}

}
