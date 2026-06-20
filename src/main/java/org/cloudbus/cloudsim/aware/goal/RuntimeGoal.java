package org.cloudbus.cloudsim.aware.goal;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adaptive.goal.Goal;

public class RuntimeGoal extends Goal {
	
	/** The threshold for violation to take pro-active adaptation (in %). */
	protected double violationThreshold;

	/** The user/SLA of this goal. */
	protected Integer userId;
	
	/** The history of this goal. */
	protected ArrayList<HistoryRecord> history;
	
		
	public RuntimeGoal() {
		super();
	}

	public RuntimeGoal(String id, String name, double constraint, String metric, boolean isMin,
			double violationThreshold) {
		super(id, name, constraint, metric, isMin);
		this.violationThreshold = violationThreshold;
	}
	
	public RuntimeGoal(
			String id, String name, double constraint, String metric, boolean isMin, double weight,
			double violationThreshold) {
		super(id, name, constraint, metric, isMin, weight);
		this.history = new ArrayList<HistoryRecord>();
		this.violationThreshold = violationThreshold;
	}
	
	public RuntimeGoal(String id, String name, double constraint, String metric, boolean isMin,
			double violationThreshold, int userId) {
		super(id, name, constraint, metric, isMin);
		this.history = new ArrayList<HistoryRecord>();
		this.violationThreshold = violationThreshold;
		this.userId = userId;
	}
	
	public RuntimeGoal(
			String id, String name, double constraint, String metric, boolean isMin, double weight,
			double violationThreshold, int userId) {
		super(id, name, constraint, metric, isMin, weight);
		this.history = new ArrayList<HistoryRecord>();
		this.violationThreshold = violationThreshold;
		this.userId = userId;
	}
	
	public boolean checkViolatonWithinThreshold(ArrayList<Double> values) {
		return isMin? getAverage(values) > (constraintValue - (constraintValue * violationThreshold/100)) : 
			getAverage(values) < (constraintValue + (constraintValue * violationThreshold/100));
	}
	
	public void addHistoryRecord(double time, ArrayList<Double> values) {
		HistoryRecord record = new HistoryRecord();
		record.setTime(time);
		record.setAverageValue(getAverage(values));
		history.add(record);
	}
	
	private double getAverage(ArrayList<Double> values) {
		double result = 0.0;

		for (double v : values) {
			result += v;
		}		
		return result/values.size();
	}

	public double getViolationThreshold() {
		return violationThreshold;
	}
	
	public void setViolationThreshold(double violationThreshold) {
		this.violationThreshold = violationThreshold;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public ArrayList<HistoryRecord> getHistory() {
		return history;
	}
	
	public void setHistoryId(ArrayList<HistoryRecord> history) {
		this.history = history;
	}
	
}
