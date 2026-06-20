package org.cloudbus.cloudsim.aware.arch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.AdaptationRecord;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * SelfAwareArchitecture class encompasses self-awareness and self-expression components 
 * for the Aware Datacenter
 * and responsible about setting adaptation goals.
 * 
 * @author m.salama
 * @version SAwCloudSim
 */
public class SelfAwareArchitecture extends SimEntity {	
	
	/** The self-aware architecture singleton instance. */
	private static SelfAwareArchitecture selfAwareArchitecture;
	
	/** The datacenter where the architecture is placed. */
	private int datacenterId;
	
	/** The QoS Monitor component. */	
	protected QoSMonitor qosMonitor;

	/** The self-awareness main component to switch between different capabilities. */	
	protected SelfAwarenessComponent selfAwarenessComponent;

	/** The self-expression component to execute adaptations. */	
	protected SelfExpressionComponent selfExpressionComponent;

	/** The time overhead for adaptation. */
	protected double adaptationOverhead;
	
	/** The adaptation history. */
	protected LinkedList<AdaptationRecord> adaptationHistory;


	private SelfAwareArchitecture(String name) {
		super(name);

		//initialise self-aware architecture components
		qosMonitor = QoSMonitor.getInstance();
		selfAwarenessComponent = SelfAwarenessComponent.getInstance();
		selfExpressionComponent = SelfExpressionComponent.getInstance();
		
		adaptationOverhead = 0.0;
		adaptationHistory = new LinkedList<AdaptationRecord>();
	}
	
	
	/**
     * Create a static method to get instance.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
    */
    public static SelfAwareArchitecture getInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        if(selfAwareArchitecture == null){
        	selfAwareArchitecture = new SelfAwareArchitecture("SelfAwareArchitecture");
        }
        return selfAwareArchitecture;
    }

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		
		//schedule first monitoring event when starting the monitor component
		ScheduleMonitoring();
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

	private void ScheduleMonitoring() {
		schedule(qosMonitor.getId(), helper.Constants.MONITORING_INTERVAL, CloudSimTags.SAW_QOS_MONITOR);
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
