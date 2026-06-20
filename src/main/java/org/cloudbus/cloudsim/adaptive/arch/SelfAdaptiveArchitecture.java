package org.cloudbus.cloudsim.adaptive.arch;

import java.util.LinkedList;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.AdaptationRecord;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import helper.Constants;

/**
 * SelfAdaptiveArchitecture class encompasses adaptation components for the Adaptive Datacenter
 * and responsible about setting adaptation goals.
 * 
 * @author m.salama
 * @version SAdCloudSim
 */
public class SelfAdaptiveArchitecture extends SimEntity {	
	
	/** The self-adaptive architecture singleton instance. */
	private static SelfAdaptiveArchitecture selfAaptiveArchitecture;
	
	/** The datacenter id where the architecture is placed. */
	private int datacenterId;

	/** The adaptive architecture components. */
	protected static Monitor monitor;
	protected static Detector detector;
	protected static AdaptationEngine adaptationEngine;
	protected static AdaptationExecutor adaptationExecutor;
	
	/** The time overhead for adaptation. */
	protected double adaptationOverhead;

	/** The adaptation history. */
	public LinkedList<AdaptationRecord> adaptationHistory;

	
	/**
     * Create a static method to get instance.
    */
    public static SelfAdaptiveArchitecture getInstance(){
        if(selfAaptiveArchitecture == null){
        	selfAaptiveArchitecture = new SelfAdaptiveArchitecture("SelfAdaptiveArchitecture");
        }
        return selfAaptiveArchitecture;
    }

	protected SelfAdaptiveArchitecture(String name) {
		super(name);

		//initialise self-adaptive architecture components
		monitor = Monitor.getInstance();
		detector = Detector.getInstance();
		adaptationEngine = AdaptationEngine.getInstance();
		adaptationExecutor = AdaptationExecutor.getInstance();
		
		adaptationOverhead = 0.0;
		adaptationHistory = new LinkedList<AdaptationRecord>();
	}
	
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		
		//schedule first monitoring event when starting the monitor component
		scheduleMonitoring();	
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {

		// other unknown tags are processed by this method
		default:
			processEvent(ev);
			break;
		}				
	}

	private void scheduleMonitoring() {
		schedule(monitor.getId(), helper.Constants.MONITORING_INTERVAL, CloudSimTags.SAD_MONITOR);
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
		Log.printLine();		
	}

	/**
	 * Gets the data center.
	 * 
	 * @return the data center where the architecture runs
	 */
	public int getDatacenterId() {
		return datacenterId;
	}

	/**
	 * Sets the data center.
	 * 
	 * @param datacenter the data center for this architecture
	 */
	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}

	/**
	 * Gets adaptation overhead.
	 * 
	 * @return adaptation overhead
	 */
	public double getAdaptationOverhead() {
		return adaptationOverhead;
	}

	/**
	 * Sets adaptation overhead.
	 * 
	 * @param overhead the adaptation overhead
	 */
	public void setAdaptationOverhead(double overhead) {
		this.adaptationOverhead = overhead;
	}

	/**
	 * Accumulate adaptation overhead.
	 * 
	 * @param overhead the adaptation overhead
	 */
	public void accumulateAdaptationOverhead(double newOverhead) {
		this.adaptationOverhead += newOverhead;
	}

	public LinkedList<AdaptationRecord> getAdaptationHistory() {
		return adaptationHistory;
	}

	public void setAdaptationHistory(LinkedList<AdaptationRecord> adaptationHistory) {
		this.adaptationHistory = adaptationHistory;
	}
	
	public void addAdaptationRecord(double adaptationTime, String adaptationTag, int hostNo, int vmNo) {
		AdaptationRecord record = new AdaptationRecord(adaptationTime, adaptationTag, hostNo, vmNo);
		this.adaptationHistory.add(record);
	}


}
