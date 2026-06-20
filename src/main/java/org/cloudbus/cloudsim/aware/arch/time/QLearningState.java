package org.cloudbus.cloudsim.aware.arch.time;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.aware.goal.RuntimeGoal;

public class QLearningState {
	
	private int Id;
	private Map<RuntimeGoal,Double> data;
	
	private double ResponseTime;
	private double EnergyConsumption;
	private double OperationalCost;

	
	public QLearningState() {
		this.data = (new HashMap<RuntimeGoal,Double>());
	}
	
	public QLearningState(int id, Map<RuntimeGoal, Double> data) {
		super();
		this.Id = id;		
		this.data = data;
		
		//ResponseTime = responseTime; //range from [0-15] anything above 15 will be a direct violation 16
		//EnergyConsumption = energyConsumption; //range [0-10,10-20,20-30,30-40,40-50] 5
		//OperationalCost = operationalCost; //range [0-100,100-200,...,900-1000] 10
	}
	
	public Boolean equals(QLearningState s){
		return (s.ResponseTime == this.ResponseTime 
				&& s.EnergyConsumption == this.EnergyConsumption 
				&& s.OperationalCost == this.OperationalCost);
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}
	public double getResponseTime() {
		return ResponseTime;
	}
	public void setResponseTime(double responseTime) {
		ResponseTime = responseTime;
	}
	public double getEnergyConsumption() {
		return EnergyConsumption;
	}
	public void setEnergyConsumption(double energyConsumption) {
		EnergyConsumption = energyConsumption;
	}
	public double getOperationalCost() {
		return OperationalCost;
	}
	public void setOperationalCost(double operationalCost) {
		OperationalCost = operationalCost;
	}
	public Map<RuntimeGoal,Double> getData() {
		return data;
	}
	public void setData(Map<RuntimeGoal,Double> data) {
		this.data = data;
	}
	
}
