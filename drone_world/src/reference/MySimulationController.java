package reference;

import java.util.Random;

import simulator.interfaces.SimulationController;

public class MySimulationController implements SimulationController {
	
	//Set this to true to get consistent behavior each run while debugging
	//Set it to false to get different random numbers on each run
	static private final boolean SAME_RANDOM_NUMBERS_EACH_TIME = true; //false
	static private final int RANDOM_NUMBER_SEED = 25; //Only matters if above is true 
	
	//True if you want to render the drones (longer to start up when higher resolution)
	static private final boolean HIGH_RESOLUTION = true;
	
	//Number of types of 3D models to load to represent drones  (up to 4, the more you pick the longer to start up)
	static private final int DRONE_MODEL_COUNT = 4;
	
	//1 for real-time
	//100 (max) to run the simulator as fast as possible
	static private final int SIMULATOR_SPEED = 100;
	
	//While debugging it is helpful to not have the simulator quarantine your drones because
	//if you pause your code to debug it, then if that pause is longer than 10 seconds then it
	//causes your drone to be quarantined
	static private final boolean QUARANTINE_DRONES = true;
	
	//Stop the simulation after this many clock ticks in case the drones aren't making
	//any progress
	static private final int SIMULATION_END_TIME = 20_000_000;
	
	
	// A global source of randomness
	public static final Random random;
	static{
		if(SAME_RANDOM_NUMBERS_EACH_TIME){
			random = new Random(RANDOM_NUMBER_SEED);
		}
		else{
			random = new Random();
		}
	}
	
	public Random getRandom() {
		return random;
	}

	@Override
	public boolean isHighResolution() {
		return HIGH_RESOLUTION;
	}
	
	@Override
	public int getNumberOfDroneModels() {
		return DRONE_MODEL_COUNT;
	}

	@Override
	public long simulatorSpeed() {
		return SIMULATOR_SPEED;
	}
	
	@Override
	public boolean shouldQuarantineDrones(){
		return QUARANTINE_DRONES;
	}
	
	@Override
	public long getSimulationEndTime() {
		return SIMULATION_END_TIME;
	}
	

}
