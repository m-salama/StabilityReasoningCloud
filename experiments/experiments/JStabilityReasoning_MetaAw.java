package experiments;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adaptive.AdaptiveDynamicDatacenterBroker;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.Service;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.aware.AwareDatacenter;
import org.cloudbus.cloudsim.aware.AwareDynamicDatacenterBroker;
import org.cloudbus.cloudsim.aware.goal.RuntimeGoalsModel;
import org.cloudbus.cloudsim.core.CloudSim;

import helper.Results;

/*
 * Title:        Aw-CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import helper.Setup;
import helper.Workload;

/**
 * A simple example using non-aware components and run cloudlets (service) from workload.
 */

public class JStabilityReasoning_MetaAw {

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

	    try {
			String workingDir = System.getProperty("user.dir");
			String inputFolder = workingDir + "//experiments//configurations//";
			String inputFolder_workload = workingDir + "//experiments//workload//";
			String resultsFolder = workingDir + "//experiments//results//";

			OutputStream output = new FileOutputStream(resultsFolder + "JStabilityReasoning_MetaAw_S4_output.txt");
			Log.setOutput(output);
			
			Log.printLine("Starting JStabilityReasoning_MetaAw Experiment...");
			Log.printLine("running self-aware architecture (meta-awareness, game for managing trade-offs) ");
			Log.printLine("on RUBis and WorldCup1998 workload");
			Log.printLine("for service type #4");

		    // First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);
			
			//List<Host> hostList = new ArrayList<Host>();
			List<AdvHost> hostList = new ArrayList<AdvHost>();
			hostList = Setup.createAdvHostList(10, 3, 2);  //2 = VmSchedulerTimeShared

			// Second step: Create Datacenters
			AwareDatacenter datacenter0 = Setup.createAwareDatacenter("Datacenter_0", hostList);

			// SAd-CloudSim: Create Adaptation Goals Model

			
			// SAw-CloudSim: Create QoS Runtime Goals Model
			String file = inputFolder + "QoSRuntimeGoals.xml";
			RuntimeGoalsModel runtimeGoalsModel = RuntimeGoalsModel.getInstance();
			runtimeGoalsModel.LoadXMLGoals(file);

			// Third step: Create Broker
			//AdaptiveDynamicDatacenterBorker broker = Setup.createAdaptiveDynamicBroker();
			AwareDynamicDatacenterBroker broker = Setup.createAwareDynamicDatacenterBorker();
			int brokerId = broker.getId();
			
			// Fourth step: Create one virtual machine
			List<? extends Vm> vmlist = new ArrayList<AdvVm>();
			//vmlist = HelperDatacenter.createAwareVmList(brokerId, 9, 9, 1);		//1 = CloudletSchedulerSpaceShared
			int[] vmType = {0,1,2};
			int[] vmsNumber = {5,5,5};
			vmlist = Setup.createHeterogenousAdvVmList(brokerId, vmsNumber, vmType, 1);

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			int serviceType = 4;
			int serviceId = 1;
			Service service = Workload.createService(serviceId, serviceType);
			
			// Fifth step: Create Cloudlets
			UtilizationModel utilizationModel = new UtilizationModelFull();
			List<ServiceRequest> cloudletList = new ArrayList<ServiceRequest>();

			cloudletList = Workload.generateWorkloadRuntime(inputFolder_workload 
							+ "worldcup98//" + "workload_worldcup98_minimal.txt", brokerId, serviceId, serviceType, utilizationModel);

			// submit cloudlet list to the broker
			broker.submitServiceRequestList(cloudletList);
			
			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			CloudSim.stopSimulation();
			
			//Final step: Print results when simulation is over
			List<ServiceRequest> cloudletSubmittedList = broker.getServiceRequestSubmittedList();
			List<ServiceRequest> cloudletReceivedList = broker.getServiceRequestReceivedList();			
			Results.printServiceRequestResults(cloudletSubmittedList, cloudletReceivedList, broker.getVmList());

			Results.printExperimentIntervalResults(cloudletReceivedList, lastClock, broker.getVmList(), datacenter0);

			Results.printDatacenterResults(datacenter0, broker.getVmList(), lastClock);			
			Results.printSelfAwarenessResults(datacenter0);
			
			Results.printExperimentTotalResults(cloudletReceivedList, lastClock, broker.getVmList(), datacenter0);

			Log.printLine();
			Log.printLine("Experiment finished!");	
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}


}