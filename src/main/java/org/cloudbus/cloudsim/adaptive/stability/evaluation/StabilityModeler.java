package org.cloudbus.cloudsim.adaptive.stability.evaluation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;
import org.cloudbus.cloudsim.adv.AdvVm;

import helper.Constants;

/**
 * This class implements a solver for M/M/1/K queues.
 * Each queue represents one VM. Eventually, queue status is updated and the model is solved again. 
 * Based on the results, recommendations are sent to the adaptation engine.
 * 
 * @author rodrigo
 * @author m.salama
 */
public class StabilityModeler {

	AdaptiveDatacenterStability datacenter;
	Hashtable<Integer, QosParameters> vmStatusTable;
	int maximumVms;
	
	double negotiatedServiceTime;
	double negotiatedRejection;
	double actualExecutionTime;
	double lowUtilizationThreshold = 0.8;

	
	public StabilityModeler(
			AdaptiveDatacenterStability datacenter, WorkloadAnalyzer analyzer, int vmType) {
		this.datacenter = datacenter;
		this.vmStatusTable = new Hashtable<Integer, QosParameters>();
		//this.maximumVms = Constants.NUMBER_OF_HOSTS * Constants.HOST_PES[Constants.HOST_PES.length-1];
		int hostType = datacenter.getHostList().get(0).getType();
		this.maximumVms = Constants.NUMBER_OF_HOSTS * Constants.HOST_PES[hostType] / Constants.VM_PES[vmType];

		//set very relaxed QoS parameters
		this.negotiatedServiceTime = 0.0;//1000000.0;
		this.negotiatedRejection = 0.0;//1000000.0;
		this.actualExecutionTime = 0.0;
		
		//create the table of VM status
		List<AdvVm> vmList = datacenter.getVmOnList(); 
		for(Vm vm : vmList) { 
			vmStatusTable.put(vm.getId(), 
					new QosParameters(datacenter.getVmCapacity(vm.getId()),0,0.0,0.0));
		}
	}

	public double getCurrentExecutionTime() {
		return this.actualExecutionTime;
	}
	
	public void setModeler(int vmCapacity) {
		List<AdvVm> vmList = datacenter.getVmOnList(); 
		for(Vm vm : vmList) { 
			vmStatusTable.put(vm.getId(), 
					new QosParameters(vmCapacity, 0, 0.0, 0.0));
		}
	}
	
	/**
	 * This method is called by a VM when number of waiting requests varies. 
	 * This variation may cause the Modeler to update the model.
	 * @param vmId
	 * @param load
	 */
	public void setVmQueueSize(Integer vmId, double size){		
		if(!vmStatusTable.containsKey(vmId)){//a new VM was added
			vmStatusTable.put(vmId, new QosParameters(datacenter.getVmCapacity(vmId), size,0.0,0.0));
		} else {//update an existing VM
			QosParameters parameters = vmStatusTable.remove(vmId);
			parameters.setQueueSize(size);
			vmStatusTable.put(vmId, parameters);
		} 
	}
	
	/**
	 * This method is called by a VM when execution time varies.
	 * This variation may cause the Modeler to update the model.
	 * @param vmId
	 * @param load
	 */
	public void setVmExecutionTime(Integer vmId, double executionTime){ 
		if(!vmStatusTable.containsKey(vmId)){	//a new VM was added
			vmStatusTable.put(vmId, new QosParameters(datacenter.getVmCapacity(vmId),executionTime,0.0,0.0));
		} else {	//update an existing VM
			QosParameters parameters = vmStatusTable.remove(vmId);
			parameters.setExecutionTime(executionTime);
			vmStatusTable.put(vmId, parameters);
		} 
	}
	
	public void removeVm(int i){
		vmStatusTable.remove(i);
	}
	
	/**
	 * Set expected QoS of user requests
	 * @param serviceTime
	 */
	public void setQos(double serviceTime,double rejection){
		negotiatedServiceTime = serviceTime;
		negotiatedRejection = rejection;
	}
	
	/**
	 * Update the model based on the current capacity of VMs and expected incoming requests. 
	 * If new model does not meet QoS, reconfiguration actions are suggested.
	 * 
	 * @return number of VMs required in the data center 
	 */
	public int solve(double expectedArrivalRate){				
		double expectedArrivalRatePerVm = expectedArrivalRate / vmStatusTable.size();
		double capacity = 1.0; 		
		double accumulatedResponseTime = 0.0;
		double accumulatedRejection = 0.0;
		double accumulatedCapacity = 0.0;
		double accumulatedLength = 0.0;
		int numberOfVms = vmStatusTable.size();
				
		this.actualExecutionTime = 0.0;
		Enumeration<Integer> keys = vmStatusTable.keys();
		int usefullVms = 0; 
		while (keys.hasMoreElements()){
			Integer id = keys.nextElement();
		
			//get current VM performance
			double currentExecutionTime = vmStatusTable.get(id).getExecutionTime();

			if (currentExecutionTime < 0.01) continue; //this is a new machine
						
			usefullVms++;
			this.actualExecutionTime += currentExecutionTime;
			
			capacity = vmStatusTable.get(id).getCapacity();
			accumulatedCapacity += capacity;
					
			double rho = currentExecutionTime * expectedArrivalRatePerVm;
				
			//q=k/2 if rho=1 
			double expectedQueueSize = capacity/2; 
			//if rho !=1, calculate q
			if (!equals(rho,1.0)){//avoid double-related rounding issues
				double num = (capacity+1) * Math.pow(rho, capacity+1);
				double den = 1 - Math.pow(rho, capacity+1);
			
				expectedQueueSize = (rho/(1-rho)) - (num/den);
			}
			accumulatedLength += expectedQueueSize+1;
				
			//Pr(Sk)=1/(k+1) if rho=1
			double expectedRejection = 1/(capacity+1);
			//if rho !=1, calculate Pr(Sk)
			if (!equals(rho,1.0)){//avoid double-related rounding issues
				double num = (1-rho)*Math.pow(rho, capacity);
				double den = 1-Math.pow(rho, capacity+1);
			
				expectedRejection = num/den;
			}
			
			double expectedResponseTime = expectedQueueSize / (expectedQueueSize*(1-expectedRejection));
							
			accumulatedResponseTime += expectedResponseTime;
			accumulatedRejection += expectedRejection;
		}
		this.actualExecutionTime /= usefullVms;
				
		double meanExpectedResponseTime = accumulatedResponseTime /usefullVms;
		double meanRejection = accumulatedRejection /usefullVms;
		double datacenterUtilization = accumulatedLength /accumulatedCapacity;
					
		//System.out.println("++++++++++++++++++++++++++++++++++");
		//System.out.println("Expected arrival rate=" + expectedArrivalRate);
		//System.out.println("Actual execution time=" + actualExecutionTime);
		//System.out.println("Expected service time=" + meanExpectedResponseTime);
		//System.out.println("Expected rejection=" + meanRejection);
		//System.out.println("Expected utilization=" + datacenterUtilization);
		
		if(meanExpectedResponseTime > negotiatedServiceTime
				|| meanRejection > negotiatedRejection
				|| datacenterUtilization < lowUtilizationThreshold){ 
			return calculateNumberOfVms(
					capacity, expectedArrivalRate, meanExpectedResponseTime, 
					meanRejection, datacenterUtilization);
		}			 
		return numberOfVms;
	}
	
