package org.cloudbus.cloudsim.aware.arch.time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequestSchedulerSpaceShared;
import org.cloudbus.cloudsim.aware.AwareDatacenter;
import org.cloudbus.cloudsim.aware.arch.SelfAwareArchitecture;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

import helper.Constants;

public class TimeAwarenessSimple implements TimeAwarenessLearning {

	/** if workload changes more than this threshold (%) */
	private final static double WORKLOAD_CHANGE_THRESHOLD = 20.0;
	/** learning discount rate of previous workload change */
	private final static double WORKLOAD_CHANGE_GAMMA = 0.8;

	private final static String name = "Simple Leaning Algorithm";
	private double currentWorkloadChange;
	private double previousWorkloadChange;
	
	protected static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	protected List<AdaptationRule> lstAdaptationRules;
	

	
	public TimeAwarenessSimple() {

	}

	public void initialise(
			AdaptationTacticsCatalogue catalogue, 
			List<AdaptationRule> rules) {

		adaptationTacticsCatalogue = catalogue;
		lstAdaptationRules = rules;

		setCurrentWorkloadChange(0.0);
		setPreviousWorkloadChange(0.0);
	}
	
	/**
     * Run learning algorithm and get Adaptation decision.
    */
	public AdaptationTactic getAdaptationTactic(
						Map<String, ArrayList<Double>> monitorData, 
						int currentWorkload, int previousWorkload) {

		AdaptationTactic adaptationDecision = null;
		boolean adaptationNeeded = false;

		//detect goals violations
		//adaptationNeeded = detectGoalViolations(monitorData) 
		//					|| detectWorkloadChange(currentWorkload, previousWorkload);
		//detect goals possible violations (used in goal-awareness)
		adaptationNeeded = detectGoalPossibleViolations(monitorData) 
							|| detectWorkloadChange(currentWorkload, previousWorkload);

		if (adaptationNeeded) { 
			adaptationDecision = getAdaptationDecisionByStimulusAwRules();
		}
		return adaptationDecision;		
	}

	/**
     * detect goals violations
    */
	protected boolean detectGoalViolations(Map<String, ArrayList<Double>> monitorData){
		boolean violationDetected = false;
		
		// sort goals by weight
		RuntimeGoalsModel.getInstance().sortGoalsByWeight();

		//compare with the list of goals in the GoalsModel
		for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
			//get list of values for each goal
			ArrayList<Double> values = new ArrayList<Double>();
			values = monitorData.get(g.getName());
			
			boolean isViolated = g.checkViolaton(values);
			g.setViolated(isViolated);
			violationDetected = violationDetected || isViolated;
		}		
		
