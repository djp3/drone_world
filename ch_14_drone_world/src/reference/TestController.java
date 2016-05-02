package reference;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import simulator.Drone;
import simulator.Place;
import simulator.Simulator;
import simulator.interfaces.DroneController;

public class TestController implements DroneController  {

	// This is set by the simulator when the simulation starts so that you can
	// get access to the places, drones, people, etc.
	// with simulator.getX
	Simulator simulator = null;

	@Override
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	protected Simulator getSimulator() {
		return this.simulator;
	}

	@Override
	public void droneEmbarkingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneEmbarkingAGroupStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneEmbarkingAGroupEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneEmbarkingEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneAscendingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneAscendingEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneTransitingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneTransiting(Drone drone, double percent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneTransitingEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDescendingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDescendingEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDisembarkingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDisembarkingGroupStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDisembarkingGroupEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDisembarkingEnd(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneRechargingStart(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneRecharging(Drone drone, double percent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneDoneRecharging(Drone drone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void droneIdling(Drone drone) {
		Simulator s =  this.getSimulator();
		
		//Set manifest to all places
		Set<String> places = new TreeSet<String>();
		for(Place p: s.getPlaces()){
			places.add(p.getName());
		}
		simulator.setDroneManifest(drone, places);
		
		//Figure out where this drone should go
		ArrayList<String> placeList = new ArrayList<String>();
		placeList.addAll(places);
		//Remove current place
		placeList.remove(drone.getStart().getName());
		
		//Use name to pick the next place so drone's go different places
		int index = Integer.parseInt(drone.getName());
		
		simulator.routeDrone(drone,placeList.get(index));
	}

	static int droneCounter = 0;
	static void incrementCounter(){
		droneCounter++;
	}
	
	@Override
	public String getNextDroneName() {
		String answer = ""+droneCounter;
		incrementCounter();
		return answer;
	}

	@Override
	public String getCompanyName() {
		return "Test Controller";
	}

}
