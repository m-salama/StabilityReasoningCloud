package org.cloudbus.cloudsim.adaptive.stability.arch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.goal.Goal;
import org.cloudbus.cloudsim.adaptive.goal.GoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class DetectorStability extends SimEntity {

	/** The detector singleton instance. */
	private static DetectorStability detector;
	
	private int adaptationEngineId;	
	private int datacenterId;


	public DetectorStability(String name) {
		super(name);
	}

	/**
     * Create a static method to get instance.
    */
    public static DetectorStability getInstance(){
        if(detector == null){
        	detector = new DetectorStability("DetectorStability");
        }
        return detector;
    }

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
	}

	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute detection of violations for Adaptation Goals
		case CloudSimTags.SAD_DETECT_VIOLATIONS_STABILITY:
			processDetectViolations(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	@SuppressWarnings("unchecked")
	protected void processDetectViolations(SimEvent ev){
		datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		adaptationEngineId = AdaptationEngineStability.getInstance().getId();
		
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Detecting violations...");

		Map<String,ArrayList<Double>> monitorData = new HashMap<String,ArrayList<Double>>();
		monitorData = (Map<String, ArrayList<Double>>) ev.getData();

		boolean violationDetected = false;
		
		// sort goals by weight
		GoalsModel.getInstance().sortGoalsByWeight();

		//compare with the list of goals in the GoalsModel
		for (Goal g : GoalsModel.getInstance().getGoals()) {
			//get list of values for each goal
			ArrayList<Double> values = new ArrayList<Double>();
			values = monitorData.get(g.getName());		
			
			boolean isViolated = g.checkViolaton(values);
			g.setViolated(isViolated);
			violationDetected = violationDetected || isViolated;
			if (isViolated) {
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] Violations detected in goal " + g.getName() + ".");
			}
		}
		
		//send violations to the AdaptationEngine for taking adaptation decision
		if (violationDetected){
			send(adaptationEngineId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAD_ADAPTATION_DECISION_STABILITY);
		} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] No violations detected.");
		}
		
		// add adaptation overhead
		SelfAdaptiveArchitectureStability.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}

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

}
