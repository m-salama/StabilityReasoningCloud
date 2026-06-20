package org.cloudbus.cloudsim.aware.arch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.xml.sax.SAXException;

import helper.Constants;

public class GoalAwareness extends StimulusAwareness {

	/** The StimulusAwarenessComponent singleton instance. */
	private static GoalAwareness goalAwareness;

	//private static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	//private List<AdaptationRule> lstAdaptationRules;
	
	private int datacenterId;
	private int datacenterBrokerId;
	private int selfExpressionId;

	
	public GoalAwareness(String name) {
		super(name);

		adaptationTacticsCatalogue = new AdaptationTacticsCatalogue();
		setLstAdaptationRules(new ArrayList<AdaptationRule>());
	}

	/**
     * Create a static method to get instance.
    */
	public static GoalAwareness getInstance() {
        if(goalAwareness == null){
        	goalAwareness = new GoalAwareness("GoalAwareness");
        }
        return goalAwareness;
	}

	@Override
	public void startEntity() {
		try {
			//set the adaptation tactics catalogue
			adaptationTacticsCatalogue.LoadXMLAdaptationTactics();
			//set the adaptation rules
			setLstAdaptationRules(super.loadXMLAdaptationRules());

			Log.printLine(getName() + " is starting...");		
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute adaptation decisions
		case CloudSimTags.SAW_GOAL_AWARENESS:
			act(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	@SuppressWarnings("unchecked")
	public void act(SimEvent ev) {
		try {
		datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
		selfExpressionId = SelfExpressionComponent.getInstance().getId();
		
		reflectAdaptatonOfLastTimeInterval();

		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Running goal-awareness...");
		
		boolean possibleViolationDetected = false;
		
		//detect goals violations
		Map<String,ArrayList<Double>> monitorData = new HashMap<String,ArrayList<Double>>();
		monitorData = (HashMap<String, ArrayList<Double>>) ev.getData();
		possibleViolationDetected = detectPossibleViolations(monitorData);

		if (possibleViolationDetected) {
			// sort goals by weight
			RuntimeGoalsModel.getInstance().sortGoalsByWeight();
		
			AdaptationTactic adaptationDecision = null;
			for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
				if (g.isViolated()) {
					// get the list of possible tactics and their priority for the violation of this goal
					Map<Integer,AdaptationTactic> possibleAdaptations = null;
					possibleAdaptations = super.getPossibleAdaptations(g);
	
					if (possibleAdaptations != null) {	
						// sort possible adaptations by priority from adaptation rules
						Map<Integer,AdaptationTactic> possibleAdaptationsSorted = null;
						possibleAdaptationsSorted = super.sortPossibleAdaptationsByRule(possibleAdaptations);
					
						// get the first possible tactic by priority after checking the max and min limits
						adaptationDecision = super.getFirstPossibleTactic(possibleAdaptationsSorted);
					}
				}
				if (adaptationDecision != null) {
					Log.printLine(CloudSim.clock() + ": [" + getName() + "] Adaptation Decision taken: "
						+ adaptationDecision.getDescription() + " for the (possibly) violated goal " + g.getName());
				
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

					//update goal history
					g.getHistory().get(g.getHistory().size()-1).setTacticExecuted(adaptationDecision.getActionTag());

					// add adaptation history and overhead
					SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
					break;
				} else {
					Log.printLine(CloudSim.clock() + ": [" + getName() + "] No possible Adaptation Decision "
						+ "for the violated goal " + g.getName());
				}
			}	
		}
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean detectPossibleViolations(Map<String, ArrayList<Double>> monitorData){
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Detecting runtime gaols possible violations...");

		boolean possibleViolationDetected = false;
		
		// sort goals by weight
		RuntimeGoalsModel.getInstance().sortGoalsByWeight();

		//compare with the list of goals in the GoalsModel
		for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
			//get list of values for each goal
			ArrayList<Double> values = new ArrayList<Double>();
			values = monitorData.get(g.getName());
			
			boolean isViolated = g.checkViolatonWithinThreshold(values);			
			if (isViolated) {
				possibleViolationDetected = true;
				g.setViolated(true);
				g.addHistoryRecord(CloudSim.clock(), values);
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] Possible Violations detected in goal " + g.getName() + ".");
				break;
			}
		}
		//send violations to stimulus-awareness for taking adaptation decision
		if (!possibleViolationDetected){
			//Log.printLine(CloudSim.clock() + ": [" + getName() + "] Possible violations detected.");
		//} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] No possible violations detected.");
		}
		
		// add adaptation overhead
		try {
			SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return possibleViolationDetected;
	}
	
	public List<AdaptationRule> getLstAdaptationRules() {
		return lstAdaptationRules;
	}

	public void setLstAdaptationRules(List<AdaptationRule> lstAdaptationRules) {
		this.lstAdaptationRules = lstAdaptationRules;
	}

	protected void reflectAdaptatonOfLastTimeInterval(){
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			datacenterBrokerId = CloudSim.getEntityId("Broker");
			sendNow(datacenterId, CloudSimTags.VM_DATACENTER_EVENT);

			if (SelfAwareArchitecture.getInstance().adaptationHistory.size() > 0) {
				double lastAdaptationTime =	SelfAwareArchitecture.getInstance().getAdaptationHistory().getLast().getAdaptationTime();
				
				double timeIntervalStart = CloudSim.clock() - (CloudSim.clock() % Constants.RUNTIME_INTERVAL);
				double timeIntervalEnd = timeIntervalStart + Constants.RUNTIME_INTERVAL;

				if ((!(lastAdaptationTime >= timeIntervalStart && lastAdaptationTime <= timeIntervalEnd)) &&
						((CloudSim.clock() - timeIntervalStart) <= Constants.MONITORING_INTERVAL)) {
					send(datacenterBrokerId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.REFLECT_ADAPTATION, datacenterId);
				}
			}
			schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
