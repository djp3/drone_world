package simulator;

public interface Controller {

	/**
	 * This is called by the simulator so that the Controller can have access to the people, places and drones that simulator keeps track of
	 *  
	 * @param simulator
	 */
	void setSimulator(Simulator simulator);
	
	/**
	 * return true if you want the drone's fully rendered, which takes a little more time on start 
	 */
	boolean isHighResolution();
	/**
	 * How fast do you want the simulator to run?
	 * 1 is real-time (1 simulator second is one real-second)
	 * 100 is faster (as fast as possible);
	 * 
	 */
	long simulatorSpeed();
	
	
	void droneEmbarkingStart(Drone drone);
	void droneEmbarkingAGroupStart(Drone drone);
	void droneEmbarkingAGroupEnd(Drone drone);
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
	 * A drone that is idling should have it's start and destination be the same and be the place where it current is
	 * @param drone
	 */
	void droneIdling(Drone drone);




}