		Log.printLine("[" + name + "] Goals violations: " + violationDetected);
		return violationDetected;
	}

	/**
     * detect goals violations
    */
	private boolean detectGoalPossibleViolations(Map<String, ArrayList<Double>> monitorData){
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
				break;
			}
		}
	
		Log.printLine("[" + name + "] Goals possible violations: " + possibleViolationDetected);
		return possibleViolationDetected;
	}
	
	protected boolean detectWorkloadChange(int currentWorkload, int previousWorkload){
		boolean workloadChange = false;
		
		previousWorkloadChange = currentWorkloadChange;
		
		if (previousWorkload > 0) {
			currentWorkloadChange = (currentWorkload - previousWorkload) / previousWorkload * 100;
		}
		workloadChange = (Math.abs(currentWorkloadChange) >= WORKLOAD_CHANGE_THRESHOLD) 
					&& (Math.abs(previousWorkloadChange * WORKLOAD_CHANGE_GAMMA) >= WORKLOAD_CHANGE_THRESHOLD);
		
		Log.printLine("[" + name + "] Workload changes: " + workloadChange);
		return workloadChange;
	}

	public double getCurrentWorkloadChange() {
		return currentWorkloadChange;
	}

	public void setCurrentWorkloadChange(double currentWorkloadChange) {
		this.currentWorkloadChange = currentWorkloadChange;
	}

	public double getPreviousWorkloadChange() {
		return previousWorkloadChange;
	}

	public void setPreviousWorkloadChange(double previousWorkloadChange) {
		this.previousWorkloadChange = previousWorkloadChange;
	}

	private AdaptationTactic getAdaptationDecisionByStimulusAwRules() {
		AdaptationTactic adaptationDecision = null;

		try {
			for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
				if (g.isViolated()) { 
					// get the list of possible tactics and their priority for the violation of this goal
					Map<Integer,AdaptationTactic> possibleAdaptations = null;
					possibleAdaptations = getPossibleAdaptations(g);
					
					if (possibleAdaptations != null) {	
						// sort possible adaptations by priority from adaptation rules
						Map<Integer,AdaptationTactic> possibleAdaptationsSorted = null;
						possibleAdaptationsSorted = sortPossibleAdaptationsByRule(possibleAdaptations);			
						// get the first possible tactic by priority after checking the max and min limits
						adaptationDecision = getFirstPossibleTactic(possibleAdaptationsSorted);
					}
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (adaptationDecision == null) {
			Log.printLine("[" + name + "] No possible adaptation found.");
		}
		return adaptationDecision;
	}
	
	protected Map<Integer,AdaptationTactic> getPossibleAdaptations(RuntimeGoal g) {
		Map<Integer,AdaptationTactic> possibleAdaptations = new HashMap<Integer,AdaptationTactic>();

		for (AdaptationRule r : lstAdaptationRules) {
			if (r.getQualityAttribute().equals(g.getName())) {
				AdaptationTactic t = adaptationTacticsCatalogue.getTacticByTag(r.getAdaptationTacticActionTag());
				if (t != null) {
					possibleAdaptations.put(r.getPriority(), t);
				}
			}
		}
		
		return possibleAdaptations;
	}
	
	protected Map<Integer,AdaptationTactic> sortPossibleAdaptationsByRule
					(Map<Integer,AdaptationTactic> possibleAdaptations) {
		
		Map<Integer,AdaptationTactic> possibleAdaptationsSorted = 
			possibleAdaptations.entrySet().stream()
			.sorted(Map.Entry.<Integer,AdaptationTactic>comparingByKey())
			.collect(Collectors.toMap(
					Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	
		return possibleAdaptationsSorted;
	}
	
	protected AdaptationTactic getFirstPossibleTactic
					(Map<Integer,AdaptationTactic> possibleAdaptationsSorted) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		AdaptationTactic adaptationDecision = null;
		boolean decisionFound = false;

		for (Entry<Integer,AdaptationTactic> entry : possibleAdaptationsSorted.entrySet()) {
		    @SuppressWarnings("unused")
			int priority = entry.getKey();
		    AdaptationTactic t = entry.getValue();
		    
		    // check the min and max limits of the tactic
	    	switch (t.getObject()) { 
	    	case "VM_Types":
	    		decisionFound = checkVMsPossibleScalingCap();
	    		break;
	    	case "VM":
	    		if (t.getChange().equals("increase")) {
	    			decisionFound = ((AwareDatacenter) datacenter).getVmOnList().size() >= t.getMin()
							&& checkVMsPossibleScalingNumMax();
	    		} else if (t.getChange().equals("decrease")) {
	    			decisionFound = ((AwareDatacenter) datacenter).getVmOnList().size() > t.getMin();
	    		}
	 	    	break;
	    	case "PM":
	    		if (t.getChange().equals("increase")) {
	    			decisionFound = ((AwareDatacenter) datacenter).getHostOnList().size() >= t.getMin()
							&& ((AwareDatacenter) datacenter).getHostOnList().size() < Constants.NUMBER_OF_HOSTS;
	    		} else if (t.getChange() .equals("decrease")) {
	    			decisionFound = ((AwareDatacenter) datacenter).getHostOnList().size() > t.getMin();
	    		}
	    		break;	
	    	} 
			if (decisionFound) {
				adaptationDecision = t;
				break;
			}
		}
		return adaptationDecision;
	}
	
	private boolean checkVMsPossibleScalingCap() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		boolean VMsPossibleScaling = false;
		
		List<AdvVm> lstVMsSorted = ((AwareDatacenter) datacenter).getVmOnList();
		lstVMsSorted.sort(Comparator.comparing(Vm::getNumberOfPes)
						.thenComparing(Comparator.comparing(Vm::getMips)));
		
		// loop through the list of VMs from the smallest VM in PES and MIPS
		//check if it could be scaled to higher PES and MIPS within its host
		for (Vm vm : lstVMsSorted) {
			int currentVmType = vm.getType();
			
			// get a higher VM type
			for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
				if (i != currentVmType) {
					if ((Constants.VM_PES[i] > Constants.VM_PES[currentVmType])) {
						// create a temp vm object with the new type to check it
						Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
								Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
								Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
						
						// check if the new type could fit with the current host
						VMsPossibleScaling = vm.getHost().isSuitableForVm(tempVm);
						tempVm = null;
					}
				}
				if (VMsPossibleScaling) {
					break;
				}
			}
			if (VMsPossibleScaling) {
				break;
			}
		}		
		return VMsPossibleScaling;
	}
	
	private boolean checkVMsPossibleScalingNumMax() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		boolean VMsPossibleScaling = false;
	
		for (Host h : ((AwareDatacenter) datacenter).getHostOnList()) {
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
		



/*	protected Map<Integer,String> getPossibleAdaptations(RuntimeGoal g, List<AdaptationRule> lstAdaptationRules) {
		Map<Integer,String> possibleAdaptations = new HashMap<Integer,String>();
		
			for (AdaptationRule r : lstAdaptationRules) {
				if (r.getQualityAttribute().equals(g.getName())) {
					possibleAdaptations.put(r.getPriority(), r.getAdaptationTacticActionTag());
				}
			}
		return possibleAdaptations;
	}
	
	protected Map<Integer,String> sortPossibleAdaptationsByRule
					(Map<Integer,String> possibleAdaptations) {

		Map<Integer,String> possibleAdaptationsSorted = 
				possibleAdaptations.entrySet().stream()
				.sorted(Map.Entry.<Integer,String>comparingByKey())
				.collect(Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		return possibleAdaptationsSorted;
	}

	protected AdaptationTactic getFirstPossibleTactic
		(Map<Integer,String> possibleAdaptationsSorted) 
				throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		AdaptationTactic adaptationDecision = null;
		boolean decisionFound = false;

		for (Entry<Integer,String> entry : possibleAdaptationsSorted.entrySet()) {
			@SuppressWarnings("unused")
			int priority = entry.getKey(); 
			AdaptationTactic t = adaptationTacticsCatalogue.getTacticByTag(entry.getValue());

			// check the min and max limits of the tactic
			switch (t.getObject()) {
			case "VM_Types":
				decisionFound = checkVMsPossibleScalingCap();
				break;
			case "VM":
				if (t.getChange().equals("increase")) {
					decisionFound = ((AwareDatacenter) datacenter).getVmOnList().size() >= t.getMin()
							&& checkVMsPossibleScalingNumMax();
				} else if (t.getChange().equals("decrease")) {
					decisionFound = ((AwareDatacenter) datacenter).getVmOnList().size() > t.getMin();
				}
				break;
			case "PM":
				if (t.getChange().equals("increase")) {
					decisionFound = ((AwareDatacenter) datacenter).getHostOnList().size() >= t.getMin()
							&& ((AwareDatacenter) datacenter).getHostOnList().size() < Constants.NUMBER_OF_HOSTS;
				} else if (t.getChange() .equals("decrease")) {
					decisionFound = ((AwareDatacenter) datacenter).getHostOnList().size() > t.getMin();
				}
				break;	
			}
			if (decisionFound) {
				adaptationDecision = t;
				break;
			}
		}
		return adaptationDecision;
	}

	private boolean checkVMsPossibleScalingCap() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());

		boolean VMsPossibleScaling = false;

		List<AdvVm> lstVMsSorted = ((AwareDatacenter) datacenter).getVmOnList();
		lstVMsSorted.sort(Comparator.comparing(Vm::getNumberOfPes)
				.thenComparing(Comparator.comparing(Vm::getMips)));

		// loop through the list of VMs from the smallest VM in PES and MIPS
		//check if it could be scaled to higher PES and MIPS within its host
		for (Vm vm : lstVMsSorted) {
			int currentVmType = vm.getType();

			// get a higher VM type
			for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
				if (i != currentVmType) {
					if ((Constants.VM_PES[i] > Constants.VM_PES[currentVmType])) {
						// create a temp vm object with the new type to check it
						Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
								Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
								Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());

						// check if the new type could fit with the current host
						VMsPossibleScaling = vm.getHost().isSuitableForVm(tempVm);
						tempVm = null;
					}
				}
				if (VMsPossibleScaling) {
					break;
				}
			}
			if (VMsPossibleScaling) {
				break;
			}
		}		
		return VMsPossibleScaling;
	}

	private boolean checkVMsPossibleScalingNumMax() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());

		boolean VMsPossibleScaling = false;

		for (Host h : ((AwareDatacenter) datacenter).getHostOnList()) {
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
	}*/


}