	/**
	 * Returns the number of VMs required in the system in order to reduce
	 * idleness of resources (i.e, QoS is kept but queue length in VMs increase)
	 * @param datacenterUtilization Percentage of resource utilization
	 * measured as (sum of queue length on each VM)/(sum of capacities of each VM)
	 * @return number of VMs required in the system
	 */
	private int calculateNumberOfVms(
			double capacity, double expectedArrivalRate, 
			double meanExpectedResponseTime, double meanRejection, double datacenterUtilization) {
		/*Binary search for the optimal number of VMs:
		  we want the number that makes utilization
		  above the threshold without compromising QoS*/
		double predictedServiceTime = meanExpectedResponseTime;
		double predictedUtilizationRate = datacenterUtilization;
		double predictedRejection = meanRejection;
				
		int min = 0;
		int max = vmStatusTable.size();
		int n = max;
		int oldN;
		
		do{
			//System.out.println("-------------------------------------");
			//System.out.println("n="+n);
			//System.out.println("Predicted service time="+predictedServiceTime);
			//System.out.println("Predicted utilization="+predictedUtilizationRate);
			//System.out.println("Predicted rejection="+predictedRejection);
			
			oldN = n;
					
			if (predictedRejection > negotiatedRejection || predictedServiceTime > negotiatedServiceTime){
				//increase the number of VMs in 50%
				n = max + ((int)Math.ceil(max/2.0));
				min = max + 1;
				max = n;
			} else {
				if (predictedUtilizationRate < lowUtilizationThreshold) {
					//reduce the number of VMs in 50%
					max = n;
					n = min + ((max-min)/2);
					if(n == min){
						//we can't reduce n anymore, or we break QoS.
						//Return the old n as the number of VMs to be created
						n = oldN;
						break;
					}
				}
			}
			//calculate QoS with this n
			
			//first, update estimated service time
			double expectedArrivalRatePerVm = expectedArrivalRate /n;
			double rho = actualExecutionTime * expectedArrivalRatePerVm;
		
			//q=k/2 if rho=1 
			double expectedQueueSize = capacity /2;
			
			//if rho !=1, calculate q
			if (!equals(rho,1.0)){//avoid double-related rounding issues
				double num = (capacity+1)*Math.pow(rho, capacity+1);
				double den = 1-Math.pow(rho, capacity+1);				
				expectedQueueSize = (rho/(1-rho)) - (num/den);
			}
			
			//Pr(Sk)=1/(k+1) if rho=1
			predictedRejection = 1/(capacity+1);
			//if rho !=1, calculate Pr(Sk)
			if (!equals(rho,1.0)){//avoid double-related rounding issues
				double num = (1-rho)*Math.pow(rho, capacity);
				double den = 1-Math.pow(rho, capacity+1);
				
				predictedRejection = num/den;
			}
			predictedServiceTime = expectedQueueSize / (expectedArrivalRatePerVm * (1-predictedRejection));
			predictedUtilizationRate = expectedQueueSize / capacity; 
		} while (predictedServiceTime > negotiatedServiceTime);//(oldN != n);
						
		if (n > this.maximumVms) 
			n = this.maximumVms; 
		return n;
	}

	private boolean equals(double op1, double op2) {
		if(Math.abs(op1-op2)<0.001) return true;
		return false;
	}

}


class QosParameters{
	int capacity;
	double queueSize;
	double executionTime;
	double serviceTime;
	
	public QosParameters(int capacity, double queueSize, double executionTime, double serviceTime) {
		this.capacity = capacity;
		this.queueSize = queueSize;
		this.executionTime = executionTime;
		this.serviceTime = serviceTime;
	}
	
	public int getCapacity(){
		return capacity;
	}
	public double getQueueSize() {
		return queueSize;
	}
	public void setQueueSize(double queueSize) {
		this.queueSize = queueSize;
	}
	public double getExecutionTime() {
		return executionTime;
	}
	public double getServiceTime() {
		return serviceTime;
	}
	public void setExecutionTime(double executionTime) {
		this.executionTime = executionTime;
	}
	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}
}

