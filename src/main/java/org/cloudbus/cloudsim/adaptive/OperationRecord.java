package org.cloudbus.cloudsim.adaptive;

import helper.Constants;

public class OperationRecord {
	
	private double operationTime;
	private int hostNo;
	private int vmNo;
	private double cost;
	private double power;

	
	public OperationRecord() {

	}

	public OperationRecord(double operationTime, int hostNo, int vmNo, double cost, double power) {
		this.operationTime = operationTime;
		this.hostNo = hostNo;
		this.vmNo = vmNo;
		this.cost = cost;
		this.power = power;
	}
	
	public double getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(double operationtime) {
		this.operationTime = operationtime;
	}

	public int getHostNo() {
		return hostNo;
	}

	public void setHostNo(int hostNo) {
		this.hostNo = hostNo;
	}

	public int getVmNo() {
		return vmNo;
	}

	public void setVmNo(int vmNo) {
		this.vmNo = vmNo;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double power) {
		this.power = power;
	}

	
}
