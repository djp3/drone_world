package reference;

import java.util.TreeSet;

import simulator.Drone;
import simulator.Person;
import simulator.Place;

public class PromiscuousController extends DroneControllerSkeleton {
	
	static int nameCount=0;
	public static final String companyName = "Promiscuous DroneCo.";
	
	public static synchronized int getNameCount() {
		return nameCount;
	}

	public static synchronized void setNameCount(int nameCount) {
		PromiscuousController.nameCount = nameCount;
	}

	public PromiscuousController() {
		super();
	}

	@Override
	public synchronized String getNextDroneName() {
		setNameCount(getNameCount()+1);
		return "Promiscuous Drone "+getNameCount();
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
	public void droneBehavingBadly(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Behaving Badly");
	}

	@Override
	public void droneIdling(Drone drone) {
		super.droneIdling(drone);
		TreeSet<String> manifest = new TreeSet<String>();
		for(Place p: this.simulator.getPlaces()){
			manifest.add(p.getName());
		}
		
		//If there are people on board, go where they want to go
		if(drone.getPassengers().size() != 0){
			simulator.setDroneManifest(drone, manifest);
			simulator.routeDrone(drone, drone.getPassengers().iterator().next().getDestination());
		}
		else{
			// If there are people waiting, then pick them up and go where the first one wants to go
			if(drone.getStart().getWaitingToEmbark().size() != 0){
				simulator.setDroneManifest(drone, manifest);
				String p = drone.getStart().getWaitingToEmbark().get(0).getDestination();
				simulator.setDroneManifest(drone, manifest);
				simulator.routeDrone(drone, p);
			}
			else{
				//Find a place where people are waiting and go there
				for(Place p: simulator.getPlaces()){
					if(!drone.getStart().equals(p)){
						if(p.getWaitingToEmbark().size() > 0){
							simulator.setDroneManifest(drone, manifest);
							simulator.routeDrone(drone, p);
							return;
						}
					}
				}
			}
		}
	}
}
