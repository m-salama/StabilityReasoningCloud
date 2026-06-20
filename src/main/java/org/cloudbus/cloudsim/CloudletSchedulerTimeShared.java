/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ResServiceRequest;
import org.cloudbus.cloudsim.adv.ServiceRequest;

/**
 * CloudletSchedulerTimeShared implements a policy of scheduling performed by a virtual machine.
 * Cloudlets execute time-shared in VM.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudletSchedulerTimeShared extends CloudletScheduler {

	/** The cloudlet exec list. */
	private List<? extends ResServiceRequest> cloudletExecList;

	/** The cloudlet paused list. */
	private List<? extends ResServiceRequest> cloudletPausedList;

	/** The cloudlet finished list. */
	private List<? extends ResServiceRequest> cloudletFinishedList;

	/** The current cp us. */
	protected int currentCPUs;

	/**
	 * Creates a new CloudletSchedulerTimeShared object. This method must be invoked before starting
	 * the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public CloudletSchedulerTimeShared() {
		super();
		cloudletExecList = new ArrayList<ResServiceRequest>();
		cloudletPausedList = new ArrayList<ResServiceRequest>();
		cloudletFinishedList = new ArrayList<ResServiceRequest>();
		currentCPUs = 0;
	}

	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each processor available to the scheduler
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
	 *         no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpam = currentTime - getPreviousTime();

		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			rcl.updateServiceRequestFinishedSoFar((long) (getCapacity(mipsShare) * timeSpam * rcl.getNumberOfPes() * Consts.MILLION));
		}

		if (getServiceRequestExecList().size() == 0) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// check finished cloudlets
		double nextEvent = Double.MAX_VALUE;
		List<ResServiceRequest> toRemove = new ArrayList<ResServiceRequest>();
		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			long remainingLength = rcl.getRemainingServiceRequestLength();
			if (remainingLength == 0) {// finished: remove from the list
				toRemove.add(rcl);
				serviceRequestFinish(rcl);
				continue;
			}
		}
		getServiceRequestExecList().removeAll(toRemove);

		// estimate finish time of cloudlets
		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			double estimatedFinishTime = currentTime
					+ (rcl.getRemainingServiceRequestLength() / (getCapacity(mipsShare) * rcl.getNumberOfPes()));
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}

			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}

	/**
	 * Gets the capacity.
	 * 
	 * @param mipsShare the mips share
	 * @return the capacity
	 */
	protected double getCapacity(List<Double> mipsShare) {
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : mipsShare) {
			capacity += mips;
			if (mips > 0.0) {
				cpus++;
			}
		}
		currentCPUs = cpus;

		int pesInUse = 0;
		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			pesInUse += rcl.getNumberOfPes();
		}

		if (pesInUse > currentCPUs) {
			capacity /= pesInUse;
		} else {
			capacity /= currentCPUs;
		}
		return capacity;
	}

	/**
	 * Cancels execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being cancealed
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public ServiceRequest serviceRequestCancel(int cloudletId) {
		boolean found = false;
		int position = 0;

		// First, looks in the finished queue
		found = false;
		for (ResServiceRequest rcl : getServiceRequestFinishedList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			return getServiceRequestFinishedList().remove(position).getServiceRequest();
		}

		// Then searches in the exec list
		position=0;
		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			ResServiceRequest rcl = getServiceRequestExecList().remove(position);
			if (rcl.getRemainingServiceRequestLength() == 0) {
				serviceRequestFinish(rcl);
			} else {
				rcl.setServiceRequestStatus(ServiceRequest.CANCELED);
			}
			return rcl.getServiceRequest();
		}

		// Now, looks in the paused queue
		found = false;
		position=0;
		for (ResServiceRequest rcl : getServiceRequestPausedList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				found = true;
				rcl.setServiceRequestStatus(ServiceRequest.CANCELED);
				break;
			}
			position++;
		}

		if (found) {
			return getServiceRequestPausedList().remove(position).getServiceRequest();
		}

		return null;
	}

	/**
	 * Pauses execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean serviceRequestPause(int cloudletId) {
		boolean found = false;
		int position = 0;

		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// remove cloudlet from the exec list and put it in the paused list
			ResServiceRequest rcl = getServiceRequestExecList().remove(position);
			if (rcl.getRemainingServiceRequestLength() == 0) {
				serviceRequestFinish(rcl);
			} else {
				rcl.setServiceRequestStatus(ServiceRequest.PAUSED);
				getServiceRequestPausedList().add(rcl);
			}
			return true;
		}
		return false;
	}

	/**
	 * Processes a finished cloudlet.
	 * 
	 * @param rcl finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	@Override
	public void serviceRequestFinish(ResServiceRequest rcl) {
		rcl.setServiceRequestStatus(ServiceRequest.SUCCESS);
		rcl.finalizeServiceRequest();
		getServiceRequestFinishedList().add(rcl);
	}

	/**
	 * Resumes execution of a paused cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being resumed
	 * @return expected finish time of the cloudlet, 0.0 if queued
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double serviceRequestResume(int cloudletId) {
		boolean found = false;
		int position = 0;

		// look for the cloudlet in the paused list
		for (ResServiceRequest rcl : getServiceRequestPausedList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			ResServiceRequest rgl = getServiceRequestPausedList().remove(position);
			rgl.setServiceRequestStatus(ServiceRequest.INEXEC);
			getServiceRequestExecList().add(rgl);

			// calculate the expected time for cloudlet completion
			// first: how many PEs do we have?

			double remainingLength = rgl.getRemainingServiceRequestLength();
			double estimatedFinishTime = CloudSim.clock()
					+ (remainingLength / (getCapacity(getCurrentMipsShare()) * rgl.getNumberOfPes()));

			return estimatedFinishTime;
		}

		return 0.0;
	}

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cloudlet the submited cloudlet
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet
	 * @pre gl != null
	 * @post $none
	 */
	@Override
	public double serviceRequestSubmit(ServiceRequest cloudlet, double fileTransferTime) {
		ResServiceRequest rcl = new ResServiceRequest(cloudlet);
		rcl.setServiceRequestStatus(ServiceRequest.INEXEC);
		for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
			rcl.setMachineAndPeId(0, i);
		}

		getServiceRequestExecList().add(rcl);

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = getCapacity(getCurrentMipsShare()) * fileTransferTime;
		long length = (long) (cloudlet.getServiceLength() + extraSize);
		cloudlet.setServiceLength(length);
		
		return cloudlet.getServiceLength() / getCapacity(getCurrentMipsShare());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#cloudletSubmit(cloudsim.Cloudlet)
	 */
	@Override
	public double serviceRequestSubmit(ServiceRequest cloudlet) {
		return serviceRequestSubmit(cloudlet, 0.0);
	}

	/**
	 * Gets the status of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int getServiceRequestStatus(int cloudletId) {
		for (ResServiceRequest rcl : getServiceRequestExecList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				return rcl.getServiceRequestStatus();
			}
		}
		for (ResServiceRequest rcl : getServiceRequestPausedList()) {
			if (rcl.getServiceRequestId() == cloudletId) {
				return rcl.getServiceRequestStatus();
			}
		}
		return -1;
	}

	/**
	 * Get utilization created by all cloudlets.
	 * 
	 * @param time the time
	 * @return total utilization
	 */
	@Override
	public double getTotalUtilizationOfCpu(double time) {
		double totalUtilization = 0;
		for (ResServiceRequest gl : getServiceRequestExecList()) {
			totalUtilization += gl.getServiceRequest().getUtilizationOfCpu(time);
		}
		return totalUtilization;
	}

	/**
	 * Informs about completion of some cloudlet in the VM managed by this scheduler.
	 * 
	 * @return $true if there is at least one finished cloudlet; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean isFinishedServiceRequests() {
		return getServiceRequestFinishedList().size() > 0;
	}

	/**
	 * Returns the next cloudlet in the finished list, $null if this list is empty.
	 * 
	 * @return a finished cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public ServiceRequest getNextFinishedServiceRequest() {
		if (getServiceRequestFinishedList().size() > 0) {
			return getServiceRequestFinishedList().remove(0).getServiceRequest();
		}
		return null;
	}

	/**
	 * Returns the number of cloudlets runnning in the virtual machine.
	 * 
	 * @return number of cloudlets runnning
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int runningServiceRequests() {
		return getServiceRequestExecList().size();
	}

	/**
	 * Returns one cloudlet to migrate to another vm.
	 * 
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public ServiceRequest migrateServiceRequest() {
		ResServiceRequest rgl = getServiceRequestExecList().remove(0);
		rgl.finalizeServiceRequest();
		return rgl.getServiceRequest();
	}

	/**
	 * Gets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet exec list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResServiceRequest> List<T> getServiceRequestExecList() {
		return (List<T>) cloudletExecList;
	}

	/**
	 * Sets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletExecList the new cloudlet exec list
	 */
	protected <T extends ResServiceRequest> void setServiceRequestExecList(List<T> cloudletExecList) {
		this.cloudletExecList = cloudletExecList;
	}

	/**
	 * Gets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet paused list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResServiceRequest> List<T> getServiceRequestPausedList() {
		return (List<T>) cloudletPausedList;
	}

	/**
	 * Sets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletPausedList the new cloudlet paused list
	 */
	protected <T extends ResServiceRequest> void setServiceRequestPausedList(List<T> cloudletPausedList) {
		this.cloudletPausedList = cloudletPausedList;
	}

	/**
	 * Gets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet finished list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResServiceRequest> List<T> getServiceRequestFinishedList() {
		return (List<T>) cloudletFinishedList;
	}

	/**
	 * Sets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletFinishedList the new cloudlet finished list
	 */
	protected <T extends ResServiceRequest> void setServiceRequestFinishedList(List<T> cloudletFinishedList) {
		this.cloudletFinishedList = cloudletFinishedList;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.ServiceRequestScheduler#getCurrentRequestedMips()
	 */
	@Override
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		return mipsShare;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#getTotalCurrentAvailableMipsForCloudlet(cloudsim.ResCloudlet,
	 * java.util.List)
	 */
	@Override
	public double getTotalCurrentAvailableMipsForServiceRequest(ResServiceRequest rcl, List<Double> mipsShare) {
		return getCapacity(getCurrentMipsShare());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#getTotalCurrentAllocatedMipsForCloudlet(cloudsim.ResCloudlet,
	 * double)
	 */
	@Override
	public double getTotalCurrentAllocatedMipsForServiceRequest(ResServiceRequest rcl, double time) {
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.CloudletScheduler#getTotalCurrentRequestedMipsForCloudlet(cloudsim.ResCloudlet,
	 * double)
	 */
	@Override
	public double getTotalCurrentRequestedMipsForServiceRequest(ResServiceRequest rcl, double time) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public double getCurrentRequestedUtilizationOfRam() {
		double ram = 0;
		for (ResServiceRequest cloudlet : cloudletExecList) {
			ram += cloudlet.getServiceRequest().getUtilizationOfRam(CloudSim.clock());
		}
		return ram;
	}

	@Override
	public double getCurrentRequestedUtilizationOfBw() {
		double bw = 0;
		for (ResServiceRequest cloudlet : cloudletExecList) {
			bw += cloudlet.getServiceRequest().getUtilizationOfBw(CloudSim.clock());
		}
		return bw;
	}


}
