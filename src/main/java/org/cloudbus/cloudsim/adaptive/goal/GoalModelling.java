package org.cloudbus.cloudsim.adaptive.goal;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface GoalModelling {

	void LoadXMLGoals(String xmlFile) 
			throws ParserConfigurationException, SAXException, IOException;
	
	void sortGoalsByWeight();
	
	List<? extends Goal> getGoals();
	
	void setGoals(List<? extends Goal> goals);
	
	int countGoals();
	
	
}