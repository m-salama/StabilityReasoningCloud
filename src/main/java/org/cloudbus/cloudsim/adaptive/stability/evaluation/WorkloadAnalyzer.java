package org.cloudbus.cloudsim.adaptive.stability.evaluation;

/**
 * This class is responsible for loading and processing
 * the cloud workload, in order to generate load estimation.
 * Implementations of this class can use different methods
 * for acquiring the workloads metrics and processing them.
 * @author rodrigo
 *
 */
public abstract class WorkloadAnalyzer {	
	public abstract double getEstimatedArrivalRate(double currentTime);	
	public abstract double delayToNextChangeInModel(double currentTime);	
}
