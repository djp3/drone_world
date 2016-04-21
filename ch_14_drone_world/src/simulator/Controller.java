package simulator;

public interface Controller {

	void setSimulator(Simulator simulator);
	
	void droneEmbarkingStart(Drone drone);
	void droneEmbarkingAGroupStart(Drone drone);
	void droneEmbarkingAGroupEnd(Drone drone);
	void droneEmbarkingEnd(Drone drone);
	
	void droneAscendingStart(Drone drone);
	void droneAscendingEnd(Drone drone);
	
	void droneTransitingStart(Drone drone);
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

	void droneIdling(Drone drone);




}
