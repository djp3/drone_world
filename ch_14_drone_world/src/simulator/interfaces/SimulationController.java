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

}
