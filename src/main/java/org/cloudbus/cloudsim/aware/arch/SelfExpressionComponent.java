package org.cloudbus.cloudsim.aware.arch;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.OperationRecord;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.aware.AwareDatacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class SelfExpressionComponent extends SimEntity {

	/** The SelfExpressionComponent singleton instance. */
	private static SelfExpressionComponent selfExpressionComponent;
	
	
	public SelfExpressionComponent(String name) {
		super(name);
	}

	/**
     * Create a static method to get instance.
    */
	public static SelfExpressionComponent getInstance() {
        if(selfExpressionComponent == null){
        	selfExpressionComponent = new SelfExpressionComponent("SelfExpressionComponent");
        }
        return selfExpressionComponent;
	}

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");		
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute adaptation decisions
		case CloudSimTags.SAW_SELF_EXPRESSION:
			act(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	public void act(SimEvent ev) {
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Executing self-expression...");

		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from the AdaptationExecutor
		
		switch (evdata[0]) {
			// Execute vertical scaling by increasing the capacity of a VM
			case "VERTICAL_SCALING_CAP":
				executeVerticalScalingCap(ev);
				break;

			// Execute vertical scaling by adding a new VM
			case "VERTICAL_SCALING_NUM":
				executeVerticalScalingNum(ev);
				break;

			// Execute vertical scaling by adding a new VM
			case "VERTICAL_DESCALING_NUM":
				executeVerticalDescalingNum(ev);
				break;

			// Execute horizontal scaling by adding a new PM
			case "HORIZONTAL_SCALING":
				executeHorizontalScaling(ev);
				break;
				
			// Execute horizontal scaling by adding a new PM
			case "HORIZONTAL_DESCALING":
				executeHorizontalDescaling(ev);
				break;
					
			// Execute VM consolidation and shutdown unused PM
			case "VM_CONSOLIDATION":
				executeVMConsolidation(ev);
				break;
		}	
	}

	protected void executeVerticalScalingCap(SimEvent ev){
		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from Self-awareness component
		
		int vmId = Integer.parseInt(evdata[1]);					
		int newVmType = Integer.parseInt(evdata[2]);

		try {
			int datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			int[] data = new int[2];
			data[0] = vmId;
			data[1] = newVmType;
			
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_SCALING_CAP, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void executeVerticalScalingNum(SimEvent ev){
		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from Self-awareness component
		
		int hostId = Integer.parseInt(evdata[1]);					//hostId where to execute the tactic
		int datacenterBrokerId = Integer.parseInt(evdata[2]);		//the datacenter borker
		int datacenterId;
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			int[] data = new int[2];
			data[0] = hostId;
			data[1] = datacenterBrokerId;
					
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_SCALING_NUM, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	protected void executeVerticalDescalingNum(SimEvent ev){
		String[] evdata = new String[4];
		evdata = (String[]) ev.getData();		//event data received from the AdaptationEngine
		
		int vmId = Integer.parseInt(evdata[1]);					//vmId to be removed
		int hostId = Integer.parseInt(evdata[2]);					//hostId for the new vm
		int datacenterBrokerId = Integer.parseInt(evdata[3]);		//the datacenter borker
		try {
			int datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			int[] data = new int[3];
			data[0] = vmId;
			data[1] = hostId;
			data[2] = datacenterBrokerId;
			
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_DESCALING_NUM, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void executeHorizontalScaling(SimEvent ev) {
		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from the AdaptationEngine
		
		int vmSchedulerType = Integer.parseInt(evdata[1]);
		int datacenterBrokerId = Integer.parseInt(evdata[2]);		//datacenterBrokerId
		try {
			int datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			int data[] = new int[2];
			data[0] = vmSchedulerType;
			data[1] = datacenterBrokerId;
							
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.HORIZONTAL_SCALING, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	protected void executeHorizontalDescaling(SimEvent ev){
		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from the AdaptationEngine
		
		int hostId = Integer.parseInt(evdata[1]);					//hostId to be removed
		int datacenterBrokerId = Integer.parseInt(evdata[2]);		//the datacenter borker
		try {
			int datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			int[] data = new int[2];
			data[0] = hostId;
			data[1] = datacenterBrokerId;
			
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.HORIZONTAL_DESCALING, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	protected void executeVMConsolidation(SimEvent ev) {
		String[] evdata = new String[3];
		evdata = (String[]) ev.getData();		//event data received from the AdaptationEngine
		
		int hostId = Integer.parseInt(evdata[1]);			//PM to be removed
		int brokerId = Integer.parseInt(evdata[2]);

		int data[] = new int[2];
		data[0] = hostId;
		data[1] = brokerId;

		try {
			int datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();

			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CONSOLIDATION, data);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
