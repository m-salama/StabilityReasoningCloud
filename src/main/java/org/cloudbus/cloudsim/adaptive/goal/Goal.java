package org.cloudbus.cloudsim.adaptive.goal;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;

public class Goal {
	
	protected String Id;
	protected String name;
	protected double constraintValue;	
	protected String metric;
	protected boolean isMin;	//if the objective is to minimise (e.g. energy consumption)
	protected double weight;
	protected boolean isViolated;

	protected Goal(){
		
	}
	
	public Goal(String id, String name, double constraint, String metric, boolean isMin) {
		this.Id = id;
		this.name = name;
		this.constraintValue = constraint;
		this.metric = metric;
		this.isMin = isMin;
	}
	
	public Goal(String id, String name, double constraint, String metric, boolean isMin, double weight) {
		this.Id = id;
		this.name = name;
		this.constraintValue = constraint;
		this.isMin = isMin;
		this.weight = weight;
	}
	
	public boolean checkViolaton(ArrayList<Double> values) {
		return isMin? constraintValue < getAverage(values) : constraintValue > getAverage(values);
	}
	
	private double getAverage(ArrayList<Double> values) {
		double result = 0.0;

		for (double v : values) {
			result += v;
		}
		
		return result/values.size();
	}

		public String getId() {
		return Id;
	}
	
	public void setId(String id) {
		this.Id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getConstraintValue() {
		return constraintValue;
	}
	
	public void setconstraintValue(double constraint) {
		this.constraintValue = constraint;
	}
	
	public boolean isMin() {
		return isMin;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public boolean isViolated() {
		return isViolated;
	}

	public void setViolated(boolean value) {
		this.isViolated = value;
	}


}
