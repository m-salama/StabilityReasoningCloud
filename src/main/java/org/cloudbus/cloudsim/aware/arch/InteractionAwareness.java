package org.cloudbus.cloudsim.aware.arch;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class InteractionAwareness extends SelfAwareness {

	/** The InteractionAwareness singleton instance. */
	private static InteractionAwareness interactionAwareness;


	public InteractionAwareness(String name) {
		super(name);
}

	/**
     * Create a static method to get instance.
    */
	public static InteractionAwareness getInstance() {
        if(interactionAwareness == null){
        	interactionAwareness = new InteractionAwareness("InteractionAwareness");
        }
        return interactionAwareness;
	}

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");		
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute adaptation decisions
		case CloudSimTags.SAW_INTERACTION_AWARENESS:
			act(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	@Override
	public void act(SimEvent ev) {
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Running interaction-awareness...");
		
	}


}
