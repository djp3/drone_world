package simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import simulator.interfaces.DroneController;
import simulator.safety.Command;

public class DroneControllerSafetyWrapper implements DroneController {
	
	// The initial amount of time a controller can take on a call
	private static final int INITIAL_TIMEOUT = 2 * Simulator.ONE_SECOND;
	private static final int FINAL_TIMEOUT = 10;
	
	// The pool that executes the drone controller calls
	private static final ExecutorService pool = Executors.newCachedThreadPool();
	private static final Object poolLock = new Object();
	
	private static ExecutorService getPool() {
		return pool;
	}
	
	private static Object getPoolLock(){
		return poolLock;
	}
	
	
	// The controller that this class is wrapping
	private DroneController wrapped = null;
	
	
	// This is the timeout for any computation in a drone callback,
	// It maps a method name to a timeout.
	// Each timeout is initially it is set to 10 seconds. 
	// If a call doesn't return then it's timeout decreases, if it does it is reset
	private Map<String, Integer> behaviorManagement;
	
	// If true, then this wrapper does it's work and quarantines a drone that takes too long
	private boolean shouldQuarantine;
	
	
	/**
	 * 
	 * @param c, A controller that is being monitored
	 * @param behaviorUnit
	 * @return
	 */
	private int getBehaviorManagement(String behaviorUnit) {
		Integer integer = behaviorManagement.get(behaviorUnit);
		if(integer == null){
			return INITIAL_TIMEOUT;
		}
		else{
			return integer;
		}
	}

	private void setBehaviorManagement(String behaviorUnit, int behaviorManagement) {
		this.behaviorManagement.put(behaviorUnit,behaviorManagement);
	}

	void behavedBadly(String behaviorUnit) {
		int timeout = this.getBehaviorManagement(behaviorUnit);
		System.err.println("Drone Controller behaved badly: "+wrapped.getCompanyName()+", "+behaviorUnit+", "+timeout+", "+timeout);
		
		if(timeout <= FINAL_TIMEOUT){
			this.setBehaviorManagement(behaviorUnit,FINAL_TIMEOUT);
		}
		else{
			this.setBehaviorManagement(behaviorUnit,timeout/2);
		}
	}
	
	void behavedWell(String behaviorUnit){
		int timeout = this.getBehaviorManagement(behaviorUnit);
		if(timeout < INITIAL_TIMEOUT){
			System.err.println("Drone Controller redeemed itself: "+wrapped.getCompanyName()+", "+behaviorUnit+", "+timeout+", "+timeout);
		}
		this.setBehaviorManagement(behaviorUnit,INITIAL_TIMEOUT);
	}


	private DroneController getWrapped() {
		return wrapped;
	}
	

	private void setWrapped(DroneController wrapped) {
		this.wrapped = wrapped;
	}

	public DroneControllerSafetyWrapper(DroneController wrapMe,boolean shouldQuarantine){
		this.setWrapped(wrapMe);
		this.behaviorManagement = new HashMap<String, Integer>();
		this.shouldQuarantine = shouldQuarantine;
	}
	

	/**
	 * Wrap a controller call that takes no parameters and returns nothing
	 * @param method, the method to call safely
	 */
	private void safeControllerCall(String methodName, Command method) {
		safeControllerCall(methodName, null,null,this.shouldQuarantine,(Drone d,Void v) -> {method.execute();});
	}
	
	/**
	 * Wrap a controller call that takes no parameters and returns a value
	 * @param method, the method to call safely
	 * @return
	 */
	private <R> R safeControllerCall(String methodName, Supplier<R> method){
		return safeControllerCall(methodName, null,null,this.shouldQuarantine,(Drone d, Void v) -> {return method.get();});
	}
	
	
	/**
	 * Wrap a controller call that takes a Drone parameters and returns nothing 
	 * @param drone, The drone to clone and pass to the method
	 * @param method, the method to call safely
	 */
	private void safeControllerCall(String methodName,Drone drone,Consumer<Drone> method) {
		safeControllerCall(methodName,drone,null,this.shouldQuarantine,(Drone d,Object o) -> {method.accept(d); return null;});
	}
	
