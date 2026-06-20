package org.cloudbus.cloudsim.adaptive.stability.arch;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.AdaptationRecord;
import org.cloudbus.cloudsim.adaptive.AdaptiveDynamicDatacenterBroker;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;
import org.cloudbus.cloudsim.adaptive.stability.analysis.StabilityAnalysis;
import org.cloudbus.cloudsim.adaptive.stability.evaluation.StabilityEvaluation;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import helper.Constants;

/**
 * SelfAdaptiveArchitectureStability class extends SelfAdaptiveArchitecture 
 * for making adaptations to achieve stability.
 * 
 * @author m.salama
 * @version SAdCloudSim
 */
public class SelfAdaptiveArchitectureStability extends SimEntity {	
	
	/** The self-adaptive architecture singleton instance. */
	private static SelfAdaptiveArchitectureStability selfAaptiveArchitectureStability;
	
	/** The datacenter id where the architecture is placed. */
	private int datacenterId;

	/** The adaptive architecture components. */
	protected static MonitorStability monitor;
	protected static DetectorStability detector;
	protected static AdaptationEngineStability adaptationEngine;
	protected static AdaptationExecutorStability adaptationExecutor;
	
	/** stability components. */
	protected static StabilityAnalysis stabilityAnalysis;
	private static StabilityEvaluation stabilityEvaluation;
	
	/** The time overhead for adaptation. */
	protected double adaptationOverhead;

	/** The adaptation history. */
	public LinkedList<AdaptationRecord> adaptationHistory;

	
	/**
     * Create a static method to get instance.
    */
    public static SelfAdaptiveArchitectureStability getInstance(){
        if(selfAaptiveArchitectureStability == null){
        	selfAaptiveArchitectureStability = new SelfAdaptiveArchitectureStability("SelfAdaptiveArchitectureStability");
        }
        return selfAaptiveArchitectureStability;
    }

	private SelfAdaptiveArchitectureStability(String name) {
		super(name);

		//initialise self-adaptive architecture components
		monitor = MonitorStability.getInstance();
		detector = DetectorStability.getInstance();
		adaptationEngine = AdaptationEngineStability.getInstance();
		adaptationExecutor = AdaptationExecutorStability.getInstance();

		adaptationOverhead = 0.0;
		adaptationHistory = new LinkedList<AdaptationRecord>();
	}
	
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		
		//schedule first monitoring event when starting the monitor component
		scheduleMonitoring();	

		//start stability component		
		if (Constants.STABILITY_ANALYSIS_ENABLED) {
			stabilityAnalysis = new StabilityAnalysis();
		} else if (Constants.STABILITY_EVALUATION_ENABLED) {
			try {  
				AdaptiveDatacenterStability datacenter = (AdaptiveDatacenterStability) CloudSim.getEntity(this.getDatacenterId());
				AdaptiveDynamicDatacenterBroker broker = (AdaptiveDynamicDatacenterBroker) CloudSim.getEntity("Broker");
				int serviceType = broker.getServiceRequestList().get(0).getServiceId();
				int vmType = broker.getVmList().get(0).getType(); 
				int hostType = datacenter.getHostList().get(0).getType();
				setStabilityEvaluationComp(new StabilityEvaluation(datacenter, serviceType, vmType, hostType));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		schedule(monitor.getId(), helper.Constants.MONITORING_INTERVAL, CloudSimTags.SAD_MONITOR_STABILITY);
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

	public static StabilityAnalysis getStabilityAnalysisComp() {
		return stabilityAnalysis;
	}

	public static StabilityEvaluation getStabilityEvaluationComp() {
		return stabilityEvaluation;
	}

	public void setStabilityEvaluationComp(StabilityEvaluation stabilityEvaluation) {
		SelfAdaptiveArchitectureStability.stabilityEvaluation = stabilityEvaluation;
	}

}
