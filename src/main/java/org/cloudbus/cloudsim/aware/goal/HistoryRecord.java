package org.cloudbus.cloudsim.aware.goal;

import org.cloudbus.cloudsim.adaptive.arch.AdaptationTactic;

public class HistoryRecord {
	
	protected double time;
	protected double averageValue;
	protected String tacticExecuted;
	protected double averageValueAfterAdaptation;

	
	public HistoryRecord() {
	}

	public HistoryRecord(double time, double averageValue, String tacticExecuted,
			double averageValueAfterAdaptation) {
		this.time = time;
		this.averageValue = averageValue;
		this.tacticExecuted = tacticExecuted;
		this.averageValueAfterAdaptation = averageValueAfterAdaptation;
	}
	
	
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getAverageValue() {
		return averageValue;
	}
	public void setAverageValue(double averageValue) {
		this.averageValue = averageValue;
	}
	public String getTacticExecuted() {
		return tacticExecuted;
	}
	public void setTacticExecuted(String tacticExecuted) {
		this.tacticExecuted = tacticExecuted;
	}
	public double getAverageValueAfterAdaptation() {
		return averageValueAfterAdaptation;
	}
	public void setAverageValueAfterAdaptation(double averageValueAfterAdaptation) {
		this.averageValueAfterAdaptation = averageValueAfterAdaptation;
	}

}
