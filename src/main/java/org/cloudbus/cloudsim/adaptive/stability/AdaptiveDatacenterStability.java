package org.cloudbus.cloudsim.adaptive.stability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.adaptive.AdaptationRecord;
import org.cloudbus.cloudsim.adaptive.AdaptiveDatacenter;
import org.cloudbus.cloudsim.adaptive.OperationRecord;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adaptive.stability.arch.SelfAdaptiveArchitectureStability;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.adv.ServiceRequestScheduler;
import org.cloudbus.cloudsim.adv.ServiceRequestSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import helper.Constants;

/**
 * AdaptiveDatacenterStability class is extending AdaptiveDatacenter, 
 * with adaptation capabilities for stability.
 * 
 * @author m.salama
 * @version SAdCloudSim
 */
public class AdaptiveDatacenterStability extends Datacenter {

	/** The Self-Adaptive Cloud Architecture for stability */
	private static SelfAdaptiveArchitectureStability selfAdaptiveArchitectureStability;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The history of configuations during operation */
	private static LinkedList<OperationRecord> operationHistory;
	

	/**
	 * Allocates a new AdaptiveDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @param characteristics an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @throws Exception This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *             more Machines. A Machine must contain one or more PEs.
	 *             </ul>
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public AdaptiveDatacenterStability(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) 
					throws Exception 
	{
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setCloudletSubmitted(-1);

		// SAd-CloudSim: Create Self-Adaptive architecture for stability
		selfAdaptiveArchitectureStability = SelfAdaptiveArchitectureStability.getInstance();
		selfAdaptiveArchitectureStability.setDatacenterId(this.getId());

		operationHistory = new LinkedList<OperationRecord>();
		super.setOperationalCost();
		operationHistory.add(new OperationRecord(CloudSim.clock(), getHostList().size(), getVmList().size(), super.getOperationalCost(), super.getPower()));
	}


	/**
	 * Returns the capacity of a VM identified by its ID
	 * @param vmId
	 * @return VM Capacity (in this case, it is equal for all VMs)
	 */
	public int getVmCapacity(Integer vmId) {
		//return vmQueueLength; 
		Vm vm = getVmList().get(vmId);	
		double vmCapacity = ((vm.getNumberOfPes() * vm.getMips()) / (Constants.SERVICE_PES[1] * Constants.SERVICE_LENGTH[1]));
		return (int) vmCapacity;
	}
	
	
	/**
	 * Processes events or services that are available for this Datacenter.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);

		switch (ev.getTag()) {
		// Process Vertical Scaling by increasing the capacity of a VM
		case CloudSimTags.VERTICAL_SCALING_CAP:
			processVerticalScalingCap(ev);
			break;

		// Process Vertical Scaling by adding a new VM
		case CloudSimTags.VERTICAL_SCALING_NUM:
			processVerticalScalingNum(ev);
			break;
		
		// Process Vertical de-scaling by removing a VM
		case CloudSimTags.VERTICAL_DESCALING_NUM:
			processVerticalDescalingNum(ev);
			break;

		// Execute horizontal scaling by adding a new PM
		case CloudSimTags.HORIZONTAL_SCALING:
			processHorizontalScaling(ev);
			break;

		// Process Vertical de-scaling by removing a PM and its VMs
		case CloudSimTags.HORIZONTAL_DESCALING:
			processHorizontalDescaling(ev);
			break;

		// Execute VM consolidation and shutdown unused PM
		case CloudSimTags.VM_CONSOLIDATION:
			processVMConsolidation(ev);
		}
	}
	
	
	protected void recordAdaptation(String adaptationTag) {
		// add adaptation history and overhead
		SelfAdaptiveArchitectureStability.getInstance().addAdaptationRecord(
				CloudSim.clock(), adaptationTag, getHostOnList().size(), getVmOnList().size());
		SelfAdaptiveArchitectureStability.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		addOperationHisotryRecord();

		// add operation history
		setOperationalCost();
		getOperationHistory().add(
			new OperationRecord(CloudSim.clock(), getHostOnList().size(), getVmOnList().size(), getOperationalCost(), getPower()));

		int datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processVerticalScalingCap(SimEvent ev) {
		int[] evdata = new int[2];
		evdata = (int[]) ev.getData();		
		
		int vmId = evdata[0];			//vmId to be upgraded		
		int newVmType = evdata[1];		//the new type of the VM
				
		Vm vm = VmList.getById(getVmList(), vmId);
		if (vm != null) {
			Host host = vm.getHost();
			
			//update VmAllocationPolicy
			getVmAllocationPolicy().deallocateHostForVm(vm);
			vm.changeType(newVmType);
			getVmAllocationPolicy().allocateHostForVm(vm, host);
			
			//update vmScheduler in the host
			host.getVmScheduler().deallocatePesForVm(vm);
			//host.getVmScheduler().allocatePesForVm(vm, )
	
			//update coudletScheduler in the vm
			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm)
					.getVmScheduler().getAllocatedMipsForVm(vm));
			
			updateAllVmsProcessing();
			
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Verical scaling executed: VM #" 
						+ vmId + " is upgraded to vmType #" + newVmType + ".");
			
			// send updates to the broker
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_SCALING_CAP, evdata);

			recordAdaptation("VERTICAL_SCALING_CAP");
		} else {
			Log.printLine("Verical scaling failed.");
		}
	}

	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processVerticalScalingNum(SimEvent ev) {
		int[] evdata = new int[2];
		evdata = (int[]) ev.getData();		
		
		int hostId = evdata[0];
		int datacenterBrokerId = evdata[1];		//the datacenter borker
		
		if (hostId == -1) {
			int vmSchedulerType = 2;
			int data[] = new int[2];
			data[0] = vmSchedulerType;
			data[1] = datacenterBrokerId;
			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.HORIZONTAL_SCALING, data);
			return;
		}
		
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity(datacenterBrokerId);
		Host host = HostList.getById(getHostList(), hostId);
		Vm newVm = null;  
		if (host.getVmList().size() > 0) {
			for (Vm vm : host.getVmList()) {	//if there is a vm to be turned on
				if (!((AdvVm) vm).isOn()) {
					((AdvVm) vm).setOn(true);
					((AdvVm) getVmList().get(vm.getId())).setOn(true);
					getVmAllocationPolicy().enableVm(vm);
					newVm = vm; 
					break;
				}
			}
		} 
		if (newVm == null) {	//or create a new vm
			int newVmType = getNewVmType(host);		//the type of the new VM
			if (newVmType >= 0) {
				newVm = createAdvVM(newVmType, datacenterBrokerId);
				if (newVm != null) {
					getVmList().add(newVm);	
					if (getVmAllocationPolicy().allocateHostForVm(newVm, host)) {
						if (newVm.isBeingInstantiated()) {
							newVm.setBeingInstantiated(false);
						}
					} 
				} 
			} else { //create a new host
				int data[] = new int[2];
				int vmSchedulerType = 2;
				data[0] = vmSchedulerType;
				data[1] = datacenterBrokerId;
				send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.HORIZONTAL_SCALING, data);
			}
		}
		if (newVm != null) {
			//update vmScheduler in the host
			host.getVmScheduler().allocatePesForVm(newVm, newVm.getCurrentRequestedMips());	
			newVm.updateVmProcessing(CloudSim.clock(), 
					getVmAllocationPolicy().getHost(newVm).getVmScheduler().getAllocatedMipsForVm(newVm));
			broker.submitVm(newVm);
			
			updateAllVmsProcessing();
			
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Vertical scaling executed, "
					+ "a new VM (type #" + newVm.getType() + ") is running (in Host#" + newVm.getHost().getId() + "). "
					+ "Number of VMs is now: " + getVmList().size());
				
			// send updates to the broker
			send(datacenterBrokerId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_SCALING_NUM, newVm);

			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
			recordAdaptation("VERTICAL_SCALING_NUM");
		} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Vertical scaling failed.");
		}
	}

	private int getNewVmType(Host host) {
		boolean parametersFound = false;
		int newVmType = -1;

		//List<Host> lstHostsSorted = getHostList();
		//lstHostsSorted.sort(Comparator.comparing(Host::getNumberOfFreePes));
		
		// get the largest Vm type possible to fit with the host
		for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
			// loop through the list of PMs
			//for (Host h : lstHostsSorted) {
				Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
						Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
						Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
				// check if the new type could fit with the current host
				parametersFound = host.isSuitableForVm(tempVm);
				tempVm = null;
				
				if (parametersFound) {
					newVmType = i;
					break;
				}
			//}
		}
		return newVmType;
	}

	private AdvVm createAdvVM(int newVmType, int datacenterBrokerId) {
		int newVmID = getVmList().size();		

		ServiceRequestScheduler cloudletScheduler = null;
		cloudletScheduler = new ServiceRequestSchedulerSpaceShared();
		
		Vm newVm = new AdvVm(
				newVmID, 
				datacenterBrokerId,
				newVmType,
				Constants.VM_MIPS[newVmType], 
				Constants.VM_PES[newVmType], 
				Constants.VM_RAM[newVmType], 
				Constants.VM_BW[newVmType], 
				Constants.VM_SIZE[newVmType], 
				1,
				Constants.VM_MONITOR[newVmType],
				cloudletScheduler,
				Constants.SCHEDULING_INTERVAL
				);
		
		return (AdvVm) newVm;
	}
	
	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processVerticalDescalingNum(SimEvent ev) {
		int[] evdata = new int[3];
		evdata = (int[]) ev.getData();			
		int vmId = evdata[0];					//vmId to be removed
		int hostId = evdata[1];					//the host id
		int datacenterBrokerId = evdata[2];		//the datacenter borker
		
		Vm vm = VmList.getById(getVmList(), vmId);  
		
		((AdvVm) getVmList().get(vmId)).setOn(false); 
		getVmAllocationPolicy().disableVm(vm);

		vm.updateVmProcessing(CloudSim.clock(), 
				getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
		
		updateAllVmsProcessing();

		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Vertical de-scaling executed, VM #" + vmId + " is removed. "
							+ "Number of VMs is now: " + getVmOnList().size());
			
		// send updates to the broker
		int[] data = new int[3];
		data[0] = vmId;					//vmId to be removed
		data[1] = hostId;					//the host id
		data[2] = this.getId();
		send(datacenterBrokerId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_DESCALING_NUM, data);
		
		send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		recordAdaptation("VERTICAL_DESCALING_NUM");
		
		//if all vms of this host are disabled, then disable the host
		Host host = HostList.getById(getHostList(), hostId);
		boolean disableHost = true;
		for (Vm tempVm : host.getVmList()) {
			disableHost &= !(((AdvVm) tempVm).isOn());
		}
		if (disableHost) {
			int[] temp = new int[2];
			temp[0] = hostId;
			temp[1] = datacenterBrokerId;
			
			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.HORIZONTAL_DESCALING, temp);
		}
	}

	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processHorizontalScaling(SimEvent ev) {
		int[] evdata = new int[2];
		evdata = (int[]) ev.getData();		
		
		int vmSchedulerType = evdata[0];
		int datacenterBrokerId = evdata[1];		//datacenterBrokerId
	
		AdvHost newHost = null; 
		if (getHostList().size() > getHostOnList().size()) {	//if there is a host to be turned on
			for (Host h : getHostList()) { 
				if (!((AdvHost) h).isOn()) {
					((AdvHost) h).setOn(true); 
					getVmAllocationPolicy().enableHost(h);
					newHost = (AdvHost) h; 
					break;
				}
			} 
		} else {	//or create a new host 
			// get the largest Host type possible
			int newHostType = Constants.HOST_TYPES-1; 
			newHost = createAdvHost(newHostType, vmSchedulerType);	
			if (newHost != null) {
				if (getHostList().add(newHost)){
					getCharacteristics().getHostList().get(newHost.getId()).setDatacenter(this);
					getVmAllocationPolicy().addHost(newHost);
				}
			} 
		} 
					
		if (newHost != null) {
			updateAllVmsProcessing();
		
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Horizontal scaling executed, a new PM is running. "
						+ "Number of PMs is now: " + getHostOnList().size());
			
			int[] data = new int[2];
			data[0] = newHost.getId();
			data[1] = datacenterBrokerId;
		
			//create vm in the new host
			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_SCALING_NUM, data);		

			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
			recordAdaptation("HORIZONTAL_SCALING");
		} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Horizontal scaling failed");
		}
	}
	
	private AdvHost createAdvHost(int newHostType, int vmSchedulerType) {
		int newHostId = getHostList().size();
		
		List<Pe> peList = new ArrayList<Pe>();
		for (int j = 0; j < Constants.HOST_PES[newHostType]; j++) {
			peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[newHostType])));
		}

		VmScheduler vmScheduler = null;
		switch(vmSchedulerType){
			case 1:	//VmSchedulerSpaceShared
				//vmScheduler = new VmSchedulerSpaceShared(peList);
				break;
			case 2:	//VmSchedulerTimeShared
				vmScheduler = new VmSchedulerTimeShared(peList);
				break;
			case 3:	//VmSchedulerOportunisticSpaceShared
				//vmScheduler = new VmSchedulerOportunisticSpaceShared(peList);
				break;
			case 4:	//VmSchedulerTimeSharedOverSubscription
				//vmScheduler = new VmSchedulerTimeSharedOverSubscription(peList);
				break;
		}

		AdvHost newHost = new AdvHost(
				newHostId, 
				newHostType,
				new RamProvisionerSimple(Constants.HOST_RAM[newHostType]), 
				new BwProvisionerSimple(Constants.HOST_BW[newHostType]), 
				Constants.HOST_STORAGE[newHostType], 
				peList, 
				vmScheduler, 
				Constants.HOST_POWER[newHostType]);
		
		return newHost;
	}

	/**
	 * Get the VM type with the largest possible capability to fit with a hostType
	 * 
	 * @param hostType
	 * @return
	 */
	@SuppressWarnings("unused")
	private int getLargestVmTypeForHost(Host host) {
		int vmType = -1;
		
		for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
			Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
						Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
						Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
			// check if the new type could fit with the current host
			if (host.isSuitableForVm(tempVm)) {
				vmType = i;
			}
			if (vmType != -1) {
				tempVm = null;
				break;
			}
		}
		return vmType;
	}
	
	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processHorizontalDescaling(SimEvent ev) {
		int[] evdata = new int[2];
		evdata = (int[]) ev.getData();		
		
		int hostId = evdata[0];					//id of the host to be removed
		int datacenterBrokerId = evdata[1];		//datacenterBrokerId
		
		Host host = HostList.getById(getHostList(), hostId);
		if (host != null) { 
			// remove vms in this host
			for (Vm vm : getVmList()) { 
				if (vm.getHost().equals(host) && ((AdvVm) vm).isOn()) {
					int[] data = new int[3];
					data[0] = vm.getId(); 
					data[1] = hostId;
					data[2] = datacenterBrokerId;

					send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VERTICAL_DESCALING_NUM, data);
				}
			}
			
			//getHostList().remove(host);
			//getVmAllocationPolicy().removeHost(host);
			((AdvHost) getHostList().get(hostId)).setOn(false);
			getVmAllocationPolicy().disableHost(host);

			updateAllVmsProcessing();
			
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Horizontal de-scaling executed, " 
						+ "PM #" + hostId + " is removed. "
						+ "Number of PMs is now: " + getHostOnList().size());
				
			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
			recordAdaptation("HORIZONTAL_DESCALING");
		} else {
			Log.printLine("Host does not exist. Horizontal de-scaling failed.");
		}		
	}
	
	/**
	 * @param ev	event data received from the AdaptationExecutor
	 */
	protected void processVMConsolidation(SimEvent ev) {
		int[] evdata = new int[1];
		evdata = (int[]) ev.getData();		//event data received from the AdaptationEngine
		
		int hostId = evdata[0];			//PM to be removed
		
		AdvHost host = (AdvHost) getHostList().get(hostId);
		
		if (host != null) {
			for (Vm vm : host.getVmList()) {
				Host newHost = getNewHostForMigration(vm, host.getId());
				if (newHost != null) {
					Map<String, Object> migrate = new HashMap<String, Object>(); 
					migrate.put("vm", vm);
					migrate.put("host", newHost);
					
					send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_MIGRATE, migrate);
				}
			}
			
			if (host.getVmList().size() == 0) {
				//getHostList().remove(host);
				//getCharacteristics().getHostList().remove(host);
				//getVmAllocationPolicy().removeHost(host);
				((AdvHost) getHostList().get(hostId)).setOn(false);
				getVmAllocationPolicy().disableHost(host);
				
				String datacenterName = this.getName();
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] VM consolidation executed in " + datacenterName 
								+ ", PM #" + hostId + " is shutdown and its VMs have been migrated. "
								+ "Number of PMs is now: " + getHostOnList().size());
			} else {
				Log.printLine("VM consolidation failed.");
			}
						
			updateAllVmsProcessing();
			
			send(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
			recordAdaptation("VM_CONSOLIDATION");
		} else {
			Log.printLine("VM consolidation failed.");
		}		
	}

	private Host getNewHostForMigration(Vm vm, int currentHostId) {
		for (Host h : getHostOnList()) {
			if (h.getId() != currentHostId) {
				if (h.isSuitableForVm(vm)) {
					return h;
				}
			}
		}		
		return null;
	}
	
	protected void updateAllVmsProcessing() {
		for (Vm vm : getVmList()) {
			//if (((AdvVm) vm).isOn()) {
				vm.updateVmProcessing(CloudSim.clock(), 
						getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
			//}
		}
	}

	/**
	 * Updates processing of each cloudlet running in this AdaptiveDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() { 
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		// if some time passed since last processing
		// R: for term is to allow loop at simulation start. 
		// Otherwise, one initial simulation step is skipped and schedulers are not properly initialized
		if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			//Log.printLine();
			List<? extends Host> list = getVmAllocationPolicy().getHostList();			
			// for each host...
			for (AdvHost host : this.<AdvHost> getHostList()) { 
				// inform VMs to update processing
				double time = host.updateVmsProcessing(currentTime); 
				// what time do we expect that the next cloudlet will finish?
				if (time < minTime) {
					minTime = time;
				}
				//Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%",
						//currentTime, host.getId(), host.getUtilizationOfCpu() * 100);
			}

			//update power and utilization
			if (timeDiff > 0) {
				for (AdvHost host : this.<AdvHost> getHostList()) {
					double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
					double utilizationOfCpu = host.getUtilizationOfCpu();
					double timeFrameHostEnergy = host.getEnergyLinearInterpolation(previousUtilizationOfCpu, utilizationOfCpu, timeDiff);
					timeFrameDatacenterEnergy += timeFrameHostEnergy;

//					Log.formatLine("%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
//							currentTime, host.getId(), getLastProcessTime(),
//							previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
//					Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec", 
//							currentTime, host.getId(), timeFrameHostEnergy);
				}
//				Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f",
//						getLastProcessTime(), currentTime);
//				Log.formatLine("\n%.2f: [Datacenter #%d] Data center's energy in the last timeframe is %.2f W*sec\n", 
//						currentTime, this.getId(), timeFrameDatacenterEnergy);
			}
			this.setPower(getPower() + timeFrameDatacenterEnergy);

			//checkCloudletCompletion();

			// gurantees a minimal interval before scheduling the event
			if (minTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
				minTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
			}
			if (minTime != Double.MAX_VALUE) {
				schedule(getId(), (minTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(currentTime);
		}
 	
		if (CloudSim.clock() < 0.111 || CloudSim.clock() > getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			List<? extends Host> list = getVmAllocationPolicy().getHostList();
			double smallerTime = Double.MAX_VALUE;
			// for each host...
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				// inform VMs to update processing
				double time = host.updateVmsProcessing(CloudSim.clock());
				// what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			// gurantees a minimal interval before scheduling the event
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
			}
			if (smallerTime != Double.MAX_VALUE) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			} 
			setLastProcessTime(CloudSim.clock()); 
		}
	}
	
	public List<AdvHost> getHostOnList() {
		List<AdvHost> hostsOn = new ArrayList<AdvHost>();
		for (Host h : getHostList()) {
			if (((AdvHost) h).isOn()) {
				hostsOn.add((AdvHost) h);
			}
		}
		return hostsOn;
	}
	
	public List<AdvVm> getVmOnList() {
		List<AdvVm> vmsOn = new ArrayList<AdvVm>();
		for (Vm vm : getVmList()) {
			if (((AdvVm) vm).isOn()) {
				vmsOn.add((AdvVm) vm);
			}
		}
		return vmsOn;
	}
	
	public void addOperationHisotryRecord() {
		//if (CloudSim.clock() % Constants.RUNTIME_INTERVAL == 0) {
		operationHistory.add(
			new OperationRecord(CloudSim.clock(), getHostOnList().size(), getVmOnList().size(), getOperationalCost(), getPower()));
		//}
	}
	
	public OperationRecord getOperationRecord(double time) {
		OperationRecord record = new OperationRecord();
		
		for (OperationRecord r : operationHistory) {
			if (r.getOperationTime() >= time &&
					r.getOperationTime() <= time + Constants.RUNTIME_INTERVAL &&
					r.getCost() > record.getCost()) {
				record = r;
			}
		}
		return record;
	}

	public LinkedList<OperationRecord> getOperationHistory() {
		return operationHistory;
	}

	public static void setOperationHistory(LinkedList<OperationRecord> operationHistory) {
		AdaptiveDatacenterStability.operationHistory = operationHistory;
	}

	protected double calculateOperationalCost() {
		double cost = 0.0;
		for (Host h : getHostOnList()) {
			cost += Constants.HOST_OPERATING_COST[h.getType()];
		}
		for (Vm vm : getVmOnList()) {
			cost += Constants.VM_OPERATING_COST[vm.getType()];
		}
		return cost;
	}

	public void printSelfAdaptationResults() {
		double adaptationOverhead = SelfAdaptiveArchitectureStability.getInstance().getAdaptationOverhead();
		LinkedList<AdaptationRecord> adaptationHistory = SelfAdaptiveArchitectureStability.getInstance().getAdaptationHistory();
		
		Log.printLine();
		Log.printLine("========== Self-Adaptation statistics ==========");
		Log.printLine();
		Log.printLine(String.format("Adaptation overhead: " + adaptationOverhead));
		Log.printLine();
		Log.printLine("Adaptation history:");
		Log.printLine("Time \t	Adaptation \t Hosts# \t Vms#");
		adaptationHistory.forEach((record) -> {
		Log.printLine(record.getAdaptationTime() + "\t" 
				+ record.getAdaptationTag() + "\t" 
				+ record.getHostNo() + "\t" 
				+ record.getVmNo());
		});
		Log.printLine();
		Log.printLine("Operation hisotry:");
		
		Log.printLine("Time \t Hosts# \t Vms# \t Operational Cost \t Energy");
		this.getOperationHistory().forEach((record) -> {
			Log.printLine(record.getOperationTime() + "\t"
					+ record.getHostNo() + "\t"
					+ record.getVmNo() + "\t"
					+ String.format("%.2f", record.getCost()) + "\t"
					+ String.format("%.2f", record.getPower() / (3600 * 1000))
					);
        });
	}


}
