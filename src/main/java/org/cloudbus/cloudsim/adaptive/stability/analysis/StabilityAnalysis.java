package org.cloudbus.cloudsim.adaptive.stability.analysis;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTactic;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;

public class StabilityAnalysis {
	
	AdaptiveDatacenterStability datacenter;

	
	
	public StabilityAnalysis() {
		
	}
	public StabilityAnalysis(AdaptiveDatacenterStability datacenter) {
		this.datacenter = datacenter;

		Log.printLine("StabilityAnalysis component is created.");
	}
	
	public List<AdaptationTactic> getAdaptationDecision(AdaptationTacticsCatalogue adaptationTacticsCatalogue) {
		List<AdaptationTactic> adaptationActions = new ArrayList<AdaptationTactic>();

		return adaptationActions;
	}
	
	
}
