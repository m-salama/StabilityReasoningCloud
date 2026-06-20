package org.cloudbus.cloudsim.aware.arch;

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
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.ServiceRequestSchedulerSpaceShared;
import org.cloudbus.cloudsim.aware.AwareDatacenter;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationRule;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTactic;
import org.cloudbus.cloudsim.aware.arch.stimulus.AdaptationTacticsCatalogue;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import helper.Constants;

public class StimulusAwareness extends SelfAwareness {

	/** The StimulusAwareness singleton instance. */
	private static StimulusAwareness stimulusAwareness;

	protected static AdaptationTacticsCatalogue adaptationTacticsCatalogue;
	protected List<AdaptationRule> lstAdaptationRules;
	
	private int datacenterId;
	private int datacenterBrokerId;
	private int selfExpressionId;
	private Datacenter datacenter;

	
	public StimulusAwareness(String name) {
		super(name);

		adaptationTacticsCatalogue = new AdaptationTacticsCatalogue();
		lstAdaptationRules = new ArrayList<AdaptationRule>();
	}

	/**
     * Create a static method to get instance.
    */
	public static StimulusAwareness getInstance() {
        if(stimulusAwareness == null){
        	stimulusAwareness = new StimulusAwareness("StimulusAwareness");
        }
        return stimulusAwareness;
	}

	@Override
	public void startEntity() {
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
			datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
			datacenterBrokerId = CloudSim.getEntityId("Broker");
			selfExpressionId = SelfExpressionComponent.getInstance().getId();
		
			//set the adaptation tactics catalogue
			adaptationTacticsCatalogue.LoadXMLAdaptationTactics();
			//set the adaptation rules
			lstAdaptationRules = loadXMLAdaptationRules();

			Log.printLine(getName() + " is starting...");		
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected List<AdaptationRule> loadXMLAdaptationRules() throws ParserConfigurationException, SAXException, IOException {
		List<AdaptationRule> lstAdaptationRules = new ArrayList<AdaptationRule>();
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
		return lstAdaptationRules;
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute adaptation decisions
		case CloudSimTags.SAW_STIMULUS_AWARENESS:
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
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] Running stimulus-awareness...");

			//detect goals violations
			Map<String,ArrayList<Double>> monitorData = new HashMap<String,ArrayList<Double>>();
			monitorData = (Map<String, ArrayList<Double>>) ev.getData();
			detectViolations(monitorData);

			// sort goals by weight
			RuntimeGoalsModel.getInstance().sortGoalsByWeight();
		
			AdaptationTactic adaptationDecision = null;
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
						if (adaptationDecision != null) {
							Log.printLine(CloudSim.clock() + ": [" + getName() + "] Adaptation Decision taken: "
								+ adaptationDecision.getDescription() + " for the violated goal " + g.getName());
							
							// get the parameters necessary for the adaptation decision
							List<Integer> parameters = new ArrayList<Integer>();
							parameters = getAdaptationTacticParameters(adaptationDecision);
							// send the adaptation decision to the self-expression 
							String[] evdata = new String[4];
							evdata[0] = adaptationDecision.getActionTag();
							for (int i=0; i<=parameters.size()-1; i++) {
								evdata[i+1] = Integer.toString(parameters.get(i));		
							}

							send(selfExpressionId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_SELF_EXPRESSION, evdata);

							// add adaptation history and overhead
							SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
							break;
						} else {
							Log.printLine(CloudSim.clock() + ": [" + getName() + "] No possible Adaptation Decision "
								+ " for the violated goal " + g.getName());
						}
					}
				}
			}			
			schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean detectViolations(Map<String, ArrayList<Double>> monitorData){
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Detecting runtime gaols violations...");

		boolean violationDetected = false;
		
		// sort goals by weight
		RuntimeGoalsModel.getInstance().sortGoalsByWeight();

		//compare with the list of goals in the GoalsModel
		for (RuntimeGoal g : RuntimeGoalsModel.getInstance().getGoals()) {
			//get list of values for each goal
			ArrayList<Double> values = new ArrayList<Double>();
			values = monitorData.get(g.getName());
			boolean isViolated = g.checkViolatonWithinThreshold(values);
			g.setViolated(isViolated);

			if (isViolated) {
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] Violations detected in " + g.getName() + ".");
				violationDetected = true;
			} else {
				Log.printLine(CloudSim.clock() + ": [" + getName() + "] No violations detected in " + g.getName() + ".");
			}
		}		
		// add adaptation overhead
		try {
			SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return violationDetected;
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
		
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
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
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
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
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
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
		return VMsPossibleScaling;	}
		
	protected List<Integer> getAdaptationTacticParameters(AdaptationTactic t) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<Integer> evdata = new ArrayList<Integer>();
		
		switch (t.getActionTag()) {
		case "VERTICAL_SCALING_CAP":
			evdata = getVerticalScalingCapParameters();
			break;
		case "VERTICAL_SCALING_NUM":
			evdata = getVerticalScalingNumParameters();
			break;
		case "VERTICAL_DESCALING_NUM":
			evdata = getVerticalDescalingNumParameters();
			break;
		case "HORIZONTAL_SCALING":
			evdata = getHorizontalScalingParameters();
			break;
		case "HORIZONTAL_DESCALING":
			evdata = getHorizontalDescalingNumParameters();
			break;
		case "VM_CONSOLIDATION":
			evdata = getVmConsolidationParameters();
			break;
		}
		
		return evdata;
	}
	
