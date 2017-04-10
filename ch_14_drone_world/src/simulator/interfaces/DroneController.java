package simulator.interfaces;

import simulator.Drone;
import simulator.Simulator;

public interface DroneController {
	
	
	/**
	 * This is called by the simulator so that the Controller
	 * can have access to the people, places and drones that simulator keeps track of.
	 *  
	 * @param simulator
	 */
	void setSimulator(Simulator simulator);
	
	/**
	 * This method should return human relevant names for drones.  It can return as many or as few unique ones as desired.
	 * @return The name of a drone, e.g., "Grumpy"
	 */
	String getNextDroneName();

	/**
	 * This method should return the name of the company that made this controller.  
	 * @return The company name, e.g., "Patterson Intelligent Drone Corporation"
	 */
	String getCompanyName();

	
	
	/***********************************/
	/* Simulator Life cycle call backs */
	
	/**
	 * The simulator is about to start simulating drone, d
	 * @param d
	 */
	void droneSimulationStart(Drone d);
	
	/**
	 * The simulator is has ended simulating drone, d
	 * @param d
	 */
	void droneSimulationEnd(Drone d);
	
	/**
	 * The simulator calls this if the drone controller timed out on some call and is being quarantined
	 * @param d
	 */
	void droneBehavingBadly(Drone d);
	/***********************************/
	
	
	/***********************************/
	/* Drone Life cycle call backs */
	
	// This is called when a drone starts embarking at all
	void droneEmbarkingStart(Drone drone);
	// This is called before each batch of people starts embarking 
	void droneEmbarkingAGroupStart(Drone drone);
	// This is called before each batch of people finishes embarking 
	void droneEmbarkingAGroupEnd(Drone drone);
	// This is called when embarking is completely done
	void droneEmbarkingEnd(Drone drone);
	
	void droneAscendingStart(Drone drone);
	void droneAscendingEnd(Drone drone);
	
	void droneTransitingStart(Drone drone);
	/**
	 * Intermediate notification of transit progress.
	 * @param drone, A copy of the drone that is transiting
	 * @param percent, How far along *original* trip the drone has gone.  If the drone is rerouted midstream to a destination further away than the original destination,
	 * then this can be greater than 100.
	 */
	void droneTransiting(Drone drone,double percent);
	void droneTransitingEnd(Drone drone);
	
	/** 
	 * The drone has run out of charge mid flight and is exploding
	 * @param drone
	 */
	void droneExploding(Drone drone);
	/**
	 * This drone has crashed and is no longer in service
	 * @param drone
	 */
	void droneHasDied(Drone drone);
	
	void droneDescendingStart(Drone drone);
	void droneDescendingEnd(Drone drone);
	
	void droneDisembarkingStart(Drone drone);
	void droneDisembarkingGroupStart(Drone drone);
	void droneDisembarkingGroupEnd(Drone drone);
	void droneDisembarkingEnd(Drone drone);
	
	void droneRechargingStart(Drone drone);
	void droneRecharging(Drone drone,double percent);
	void droneDoneRecharging(Drone drone);
	
	/**
	 * A drone that is idling should have it's start and destination be the same which is also the 
	 * place where it current is
	 * @param drone
	 */
	void droneIdling(Drone drone);
	
	/* End Lifecyle routines */

}
