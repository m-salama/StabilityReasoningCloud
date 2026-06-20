package org.cloudbus.cloudsim.adaptive.stability.evaluation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTactic;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.adaptive.goal.GoalsModel;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;
import org.cloudbus.cloudsim.adaptive.stability.arch.SelfAdaptiveArchitectureStability;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.adv.ServiceRequestSchedulerSpaceShared;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;

import helper.Constants;

public class StabilityEvaluation {

	AdaptiveDatacenterStability datacenter;
	StabilityModeler modeler;
	WorkloadAnalyzerWorldCup analyzer;

	String scheduler;
	int vmQueueLength;
	
	//workload characteristics
	int cloudletLength;
	long fileSize;
	
	//host properties
	double pesMips;
	
	int minVms;
	int maxVms;
	int initialVms;
	
	boolean reconfigure;

	//structures for control of use of VMs
	Hashtable<Integer, Integer> queueLength;
	Hashtable<Integer, Double> lastExecutionTime;
	List<AdvVm> vmList;
	List<AdvVm> vmToDestroyList;
	int currentVmName;
	double vmSeconds;
	double lastVmAccounting;
	
	Random generator;

	
	public StabilityEvaluation() {
		
	}
	public StabilityEvaluation(
			AdaptiveDatacenterStability datacenter, 
			int serviceType, int vmType, int hostType) throws Exception {
		
		this.datacenter = datacenter;
		this.vmList = new ArrayList<AdvVm>();

		this.vmQueueLength = getVmCapacity(vmType, serviceType); 
		if (vmQueueLength < 1) throw new Exception("[StabilityEvaluation] : Error - Capacity must be positive.");

		//this.analyzer = new SimpleWorkloadAnalyzer(mean);
		this.analyzer = new WorkloadAnalyzerWorldCup();
		this.modeler = new StabilityModeler(datacenter, analyzer, vmType);
	
		this.scheduler = "VmSchedulerTimeShared";
		this.queueLength = new Hashtable<Integer, Integer>();
		this.lastExecutionTime = new Hashtable<Integer, Double>();
		this.vmToDestroyList = new LinkedList<AdvVm>();
		this.currentVmName = 0;
		this.vmSeconds = 0.0;
		this.lastVmAccounting = 0.0;
		
		this.cloudletLength = Constants.SERVICE_PES[serviceType];
		this.fileSize = Constants.SERVICE_FILESIZE[serviceType];
		
		this.pesMips = Constants.HOST_MIPS[hostType];
		
		this.generator = new Random(System.currentTimeMillis());
		this.reconfigure = true;
		this.minVms = 999999999;
		this.maxVms = -1;
		
		double goalResponseTime = GoalsModel.getInstance().getGoalByName("ResponseTime").getConstraintValue();
		setQos(goalResponseTime, 0.0);
		
		Log.printLine("StabilityEvaluation component is created.");
	}

	protected void setQos(double serviceTime, double rejection) {
		modeler.setQos(serviceTime, rejection);
		Log.printLine("QoS parameters set: Service time="+serviceTime+", Rejection="+rejection);
	}

	/**
	 * Returns the capacity of vms
	 * @return VM Capacity (in this case, it is equal for all VMs)
	 */
	public int getVmCapacity(int vmType, int serviceType) {
		return (int) ((Constants.VM_PES[vmType] * Constants.VM_MIPS[vmType]) / (Constants.SERVICE_PES[serviceType] * Constants.SERVICE_LENGTH[serviceType]));
	}
	
	private void setVmControlStructures() {
		this.vmList.addAll(datacenter.getVmOnList());
		this.initialVms = datacenter.getVmOnList().size();
		
		for (Vm vm : vmList) { 
			queueLength.put(vm.getId(), vm.getAssignedServiceRequestList().size());
			lastExecutionTime.put(vm.getId(), 0.0);
		} 
	}
	
	protected int getReconfiguration(){
		if (vmList.size() == 0) {
			setVmControlStructures();
			modeler.setModeler(this.vmQueueLength);
		}
		
		//update accounting of use of vms
		updateVmUsage();
		updateVmControlStructures();
		updateModeler();
		
		//run the solver
		int requiredVms =  modeler.solve(analyzer.getEstimatedArrivalRate(CloudSim.clock()));
		Log.printLine(CloudSim.clock()+": getReconfiguration(): solver required "+requiredVms+" virtual machines.");

		analyzer.delayToNextChangeInModel(CloudSim.clock());
		return requiredVms;
	}

	private void updateVmUsage() {
		double currentTime = CloudSim.clock();
		double interval = currentTime - lastVmAccounting;
		if (interval > 1.0) {
			int machines = datacenter.getVmList().size();	//vmList.size()+vmToDestroyList.size();
			this.vmSeconds += (machines*interval); 
			this.lastVmAccounting = currentTime;
		}
	}
	