	/**
	 * Wrap a controller call that takes a Drone parameters and returns a result 
	 * @param drone, The drone to clone and pass to the method
	 * @param method, the method to call safely
	 * @return
	 */
	private <T> void safeControllerCall(String methodName,Drone drone,T data,boolean manageBehavior,BiConsumer<Drone,T> method) {
		safeControllerCall(methodName,drone,data,manageBehavior, (Drone d,T x) -> {method.accept(d,x); return null;});
	}
	
	
	
	/**
	 * Wrap a controller call that takes a Drone and one other parameters and returns a result 
	 * @param drone, The drone to clone and pass to the method
	 * @param data, The other data to pass to the method
	 * @param method, the method to call safely
	 * @return
	 */
	private <T,R> R safeControllerCall(String methodName, Drone drone, T data, boolean manageBehavior, BiFunction<Drone,T,R> method) {
		
		/* To whom should good and bad behavior be credited */
		String behaviorUnit;
		if(drone != null){
			behaviorUnit = drone.getCompanyName()+":"+methodName;
		}
		else{
			behaviorUnit = "Generic Behavior Unit";
		}
		
		
		R result = null;
		Future<R> f = null;
		
		synchronized(getPoolLock()){
			if(!getPool().isShutdown()){
				//Call the idle and allow 1 second for it to complete in case the code is badly formed
				f = getPool().submit(new Callable<R>(){
					@Override
					public R call() throws Exception {
						//Call the method
						if(drone != null){
							return method.apply(new Drone(drone),data);
						}
						else{
							return method.apply(null,data);
						}
					}
				});
			}
		}
		
		if(f != null){
			do{
				try {
					
					if(manageBehavior){
						result = f.get(getBehaviorManagement(behaviorUnit),TimeUnit.MILLISECONDS);
						behavedWell(behaviorUnit);
					}
					else{
						result = f.get();
					}
				} catch (InterruptedException e) {
				} catch (ExecutionException | TimeoutException e) {
					//This is a drone Controller that throws an exception 
					f.cancel(true);
					if(e instanceof ExecutionException){
						System.out.println("Drone Controller threw an exception: ");
						e.printStackTrace();
					}
					else{
						System.out.println("Drone Controller timed out:");
					}
					if(manageBehavior){
						behavedBadly(behaviorUnit);
					
					if(wrapped != null){
							wrapped.droneBehavingBadly(drone);
							if(this.getBehaviorManagement(behaviorUnit) <= FINAL_TIMEOUT){
								if(drone != null){
									drone.quarantine();
								}
							}
						}
						else{
							if(drone != null){
								drone.quarantine();
							}
						}
					}
				}
			}while(!f.isDone());
		}
		
		return result;
	}


	
	@Override
	public void setSimulator(Simulator simulator) {
		safeControllerCall("setSimulator",() -> {this.getWrapped().setSimulator(simulator);});
	}
	
	@Override
	public String getNextDroneName() {
		return safeControllerCall("getNextDroneName",()->{ return this.getWrapped().getNextDroneName();});
	}


	@Override
	public String getCompanyName() {
		return safeControllerCall("getCompanyName",()->{ return this.getWrapped().getCompanyName();});
	}
	

	@Override
	public void droneSimulationStart(Drone drone) {
		safeControllerCall("droneSimulationStart",drone,(Drone d)->{ this.getWrapped().droneSimulationStart(d);});
	}

