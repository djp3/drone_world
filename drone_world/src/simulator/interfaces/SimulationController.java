package simulator.interfaces;

import java.util.Random;

public interface SimulationController {
	
	/**
	 * A global source of random numbers
	 * @return
	 */
	public Random getRandom();
	
	
	
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
	
	/**
	 *  This returns true if the simulator should quarantine drones for responding to slowly.
	 *  This is set to true for the test simulation, but for debugging it's helpful to set it to false
	 */
	public boolean shouldQuarantineDrones();



	/**
	 * 
	 * @return The number of clock ticks at which the simulation should be ended regardless of
	 * any outstanding deliveries
	 */
	long getSimulationEndTime();

}