	private void updateVmControlStructures() {
		vmList.clear();
		vmToDestroyList.clear();
		lastExecutionTime.clear();
		queueLength.clear();
		
		for (Vm vm : datacenter.getVmList()) {
			if (((AdvVm) vm).isOn()) {
				vmList.add((AdvVm) vm); 
				if (vm.getFinishedServiceRequestList().size() > 0) {
					ServiceRequest lastExecutedRequest = vm.getFinishedServiceRequestList().get(vm.getFinishedServiceRequestList().size()-1);
					lastExecutionTime.put(vm.getId(), lastExecutedRequest.getFinishTime() - lastExecutedRequest.getExecStartTime());
				} else {
					lastExecutionTime.put(vm.getId(), 0.0);
				}
				queueLength.put(vm.getId(), vm.getAssignedServiceRequestList().size() - vm.getFinishedServiceRequestList().size());
			} else {
				vmToDestroyList.add((AdvVm) vm);
				modeler.removeVm(vm.getId());
			} 	
		}
		
		if (maxVms < (vmList.size()+vmToDestroyList.size())) 
			maxVms = vmList.size() + vmToDestroyList.size();		
		if (minVms > (vmList.size() + vmToDestroyList.size())) 
			minVms  = vmList.size() + vmToDestroyList.size();		
	}
	
	private void updateModeler() {
		for (Vm vm : vmList){ 		
			modeler.setVmQueueSize(vm.getId(), queueLength.get(vm.getId()));
			modeler.setVmExecutionTime(vm.getId(), lastExecutionTime.get(vm.getId()));
		}
	}

	public List<AdaptationTactic> getAdaptationDecision(AdaptationTacticsCatalogue adaptationTacticsCatalogue) {
		List<AdaptationTactic> adaptationActions = new ArrayList<AdaptationTactic>();
		int requiredVms = getReconfiguration();
			
		if (requiredVms != vmList.size()) {//number of VMs has to change...
			int requiredActions = Math.abs(requiredVms - vmList.size());
			if (requiredVms > vmList.size()) { //scaling
				for (int i=0; i < requiredActions; i++) {
					AdaptationTactic t = adaptationTacticsCatalogue.getTacticByTag("VERTICAL_SCALING_NUM");
					if (((AdaptiveDatacenterStability) datacenter).getVmOnList().size()+i+1 >= t.getMin()
								&& checkVMsPossibleScalingNumMax()) {
						adaptationActions.add(t);
					} else {
						t = adaptationTacticsCatalogue.getTacticByTag("HORIZONTAL_SCALING");
						if (((AdaptiveDatacenterStability) datacenter).getHostOnList().size() >= t.getMin()
									&& ((AdaptiveDatacenterStability) datacenter).getHostOnList().size() < Constants.NUMBER_OF_HOSTS) {
							adaptationActions.add(t);
						}
					}
				} 
			} else if (requiredVms < vmList.size()) {	//de-scaling
				for (int i=0; i < requiredActions; i++) {
					AdaptationTactic t = adaptationTacticsCatalogue.getTacticByTag("VERTICAL_DESCALING_NUM");
					if (((AdaptiveDatacenterStability) datacenter).getVmOnList().size()-i+1 > t.getMin()) {
						adaptationActions.add(t);
					} else {
						t = adaptationTacticsCatalogue.getTacticByTag("HORIZONTAL_DESCALING");
						if (((AdaptiveDatacenterStability) datacenter).getHostOnList().size() > t.getMin()) {
							adaptationActions.add(t);
						}
					}
				} 
			}
		}		
		return adaptationActions;
	}

	private boolean checkVMsPossibleScalingNumMax() {
		boolean VMsPossibleScaling = false;
	
		for (Host h : ((AdaptiveDatacenterStability) datacenter).getHostOnList()) {
			for (Vm vm : h.getVmList()) {
				if (!((AdvVm) vm).isOn()) {
					VMsPossibleScaling = true;
				}
				if (VMsPossibleScaling) {
					break;
				}
			}
			if (!VMsPossibleScaling) {		
				for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
					// create a temp vm object with the new type to check it
					Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
							Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
							Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
				
					// check if the new type could fit with the current host
					VMsPossibleScaling = h.isSuitableForVm(tempVm);
					tempVm = null;
					if (VMsPossibleScaling) {
						break;
					}
				}
				if (VMsPossibleScaling) {
					break;
				}
			}
		}
		return VMsPossibleScaling;
	}
		
	private boolean vmIsInList(Integer vmId, List<AdvVm> list) {		
		for (Vm vm : list){
			if (vm.getId() == vmId) return true;
		}	
		return false;
	}


	
}
