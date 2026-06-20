package org.cloudbus.cloudsim.aware.arch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SelfAwarenessComponent extends SimEntity {

	/** The SelfAwarenessComponent singleton instance. */
	private static SelfAwarenessComponent selfAwarenessComponent;

	/** Configuration of different self-awareness capabilities. */	
	private Map<String, Boolean> selfAwarenessConfiguration;
	
	/** Configuration of different self-awareness capabilities. */	
	private ArrayList<SelfAwareness> listSelfAwarenessComponents;	

	/** The self-aware architecture components. */
	protected StimulusAwareness stimulusAwareness;
	protected GoalAwareness goalAwareness;
	protected TimeAwareness timeAwareness;
	protected InteractionAwareness interactionAwareness;
	protected MetaSelfAwareness metaSelfAwareness;
		
	
	private int datacenterId;


	public SelfAwarenessComponent(String name) {
		super(name);
		
		selfAwarenessConfiguration = new HashMap<String, Boolean>();
		
		stimulusAwareness = StimulusAwareness.getInstance();
		goalAwareness = GoalAwareness.getInstance();
		timeAwareness = TimeAwareness.getInstance();
		interactionAwareness = InteractionAwareness.getInstance();
		metaSelfAwareness = MetaSelfAwareness.getInstance();
		
		listSelfAwarenessComponents = new ArrayList<SelfAwareness>();
		
		listSelfAwarenessComponents.add(stimulusAwareness);
		listSelfAwarenessComponents.add(goalAwareness);
		listSelfAwarenessComponents.add(timeAwareness);
		listSelfAwarenessComponents.add(interactionAwareness);
		listSelfAwarenessComponents.add(metaSelfAwareness);
	
		//load configuration from xml 
		loadConfiguration();
		//set configuration as loaded from configuration
		for (SelfAwareness component : listSelfAwarenessComponents){
			boolean isActive = selfAwarenessConfiguration.get(component.getName());
			component.setActive(isActive);
		}
	}

	private void loadConfiguration() {
		try {
			String workingDir = System.getProperty("user.dir");
			String inputFolder = workingDir + "//experiments//configurations//";
			String xmlFile = inputFolder + "configSelfAwareness.xml";

			File inputFile = new File(xmlFile);
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         
	         // get root element <SelfAwareness>
	         doc.getDocumentElement().normalize();
	         //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	         
	         // get SelfAwareness components
	         NodeList nList = doc.getElementsByTagName("Component");
	     	
	         String id;
	         String name;
	         String className;
	         boolean isActive;

    		 for (int temp = 0; temp < nList.getLength(); temp++) {
	        	 Node nNode = nList.item(temp);
	        	 //System.out.println("\nCurrent Element :" + nNode.getNodeName());
	            
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {	            	
	        		 Element eElement = (Element) nNode;

	        		 id = eElement.getAttribute("id").toString();
	        		 name = eElement.getElementsByTagName("name").item(0).getTextContent();
	        		 className = eElement.getElementsByTagName("className").item(0).getTextContent();
	        		 isActive =  Boolean.parseBoolean(eElement.getElementsByTagName("isActive").item(0).getTextContent());

	        		 selfAwarenessConfiguration.put(className, isActive);
	        	}
    		 }
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
     * Create a static method to get instance.
    */
	public static SelfAwarenessComponent getInstance() {
        if(selfAwarenessComponent == null){
        	selfAwarenessComponent = new SelfAwarenessComponent("SelfAwarenessComponent");
        }
        return selfAwarenessComponent;
	}

	@Override
	public void startEntity() {
		try {
			datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();

			Log.printLine(getName() + " is starting...");		
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
			case CloudSimTags.SAW_SELF_AWARENESS:
			act(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}		
	}

	public void act(SimEvent ev) {
		try {
		datacenterId = SelfAwareArchitecture.getInstance().getDatacenterId();
		
		Log.printLine();
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] Making adaptation decision using different awareness components...");

		if (goalAwareness.isActive()) {
			send(goalAwareness.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_GOAL_AWARENESS, ev.getData());
		} else if (timeAwareness.isActive()) {
			send(timeAwareness.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_TIME_AWARENESS, ev.getData());
		} else if (interactionAwareness.isActive()) {
			send(interactionAwareness.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_INTERACTION_AWARENESS, ev.getData());
		} else {
			send(stimulusAwareness.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.SAW_STIMULUS_AWARENESS, ev.getData());
		}
		
		SelfAwareArchitecture.getInstance().accumulateAdaptationOverhead(CloudSim.getMinTimeBetweenEvents());
		schedule(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_DATACENTER_EVENT);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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



}
