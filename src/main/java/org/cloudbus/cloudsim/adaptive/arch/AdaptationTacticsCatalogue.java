package org.cloudbus.cloudsim.adaptive.arch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cloudbus.cloudsim.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AdaptationTacticsCatalogue {
	
	private List<AdaptationTactic> lstAdaptationTactics = new ArrayList<AdaptationTactic>();

	
	public AdaptationTacticsCatalogue() {

	}

	public void LoadXMLAdaptationTactics() 
			throws ParserConfigurationException, SAXException, IOException {
		try {
			String workingDir = System.getProperty("user.dir");
			String inputFolder = workingDir + "//experiments//configurations//";
			String xmlFile = inputFolder + "adaptationTactics.xml";

			File inputFile = new File(xmlFile);
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         
	         // get root element <AdaptationTactics>
	         doc.getDocumentElement().normalize();
	         //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	         
	         // get Quality Attributes nodes
	         NodeList nList = doc.getElementsByTagName("Tactic");
	     	
	         String id;
	         String description;
	         String object;
	         String change;
	         int min = 0;
	         int max = 0;
	         String actionTag;

    		 for (int temp = 0; temp < nList.getLength(); temp++) {
	        	 Node nNode = nList.item(temp);
	        	 //System.out.println("\nCurrent Element :" + nNode.getNodeName());
	            
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {	            	
	        		 Element eElement = (Element) nNode;

	        		 id = eElement.getAttribute("id").toString();
	        		 description = eElement.getElementsByTagName("description").item(0).getTextContent();
	        		 object = eElement.getElementsByTagName("Object").item(0).getTextContent();
	        		 change = eElement.getElementsByTagName("change").item(0).getTextContent();
	        		 if (!eElement.getElementsByTagName("min").item(0).getTextContent().isEmpty()) {
	        			 min = Integer.parseInt(eElement.getElementsByTagName("min").item(0).getTextContent());
	        		 }
	        		 if (!eElement.getElementsByTagName("max").item(0).getTextContent().isEmpty()) {
	        			 max = Integer.parseInt(eElement.getElementsByTagName("max").item(0).getTextContent());
	        		 }       		 
	        		 actionTag = eElement.getElementsByTagName("tag").item(0).getTextContent();
	               
	        		 AdaptationTactic t = new AdaptationTactic(id, description, object, change, min, max, actionTag);
	        		 lstAdaptationTactics.add(t);	            
	        	}
    		 }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AdaptationTactic getTacticByTag(String tag) {
		//lstAdaptationTactics.forEach((item)->{if(item.getActionTag().equals(tag)) return item;});
		for (AdaptationTactic t : lstAdaptationTactics) {
			if (t.getActionTag().equals(tag)) {
				return t;
			}
		}
		return null;
	}

	public List<AdaptationTactic> getListOfAdaptationTactics() {
		return lstAdaptationTactics;
	}

	public void setListOfAdaptationTactics(List<AdaptationTactic> lstAdaptationTactics) {
		this.lstAdaptationTactics = lstAdaptationTactics;
	}



}
