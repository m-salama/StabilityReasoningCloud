package org.cloudbus.cloudsim.adaptive;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import helper.Constants;

public class AdaptiveDynamicDatacenterBroker extends AdaptiveDatacenterBroker {

	
	public AdaptiveDynamicDatacenterBroker(String name) throws Exception {
		super(name);
	}
	
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {	
		case CloudSimTags.REFLECT_ADAPTATION:
			processRefelctAdaptation(ev);
			break;
		
		default:
			super.processEvent(ev);
		}		
	}
	
	protected void processRefelctAdaptation(SimEvent ev) {
		int evdata = (int) ev.getData();			
		int datacenterId = evdata;

		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Reflecting last time interval adaptation...");

		//get time interval start and end
		double start = CloudSim.clock() - (CloudSim.clock() % Constants.RUNTIME_INTERVAL);
		double end = start + Constants.RUNTIME_INTERVAL;

		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);

		Log.printLine("aaa"+ "/" + getServiceRequestSubmittedList().size() + "/" + getServiceRequestReceivedList().size());
		//move the waiting requests to benefit from adaptations of the previous time interval
		if (getServiceRequestSubmittedList().size() > getServiceRequestReceivedList().size()) {
			ArrayList<ServiceRequest> queuedServiceRequests = new ArrayList<>();
			/*		for (ServiceRequest r : getServiceRequestSubmittedList()) {
						if (r.getArrivalTime() >= start && r.getArrivalTime() < end && r.getStatus() == 2) {			//2: QUEUED	
							queuedServiceRequests.add(r); 
						} else if (r.getArrivalTime() >= start && r.getArrivalTime() < end && r.getStatus() == 3) {		//3: INEXEC	
							Vm vm = VmList.getById(getVmList(), r.getVmId());
							vm.updateVmProcessing(CloudSim.clock(), 
									datacenter.getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
						}
					} 
			 */		
			for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
				Vm vm = VmList.getById(getVmList(), cloudlet.getVmId());
				vm.updateVmProcessing(CloudSim.clock(), 
						datacenter.getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
				if (cloudlet.getServiceRequestStatus() == 2) {		//status = QUEUED
					queuedServiceRequests.add(cloudlet);
				}
			} 

			moveServiceRequestsToNextLessBusyVm(datacenter, queuedServiceRequests);	
			//super.moveServiceRequests(datacenterId);
			send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
			schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);

		}
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * allows multiple cloudltes to be executed by one VM, 
	 * submitting cloudlets to the VM with the least number of cloudlets assigned.
	 * Cloudlets are runtime workload and are assigned using multiple queues.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitServiceRequests() {
		//get all time intervals of the workload
		List<Double> intervals = getWorkloadTimeIntervals();
		
		for (double interval : intervals) {
			//get requests of this time interval
			List<ServiceRequest> interval_cloudlets = getIntervalCloudlets(interval);

			int vmIndex = 0;
			for (ServiceRequest cloudlet : interval_cloudlets) {
				Vm vm;		
				if (cloudlet.getVmId() == -1) {// if user didn't bind this cloudlet and it has not been executed yet
					vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				} else { // submit to the specific vm
					vmIndex = super.getLessBusyVm();
					vm = getVmList().get(vmIndex);					
					if (vm == null) { // vm was not created
						Log.printLine(CloudSim.clock() + ": [" + getName() + "] Postponing execution of cloudlet " + cloudlet.getServiceRequestId() + ": bount VM not available");
						continue;
					}
				}
				ScheduleServiceRequestToVm(cloudlet, vm);
				cloudletsSubmitted++;
				getServiceRequestSubmittedList().add(cloudlet);
			}
			Log.printLine();
		}
		
		// remove submitted cloudlets from waiting list
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
			getServiceRequestList().remove(cloudlet);
		}
	}
	
	protected void ScheduleServiceRequestToVm (ServiceRequest cloudlet, Vm vm) {
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Sending Service Request " + cloudlet.getServiceRequestId() + " to VM #" + vm.getId());
		cloudlet.setVmId(vm.getId());
		vm.getAssignedServiceRequestList().add(cloudlet);		

		//schedule request processing at its arrival time
		schedule(getVmsToDatacentersMap().get(vm.getId()), cloudlet.getArrivalTime(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
	}

	protected List<Double> getWorkloadTimeIntervals() {
		List<Double> intervals = new ArrayList<Double>();

		double temp = 0.0;
		intervals.add(temp);
		for (ServiceRequest cloudlet : getServiceRequestList()) {
			if (cloudlet.getArrivalTime() != temp) {
				temp = cloudlet.getArrivalTime();
				intervals.add(temp);
 			}
		}
		return intervals;	
	}

	public ArrayList<ServiceRequest> getIntervalCloudlets(double intervalTime) {
		ArrayList<ServiceRequest> interval_cloudlets = new ArrayList<ServiceRequest>();
	
		for (ServiceRequest cloudlet : getServiceRequestList()) {
			if (cloudlet.getArrivalTime() == intervalTime){
				interval_cloudlets.add(cloudlet);
			}
		}
		return interval_cloudlets;
	}

/*	private List<Double> getNextWorkloadTimeIntervals() {
		List<Double> intervals = new ArrayList<Double>();

		double temp = CloudSim.clock();
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
			if ((cloudlet.getArrivalTime() >= temp) && (cloudlet.getArrivalTime() != temp)) {
				temp = cloudlet.getArrivalTime();
				intervals.add(temp);
 			}
		}
		return intervals;	
	}

	protected ArrayList<ServiceRequest> getNextIntervalCloudlets(double intervalTime) {
		ArrayList<ServiceRequest> interval_cloudlets = new ArrayList<ServiceRequest>();
	
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
			if (cloudlet.getArrivalTime() == intervalTime){
				interval_cloudlets.add(cloudlet);
			}
		}
		return interval_cloudlets;
	}
*/

	
}
