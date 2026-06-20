package org.cloudbus.cloudsim.aware.arch.time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTacticsCatalogue;

public interface TimeAwarenessLearning {
	
	void initialise(
			AdaptationTacticsCatalogue catalogue, 
			List<AdaptationRule> rules);
	
	AdaptationTactic getAdaptationTactic(
			Map<String, ArrayList<Double>> monitorData, 
			int currentWorkload, int previousWorkload);

}
