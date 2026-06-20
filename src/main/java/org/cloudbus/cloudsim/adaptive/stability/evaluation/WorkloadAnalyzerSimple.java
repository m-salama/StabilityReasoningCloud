package org.cloudbus.cloudsim.adaptive.stability.evaluation;


public class WorkloadAnalyzerSimple extends WorkloadAnalyzer {

	double mean;
	
	public WorkloadAnalyzerSimple(double mean) {
		this.mean = mean;
	}

	@Override
	public double delayToNextChangeInModel(double currentTime) {
		//update every 15 minutes
		return 15.0*60.0;
	}

	@Override
	public double getEstimatedArrivalRate(double currentTime) {
		return mean;
	}

}