	@Override
	public void droneSimulationEnd(Drone drone) {
		safeControllerCall("droneSimulationEnd",drone,(Drone d)->{ this.getWrapped().droneSimulationEnd(d);});
		synchronized(getPoolLock()){
			getPool().shutdown();
			while(!getPool().isTerminated()){
				try {
					getPool().awaitTermination(10, TimeUnit.SECONDS);
					if(!getPool().isTerminated()){
						getPool().shutdownNow();
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}
	

	@Override
	public void droneBehavingBadly(Drone drone) {
		boolean manageBehavior = false;
		safeControllerCall("droneBehavingBadly",drone,(Object) null,manageBehavior,(Drone d,Object o)->{ this.getWrapped().droneBehavingBadly(d);});
	}
	

	@Override
	public void droneEmbarkingStart(Drone drone) {
		safeControllerCall("droneEmbarkingStart",drone, (Drone d) -> {this.getWrapped().droneEmbarkingStart(d);});
	}

	@Override
	public void droneEmbarkingAGroupStart(Drone drone) {
		safeControllerCall("droneEmbarkingAGroupStart",drone, (Drone d) -> {this.getWrapped().droneEmbarkingAGroupStart(d);});
	}

	@Override
	public void droneEmbarkingAGroupEnd(Drone drone) {
		safeControllerCall("droneEmbarkingAGroupEnd",drone, (Drone d) -> {this.getWrapped().droneEmbarkingAGroupEnd(d);});
	}

	@Override
	public void droneEmbarkingEnd(Drone drone) {
		safeControllerCall("droneEmbarkingEnd",drone, (Drone d) -> {this.getWrapped().droneEmbarkingEnd(d);});
	}

	@Override
	public void droneAscendingStart(Drone drone) {
		safeControllerCall("droneAscendingStart",drone, (Drone d) -> {this.getWrapped().droneAscendingStart(d);});
	}

	@Override
	public void droneAscendingEnd(Drone drone) {
		safeControllerCall("droneAscendingEnd",drone, (Drone d) -> {this.getWrapped().droneAscendingEnd(d);});
	}

	@Override
	public void droneTransitingStart(Drone drone) {
		safeControllerCall("droneTransitingStart",drone, (Drone d) -> {this.getWrapped().droneTransitingStart(d);});
	}

	@Override
	public void droneTransiting(Drone drone, double percent) {
		boolean manageBehavior = true;
		safeControllerCall("droneTransiting",drone, percent,manageBehavior, (Drone d,Double p) -> {this.getWrapped().droneTransiting(d,p);});
	}

	@Override
	public void droneTransitingEnd(Drone drone) { 
		safeControllerCall("droneTranistingEnd",drone, (Drone d) -> {this.getWrapped().droneTransitingEnd(d);});
	}

	@Override
	public void droneDescendingStart(Drone drone) {
		safeControllerCall("droneDescendingStart",drone, (Drone d) -> {this.getWrapped().droneDescendingStart(d);});
	}

	@Override
	public void droneDescendingEnd(Drone drone) { 
		safeControllerCall("droneDescendingEnd",drone, (Drone d) -> {this.getWrapped().droneDescendingEnd(d);});
	}

	@Override
	public void droneDisembarkingStart(Drone drone) {
		safeControllerCall("droneDisembarkingStart",drone, (Drone d) -> {this.getWrapped().droneDisembarkingStart(d);});
	}

	@Override
	public void droneDisembarkingGroupStart(Drone drone) {
		safeControllerCall("droneDisembarkingGroupStart",drone, (Drone d) -> {this.getWrapped().droneDisembarkingGroupStart(d);});
	}

	@Override
	public void droneDisembarkingGroupEnd(Drone drone) {
		safeControllerCall("droneDisembarkingGroupEnd",drone, (Drone d) -> {this.getWrapped().droneDisembarkingGroupEnd(d);});
	}

	@Override
	public void droneDisembarkingEnd(Drone drone) {
		safeControllerCall("droneDisembarkingEnd",drone, (Drone d) -> {this.getWrapped().droneDisembarkingEnd(d);});
	}

	@Override
	public void droneRechargingStart(Drone drone) {
		safeControllerCall("doneRechargingStart",drone, (Drone d) -> {this.getWrapped().droneRechargingStart(d);});
	}

	@Override
	public void droneRecharging(Drone drone, double percent) {
		boolean manageBehavior = true;
		safeControllerCall("droneRecharging",drone, percent, manageBehavior,(Drone d,Double p) -> {this.getWrapped().droneRecharging(d,p);});
	}

	@Override
	public void droneDoneRecharging(Drone drone) {
		safeControllerCall("droneDoneRecharging", drone, (Drone d) -> {this.getWrapped().droneDoneRecharging(d);});
	}

	@Override
	public void droneIdling(Drone drone) {
		safeControllerCall("droneIdling",drone, (Drone d) -> {this.getWrapped().droneIdling(d);});
	}

	@Override
	public void droneExploding(Drone drone) {
		safeControllerCall("droneExploding",drone, (Drone d) -> {this.getWrapped().droneExploding(d);});
	}

	@Override
	public void droneHasDied(Drone drone) {
		safeControllerCall("droneHasDied",drone, (Drone d) -> {this.getWrapped().droneHasDied(d);});
	}

}
