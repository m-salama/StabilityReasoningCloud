package org.cloudbus.cloudsim.adaptive.stability.arch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.AdaptiveDatacenter;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationEngine;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationExecutor;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationRule;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTactic;
import org.cloudbus.cloudsim.adaptive.arch.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adaptive.goal.Goal;
import org.cloudbus.cloudsim.adaptive.goal.GoalsModel;
import org.cloudbus.cloudsim.adaptive.stability.AdaptiveDatacenterStability;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequestSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import helper.Constants;

public class AdaptationEngineStability extends SimEntity {

	/** The AdaptationEngine singleton instance. */
	private static AdaptationEngineStability adaptationEngine;
	
	protected static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	private List<AdaptationRule> lstAdaptationRules;

	protected int datacenterId;
	protected int datacenterBrokerId;
	protected int adaptationExecutorId;	

	private Datacenter datacenter;


	public AdaptationEngineStability(String name) {
		super(name);
		
		adaptationTacticsCatalogue = new AdaptationTacticsCatalogue();
		lstAdaptationRules = new ArrayList<AdaptationRule>();		
	}

	/**
     * Create a static method to get instance.
    */
    public static AdaptationEngineStability getInstance(){
        if(adaptationEngine == null){
        	adaptationEngine = new AdaptationEngineStability("AdaptationEngineStability");
        }
        return adaptationEngine;
    }

