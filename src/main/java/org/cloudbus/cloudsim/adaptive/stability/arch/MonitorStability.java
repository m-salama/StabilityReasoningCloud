package org.cloudbus.cloudsim.adaptive.stability.arch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.goal.Goal;
import org.cloudbus.cloudsim.adaptive.goal.GoalsModel;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import helper.Constants;

public class MonitorStability extends SimEntity {

	/** The monitor singleton instance. */
	private static MonitorStability monitor;

	/** The data collected by the monitor. */
	protected Map<String,ArrayList<Double>> monitorData;
	
	/** The time overhead for monitoring. */
	protected double monitorOverhead;
	
	
	private int datacenterId;
	private int datacenterBrokerId;
	private int detectorId;	

	
	private MonitorStability(String name) {
		super(name);
		
		// initialise monitor data structure
		monitorData = new HashMap<String,ArrayList<Double>>();
		monitorOverhead = 0.0;
	}

	/**
     * Create a static method to get instance.
    */
    public static MonitorStability getInstance(){
        if(monitor == null){
        	monitor = new MonitorStability("MonitorStability");
        }
        return monitor;
    }

	@Override
	public void startEntity() {
		//set the list of goals for the monitorData arraylist
		for (Goal g : GoalsModel.getInstance().getGoals()) {
			monitorData.computeIfAbsent(g.getName(), ignored -> new ArrayList<>());
		}
		//monitorData.computeIfAbsent("ResponseTime", ignored -> new ArrayList<>());
		//monitorData.computeIfAbsent("Throughput", ignored -> new ArrayList<>());
		//monitorData.computeIfAbsent("EnergyConsumption", ignored -> new ArrayList<>());
		//monitorData.computeIfAbsent("Cost", ignored -> new ArrayList<>());
		
		Log.printLine(getName() + " is starting...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute monitoring for Adaptation Goals
		case CloudSimTags.SAD_MONITOR_STABILITY:
			processMonitor(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	protected void processMonitor(SimEvent ev){
		datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		detectorId = DetectorStability.getInstance().getId();	
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity("Broker");
		
//		if (CloudSim.getNumFutureEvents() == 0) {
//			return;
//		} 
		double lastArrivalTime = broker.getServiceRequestSubmittedList().get(broker.getServiceRequestSubmittedList().size()-1).getArrivalTime();
		
		//if (CloudSim.clock() > lastArrivalTime + Constants.RUNTIME_INTERVAL *3) {
		if (broker.getServiceRequestSubmittedList().size() == broker.getServiceRequestReceivedList().size()) {
			return;
		//} else {
			//schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);			
		}
		
		reflectAdaptatonOfLastTimeInterval();

		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Monitoring...");
		
		// clear the data structure
		clearMonitoringData();
		// collect monitoring data
		collectMonitoringData();
		// send monitor data to the Detector
		send(detectorId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAD_DETECT_VIOLATIONS_STABILITY, monitorData);	
		
		// schedule the next monitoring event after one monitoring interval
		scheduleNextMonitoring();
		
		// add monitoring overhead and adaptation overhead
		monitorOverhead += CloudSim.getMinTimeBetweenEvents();
		SelfAdaptiveArchitectureStability.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());

		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	private void reflectAdaptatonOfLastTimeInterval(){
		datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		datacenterBrokerId = CloudSim.getEntityId("Broker");		
		
		sendNow(datacenterId, CloudSimTags.VM_DATACENTER_EVENT);
		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		
		if (SelfAdaptiveArchitectureStability.getInstance().adaptationHistory.size() > 0) {
			double lastAdaptationTime =	SelfAdaptiveArchitectureStability.getInstance().getAdaptationHistory().getLast().getAdaptationTime();
			
			double timeIntervalStart = CloudSim.clock() - (CloudSim.clock() % Constants.RUNTIME_INTERVAL);
			double timeIntervalEnd = timeIntervalStart + Constants.RUNTIME_INTERVAL;

			if ((!(lastAdaptationTime >= timeIntervalStart && lastAdaptationTime <= timeIntervalEnd)) &&
					((CloudSim.clock() - timeIntervalStart) <= Constants.MONITORING_INTERVAL)) {
				send(datacenterBrokerId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.REFLECT_ADAPTATION, datacenterId);
			}
		}
		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}

	private void clearMonitoringData(){
		for (ArrayList<Double> value : MonitorStability.getInstance().getMonitorData().values()) {
			value.clear();
		}
	}
		
	private void collectMonitoringData(){
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAdaptiveArchitectureStability.getInstance().getDatacenterId());
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity("Broker");
		
		List<ServiceRequest> rlist = new ArrayList<ServiceRequest>();

		ArrayList<Double> valuesResponseTime = new ArrayList<Double>();
		ArrayList<Double> valuesThroughput = new ArrayList<Double>();
		ArrayList<Double> valuesEnergy = new ArrayList<Double>();
		ArrayList<Double> valuesCost = new ArrayList<Double>();
		
		//get list of completed requests from datacenterBroker
		rlist = broker.getServiceRequestReceivedList();
		
		double totalThroughput = 0.0;
		double totalCost = 0.0;
		double energy = datacenter.getPower() / (3600 * 1000);
		
		for (ServiceRequest r : rlist) {
			if (r.getFinishTime() > (CloudSim.clock() - helper.Constants.MONITORING_INTERVAL)  
				&& (r.getServiceRequestStatus() == ServiceRequest.SUCCESS)) {
					valuesResponseTime.add(r.getResponseTime());
					totalThroughput++;
					totalCost += r.getTotalCost();
			}
		}
		
		valuesThroughput.add(totalThroughput/helper.Constants.MONITORING_INTERVAL);
		valuesCost.add(totalCost);
		valuesEnergy.add(energy);

		monitorData.put("ResponseTime", valuesResponseTime);
		monitorData.put("Throughput", valuesThroughput);
		monitorData.put("EnergyConsumption", valuesEnergy);
		monitorData.put("OperationalCost", valuesCost);
	}
	
	private void scheduleNextMonitoring(){ 
		//if (getServiceRequestList().size() == 0 && getServiceRequestSubmittedList == 0)
		//if (CloudSim.getNumFutureEvents() > 4)
		//if (CloudSim.clock() < 75168.0)
			schedule(this.getId(), Constants.MONITORING_INTERVAL, CloudSimTags.SAD_MONITOR_STABILITY);
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
	
	/**
	 * Gets monitoring data.
	 * 
	 * @return monitoring data
	 */
	public Map<String,ArrayList<Double>> getMonitorData() {
		return monitorData;
	}

	/**
	 * Sets monitoring overhead.
	 * 
	 * @param overhead the monitoring overhead
	 */
	public void setMonitorOverhead(double overhead) {
		this.monitorOverhead = overhead;
	}
	/**
	 * Gets monitoring overhead.
	 * 
	 * @return monitoring overhead
	 */
	public double getMonitorOverhead() {
		return monitorOverhead;
	}

	/**
	 * Sets monitoring data.
	 * 
	 * @param data the monitoring data
	 */
	public void setMonitorData(Map<String,ArrayList<Double>> data) {
		this.monitorData = data;
	}

}
