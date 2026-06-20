package org.cloudbus.cloudsim.aware.goal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cloudbus.cloudsim.adaptive.goal.Goal;
import org.cloudbus.cloudsim.adaptive.goal.GoalModelling;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RuntimeGoalsModel implements GoalModelling {

	/** The GoalsModel singleton instance. */
	private static RuntimeGoalsModel runtimeGoalsModel;

	private List<RuntimeGoal> lstRuntimeGoals = new ArrayList<RuntimeGoal>();
	
	
	protected RuntimeGoalsModel() {

	}

	/**
     * Create a static method to get instance.
    */
    public static RuntimeGoalsModel getInstance(){
        if(runtimeGoalsModel == null){
        	runtimeGoalsModel = new RuntimeGoalsModel();
        }
        return runtimeGoalsModel;
    }
    
	public void LoadXMLGoals(String xmlFile) 
			throws ParserConfigurationException, SAXException, IOException {
		try {
	         File inputFile = new File(xmlFile);
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         
	         // get root element <QoSGoals>
	         doc.getDocumentElement().normalize();
	         //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	         
	         // get Quality Attributes nodes
	         NodeList nList = doc.getElementsByTagName("QualityAttribute");
	         
	         String id;
	         String name;
	         double constraintValue;	
	         String metric;
	         boolean isMin;
	         double weight;
	         double violationThreshold;

	         for (int temp = 0; temp < nList.getLength(); temp++) {
	        	 Node nNode = nList.item(temp);
	        	 //System.out.println("\nCurrent Element :" + nNode.getNodeName());
	            
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {	            	
	        		 Element eElement = (Element) nNode;

	        		 id = eElement.getAttribute("id").toString();
	        		 name = eElement.getElementsByTagName("name").item(0).getTextContent();
	        		 constraintValue = Double.parseDouble(eElement.getElementsByTagName("constraintValue").item(0).getTextContent());
	        		 metric = eElement.getElementsByTagName("metric").item(0).getTextContent();
	        		 isMin = Boolean.parseBoolean(eElement.getElementsByTagName("isMin").item(0).getTextContent());
	        		 weight = Double.parseDouble(eElement.getElementsByTagName("weight").item(0).getTextContent());
	        		 violationThreshold = Double.parseDouble(eElement.getElementsByTagName("threshold").item(0).getTextContent());
        		 
	        		 RuntimeGoal g = new RuntimeGoal(id, name, constraintValue, metric, isMin, weight, violationThreshold);
	        		 lstRuntimeGoals.add(g);	            
	        	 }
	         }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sortGoalsByWeight() {
		Comparator<Goal> c = Comparator.comparing(Goal::getWeight);
		lstRuntimeGoals.sort(c);
	}

	public List<? extends RuntimeGoal> getGoals() {
		return lstRuntimeGoals;
	}

	@SuppressWarnings("unchecked")
	public void setGoals(List<? extends Goal> goals) {
		this.lstRuntimeGoals = (List<RuntimeGoal>) goals;
	}

	public int countGoals() {
		return lstRuntimeGoals.size();
	}

	public int countViolatedGoals() {
		int violatedGoals = 0;
		
		for (RuntimeGoal g : lstRuntimeGoals) {
			if (g.isViolated()) {
				violatedGoals++;
			}
		}
		return violatedGoals;
	}

	public List<RuntimeGoal> getViolatedGoals() {
		List<RuntimeGoal> lstViolatedGoals = new ArrayList<RuntimeGoal>();
		
		for (RuntimeGoal g : lstRuntimeGoals) {
			if (g.isViolated()) {
				lstViolatedGoals.add(g);
			}
		}
		return lstViolatedGoals;
	}

	public RuntimeGoal getGoalById(String id) {
		for (RuntimeGoal g : lstRuntimeGoals) {
			if (g.getName().equals(id)) {
				return g;
			}
		}
		return null;
	}	
	
	public RuntimeGoal getGoalByName(String name) {
		for (RuntimeGoal g : lstRuntimeGoals) {
			if (g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}
		
	
}