	@Override
	public void startEntity() {
		try {
			//set the adaptation tactics catalogue
			adaptationTacticsCatalogue.LoadXMLAdaptationTactics();
			//set the adaptation rules
			LoadXMLAdaptationRules();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.printLine(getName() + " is starting...");
	}

	private void LoadXMLAdaptationRules() throws ParserConfigurationException, SAXException, IOException {
		try {
			String workingDir = System.getProperty("user.dir");
			String inputFolder = workingDir + "//experiments//configurations//";
			String xmlFile = inputFolder + "adaptationRules.xml";

			File inputFile = new File(xmlFile);
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(inputFile);
	         
	        // get root element <AdaptationTactics>
	        doc.getDocumentElement().normalize();
	        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	         
	        // get Adaptation Rules nodes
	        NodeList nList = doc.getElementsByTagName("Rule");
	    	
	        String id;
	        String description;
	        String qualityAttribute;
	        String actionTag;
	        int priority;

	        for (int temp = 0; temp < nList.getLength(); temp++) {
	        	Node nNode = nList.item(temp);
	        	//System.out.println("\nCurrent Element :" + nNode.getNodeName());	            
	        	if (nNode.getNodeType() == Node.ELEMENT_NODE) {	            	
	        		Element eElement = (Element) nNode;

	        		id = eElement.getAttribute("id").toString();
	        		description = eElement.getElementsByTagName("description").item(0).getTextContent();
	        		qualityAttribute = eElement.getElementsByTagName("QualityAttribute").item(0).getTextContent();
	        		actionTag = eElement.getElementsByTagName("AdaptationTactic").item(0).getTextContent();
	        		priority = Integer.parseInt(eElement.getElementsByTagName("priority").item(0).getTextContent());
	               
	        		AdaptationRule r = new AdaptationRule(id, description, qualityAttribute, actionTag, priority);
	        		lstAdaptationRules.add(r);	            
	        	}
    		 }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute taking adaptation decisions
		case CloudSimTags.SAD_ADAPTATION_DECISION_STABILITY:
			processMakeAdaptationDecision();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}
	
	protected void processMakeAdaptationDecision(){
		datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		adaptationExecutorId = AdaptationExecutorStability.getInstance().getId();
		
		List<AdaptationTactic> adaptationActions = new ArrayList<AdaptationTactic>();

		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + super.getName() + "] Making Adaptation Decision for stability...");

		if (Constants.STABILITY_ANALYSIS_ENABLED) {
			adaptationActions = getAdaptationForStabilityByAnalysis();
		} else if (Constants.STABILITY_EVALUATION_ENABLED) {
			adaptationActions = getAdaptationForStabilityByEvaluation();
		}
		setActions(adaptationActions);
		
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
	}
	
	private List<AdaptationTactic> getAdaptationForStabilityByAnalysis() {
		List<AdaptationTactic> adaptationActions = new ArrayList<AdaptationTactic>();
		//adaptationActions = SelfAdaptiveArchitectureStability.getStabilityAnalysisComp().getAdaptationDecision();
		
		// sort goals by weight
		GoalsModel.getInstance().sortGoalsByWeight();
		
		for (Goal g : GoalsModel.getInstance().getViolatedGoals()) { 	
			AdaptationTactic t = getAdaptation(g);
			if (g.getName().equals("ResponseTime")) {
				if (t != null) {
					adaptationActions.add(t);
					adaptationActions.add(t);
					break;
				}
			} else { 
				if (GoalsModel.getInstance().countViolatedGoals() == 1) {
					if (t != null) {
						adaptationActions.add(t);
						adaptationActions.add(t);
					}
				} else if (GoalsModel.getInstance().countViolatedGoals() > 1) {
					if (t != null) adaptationActions.add(t);
				}
			}
		}
		return adaptationActions;
	}
	
	private List<AdaptationTactic> getAdaptationForStabilityByEvaluation() {
		List<AdaptationTactic> adaptationActions = new ArrayList<AdaptationTactic>();

		for (Goal g : GoalsModel.getInstance().getViolatedGoals()) {

				if (g.getName().equals("ResponseTime")) {
					adaptationActions = SelfAdaptiveArchitectureStability.getStabilityEvaluationComp().getAdaptationDecision(adaptationTacticsCatalogue);
					if (adaptationActions.size() > 0) break;
				} else {
					AdaptationTactic t = getAdaptation(g);
					if (t != null) adaptationActions.add(t);
					break;
				}
		}
		return adaptationActions;
	}

	private void setActions(List<AdaptationTactic> adaptationActions) {
		datacenterId = SelfAdaptiveArchitectureStability.getInstance().getDatacenterId();
		adaptationExecutorId = AdaptationExecutorStability.getInstance().getId();
		
		if (adaptationActions.size() > 0) { 
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Adaptation Decision for stability taken: "
					+ adaptationActions.size() + " actions.");
			
			for (int i=0; i<=adaptationActions.size()-1; i++) { 
				AdaptationTactic t = adaptationActions.get(i);
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] Adaptation Decision for stability taken: "
						+ t.getActionTag());

				// get the parameters necessary for the adaptation decision
				List<Integer> parameters = new ArrayList<Integer>();
				parameters = getAdaptationTacticParameters(t, i);
						
				// send the adaptation decision to the adaptation executor
				String[] evdata = new String[4];
				evdata[0] = t.getActionTag(); 
				for (int j=0; j<=parameters.size()-1; j++) { 
					evdata[j+1] = Integer.toString(parameters.get(j));	
				}
				
				send(adaptationExecutorId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAD_EXECUTE_ADAPTATION_STABILITY, evdata);
			}
			// add adaptation overhead
			SelfAdaptiveArchitectureStability.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());	

		} else {
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] No action taken.");
		}
	}
	
	private AdaptationTactic getAdaptation(Goal g) {
		AdaptationTactic adaptationDecision = null;

		// get the list of possible tactics and their priority for the violation of this goal
		Map<Integer,AdaptationTactic> possibleAdaptations = null;
		possibleAdaptations = getPossibleAdaptations(g);
		
		if (possibleAdaptations != null) {	
			// sort possible adaptations by priority from adaptation rules
			Map<Integer,AdaptationTactic> possibleAdaptationsSorted = null;
			possibleAdaptationsSorted = sortPossibleAdaptationsByRule(possibleAdaptations);
			
			// get the first possible tactic by priority after checking the max and min limits
			adaptationDecision = getFirstPossibleTactic(g, possibleAdaptationsSorted);
			
		} 
		return adaptationDecision;
	}
	
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}
		Log.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this QoSMonitor.");
	}

	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	private Map<Integer,AdaptationTactic> getPossibleAdaptations(Goal g) {
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
	
	private Map<Integer,AdaptationTactic> sortPossibleAdaptationsByRule
					(Map<Integer,AdaptationTactic> possibleAdaptations) {
		
		Map<Integer,AdaptationTactic> possibleAdaptationsSorted = 
			possibleAdaptations.entrySet().stream()
			.sorted(Map.Entry.<Integer,AdaptationTactic>comparingByKey())
			.collect(Collectors.toMap(
					Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	
		return possibleAdaptationsSorted;
	}
	
	private AdaptationTactic getFirstPossibleTactic
					(Goal goal, Map<Integer,AdaptationTactic> possibleAdaptationsSorted) {
		datacenter = (Datacenter) CloudSim.getEntity(SelfAdaptiveArchitectureStability.getInstance().getDatacenterId());
		AdaptationTactic adaptationDecision = null;
		boolean decisionFound = false;
		
		for (Entry<Integer, AdaptationTactic> entry : possibleAdaptationsSorted.entrySet()) {
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
	    			decisionFound = ((AdaptiveDatacenterStability) datacenter).getVmOnList().size() >= t.getMin()
							&& checkVMsPossibleScalingNumMax();
	    		} else if (t.getChange().equals("decrease")) {
	    			decisionFound = ((AdaptiveDatacenterStability) datacenter).getVmOnList().size() > t.getMin();
	    		}
	 	    	break;
	    	case "PM":
	    		if (t.getChange().equals("increase")) {
	    			decisionFound = ((AdaptiveDatacenterStability) datacenter).getHostOnList().size() >= t.getMin()
							&& ((AdaptiveDatacenterStability) datacenter).getHostOnList().size() < Constants.NUMBER_OF_HOSTS;
	    		} else if (t.getChange() .equals("decrease")) {
	    			decisionFound = ((AdaptiveDatacenterStability) datacenter).getHostOnList().size() > t.getMin();
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
	
	private boolean checkVMsPossibleScalingCap() {
		boolean VMsPossibleScaling = false;
		
		List<AdvVm> lstVMsSorted = ((AdaptiveDatacenterStability) datacenter).getVmOnList();
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
	
	private boolean checkVMsPossibleScalingNumMax() {
		boolean VMsPossibleScaling = false;
	
		for (Host h : ((AdaptiveDatacenterStability) datacenter).getHostOnList()) {
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
		
/*	private boolean checkVMsPossibleConsolidation() {
		return (((AdaptiveDatacenter) datacenter).getVmOnList().size() > 1) ? true : false;
	}
*/
	protected List<Integer> getAdaptationTacticParameters(AdaptationTactic t, int num) {
		List<Integer> evdata = new ArrayList<Integer>();
		datacenter = (Datacenter) CloudSim.getEntity(SelfAdaptiveArchitectureStability.getInstance().getDatacenterId());

		switch (t.getActionTag()) {
		case "VERTICAL_SCALING_CAP":
			evdata = getVerticalScalingCapParameters(num);
			break;
		case "VERTICAL_SCALING_NUM":
			evdata = getVerticalScalingNumParameters(num);
			break;
		case "VERTICAL_DESCALING_NUM":
			evdata = getVerticalDescalingNumParameters(num);
			break;
		case "HORIZONTAL_SCALING":
			evdata = getHorizontalScalingParameters();
			break;
		case "HORIZONTAL_DESCALING":
			evdata = getHorizontalDescalingNumParameters(num);
			break;
		case "VM_CONSOLIDATION":
			evdata = getVmConsolidationParameters(num);
			break;
		}
		
		return evdata;
	}
	
	private List<Integer> getVerticalScalingCapParameters(int num) {
		List<Integer> parameters = new ArrayList<Integer>();
		boolean parametersFound = false;
		
		List<AdvVm> lstVMsSorted =  ((AdaptiveDatacenter) datacenter).getVmOnList();
		lstVMsSorted.sort(Comparator.comparing(Vm::getNumberOfPes)
						.thenComparing(Comparator.comparing(Vm::getMips)));
		
		// loop through the list of VMs from the smallest VM in PES and MIPS
		//get the highest PES and MIPS to be scaled to within its host
		for (Vm vm : lstVMsSorted) {
			if (vm.getId() >= num) { //to get the next vm 
				int currentVmType = vm.getType();
				// get the largest VM type possible
				for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
					if (i != currentVmType) {
						if ((Constants.VM_PES[i] > Constants.VM_PES[currentVmType])) {
							//change the vm to the new type
							vm.setType(i);
							vm.setMips(Constants.VM_MIPS[i]);
							vm.setNumberOfPes(Constants.VM_PES[i]);
							vm.setRam(Constants.VM_RAM[i]); 
							vm.setBw(Constants.VM_BW[i]); 
							vm.setSize(Constants.VM_SIZE[i]);
							vm.setVmm(Constants.VM_MONITOR[i]); 

							// check if the new type could fit with the current host
							parametersFound = vm.getHost().isSuitableForVm(vm);
						}
					}
					if (parametersFound) {
						parameters.add(vm.getId()); //vmId to be upgraded
						parameters.add(i);	//the new type of the VM
						break;
					}
				}
			}
			if (parametersFound) {
				break;		
			}
		}
		return parameters;
	}
	
	private List<Integer> getVerticalScalingNumParameters(int num) {
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		
		List<Integer> parameters = new ArrayList<Integer>();
		int hostId = -1;
		
		for (Host h : ((AdaptiveDatacenterStability) datacenter).getHostOnList()) {
			for (Vm vm : h.getVmList()) {
				if ((!((AdvVm) vm).isOn()) && (vm.getId() >= num))
					hostId = h.getId(); 
				if (hostId != -1) 
					break;
			}
			if (hostId == -1) {		
				for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
					// create a temp vm object with the new type to check it
					Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
							Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
							Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
				
					// check if the new type could fit with the current host
					if (h.isSuitableForVm(tempVm) && hostId >= num) 
						hostId = h.getId();
					if (hostId != -1) 
						break;
				}
			} 
			if (hostId != -1) 
				break;
		}
		
		parameters.add(hostId);
		parameters.add(datacenterBrokerId);	

		return parameters;
	}

	private List<Integer> getVerticalDescalingNumParameters(int num) {
		List<Integer> parameters = new ArrayList<Integer>();

		List<AdvVm> lstVmsSorted = ((AdaptiveDatacenterStability) datacenter).getVmOnList();
		lstVmsSorted.sort(Comparator.comparing(Vm::getNumberOfPes).reversed());
		
		// get the largest Vm type possible for descaling
		Vm vm = lstVmsSorted.get(num);

		parameters.add(vm.getId());				//id the vm to be removed
		parameters.add(vm.getHost().getId());	//id of the host
		parameters.add(vm.getUserId());			//datacenterBrokerId
		
		return parameters;
	}

	private List<Integer> getHorizontalScalingParameters() {
		List<Integer> parameters = new ArrayList<Integer>();
		
		int vmSchedulerType = 2;
		int brokerId = CloudSim.getEntityId("Broker");
		
		parameters.add(vmSchedulerType);
		parameters.add(brokerId);	//datacenterBrokerId
		
		return parameters;
	}
	
	private List<Integer> getHorizontalDescalingNumParameters(int num) {
		List<Integer> parameters = new ArrayList<Integer>();

		List<AdvHost> lstHostsSorted = ((AdaptiveDatacenterStability) datacenter).getHostOnList();
		lstHostsSorted.sort(Comparator.comparing(Host::getNumberOfPes).reversed());
		
		// get the largest host type possible for descaling
		Host host = lstHostsSorted.get(num);
		int brokerId = CloudSim.getEntityId("Broker");

		parameters.add(host.getId());			//id the host to be removed
		parameters.add(brokerId);				//datacenterBrokerId
		
		return parameters;
	}
	
	private List<Integer> getVmConsolidationParameters(int num) {
		List<Integer> parameters = new ArrayList<Integer>();
		int hostId = 0;		//PM to be removed
		int NumOfVm = 0;

		List<AdvHost> lstHosts = ((AdaptiveDatacenterStability) datacenter).getHostOnList();
		
		// get host with less VMs to be removed 
		for (Host h : lstHosts) {
			if (NumOfVm < h.getVmList().size() && hostId >= num) {
				hostId = h.getId();
				NumOfVm = h.getVmList().size();
			}
		}
		parameters.add(hostId);

		return parameters;
	}
	
	
	public static AdaptationTacticsCatalogue getAdaptationTacticsCatalogue() {
		return adaptationTacticsCatalogue;
	}

	public static void setAdaptationTacticsCatalogue(AdaptationTacticsCatalogue adaptationTacticsCatalogue) {
		AdaptationEngineStability.adaptationTacticsCatalogue = adaptationTacticsCatalogue;
	}

	public List<AdaptationRule> getLstAdaptationRules() {
		return lstAdaptationRules;
	}

	public void setLstAdaptationRules(List<AdaptationRule> lstAdaptationRules) {
		this.lstAdaptationRules = lstAdaptationRules;
	}

}
