package org.cloudbus.cloudsim.aware.arch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.AdaptiveDynamicDatacenterBroker;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.time.TimeAwarenessSimple;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.xml.sax.SAXException;

import helper.Constants;

public class TimeAwareness extends StimulusAwareness {

	/** The TimeAwareness singleton instance. */
	private static TimeAwareness timeAwareness;
	
	//protected static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	//protected List<AdaptationRule> lstAdaptationRules;

	private static TimeAwarenessSimple simpleLearning;
	//private static TimeAwarenessQLearning qLearning;
	
	private int datacenterId;
	private int datacenterBrokerId;
	private int selfExpressionId;

	
	public TimeAwareness(String name) {
		super(name);
		
		//adaptationTacticsCatalogue = new AdaptationTacticsCatalogue();
		//lstAdaptationRules = new ArrayList<AdaptationRule>();

		simpleLearning = new TimeAwarenessSimple();
		//qLearning = new TimeAwarenessQLearning();
	}

	/**
     * Create a static method to get instance.
    */
	public static TimeAwareness getInstance() {
        if(timeAwareness == null){
        	timeAwareness = new TimeAwareness("TimeAwareness");
        }
        return timeAwareness;
	}

	@Override
	public void startEntity() {
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			selfExpressionId = SelfExpressionComponent.getInstance().getId();
			
			reflectAdaptatonOfLastTimeInterval();
		
			//set the adaptation tactics catalogue
			adaptationTacticsCatalogue.LoadXMLAdaptationTactics();
			//set the adaptation rules
			lstAdaptationRules = loadXMLAdaptationRules();

			//initialise the learning algorithm
			simpleLearning.initialise(adaptationTacticsCatalogue, lstAdaptationRules);
			//qLearning.initialise();
			
			Log.printLine(getName() + " is starting...");		
			
			// add adaptation history and overhead
			SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
			case CloudSimTags.SAW_TIME_AWARENESS:
			act(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void act(SimEvent ev) {
		try {
			datacenterBrokerId = CloudSim.getEntityId("Broker");
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			selfExpressionId = SelfExpressionComponent.getInstance().getId();
	
			reflectAdaptatonOfLastTimeInterval();

			Log.printLine();
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Running time-awareness...");
		
			//detect goals violations
			Map<String,ArrayList<Double>> monitorData = new HashMap<String,ArrayList<Double>>();
			monitorData = (HashMap<String, ArrayList<Double>>) ev.getData();
			
			AdaptationTactic adaptationDecision = runLearningAlgorithm(monitorData);
			
			if (adaptationDecision != null) {
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] Adaptation Decision taken: "
						+ adaptationDecision.getDescription());

				// get the parameters necessary for the adaptation decision
				List<Integer> parameters = new ArrayList<Integer>();
				parameters = super.getAdaptationTacticParameters(adaptationDecision);

				// send the adaptation decision to the self-expression component
				String[] evdata = new String[4];
				evdata[0] = adaptationDecision.getActionTag();
				for (int i=0; i<=parameters.size()-1; i++) {
					evdata[i+1] = Integer.toString(parameters.get(i));		
				}

				send(selfExpressionId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_SELF_EXPRESSION, evdata);

				//update goals history
				for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
					g.addHistoryRecord(CloudSim.clock(), monitorData.get(g.getName()));
					g.getHistory().get(g.getHistory().size()-1).setTacticExecuted(adaptationDecision.getActionTag());
				}

				// add adaptation history and overhead
				SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
			} else {
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] No action taken.");
			}
			schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected AdaptationTactic runLearningAlgorithm(Map<String,ArrayList<Double>> monitorData) {
		AdaptationTactic adaptationDecision = null;
		
		AdaptiveDynamicDatacenterBroker datacenterBorker = (AdaptiveDynamicDatacenterBroker) CloudSim.getEntity("Broker");
		
		// run simple learning algorithm
		//get workload of current time instance
		int currentWorkload = datacenterBorker.getIntervalCloudlets(CloudSim.clock()).size();
		//get workload of last time instance
		int previousWorkload = datacenterBorker.getIntervalCloudlets(CloudSim.clock() - Constants.RUNTIME_INTERVAL).size();
		
		adaptationDecision = simpleLearning.getAdaptationTactic(monitorData, currentWorkload, previousWorkload);	
		// or run qLearning
		//adaptationDecision = qLearning.getAdaptationTactic(monitorData);

		return adaptationDecision;
	}
	

}
