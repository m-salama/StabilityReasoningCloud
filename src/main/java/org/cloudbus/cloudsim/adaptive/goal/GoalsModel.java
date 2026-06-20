package org.cloudbus.cloudsim.adaptive.goal;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.cloudbus.cloudsim.adaptive.arch.Monitor;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class GoalsModel implements GoalModelling {
	
	/** The GoalsModel singleton instance. */
	private static GoalsModel goalsModel;

	private List<Goal> lstGoals = new ArrayList<Goal>();
	
	
	protected GoalsModel() {

	}

	/**
     * Create a static method to get instance.
    */
    public static GoalsModel getInstance(){
        if(goalsModel == null){
        	goalsModel = new GoalsModel();
        }
        return goalsModel;
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
	               
	        		 Goal g = new Goal(id, name, constraintValue, metric, isMin, weight);
	        		 lstGoals.add(g);	            
	        	}
    		 }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sortGoalsByWeight() {
		Comparator<Goal> c = Comparator.comparing(Goal::getWeight);
		lstGoals.sort(c);
	}
	
	public List<? extends Goal> getGoals() {
		return lstGoals;
	}

	@SuppressWarnings("unchecked")
	public void setGoals(List<? extends Goal> goals) {
		this.lstGoals = (List<Goal>) goals;
	}
	
	public int countGoals() {
		return lstGoals.size();
	}

	public int countViolatedGoals() {
		int violatedGoals = 0;
		
		for (Goal g : lstGoals) {
			if (g.isViolated()) {
				violatedGoals++;
			}
		}
		return violatedGoals;
	}

	public List<Goal> getViolatedGoals() {
		List<Goal> lstViolatedGoals = new ArrayList<Goal>();
		
		for (Goal g : lstGoals) {
			if (g.isViolated()) {
				lstViolatedGoals.add(g);
			}
		}
		return lstViolatedGoals;
	}

	public Goal getGoalById(String id) {
		for (Goal g : lstGoals) {
			if (g.getName().equals(id)) {
				return g;
			}
		}
		return null;
	}	
	
	public Goal getGoalByName(String name) {
		for (Goal g : lstGoals) {
			if (g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}

	
}
