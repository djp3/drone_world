package reference;

import java.util.ArrayList;
import java.util.TreeSet;

import simulator.DistanceCalculator;
import simulator.Drone;
import simulator.Person;
import simulator.Place;

public class DistanceAwarePromiscuousController extends DroneControllerSkeleton {
	
	static int nameCount=0;
	public static final String companyName = "Long Distance Promiscuous DroneCo";
	
	public static synchronized int getNameCount() {
		return nameCount;
	}

	public static synchronized void setNameCount(int nameCount) {
		DistanceAwarePromiscuousController.nameCount = nameCount;
	}

	public DistanceAwarePromiscuousController() {
		super();
	}

	@Override
	public String getNextDroneName() {
		setNameCount(getNameCount()+1);
		return "DA Prom Drone "+getNameCount();
	}

	@Override
	public String getCompanyName() {
		return companyName;
	}
	
	
	public void output(Drone drone) {
		System.out.println("People:");
		for(Person p:simulator.getPeople()){
			System.out.println("\t"+p.getName()+" "+p.getState()+" "+p.getPosition());
		}
		System.out.println("Drones:");
		for(Drone d:simulator.getDrones()){
			System.out.println("\t"+d.getId()+" "+d.getState()+" "+d.getPosition());
		}
	}

	@Override
	public void droneIdling(Drone drone) {
		super.droneIdling(drone);
		
		//Say that we go everywhere
		TreeSet<String> manifest = new TreeSet<String>();
		for(Place p: this.simulator.getPlaces()){
			manifest.add(p.getName());
		}
		simulator.setDroneManifest(drone, manifest);
		
		
		//Figure out what places we can reach
		TreeSet<String> possiblePlaces = new TreeSet<String>();
		for(Place p: this.simulator.getPlaces()){
			double distance = DistanceCalculator.distance(p.getPosition().getLatitude(),p.getPosition().getLongitude(),drone.getStart().getPosition().getLatitude(),drone.getStart().getPosition().getLongitude());
			if(distance > 0){
				if(distance*drone.getDischargeRate() < 1.0){
					possiblePlaces.add(p.getName());
				}
			}
		}
		
		
		//If there are people on board, go where they want to go, if we can reach it.
		boolean routed = false;
		if(drone.getPassengers().size() != 0){
			for(Person p:drone.getPassengers()){
				if(possiblePlaces.contains(p.getDestination())){
					simulator.routeDrone(drone, p.getDestination());
					routed = true;
				}
			}
		}
		
		if(drone.getPassengers().size() < drone.getCapacity()){
			//If we didn't find a place where people on board want to go, find a place where waiting people want to go and hope we take them with us
			if(!routed){
				// If there are people waiting, then pick them up and go where the first one wants to go
				for(Person p:drone.getStart().getWaitingToEmbark()){
					if(possiblePlaces.contains(p.getDestination())){
						simulator.routeDrone(drone, p.getDestination());
						routed = true;
					}
				}
			}
		
			//Find a place where people are waiting and go there
			if(!routed){
				for(Place p: this.simulator.getPlaces()){
					if(possiblePlaces.contains(p.getName())){
						if(!drone.getStart().equals(p)){
							if(p.getWaitingToEmbark().size() > 0){
								simulator.routeDrone(drone, p);
								routed = true;
							}	
						}
					}
				}
			}
		}
		
		//Go to a random place because we aren't analyzing the network graph
		if(!routed){
			int index = simulator.getSimulationController().getRandom().nextInt(possiblePlaces.size());
			ArrayList<String> pp = new ArrayList<String>();
			pp.addAll(possiblePlaces);
			simulator.routeDrone(drone, pp.get(index));
			routed = true;
		}
		
		
		if(!routed){
			System.out.println(possiblePlaces.toString());
			throw new RuntimeException("How could I not be routed by now?");
		}
	}
}
