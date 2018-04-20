package reference;

import simulator.Drone;
import simulator.Simulator;
import simulator.interfaces.DroneController;

/** 
 * This is a class that is a parent class for students' drone controllers.
 * Students should override methods in here by changing MyDroneController.java
 * @author djp3
 *
 */
public class DroneControllerSkeleton implements DroneController {

	/*********************************************/
	/****	Things students shouldn't override *****/
	// This is set by the simulator when the simulation starts so that you can get access to the places, drones, people, etc. with
	//	simulator.getPlaces(),
	//	simulator.getDrones(),
	//	simulator.getPeople() etc.
	private Simulator simulator = null;
	
	@Override
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}
	
	protected Simulator getSimulator(){
		return this.simulator;
	}
	/*********************************************/
	
	/*********************************************/
	/****	Things students can override *****/
	static int droneCounter = 0;
	static int incrementDroneCounter() {
		droneCounter++;
		return droneCounter;
	}
	
	@Override
	public String getNextDroneName() {
		return "Skeleton Drone #"+incrementDroneCounter();
	}

	@Override
	public String getCompanyName() {
		return "Skeleton Drone Controller";
	}

	/*** These are all the messages that the simulator will send to the controller when drones are in different situations ***/
	@Override
	public void droneSimulationStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Simulation Starting");
	}

	@Override
	public void droneSimulationEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Simulation Ending");
	}
	

	@Override
	public void droneBehavingBadly(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Behaving Badly");
	}

	@Override
	public void droneEmbarkingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Embarking Start");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneEmbarkingAGroupStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Embarking Group Start");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneEmbarkingAGroupEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Embarking Group End");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneEmbarkingEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Embarking End");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneAscendingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Ascending Start");
	}

	@Override
	public void droneAscendingEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Ascending End");
	}

	@Override
	public void droneTransitingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Transit Start");
	}

	@Override
	public void droneTransiting(Drone drone, double percent) {
		System.out.println(String.format("*** Simulator told: Drone %s: In Transit %5.2f%%, Charge: %5.2f%%",drone.getId(), (percent * 100f), 100f*drone.getCharge()));
	}

	@Override
	public void droneTransitingEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Transit End");
	}

	@Override
	public void droneDescendingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Descending Start");
	}

	@Override
	public void droneDescendingEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Descending End");
	}

	@Override
	public void droneDisembarkingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Disembarking Start");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneDisembarkingGroupStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Disembarking Group Start");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneDisembarkingGroupEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Disembarking Group End");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneDisembarkingEnd(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Disembarking End");
		System.out.println("    " + drone.getEmbarkers().size()+" embarking");
		System.out.println("    " + drone.getPassengers().size()+" passengers");
		System.out.println("    " + drone.getDisembarkers().size()+" disembarking");
	}

	@Override
	public void droneRechargingStart(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Recharging Start");
	}

	@Override
	public void droneRecharging(Drone drone, double percent) {
		System.out.println(String.format("*** Simulator told: Drone %s: Charge at %5.2f%%",drone.getId(), + (percent * 100)));

	}

	@Override
	public void droneDoneRecharging(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Recharging End");

	}

	@Override
	public void droneIdling(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Idling");
	}

	@Override
	public void droneExploding(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Exploding");
	}

	@Override
	public void droneHasDied(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Died");
	}

}
