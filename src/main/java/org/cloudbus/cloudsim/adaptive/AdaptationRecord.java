package org.cloudbus.cloudsim.adaptive;

import org.cloudbus.cloudsim.core.CloudSim;

public class AdaptationRecord {
	
	private double adaptationTime;
	private String adaptationTag;
	private int hostNo;
	private int vmNo;

	
	public AdaptationRecord() {

	}

	public AdaptationRecord(double adaptationTime, String adaptationTag, int hostNo, int vmNo) {
		this.adaptationTime = adaptationTime;
		this.adaptationTag = adaptationTag;
		this.hostNo = hostNo;
		this.vmNo = vmNo;
	}
	
	public double getAdaptationTime() {
		return adaptationTime;
	}

	public void setAdaptationTime(double adaptationTime) {
		this.adaptationTime = adaptationTime;
	}

	public String getAdaptationTag() {
		return adaptationTag;
	}

	public void setAdaptationTag(String adaptationTag) {
		this.adaptationTag = adaptationTag;
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
		

}
