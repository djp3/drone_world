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

/**
 * This is the class that students should work with to create there drone controller
 * 
 */
public class DroneControllerSafetyWrapper implements DroneController {
	
	// The initial amount of time a controller can take on a call
	private static final int INITIAL_TIMEOUT = 10 * Simulator.ONE_SECOND;
	
	// The pool that executes the drone controller calls
	private static final ExecutorService pool = Executors.newFixedThreadPool(10);
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
	
	
	/**
	 * 
	 * @param c, A controller that is being monitored
	 * @param methodName
	 * @return
	 */
	private int getBehaviorManagement(String methodName) {
		Integer integer = behaviorManagement.get(methodName);
		if(integer == null){
			return INITIAL_TIMEOUT;
		}
		else{
			return integer;
		}
	}

	private void setBehaviorManagement(String methodName, int behaviorManagement) {
		this.behaviorManagement.put(methodName,behaviorManagement);
	}

	void behavedBadly(String methodName) {
		int timeout = this.getBehaviorManagement(methodName);
		System.err.println("Drone Controller behaved badly: "+wrapped.getCompanyName()+", "+methodName+", "+timeout+", "+timeout);
		
		if(timeout <= 10){
			this.setBehaviorManagement(methodName,10);
		}
		else{
			if(timeout < 1000){
				if(timeout < 100){
					this.setBehaviorManagement(methodName,timeout - 10);
				}
				else{
					this.setBehaviorManagement(methodName,timeout - 100);
				}
			}
			else{
				this.setBehaviorManagement(methodName,timeout/2);
			}
		}
	}
	
	void behavedWell(String methodName){
		int timeout = this.getBehaviorManagement(methodName);
		if(timeout < INITIAL_TIMEOUT){
			System.err.println("Drone Controller redeemed itself: "+wrapped.getCompanyName()+", "+methodName+", "+timeout+", "+timeout);
		}
		this.setBehaviorManagement(methodName,INITIAL_TIMEOUT);
	}


	private DroneController getWrapped() {
		return wrapped;
	}
	

	private void setWrapped(DroneController wrapped) {
		this.wrapped = wrapped;
	}

	public DroneControllerSafetyWrapper(DroneController wrapMe){
		this.setWrapped(wrapMe);
		this.behaviorManagement = new HashMap<String, Integer>();
	}
	

	/**
	 * Wrap a controller call that takes no parameters and returns nothing
	 * @param method, the method to call safely
	 */
	private void safeControllerCall(Command method) {
		safeControllerCall(null,null,(Drone d,Void v) -> {method.execute();});
	}
	
	/**
	 * Wrap a controller call that takes no parameters and returns a value
	 * @param method, the method to call safely
	 * @return
	 */
	private <R> R safeControllerCall(Supplier<R> method){
		return safeControllerCall(null,null,(Drone d, Void v) -> {return method.get();});
	}
	
	
	/**
	 * Wrap a controller call that takes a Drone parameters and returns nothing 
	 * @param drone, The drone to clone and pass to the method
	 * @param method, the method to call safely
	 */
	private void safeControllerCall(Drone drone,Consumer<Drone> method) {
		safeControllerCall(drone,null,(Drone d,Object o) -> {method.accept(d); return null;});
	}
	
	/**
	 * Wrap a controller call that takes a Drone parameters and returns a result 
	 * @param drone, The drone to clone and pass to the method
	 * @param method, the method to call safely
	 * @return
	 */
	private <T> void safeControllerCall(Drone drone,T data,BiConsumer<Drone,T> method) {
		safeControllerCall(drone,data,(Drone d,T x) -> {method.accept(d,x); return null;});
	}
	
	
	
	/**
	 * Wrap a controller call that takes a Drone and one other parameters and returns a result 
	 * @param drone, The drone to clone and pass to the method
	 * @param data, The other data to pass to the method
	 * @param method, the method to call safely
	 * @return
	 */
	private <T,R> R safeControllerCall(Drone drone,T data,BiFunction<Drone,T,R> method) {
		String methodName;
		methodName = method.toString();
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
		
		while((f != null) && (!f.isDone())){
			try {
				result = f.get(getBehaviorManagement(methodName),TimeUnit.MILLISECONDS);
				behavedWell(methodName);
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				//This is a drone Controller that throws an exception 
				System.out.println("Drone Controller threw an exception: "+e);
				behavedBadly(methodName);
			} catch (TimeoutException e) {
				//This is an idler that doesn't stop computing
				f.cancel(true);
				behavedBadly(methodName);
			}
		}
		
		return result;
	}


	
	@Override
	public void setSimulator(Simulator simulator) {
		safeControllerCall(() -> {this.getWrapped().setSimulator(simulator);});
	}
	
	@Override
	public String getNextDroneName() {
		return safeControllerCall(()->{ return this.getWrapped().getNextDroneName();});
	}


	@Override
	public String getCompanyName() {
		return safeControllerCall(()->{ return this.getWrapped().getCompanyName();});
	}
	

	@Override
	public void droneSimulationStart(Drone d) {
		safeControllerCall(()->{ this.getWrapped().droneSimulationStart(d);});
	}

	@Override
	public void droneSimulationEnd(Drone d) {
		safeControllerCall(()->{ this.getWrapped().droneSimulationEnd(d);});
		synchronized(getPoolLock()){
			getPool().shutdown();
		}
	}

	@Override
	public void droneEmbarkingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneEmbarkingStart(d);});
	}

	@Override
	public void droneEmbarkingAGroupStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneEmbarkingAGroupStart(d);});
	}

	@Override
	public void droneEmbarkingAGroupEnd(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneEmbarkingAGroupEnd(d);});
	}

	@Override
	public void droneEmbarkingEnd(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneEmbarkingEnd(d);});
	}

	@Override
	public void droneAscendingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneAscendingStart(d);});
	}

	@Override
	public void droneAscendingEnd(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneAscendingEnd(d);});
	}

	@Override
	public void droneTransitingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneTransitingStart(d);});
	}

	@Override
	public void droneTransiting(Drone drone, double percent) {
		safeControllerCall(drone, percent, (Drone d,Double p) -> {this.getWrapped().droneTransiting(d,p);});
	}

	@Override
	public void droneTransitingEnd(Drone drone) { 
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneTransitingEnd(d);});
	}

	@Override
	public void droneDescendingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDescendingStart(d);});
	}

	@Override
	public void droneDescendingEnd(Drone drone) { 
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDescendingEnd(d);});
	}

	@Override
	public void droneDisembarkingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDisembarkingStart(d);});
	}

	@Override
	public void droneDisembarkingGroupStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDisembarkingGroupStart(d);});
	}

	@Override
	public void droneDisembarkingGroupEnd(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDisembarkingGroupEnd(d);});
	}

	@Override
	public void droneDisembarkingEnd(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDisembarkingEnd(d);});
	}

	@Override
	public void droneRechargingStart(Drone drone) {
		safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneRechargingStart(d);});
	}

	@Override
	public void droneRecharging(Drone drone, double percent) {
		safeControllerCall(drone, percent, (Drone d,Double p) -> {this.getWrapped().droneRecharging(d,p);});
	}

	@Override
	public void droneDoneRecharging(Drone drone) { safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneDoneRecharging(d);});
	}

	@Override
	public void droneIdling(Drone drone) { safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneIdling(d);});
	}

	@Override
	public void droneExploding(Drone drone) { safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneExploding(d);});
	}

	@Override
	public void droneHasDied(Drone drone) { safeControllerCall(drone, (Drone d) -> {this.getWrapped().droneHasDied(d);});
	}
}
