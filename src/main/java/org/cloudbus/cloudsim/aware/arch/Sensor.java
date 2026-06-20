package org.cloudbus.cloudsim.aware.arch;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.core.CloudSim;

public class Sensor {
	
	private String QualityAttribute;
	private ArrayList<Double> sensorData;
	
	
	public Sensor() {
		
	}
	
	public Sensor(String qualityAttribute) {
		this.QualityAttribute = qualityAttribute;
		this.sensorData = new ArrayList<Double>();
	}

	public void collectMonitoringData() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		List<ServiceRequest> rlist = new ArrayList<ServiceRequest>();

		//get Ids of related datacenter and broker
		Datacenter datacenter = (Datacenter) CloudSim.getEntity(SelfAwareArchitecture.getInstance().getDatacenterId());
		//DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity(datacenter.getVmList().get(0).getUserId());
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity("Broker");
		
		//get list of completed requests from datacenterBroker
		rlist = broker.getServiceRequestReceivedList();
		
		double totalThroughput = 0.0;
		double totalCost = datacenter.getOperationalCost();
		double energy = datacenter.getPower() / (3600 * 1000);
		
		for (ServiceRequest r : rlist) {
			if (r.getFinishTime() > (CloudSim.clock() - helper.Constants.MONITORING_INTERVAL)  
				&& (r.getServiceRequestStatus() == ServiceRequest.SUCCESS)) {
				if (this.QualityAttribute.equals("ResponseTime")) {
					this.sensorData.add(r.getResponseTime());
				}
				totalThroughput++;
			}
		}
		switch (this.QualityAttribute) {
		case "Throughput":
			this.sensorData.add(totalThroughput/helper.Constants.MONITORING_INTERVAL);
			break;
		case "OperationalCost":
			this.sensorData.add(totalCost);
			break;
		case "EnergyConsumption":
			this.sensorData.add(energy);
			break;
		}
	}

	public void clearMonitoringData() {
		sensorData.clear();
	}

	public String getQualityAttribute() {
		return QualityAttribute;
	}

	public void setQualityAttribute(String qualityAttribute) {
		QualityAttribute = qualityAttribute;
	}

	public ArrayList<Double> getSensorData() {
		return sensorData;
	}

	public void setSensorData(ArrayList<Double> sensorData) {
		this.sensorData = sensorData;
	}

	
}
