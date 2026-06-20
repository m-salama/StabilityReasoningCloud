package org.cloudbus.cloudsim.aware.arch.time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.AdaptiveDynamicDatacenterBorker;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.aware.arch.time.QLearningAction;
import org.cloudbus.cloudsim.aware.arch.time.QLearningAlgorithm;
import org.cloudbus.cloudsim.aware.arch.time.QLearningState;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.xml.sax.SAXException;

import helper.Constants;

public class TimeAwarenessQLearning implements TimeAwarenessLearning {

	private static QLearningAlgorithm learningAlgorithm;
	
	private final static String name = "QLeaning Algorithm";
	
	/** the number of states for a goal (goal itself, plus values, minus values) */
	private final static int NUMBER_OF_STATES_PER_GOAL = 5;
	/** to increase/decrease the possible goal states  */
	private final static double GOAL_STATE_CHANGE_STEP = 20.0;
	/** the number of configurations for a tactic */
	private final static int NUMBER_OF_CONFIG_PER_TACTIC = 10;
	/** to increase/decrease the possible configurations of each tactic */
	private final static int ACTION_CONFIG_STEP = 3;
	
   /** list of states (of stability goals) */
	private List<QLearningState> lstStabilityStates; 
    /** list of actions (adaptation actions) */ 
	private List<QLearningAction> lstAdaptationActions;
	
	/** valid actions (adaptation actions) for each state */
	private Map<QLearningState,List<QLearningAction>> validActions;
	/** transition matrix */
	private int[][] transitionMatrix;

	/** current state */
	private int currentState;
	
	protected static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	protected List<AdaptationRule> lstAdaptationRules;

	
	public TimeAwarenessQLearning() {
		learningAlgorithm = new QLearningAlgorithm();
		lstStabilityStates = new ArrayList<QLearningState>();
		lstAdaptationActions = new ArrayList<QLearningAction>();
		currentState = -1;
	}

	@Override
	public void initialise(AdaptationTacticsCatalogue catalogue, List<AdaptationRule> rules) {
		adaptationTacticsCatalogue = catalogue;
		lstAdaptationRules = rules;

		//define stability states
		loadStabilityStates();
		//define adaptation actions
		loadAdaptationActions();
		loadValidAdaptationActions();

		//set the learning algorithm
		learningAlgorithm.setLstStates(lstStabilityStates);
		learningAlgorithm.setStateSize(lstStabilityStates.size());

		learningAlgorithm.setLstActions(lstAdaptationActions);
		learningAlgorithm.setActionSize(lstAdaptationActions.size());

		learningAlgorithm.setValidActions(validActions);
	}
	
	/**
     * Run learning algorithm and get Adaptation decision.
    */
	@Override
	public AdaptationTactic getAdaptationTactic(
					Map<String, ArrayList<Double>> monitorData, 
					int currentWorkload, int previousWorkload) {

		AdaptationTactic adaptationDecision = null;
		adaptationDecision = learningAlgorithm.getDecision();

		return adaptationDecision;
	}

