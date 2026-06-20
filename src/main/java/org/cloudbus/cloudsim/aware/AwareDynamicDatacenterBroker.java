/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.aware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.AdaptiveDynamicDatacenterBroker;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * 
 * @author mxs512 
 * @since SAw-CloudSim Toolkit 
 */
public class AwareDynamicDatacenterBroker extends AdaptiveDynamicDatacenterBroker {

		
	public AwareDynamicDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * allows multiple cloudltes to be executed by one VM, 
	 * submitting cloudlets to the VM with the least number of cloudlets assigned.
	 * Cloudlets are runtime workload and are assigned using multiple queues.
	 * Workload of each time interval is distributed equally by the number of PMs 
	 * and then dynamically according to VM capacity
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitServiceRequests() {
		//get all time intervals of the workload
		List<Double> intervals = super.getWorkloadTimeIntervals();
		//get all the hosts in all datanceneters
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(getDatacenterIdsList().get(0));
		List<Host> lstHosts = datacenter.getHostList();
		int hostNo = lstHosts.size();
				
		for (double interval : intervals) {
			//get requests of this time interval
			List<ServiceRequest> interval_cloudlets = super.getIntervalCloudlets(interval);
			
			//distribute the workload equally by the number of PMs
			double hostWorkloadShare = calculateHostWorkloadShare(interval_cloudlets.size(), hostNo);
			//calculate the workload for each vm according to its capacity
			Map<Integer, Integer> vmsWorkload = calculateVmsWorkload(lstHosts, hostWorkloadShare);
			//and initialise the number of requests assigned to each vm
			Map<Integer, Integer> vmsAssignedServiceRequests = initialiseVmsAssignedServiceRequests(lstHosts);

			int vmIndex = 0;
			for (ServiceRequest cloudlet : interval_cloudlets) {
				Vm vm = null;			
				if (cloudlet.getVmId() == -1) {// if user didn't bind this cloudlet and it has not been executed yet
					vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				} else { // submit to the specific vm
					//find vm for the cloudlet according the distributed workload
					
					for (Vm tempVm : getVmsCreatedList()){ 
						if ((vmsAssignedServiceRequests.get(tempVm.getId()) <= vmsWorkload.get(tempVm.getId())) 
								&& (tempVm.getId() >= vmIndex)) { 
							vm = tempVm;
							vmIndex++;
							vmsAssignedServiceRequests.put(tempVm.getId(), vmsAssignedServiceRequests.getOrDefault(tempVm.getId(), 0)+1);
						} 					
						if (vm != null) {
							break;
						} 
					} 
					if (vm == null) { // vm was not created
						Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet " + cloudlet.getServiceRequestId() + ": bount VM not available");							
						continue;
					}
				}
				ScheduleServiceRequestToVm(cloudlet, vm);
				cloudletsSubmitted++;
				getServiceRequestSubmittedList().add(cloudlet);
				
				//reset the vmIndex
				if (vmIndex >  getVmsCreatedList().size()-1) {
					vmIndex = 0;
				}
			}	
			Log.printLine();		
		}		
		
		// remove submitted cloudlets from waiting list
		for (ServiceRequest c : getServiceRequestSubmittedList()) {
			getServiceRequestList().remove(c);
		}
	}
	
	private double calculateHostWorkloadShare(int interval_cloudlets_size, int hostNo) {
		return Math.ceil(interval_cloudlets_size / hostNo);
	}

	
	private Map<Integer, Integer> calculateVmsWorkload(List<Host> lstHosts, double hostWorkloadShare) {
		Map<Integer, Integer> vmsWorkload = new HashMap<Integer, Integer>();

		for (Host h : lstHosts) {
			double totalVmsCapacity = getTotalVmsCapacity(h);
			for (Vm vm :  h.getVmList()){
				double vm_mips = vm.getMips() * vm.getNumberOfPes();
				int vmWorkload =  (int) Math.ceil((hostWorkloadShare * vm_mips) / totalVmsCapacity);
				vmsWorkload.put(vm.getId(), vmWorkload);
			}
		}

		return vmsWorkload;
	}
	
	private Map<Integer, Integer> initialiseVmsAssignedServiceRequests(List<Host> lstHosts) {
		Map<Integer, Integer> vmsAssignedServiceRequests = new HashMap<Integer, Integer>();

		for (Host h : lstHosts) {
			for (Vm vm : h.getVmList()){
				vmsAssignedServiceRequests.put(vm.getId(), 0);
			}
		}

		return vmsAssignedServiceRequests;
	}
		
	private double getTotalVmsCapacity(Host h) {
		double totalVmsCapacity = 0.0;
		for (Vm vm :  h.getVmList()){
			totalVmsCapacity += vm.getMips() * vm.getNumberOfPes();
		}
		return totalVmsCapacity;
	}	