/*	private boolean checkVMsPossibleConsolidation() {
	return (((AdaptiveDatacenter) datacenter).getVmOnList().size() > 1) ? true : false;
}
*/
	
	private List<Integer> getVerticalScalingCapParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		List<Integer> parameters = new ArrayList<Integer>();
		boolean parametersFound = false;
		
		List<AdvVm> lstVMsSorted =  ((AwareDatacenter) datacenter).getVmOnList();
		lstVMsSorted.sort(Comparator.comparing(Vm::getNumberOfPes)
						.thenComparing(Comparator.comparing(Vm::getMips)));
		
		// loop through the list of VMs from the smallest VM in PES and MIPS
		//get the highest PES and MIPS to be scaled to within its host
		for (Vm vm : lstVMsSorted) {
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
			if (parametersFound) {
				break;		
			}
		}
		return parameters;
	}
	
	private List<Integer> getVerticalScalingNumParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		List<Integer> parameters = new ArrayList<Integer>();
		int hostId = -1;

		for (Host h : ((AwareDatacenter) datacenter).getHostOnList()) {
			for (Vm vm : h.getVmList()) {
				if (!((AdvVm) vm).isOn()) {
					hostId = h.getId();
				}
				if (hostId != -1) {
					break;
				}
			}
		}		

		if (hostId == -1) {	
			List<AdvHost> lstHostsSorted = ((AwareDatacenter) datacenter).getHostOnList();
			lstHostsSorted.sort(Comparator.comparing(Host::getNumberOfFreePes).reversed());
			for (Host h : lstHostsSorted) {
				for (int i=Constants.VM_TYPES-1; i >= 0; i--) {
					// create a temp vm object with the new type to check it
					Vm tempVm = new Vm(0, 0, i, Constants.VM_MIPS[i], Constants.VM_PES[i], 
							Constants.VM_RAM[i], Constants.VM_BW[i], Constants.VM_SIZE[i], 
							Constants.VM_MONITOR[i], new ServiceRequestSchedulerSpaceShared());
				
					// check if the new type could fit with the current host
					if (h.isSuitableForVm(tempVm)) {
						hostId = h.getId();
					}
					if (hostId != -1) {
						break;
					}
				}
				if (hostId != -1) {
					break;
				}
			}
		}

		parameters.add(hostId);
		parameters.add(datacenterBrokerId);	

		return parameters;
	}

	private List<Integer> getVerticalDescalingNumParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		List<Integer> parameters = new ArrayList<Integer>();

		List<AdvVm> lstVmsSorted = ((AwareDatacenter) datacenter).getVmOnList();
		lstVmsSorted.sort(Comparator.comparing(Vm::getNumberOfPes).reversed());
		
		// get the largest Vm type possible for descaling
		Vm vm = lstVmsSorted.get(0);

		parameters.add(vm.getId());				//id the vm to be removed
		parameters.add(vm.getHost().getId());	//id of the host
		parameters.add(vm.getUserId());			//datacenterBrokerId
		
		return parameters;
	}

	private List<Integer> getHorizontalScalingParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		
		List<Integer> parameters = new ArrayList<Integer>();
		
		int vmSchedulerType = 2;
						
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		parameters.add(vmSchedulerType);
		parameters.add(datacenterBrokerId);	//datacenterBrokerId
		
		return parameters;
	}
	
	private List<Integer> getHorizontalDescalingNumParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		
		List<Integer> parameters = new ArrayList<Integer>();

		List<AdvHost> lstHostsSorted = ((AwareDatacenter) datacenter).getHostOnList();
		lstHostsSorted.sort(Comparator.comparing(Host::getNumberOfPes).reversed());
		
		// get the largest host type possible for descaling
		Host host = lstHostsSorted.get(0);

		datacenterBrokerId = CloudSim.getEntityId("Broker");
		parameters.add(host.getId());			//id the host to be removed
		parameters.add(datacenterBrokerId);				//datacenterBrokerId
		
		return parameters;
	}

	private List<Integer> getVmConsolidationParameters() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		datacenterBrokerId = CloudSim.getEntityId("Broker");
		
		List<Integer> parameters = new ArrayList<Integer>();
		int hostId = 0;		//PM to be removed
		int NumOfVm = 0;
	
		List<AdvHost> lstHosts = ((AwareDatacenter) datacenter).getHostOnList();
		
		// get host with less VMs to be removed 
		for (Host h : lstHosts) {
			if (NumOfVm < ((AdvHost) h).getVmOnList().size()) {
				hostId = h.getId();
				NumOfVm = ((AdvHost) h).getVmOnList().size();
			}
		}
		parameters.add(hostId);
		parameters.add(datacenterBrokerId);

		return parameters;
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
