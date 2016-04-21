
package reference;

import simulator.Controller;
import simulator.Drone;
import simulator.Simulator;

public class MyController implements Controller {
	
	protected Simulator simulator;

	@Override
	public boolean isHighResolution() {
		return true;
	}

	@Override
	public long simulatorSpeed() {
		return 20;
	}

	@Override
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
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
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": In Transit " + (percent * 100) + "%");

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
		System.out
				.println("*** Simulator told: Drone " + drone.getId() + ": Charge at " + (percent * 100) + "%");

	}

	@Override
	public void droneDoneRecharging(Drone drone) {
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Recharging End");

	}

	@Override
	public void droneIdling(Drone drone) {
		simulator.getPlaces();
		System.out.println("*** Simulator told: Drone " + drone.getId() + ": Drone Idling");
	}


}