	/**
	 * Move cloudlets to the VMs.
	 * after the execution of adaptations
	 * submitting cloudlets of each time interval equally by the number of PMs 
	 * and then dynamically according to VM capacity
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void moveServiceRequests(int datacenterId) {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);	
		//get all the hosts in all datanceneters
		List<Host> lstHosts = datacenter.getHostList();

		//get list of queued requests of this time interval
		ArrayList<ServiceRequest> queuedServiceRequests = new ArrayList<ServiceRequest>();
		//ArrayList<ServiceRequest> nextTimeIntervalsQueuedServiceRequests = new ArrayList<ServiceRequest>();
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) { 
			if (cloudlet.getServiceRequestStatus() == 2) {			//status = QUEUED		
				queuedServiceRequests.add(cloudlet);
			} else if (cloudlet.getServiceRequestStatus() == 3) {		//status = INEXEC
				
			} 
		}	
		moveServiceRequestsToNextVmByCapacity(queuedServiceRequests, datacenter, lstHosts);

		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
		
	private void moveServiceRequestsToNextVmByCapacity(List<ServiceRequest> queuedServiceRequests, Datacenter datacenter, List<Host> lstHosts) {
		//distribute the workload equally by the number of PMs
		double hostWorkloadShare = calculateHostWorkloadShare(queuedServiceRequests.size(), lstHosts.size());
		//calculate the workload for each vm according to its capacity
		Map<Integer, Integer> vmsWorkload = calculateVmsWorkload(lstHosts, hostWorkloadShare);
		//and initialise the number of requests assigned to each vm
		Map<Integer, Integer> vmsAssignedServiceRequests = initialiseVmsAssignedServiceRequests(lstHosts);

		for (ServiceRequest r : queuedServiceRequests) {
			//remove this cloudlet from the current submitted vm
			int currentVmId = r.getVmId();

			Vm vm = vmList.get(currentVmId);
			vm.getAssignedServiceRequestList().remove(r);
			vm.getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());

			//Host host = datacenter.getVmAllocationPolicy().getHost(currentVmId, r.getUserId());
			//host.getVm(currentVmId, r.getUserId()).getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());						
		}

		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Moving cloudlets...");
		int vmIndex = 0;
		for (ServiceRequest r : queuedServiceRequests) {
			Vm newVm = null;			
			//find new vm for the cloudlet according to vms capacity
			for (Vm tempVm : vmList) {
				if (((AdvVm) tempVm).isOn()) {
					if ((vmsAssignedServiceRequests.get(tempVm.getId()) < vmsWorkload.get(tempVm.getId())) 
							&& (tempVm.getId() >= vmIndex)) {
						newVm = tempVm;
						vmIndex++;
					}
				}					
				if (newVm != null) {
					//assign cloudlet to the new vm
					r.setVmId(newVm.getId());
					newVm.getAssignedServiceRequestList().add(r);
					vmsAssignedServiceRequests.put(newVm.getId(), vmsAssignedServiceRequests.getOrDefault(newVm.getId(), 0)+1);

					double fileTransferTime = datacenter.predictFileTransferTime(r.getRequiredFiles());
					newVm.getServiceRequestScheduler().serviceRequestSubmit(r, fileTransferTime);
					//newVm.getServiceRequestScheduler().serviceRequestSubmit(r);
					
					//Host host = datacenter.getVmAllocationPolicy().getHost(newVm.getId(), r.getUserId());
					//host.getVm(newVm.getId(), r.getUserId()).getServiceRequestScheduler().serviceRequestSubmit(r);				

					newVm.updateVmProcessing(CloudSim.clock(), 
							datacenter.getVmAllocationPolicy().getHost(newVm).getVmScheduler().getAllocatedMipsForVm(newVm));

					//Log.printLine(CloudSim.clock() + ": [" + getName() + "] Moving cloudlet " + r.getServiceRequestId() + " to VM #" + newVm.getId());
					break;
				}
			}			
			//reset the vmIndex
			if (vmIndex > vmList.size()-1) {
				vmIndex = 0;
			}
		}
	}

	protected int getNextLessBusyVm(Datacenter datacenter) {
		List<AdvVm> vmsOnList = ((AwareDatacenter) datacenter).getVmOnList();
	
		Vm vm = vmsOnList.get(0);
		if (vmsOnList.size() > 1) {
			for (int i=0; i<vmsOnList.size()-1; i++) {
				//compare with the number of waiting requests and in execution requests
				if ( ((vmsOnList.get(i+1)).getWaitingServiceRequestList().size() + vmsOnList.get(i+1).getInExecServiceRequestList().size()) <
						(vm.getWaitingServiceRequestList().size() + vm.getInExecServiceRequestList().size())  ) {
					vm = vmsOnList.get(i+1);
				}
			}
		}
		return vm.getId();	
	}
	
}
