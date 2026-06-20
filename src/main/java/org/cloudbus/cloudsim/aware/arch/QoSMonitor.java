package org.cloudbus.cloudsim.aware.arch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adaptive.goal.Goal;
import org.cloudbus.cloudsim.adaptive.goal.GoalsModel;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import helper.Constants;

public class QoSMonitor extends SimEntity {

	/** The monitor singleton instance. */
	private static QoSMonitor qosMonitor;

	/** The list of sensors. */
	protected ArrayList<Sensor> sensors;
	
	/** The data collected by the sensors. */
	protected Map<String,ArrayList<Double>> monitorData;

	/** The time overhead for monitoring. */
	protected double monitorOverhead;
	
	private int datacenterId;
	private DatacenterBroker broker;
	private int selfAwarenessComponentId;

	
	private QoSMonitor(String name) {
		super(name);
		
		// initialise the sensors
		sensors = new ArrayList<Sensor>();
		monitorOverhead = 0.0;		
	}

	/**
     * Create a static method to get instance.
    */
    public static QoSMonitor getInstance(){
        if(qosMonitor == null){
        	qosMonitor = new QoSMonitor("QoSMonitor");
        }
        return qosMonitor;
    }

	@Override
	public void startEntity() {
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			selfAwarenessComponentId = SelfAwarenessComponent.getInstance().getId();
			broker = (DatacenterBroker) CloudSim.getEntity("Broker");

			monitorData = new HashMap<String,ArrayList<Double>>();
			//set the list of goals for the sensors
			for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
				Sensor sensor = new Sensor(g.getName());
				sensors.add(sensor);
				monitorData.computeIfAbsent(g.getName(), ignored -> new ArrayList<>());
			}
			
			Log.printLine(getName() + " is starting...");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute monitoring for Adaptation Goals
		case CloudSimTags.SAW_QOS_MONITOR:
			try {
				processQoSMonitor(ev);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	protected void processQoSMonitor(SimEvent ev) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		try {
		datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
		broker = (DatacenterBroker) CloudSim.getEntity("Broker");
		selfAwarenessComponentId = SelfAwarenessComponent.getInstance().getId();

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

		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Monitoring...");

		// clear the data structure
		clearMonitoringData();
		
		// collect monitoring data
		collectMonitoringData();

		// send monitor data to the self-awareness component
		send(selfAwarenessComponentId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_SELF_AWARENESS, monitorData);	

		// schedule the next monitoring event after one monitoring interval
		scheduleNextMonitoring();
		
		// add monitoring overhead and adaptation overhead
		monitorOverhead += CloudSim.getMinTimeBetweenEvents();

		SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Log.print("Monitoring Overhead Time: " + monitorOverhead);
	}
	
	private void clearMonitoringData(){
		for (Sensor s : sensors) {
			s.clearMonitoringData();
		}
	}
		
	private void collectMonitoringData() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		for (Sensor s : sensors) {
			s.collectMonitoringData();
			monitorData.put(s.getQualityAttribute(), s.getSensorData());
		}
	}
	
	private void scheduleNextMonitoring(){
		//if (getServiceRequestList().size() == 0 && getServiceRequestSubmittedList == 0) {
		schedule(this.getId(), Constants.MONITORING_INTERVAL, CloudSimTags.SAW_QOS_MONITOR);
		//}
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
