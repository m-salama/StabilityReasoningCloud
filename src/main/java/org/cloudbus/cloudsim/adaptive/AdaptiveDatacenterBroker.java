package org.cloudbus.cloudsim.adaptive;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.aware.arch.SelfAwareArchitecture;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.VmList;

import helper.Constants;

public class AdaptiveDatacenterBroker extends DatacenterBroker {

	
	public AdaptiveDatacenterBroker(String name) throws Exception {
		super(name);
	}
	

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
			// Update after processing Vertical scaling increasing the capacity of a vm 
			case CloudSimTags.VERTICAL_SCALING_CAP:
				processVerticalScalingCap(ev);
				break;	

			// Update after processing Vertical scaling adding a new vm	 
			case CloudSimTags.VERTICAL_SCALING_NUM:
				processVerticalScalingNum(ev);
				break;	

			// Update after processing Vertical de-scaling removing a vm	 
			case CloudSimTags.VERTICAL_DESCALING_NUM:
				processVerticalDescalingNum(ev);
				break;	

			// Update after processing vm consolidation 
			case CloudSimTags.VM_CONSOLIDATION:
				processVmConsolidation(ev);
				break;	

			default:
				super.processEvent(ev);
		}
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * allows multiple cloudltes to be executed by one VM, 
	 * submitting cloudlets to the VM with the least number of cloudlets assigned.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitServiceRequests() {
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Sending Service Requests to VMs...");
		int vmIndex = 0;
		
		for (ServiceRequest cloudlet : getServiceRequestList()) {
			Vm vm;		
			if (cloudlet.getVmId() == -1) {// if user didn't bind this cloudlet and it has not been executed yet
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			} else { // submit to the specific vm
				vmIndex = getLessBusyVm();
				vm = getVmList().get(vmIndex);					
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": [" + getName() + "] Postponing execution of cloudlet " + cloudlet.getServiceRequestId() + ": bount VM not available");
					continue;
				}
			}
			super.SubmitServiceRequestToVm(cloudlet, vm);
			cloudletsSubmitted++;
			getServiceRequestSubmittedList().add(cloudlet);
		}
		Log.printLine();
		
		// remove submitted cloudlets from waiting list
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
			getServiceRequestList().remove(cloudlet);
		}
	}
	
	protected int getLessBusyVm() {
		int vmId = 0;

		for (Vm tempVm : getVmList()) {
			if (tempVm.getAssignedServiceRequestList().size() < 
					(getVmList().get(vmId)).getAssignedServiceRequestList().size()) {
				vmId = tempVm.getId();
				return vmId;
			} 
		}	
		return vmId;
	}

	/**
	 * Process updates after processing Vertical scaling increasing the capacity of a vm .
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@SuppressWarnings("unused")
	protected void processVerticalScalingCap(SimEvent ev) {
		int[] evdata = new int[2];
		evdata = (int[]) ev.getData();		//event data received from the AdaptationExecutor
		
		int vmId = evdata[0];			//vmId to be upgraded		
		int newVmType = evdata[1];		//the new type of the VM
		
		Vm vm = VmList.getById(getVmList(), vmId);		
		int datacenterId = vm.getHost().getDatacenter().getId();

		if (vm != null){
			//update the vmList with the new vmType
			vm.changeType(newVmType);

			// update the vmCreatedList with the new vmType
			vm = getVmsCreatedList().get(vmId);
			vm.changeType(newVmType);
			
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] VM #" 
					+ vmId + " has been upgraded in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId() 
					+ " to vmType #" + newVmType);
			Log.printLine();

			moveServiceRequests(datacenterId);	
		} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Vertical scaling failed. Upgrade of VM #" 
					+ vmId + " failed in Datacenter #" + datacenterId);
		}
	}
	
	/**
	 * Process updates after processing Vertical scaling adding a new vm.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVerticalScalingNum(SimEvent ev) {
		Vm vm = (Vm) ev.getData();
		
		int vmId = vm.getId();
		int datacenterId = vm.getHost().getDatacenter().getId();
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);
		
		if (getVmList().contains(vm)) {
			((AdvVm) getVmList().get(vm.getId())).setOn(true);
		} else {
			if (getVmList().add(vm) && getVmsCreatedList().add(vm)) {
				getVmsToDatacentersMap().put(vmId, datacenterId);
				incrementVmsAcks();		
			}
		}
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] A new VM has been added. VM #" + vmId 
					+ " has been created in Datacenter #" + datacenterId 
					+ ", Host #" + vm.getHost().getId());
		Log.printLine();
	
		//migrate service requests inexecution
		//for (Vm tempVm : getVmList()) {
			//migrateServiceRequestsInExecution(tempVm, datacenter);
		//}

		moveServiceRequests(datacenterId);
	}	
	
	/**
	 * Process updates after processing Vertical de-scaling remving a vm.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVerticalDescalingNum(SimEvent ev) {	
		//event data received from the AdaptationExecutor		
		int[] evdata = new int[3];
		evdata = (int[]) ev.getData();			
		int vmId = evdata[0];					//vmId to be removed
		int hostId = evdata[1];					//the host id
		int datacenterId = evdata[2];

		Vm vm = VmList.getById(getVmList(), vmId);
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);
		
		//migrate service requests inexecution
		//migrateServiceRequestsInExecution(vm, datacenter);
		
		//get the service requests currently assigned and waiting in the vm before removing it
		//migrate the service requests from this vm and assign them to another vm			
		moveServiceRequests(datacenterId);

		//remove the vm
		//getVmList().remove(vm);
		//getVmsCreatedList().remove(vm);
		//getVmsToDatacentersMap().remove(vmId, datacenterId)) 
		//decrementVmsAcks();
		//setVmsDestroyed(getVmsDestroyed()+1);
		((AdvVm) getVmList().get(vmId)).setOn(false);
		//((AdvVm) getVmsCreatedList().get(vmId)).setOn(false);

		Log.printLine(CloudSim.clock() + ": [" + getName() + "] VM #" + vmId 
					+ " has been removed in Datacenter #" + datacenterId 
					+ ", Host #" + hostId);
		Log.printLine();
	}	
	
	/**
	 * Process updates after processing vm cnsolidation
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmConsolidation(SimEvent ev) {	
		//event data received from the AdaptationExecutor		
		int[] evdata = new int[3];
		evdata = (int[]) ev.getData();			
		int vmId = evdata[0];					//vmId to be truned off
		int newVmId = evdata[1];				//the new vm id to remove requests to
		int datacenterId = evdata[2];

		Vm vm = VmList.getById(getVmList(), vmId);
		Vm newVm = VmList.getById(getVmList(), newVmId);
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);
		
		//disable vm
		((AdvVm) getVmList().get(vmId)).setOn(false);
		//enable the new vm
		/*if (!getVmList().contains(newVm)) {
			if (getVmList().add(vm) && getVmsCreatedList().add(newVm)) {
				getVmsToDatacentersMap().put(newVmId, datacenterId);
				incrementVmsAcks();
			}
		}*/
		((AdvVm) getVmList().get(newVmId)).setOn(true);

		//migrate service requests inexecution
		//migrateServiceRequestsInExecution(vm, datacenter);
		
		//get the service requests currently assigned and waiting in the vm 
		//migrate the service requests from this vm and assign them to the new vm
		moveServiceRequests(datacenterId, vmId, newVmId);

		Log.printLine(CloudSim.clock() + ": [" + getName() + "] VM #" + vmId 
					+ " has been consolidated in Datacenter #" + datacenterId);
		Log.printLine();
	}	
	
	protected void migrateServiceRequestsInExecution(Vm vm, Datacenter datacenter) {
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Migrating cloudlets... ");					
		for (ServiceRequest r : vm.getInExecServiceRequestList()) {
			ServiceRequest cl = vm.getServiceRequestScheduler().migrateServiceRequest();
			if (cl != null) {
				int newVmId = getNextLessBusyVm(datacenter);
				//get the new vm
				Vm newVm = datacenter.getVmAllocationPolicy().getHost(newVmId, cl.getUserId()).getVm(newVmId, cl.getUserId());					
				cl.setVmId(newVmId);
				newVm.getAssignedServiceRequestList().add(cl);

				double fileTransferTime = datacenter.predictFileTransferTime(cl.getRequiredFiles());
				newVm.getServiceRequestScheduler().serviceRequestSubmit(cl, fileTransferTime);

				//Log.printLine(CloudSim.clock() + ": [" + getName() + "] Migrating cloudlet " + r.getServiceRequestId() + " to VM #" + newVmId);					
			}
		}
		send(datacenter.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		schedule(datacenter.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	/**
	 * Move cloudlets to the VMs.
	 * after the execution of adaptations
	 * submitting cloudlets to the VM with the least number of cloudlets assigned.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void moveServiceRequests(int datacenterId) {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);

		//get list of queued requests
		ArrayList<ServiceRequest> queuedServiceRequests = new ArrayList<ServiceRequest>();
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) { 
			if (cloudlet.getServiceRequestStatus() == 2) {		//status = QUEUED
				queuedServiceRequests.add(cloudlet);
			} else if (cloudlet.getServiceRequestStatus() == 3) {		//status = INEXEC
				sendNow(datacenterId, CloudSimTags.VM_DATACENTER_EVENT);
			}
		} 
		moveServiceRequestsToNextLessBusyVm(datacenter, queuedServiceRequests);

		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	protected void moveServiceRequestsToNextLessBusyVm(Datacenter datacenter, ArrayList<ServiceRequest> serviceRequests) {
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Moving cloudlets...");
		//remove this cloudlet from the current submitted vm
		for (ServiceRequest r : serviceRequests) {
			int currentVmId = r.getVmId();
			
			Vm vm = vmList.get(currentVmId);
			vm.getAssignedServiceRequestList().remove(r);
			vm.getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());

			//Host host = datacenter.getVmAllocationPolicy().getHost(currentVmId, r.getUserId());
			//host.getVm(currentVmId, r.getUserId()).getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());				
		}

		int newVmId = 0;
		for (ServiceRequest r : serviceRequests) {
			//get the new vm
			newVmId = getNextLessBusyVm(datacenter); 
			Vm newVm = datacenter.getVmAllocationPolicy().getHost(newVmId, r.getUserId()).getVm(newVmId, r.getUserId());			

			r.setVmId(newVmId);
			newVm.getAssignedServiceRequestList().add(r);

			double fileTransferTime = datacenter.predictFileTransferTime(r.getRequiredFiles());
			newVm.getServiceRequestScheduler().serviceRequestSubmit(r, fileTransferTime);
			//newVm.getServiceRequestScheduler().serviceRequestSubmit(r);
			
			//Host host = datacenter.getVmAllocationPolicy().getHost(newVmId, r.getUserId());
			//host.getVm(newVmId, r.getUserId()).getServiceRequestScheduler().serviceRequestSubmit(r);				

			newVm.updateVmProcessing(CloudSim.clock(), 
					datacenter.getVmAllocationPolicy().getHost(newVm).getVmScheduler().getAllocatedMipsForVm(newVm));

			//Log.printLine(CloudSim.clock() + ": [" + getName() + "] Moving cloudlet " + r.getServiceRequestId() + " to VM #" + newVmId);
		}		
	}

	protected int getNextLessBusyVm(Datacenter datacenter) {
		List<AdvVm> vmsOnList;
		if (Constants.STABILITY_ANALYSIS_ENABLED || Constants.STABILITY_EVALUATION_ENABLED) {
			vmsOnList = ((AdaptiveDatacenterStability) datacenter).getVmOnList();
		} else {
			vmsOnList = ((AdaptiveDatacenter) datacenter).getVmOnList();
		}
	
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

	/**
	 * Move cloudlets of a consolidated vm to another vm running 
	 * after the execution of vm consolidation
	 * submitting cloudlets to another vm running (newVm).
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void moveServiceRequests(int datacenterId, int vmId, int newVmId) {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);

		//get list of queued requests
		ArrayList<ServiceRequest> queuedServiceRequests = new ArrayList<ServiceRequest>();
		for (ServiceRequest cloudlet : getServiceRequestSubmittedList()) {
			if (cloudlet.getVmId() == vmId) { 
				if (cloudlet.getServiceRequestStatus() == 2) {		//status = QUEUED
					queuedServiceRequests.add(cloudlet);
				} else if (cloudlet.getServiceRequestStatus() == 3) {		//status = INEXEC
					//sendNow(datacenterId, CloudSimTags.VM_DATACENTER_EVENT);
				}
			}
		} 
		moveServiceRequestsToVm(datacenter, queuedServiceRequests, newVmId);

		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	protected void moveServiceRequestsToVm (Datacenter datacenter, ArrayList<ServiceRequest> serviceRequests, int newVmId){
		for (ServiceRequest r : serviceRequests) {
			int currentVmId = r.getVmId();
			
			Vm vm = vmList.get(currentVmId);
			vm.getAssignedServiceRequestList().remove(r);
			vm.getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());

			//Host host = datacenter.getVmAllocationPolicy().getHost(currentVmId, r.getUserId());
			//host.getVm(currentVmId, r.getUserId()).getServiceRequestScheduler().serviceRequestRemove(r.getServiceRequestId());				
		}

		for (ServiceRequest r : serviceRequests) {
			//get the new vm
			Vm newVm = datacenter.getVmAllocationPolicy().getHost(newVmId, r.getUserId()).getVm(newVmId, r.getUserId());			

			r.setVmId(newVmId);
			newVm.getAssignedServiceRequestList().add(r);

			double fileTransferTime = datacenter.predictFileTransferTime(r.getRequiredFiles());
			newVm.getServiceRequestScheduler().serviceRequestSubmit(r, fileTransferTime);
			//newVm.getServiceRequestScheduler().serviceRequestSubmit(r);
			
			//Host host = datacenter.getVmAllocationPolicy().getHost(newVmId, r.getUserId());
			//host.getVm(newVmId, r.getUserId()).getServiceRequestScheduler().serviceRequestSubmit(r);				

			newVm.updateVmProcessing(CloudSim.clock(), 
					datacenter.getVmAllocationPolicy().getHost(newVm).getVmScheduler().getAllocatedMipsForVm(newVm));

			//Log.printLine(CloudSim.clock() + ": [" + getName() + "] Moving cloudlet " + r.getServiceRequestId() + " to VM #" + newVmId);
		}		
	}
	
}
