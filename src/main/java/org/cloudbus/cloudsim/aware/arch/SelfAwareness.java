package org.cloudbus.cloudsim.aware.arch;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public abstract class SelfAwareness extends SimEntity {

	protected boolean isActive;

	
	public SelfAwareness(String name) {
		super(name);
	}
	
	abstract void act(SimEvent ev);

	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}
		Log.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this QoSMonitor.");
	}

	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	protected boolean isActive() {
		return isActive;	
	}
	
	protected void setActive(boolean isActive) {
		this.isActive = isActive;
	}
		

}
