package reference;

import java.util.Random;

import simulator.interfaces.SimulationController;

public class MySimulationController implements SimulationController {
	
	// A global source of randomness
	public static final Random random = new Random(10);

	public Random getRandom() {
		return random;
	}

	@Override
	public boolean isHighResolution() {
		return true;
	}

	@Override
	public long simulatorSpeed() {
		return 20;
	}

}
