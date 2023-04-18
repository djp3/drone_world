package reference;

import java.util.Random;

import simulator.interfaces.SimulationController;

public class MySimulationController implements SimulationController {
	
	//Visualization settings
	public static final boolean FULL_SCREEN = false;
	public static final int SCREEN_WIDTH = 1280;
	public static final int SCREEN_HEIGHT = 720;
	
	//Set this to true to get consistent behavior each run while debugging
	//Set it to false to get different random numbers on each run
	static private final boolean SAME_RANDOM_NUMBERS_EACH_TIME = true;
	static private final int RANDOM_NUMBER_SEED = 25; //Only matters if above is true 
	
	//True if you want to render the drones (longer to start up when higher resolution)
	static private final boolean HIGH_RESOLUTION = true;
	
	//Number of types of 3D models to load to represent drones  (up to 3, the more you pick the longer to start up)
	static private final int DRONE_MODEL_COUNT = 3;
	
	//1 for one second in game time is one second in real-time (slow)
	//100 to run the simulator as fast as possible (fast)
	static private final int SIMULATOR_SPEED = 20;
	
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
	public int getSimulatorSpeed() {
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