	private void loadStabilityStates() {
		List<Double> responseTimeValues = new ArrayList<Double>(NUMBER_OF_STATES_PER_GOAL);
		List<Double> energyValues = new ArrayList<Double>(NUMBER_OF_STATES_PER_GOAL);
		List<Double> costValues = new ArrayList<Double>(NUMBER_OF_STATES_PER_GOAL);
		
		//create possible states for each goal
		for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
			if (g.getName().equals("ResponseTime")) {
				responseTimeValues = createGoalStates(g);
			}
			if (g.getName().equals("EnergyConsumption")) {
				energyValues = createGoalStates(g);
			}
			if (g.getName().equals("OperationalCost")) {
				costValues = createGoalStates(g);
			}
		}
		for (double rt : responseTimeValues) {
			for (double e : energyValues) {
				for (double c : costValues) {
					Map<RuntimeGoal,Double> data = new HashMap<RuntimeGoal,Double>();
					data = createStateData(rt, e, c);					
					lstStabilityStates.add(new QLearningState(lstStabilityStates.size(), data));					
				}
			}
		}
	}
	
	private List<Double> createGoalStates(RuntimeGoal g) {
		List<Double> lst = new ArrayList<Double>(NUMBER_OF_STATES_PER_GOAL);	

		lst.add(g.getConstraintValue());
		for (int i=0; i < (NUMBER_OF_STATES_PER_GOAL-1)/2; i++) {
			lst.add(g.getConstraintValue() 
					+ (g.getConstraintValue() * g.getViolationThreshold() * (i+1)));
			lst.add(g.getConstraintValue() 
					- (g.getConstraintValue() * g.getViolationThreshold() * (i+1)));
		}
		return lst;
	}

	private Map<RuntimeGoal, Double> createStateData(Double rt, Double e, Double c) {
		Map<RuntimeGoal,Double> data = new HashMap<RuntimeGoal,Double>();
		for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
			if (g.getName().equals("ResponseTime")) {
				data.put(g, rt);
			}
			if (g.getName().equals("EnergyConsumption")) {
				data.put(g, e);
			}
			if (g.getName().equals("OperationalCost")) {
				data.put(g, c);
			}
		}
		return data;
	}

	private void loadAdaptationActions() {
		for (AdaptationTactic t : adaptationTacticsCatalogue.getListOfAdaptationTactics()) {
			for (int i=0; i<NUMBER_OF_CONFIG_PER_TACTIC; i++) {
				lstAdaptationActions.add(new QLearningAction(i, t, (i + ACTION_CONFIG_STEP)));
			}
		}
	}
	
	/** 
	 * create a set of valid actions for all states
	 * */
	private void loadValidAdaptationActions() {
		for (QLearningState s : lstStabilityStates) {
			List<QLearningAction> temp = new ArrayList<QLearningAction>();
			for (QLearningAction a : lstAdaptationActions) {
				while (s.getData().entrySet().iterator().hasNext()) {//for each goal in the state
					Entry<RuntimeGoal, Double> item = s.getData().entrySet().iterator().next();
					RuntimeGoal g = (RuntimeGoal) item.getKey();
						//check all adaptation rules
						while(lstAdaptationRules.iterator().hasNext()) {
							AdaptationRule r = lstAdaptationRules.iterator().next();
							if (g.getName().equals(r.getQualityAttribute().getName())
									&& a.getTactic().getActionTag().equals(r.getAdaptationTactic().getActionTag())) {
								temp.add(a);
						}//check adaptation rules
					}//loop all rules
				}//loop of all goals in a state
			}//loop of all actions for a state
			validActions.put(s, temp);
		}//loop of all states
	}
	
	/** 
	 * create matrix of transitions
	 * when in state x and perform action y, will end in state s'
	 * -1 invalid transition
	 * else new state id
	 * 
	 * */
	private void loadTransitionsMatrix() {
		for (QLearningState s : lstStabilityStates) {
			for (QLearningAction a : lstAdaptationActions) {
				List<QLearningAction> temp = new ArrayList<QLearningAction>();
				temp = validActions.get(s);
				if (temp.contains(a)) {
					transitionMatrix[s.getId()][a.getId()] = s.getId();
				} else {
					transitionMatrix[s.getId()][a.getId()] = -1; 	//invalid transition
				}
				
				while (s.getData().entrySet().iterator().hasNext()) {
					Entry<RuntimeGoal, Double> item = s.getData().entrySet().iterator().next();
					RuntimeGoal g = (RuntimeGoal) item.getKey();
					Double value = (Double) item.getValue();
					//if the value is the initial value -1
					if (validActions[s.getId()][a.getId()] == -1) {
						//check all adaptation rules
						while(lstAdaptationRules.iterator().hasNext()) {
							AdaptationRule r = lstAdaptationRules.iterator().next();
							if (g.getName().equals(r.getQualityAttribute().getName())
									&& a.getTactic().getActionTag().equals(r.getAdaptationTactic().getActionTag())) {
								if (value == g.getConstraintValue()) {
									validActions[s.getId()][a.getId()] = 0; //do nothing
								} else {
									validActions[s.getId()][a.getId()] = 100; 	//valid action
								} 
							} else {
								validActions[s.getId()][a.getId()] = -100; 	//invalid action
							}
						}//check adaptation rules
					}
				}//loop of all goals in a state
			}//loop of all actions of a state
		}//loop of all states
	}
	

}
