package reference;

import simulator.Drone;
import simulator.Person;
import simulator.Place;

public class GreedyController extends DroneControllerSkeleton {
	
	static int nameCount=0;
	public static final String companyName = "Greedy & Associates";
	
	public static synchronized int getNameCount() {
		return nameCount;
	}

	public static synchronized void setNameCount(int nameCount) {
		GreedyController.nameCount = nameCount;
	}
	
	public GreedyController() {
		super();
	}
	
	@Override
	public String getNextDroneName() {
		setNameCount(getNameCount()+1);
		return "Greedy Drone "+getNameCount();
	}

	@Override
	public String getCompanyName() {
		return companyName;
	}

	
	public void output(Drone drone){
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
		
		//If there are people waiting, service them
		if(drone.getStart().getWaitingToEmbark().size() != 0){
			int whichNinja = this.simulator.getSimulationController().getRandom().nextInt(drone.getStart().getWaitingToEmbark().size());
			String p = drone.getStart().getWaitingToEmbark().get(whichNinja).getDestination();
			simulator.setDroneManifest(drone, p);
			simulator.routeDrone(drone, p);
		}
		else{
			//Go somewhere where people are waiting
			for(Place p: simulator.getPlaces()){
				if(!drone.getStart().equals(p)){
					if(p.getWaitingToEmbark().size() > 0){
						simulator.setDroneManifest(drone, p.getName());
						simulator.routeDrone(drone, p);
						return;
					}
				}
			}
		}
		
		if(drone.getPassengers().size() > 0){
			simulator.routeDrone(drone, drone.getPassengers().iterator().next().getDestination());
		}
	}
}
