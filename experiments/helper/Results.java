/**
 * 
 */
package helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.adaptive.AdaptationRecord;
import org.cloudbus.cloudsim.adaptive.AdaptiveDatacenter;
import org.cloudbus.cloudsim.adaptive.arch.SelfAdaptiveArchitecture;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.aware.AwareDatacenter;
import org.cloudbus.cloudsim.aware.arch.SelfAwareArchitecture;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * @author mxs512
 *
 */
public class Results {
	
	
	public static void printResultsInCsv(
			PowerDatacenter datacenter,
			List<Vm> vms,
			double lastClock,
			String experimentName,
			boolean outputInCsv,
			String outputFolder){
		
/*		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		File folder1 = new File(outputFolder + "/stats");
		if (!folder1.exists()) {
			folder1.mkdir();
		}
		File folder2 = new File(outputFolder + "/time_before_host_shutdown");
		if (!folder2.exists()) {
			folder2.mkdir();
		}
		File folder3 = new File(outputFolder + "/time_before_vm_migration");
		if (!folder3.exists()) {
			folder3.mkdir();
		}
		File folder4 = new File(outputFolder + "/metrics");
		if (!folder4.exists()) {
			folder4.mkdir();
		}

		StringBuilder data = new StringBuilder();
		String delimeter = ",";

		data.append(experimentName + delimeter);
		data.append(parseExperimentName(experimentName));
		data.append(String.format("%d", numberOfHosts) + delimeter);
		data.append(String.format("%d", numberOfVms) + delimeter);
		data.append(String.format("%.2f", totalSimulationTime) + delimeter);
		data.append(String.format("%.5f", energy) + delimeter);
		data.append(String.format("%d", numberOfMigrations) + delimeter);
		data.append(String.format("%.10f", sla) + delimeter);
		data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
		data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
		data.append(String.format("%.10f", slaOverall) + delimeter);
		data.append(String.format("%.10f", slaAverage) + delimeter);
		// data.append(String.format("%.5f", slaTimePerVmWithMigration) + delimeter);
		// data.append(String.format("%.5f", slaTimePerVmWithoutMigration) + delimeter);
		// data.append(String.format("%.5f", slaTimePerHost) + delimeter);
		data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
		data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
		data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
		data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
		data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

		if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
					.getVmAllocationPolicy();

			double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryVmSelection());
			double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryVmSelection());
			double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryHostSelection());
			double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryHostSelection());
			double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryVmReallocation());
			double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryVmReallocation());
			double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryTotal());
			double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryTotal());

			data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
			data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
			data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
			data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
			data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
			data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
			data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
			data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

			writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName
					+ "_metric");
		}

		data.append("\n");

		writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
		writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/"
				+ experimentName + "_time_before_host_shutdown.csv");
		writeDataColumn(timeBeforeVmMigration, outputFolder + "/time_before_vm_migration/"
				+ experimentName + "_time_before_vm_migration.csv");
*/	}


	/**
	 * Prints the results Power Data center.
	 * 
	 * @param datacenter the powerdatacenter
	 * @param lastClock the last clock
	 * @param experimentName the experiment name
	 * @param outputInCsv the output in csv
	 * @param outputFolder the output folder
	 */
/*	public static void printResults(
			PowerDatacenter datacenter,
			List<Vm> vms,
			double lastClock,
			String experimentName,
			boolean outputInCsv,
			String outputFolder) {
		Log.enable();
		List<Host> hosts = datacenter.getHostList();

		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);
		int numberOfMigrations = datacenter.getMigrationCount();

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		// double slaTimePerVmWithMigration = slaMetrics.get("sla_time_per_vm_with_migration");
		// double slaTimePerVmWithoutMigration =
		// slaMetrics.get("sla_time_per_vm_without_migration");
		// double slaTimePerHost = getSlaTimePerHost(hosts);
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

		int numberOfHostShutdowns = timeBeforeHostShutdown.size();

		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}

		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}

		if (outputInCsv) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File folder1 = new File(outputFolder + "/stats");
			if (!folder1.exists()) {
				folder1.mkdir();
			}
			File folder2 = new File(outputFolder + "/time_before_host_shutdown");
			if (!folder2.exists()) {
				folder2.mkdir();
			}
			File folder3 = new File(outputFolder + "/time_before_vm_migration");
			if (!folder3.exists()) {
				folder3.mkdir();
			}
			File folder4 = new File(outputFolder + "/metrics");
			if (!folder4.exists()) {
				folder4.mkdir();
			}

			StringBuilder data = new StringBuilder();
			String delimeter = ",";

			data.append(experimentName + delimeter);
			data.append(parseExperimentName(experimentName));
			data.append(String.format("%d", numberOfHosts) + delimeter);
			data.append(String.format("%d", numberOfVms) + delimeter);
			data.append(String.format("%.2f", totalSimulationTime) + delimeter);
			data.append(String.format("%.5f", energy) + delimeter);
			data.append(String.format("%d", numberOfMigrations) + delimeter);
			data.append(String.format("%.10f", sla) + delimeter);
			data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
			data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
			data.append(String.format("%.10f", slaOverall) + delimeter);
			data.append(String.format("%.10f", slaAverage) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithoutMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerHost) + delimeter);
			data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());

				data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

				writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName
						+ "_metric");
			}

			data.append("\n");

			writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
			writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/"
					+ experimentName + "_time_before_host_shutdown.csv");
			writeDataColumn(timeBeforeVmMigration, outputFolder + "/time_before_vm_migration/"
					+ experimentName + "_time_before_vm_migration.csv");

		} else {
			Log.setDisabled(false);
			Log.printLine();
			Log.printLine(String.format("Experiment name: " + experimentName));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
			Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
			Log.printLine(String.format("SLA: %.5f%%", sla * 100));
			Log.printLine(String.format(
					"SLA perf degradation due to migration: %.2f%%",
					slaDegradationDueToMigration * 100));
			Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
			Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
			Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
			// Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
			// slaTimePerVmWithMigration * 100));
			// Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
			// slaTimePerVmWithoutMigration * 100));
			// Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost * 100));
			Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
			Log.printLine(String.format(
					"Mean time before a host shutdown: %.2f sec",
					meanTimeBeforeHostShutdown));
			Log.printLine(String.format(
					"StDev time before a host shutdown: %.2f sec",
					stDevTimeBeforeHostShutdown));
			Log.printLine(String.format(
					"Mean time before a VM migration: %.2f sec",
					meanTimeBeforeVmMigration));
			Log.printLine(String.format(
					"StDev time before a VM migration: %.2f sec",
					stDevTimeBeforeVmMigration));

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());

				Log.printLine(String.format(
						"Execution time - VM selection mean: %.5f sec",
						executionTimeVmSelectionMean));
				Log.printLine(String.format(
						"Execution time - VM selection stDev: %.5f sec",
						executionTimeVmSelectionStDev));
				Log.printLine(String.format(
						"Execution time - host selection mean: %.5f sec",
						executionTimeHostSelectionMean));
				Log.printLine(String.format(
						"Execution time - host selection stDev: %.5f sec",
						executionTimeHostSelectionStDev));
				Log.printLine(String.format(
						"Execution time - VM reallocation mean: %.5f sec",
						executionTimeVmReallocationMean));
				Log.printLine(String.format(
						"Execution time - VM reallocation stDev: %.5f sec",
						executionTimeVmReallocationStDev));
				Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
				Log.printLine(String
						.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
			}
			Log.printLine();
		}

		Log.setDisabled(true);
	}*/

	public static void printSlaMetrics(
			Datacenter datacenter,
			List<PowerVm> vms)
	{
		String indent = "    ";
		Log.printLine();
		
		Log.enable();
		List<Host> hosts = datacenter.getHostList();

		Map<String, Double> slaMetrics = getSlaMetrics(vms);
		
		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
	
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);
	
		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;
	
		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);
	
		int numberOfHostShutdowns = timeBeforeHostShutdown.size();
	
		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}
	
		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}
		
		Log.printLine(String.format("SLA: %.5f%%", sla * 100));
		Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
		Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
		Log.printLine(String.format("SLA perf degradation due to migration: %.2f%%", slaDegradationDueToMigration * 100));
		Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
		Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
		Log.printLine(String.format("Mean time before a host shutdown: %.2f sec", meanTimeBeforeHostShutdown));
		Log.printLine(String.format("StDev time before a host shutdown: %.2f sec", stDevTimeBeforeHostShutdown));
		Log.printLine(String.format("Mean time before a VM migration: %.2f sec", meanTimeBeforeVmMigration));
		Log.printLine(String.format("StDev time before a VM migration: %.2f sec", stDevTimeBeforeVmMigration));

		Log.printLine();

	}
	
	/**
	 * Gets the times before host shutdown.
	 * 
	 * @param hosts the hosts
	 * @return the times before host shutdown
	 */
	public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
		List<Double> timeBeforeShutdown = new LinkedList<Double>();
		for (Host host : hosts) {
			boolean previousIsActive = true;
			double lastTimeSwitchedOn = 0;
			for (HostStateHistoryEntry entry : ((AdvHost) host).getStateHistory()) {
				if (previousIsActive == true && entry.isActive() == false) {
					timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
				}
				if (previousIsActive == false && entry.isActive() == true) {
					lastTimeSwitchedOn = entry.getTime();
				}
				previousIsActive = entry.isActive();
			}
		}
		return timeBeforeShutdown;
	}

	/**
	 * Gets the times before vm migration.
	 * 
	 * @param vms the vms
	 * @return the times before vm migration
	 */
	public static List<Double> getTimesBeforeVmMigration(List<PowerVm> vms) {
		List<Double> timeBeforeVmMigration = new LinkedList<Double>();
		for (Vm vm : vms) {
			boolean previousIsInMigration = false;
			double lastTimeMigrationFinished = 0;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousIsInMigration == true && entry.isInMigration() == false) {
					timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
				}
				if (previousIsInMigration == false && entry.isInMigration() == true) {
					lastTimeMigrationFinished = entry.getTime();
				}
				previousIsInMigration = entry.isInMigration();
			}
		}
		return timeBeforeVmMigration;
	}

	/**
	 * Gets the sla time per active host.
	 * 
	 * @param hosts the hosts
	 * @return the sla time per active host
	 */
	protected static double getSlaTimePerActiveHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			AdvHost host = (AdvHost) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsActive = true;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1 && previousIsActive) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsActive = entry.isActive();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla time per host.
	 * 
	 * @param hosts the hosts
	 * @return the sla time per host
	 */
	protected static double getSlaTimePerHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			AdvHost host = (AdvHost) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla metrics.
	 * 
	 * @param vms the vms
	 * @param lastClock 
	 * @return the sla metrics
	 */
	protected static Map<String, Double> getSlaMetrics(List<PowerVm> vms) {
		Map<String, Double> metrics = new HashMap<String, Double>();
		List<Double> slaViolation = new LinkedList<Double>();
		double totalAllocated = 0;
		double totalRequested = 0;
		double totalUnderAllocatedDueToMigration = 0;

		for (Vm vm : vms) {
			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
			double vmUnderAllocatedDueToMigration = 0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsInMigration = false;

			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						slaViolation.add((previousRequested - previousAllocated) / previousRequested);
						if (previousIsInMigration) {
							vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated) * timeDiff;
						}
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsInMigration = entry.isInMigration();
			}
			
			totalAllocated += vmTotalAllocated;
			totalRequested += vmTotalRequested;
			totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration;
		}

		metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
		if (slaViolation.isEmpty()) {
			metrics.put("average", 0.);
		} else {
			metrics.put("average", MathUtil.mean(slaViolation));
		}
		metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
		// metrics.put("sla_time_per_vm_with_migration", slaViolationTimePerVmWithMigration / totalTime);
		// metrics.put("sla_time_per_vm_without_migration", slaViolationTimePerVmWithoutMigration / totalTime);

		return metrics;
	}

	/**
	 * Write data column.
	 * 
	 * @param data the data
	 * @param outputPath the output path
	 */
	public static void writeDataColumn(List<? extends Number> data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Number value : data) {
				writer.write(value.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write data row.
	 * 
	 * @param data the data
	 * @param outputPath the output path
	 */
	public static void writeDataRow(String data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write metric history.
	 * 
	 * @param hosts the hosts
	 * @param vmAllocationPolicy the vm allocation policy
	 * @param outputPath the output path
	 */
	public static void writeMetricHistory(
			List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy,
			String outputPath) {
		// for (Host host : hosts) {
		for (int j = 0; j < 10; j++) {
			Host host = hosts.get(j);

			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			File file = new File(outputPath + "_" + host.getId() + ".csv");
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
				List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
				List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

				for (int i = 0; i < timeData.size(); i++) {
					writer.write(String.format(
							"%.2f,%.2f,%.2f\n",
							timeData.get(i),
							utilizationData.get(i),
							metricData.get(i)));
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * Prints the metric history.
	 * 
	 * @param hosts the hosts
	 * @param vmAllocationPolicy the vm allocation policy
	 */
	public static void printMetricHistory(
			List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
		for (int i = 0; i < 10; i++) {
			Host host = hosts.get(i);

			Log.printLine("Host #" + host.getId());
			Log.printLine("Time:");
			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			for (Double time : vmAllocationPolicy.getTimeHistory().get(host.getId())) {
				Log.format("%.2f, ", time);
			}
			Log.printLine();

			for (Double utilization : vmAllocationPolicy.getUtilizationHistory().get(host.getId())) {
				Log.format("%.2f, ", utilization);
			}
			Log.printLine();

			for (Double metric : vmAllocationPolicy.getMetricHistory().get(host.getId())) {
				Log.format("%.2f, ", metric);
			}
			Log.printLine();
		}
	}

	//draft having all results
	public static void printResults(AwareDatacenter datacenter, List<Vm> vms){			
			String indent = "    ";
			Log.printLine();
			
			Log.enable();
			List<Host> hosts = datacenter.getHostList();
		
			int numberOfHosts = hosts.size();
			int numberOfVms = vms.size();
		
			//double totalSimulationTime = lastClock;
			double energy = datacenter.getPower() / (3600 * 1000);
			//int numberOfMigrations = datacenter.getMigrationCount();
		
	/*		Map<String, Double> slaMetrics = getSlaMetrics(vms);
		
			double slaOverall = slaMetrics.get("overall");
			double slaAverage = slaMetrics.get("average");
			double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		
			double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);
		
			double sla = slaTimePerActiveHost * slaDegradationDueToMigration;
		
			List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);
		
			int numberOfHostShutdowns = timeBeforeHostShutdown.size();
		
			double meanTimeBeforeHostShutdown = Double.NaN;
			double stDevTimeBeforeHostShutdown = Double.NaN;
			if (!timeBeforeHostShutdown.isEmpty()) {
				meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
				stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
			}
		
			List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
			double meanTimeBeforeVmMigration = Double.NaN;
			double stDevTimeBeforeVmMigration = Double.NaN;
			if (!timeBeforeVmMigration.isEmpty()) {
				meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
				stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
			}*/
			
			Log.printLine("=================================");
			Log.printLine();
			Log.printLine(String.format("Datacenter: " + datacenter.getName()));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			//Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
			//Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
			//Log.printLine(String.format("SLA: %.5f%%", sla * 100));
			//Log.printLine(String.format("SLA perf degradation due to migration: %.2f%%", slaDegradationDueToMigration * 100));
			//Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
			//Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
			//Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
			//Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
			//Log.printLine(String.format("Mean time before a host shutdown: %.2f sec", meanTimeBeforeHostShutdown));
			//Log.printLine(String.format("StDev time before a host shutdown: %.2f sec", stDevTimeBeforeHostShutdown));
			//Log.printLine(String.format("Mean time before a VM migration: %.2f sec", meanTimeBeforeVmMigration));
			//Log.printLine(String.format("StDev time before a VM migration: %.2f sec", stDevTimeBeforeVmMigration));
	}
	

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list list of Cloudlets
	 */
	public static void printServiceRequestResults(List<ServiceRequest> cloudletSubmittedList, List<ServiceRequest> cloudletReceivedList, List<? extends Vm> vmlist) {
		String indent = "    ";
		ServiceRequest cloudlet;
		
		Log.printLine();
		Log.printLine("======= ServiceRequests =======");
		Log.printLine();
		Log.printLine(
			"Request ID" + indent + 
			"Status" + indent +
			"Datacenter ID" + indent + 
			//"Host ID" + indent + 
			"VM ID" + indent + 
			"Arrival Time" + indent +		//getArrivalTime()
			"Submission Time" + indent +	//getSubmissionTime()
			"Exec. Start Time" + indent + 		//getExecStartTime()
			"Finish Time" + indent +		//getFinishTime()
			//"WallClock Time" + indent +
			"Execution Time" + indent +  	//getActualCPUTime()	= getFinishTime() - getExecStartTime()
			"Response Time" + indent +		//getResponseTime()		= getfinishTime() - getSubmissionTime()
			"Latency Time" + indent +		//getLatencyTime()		= getExecStartTime() - getSubmissionTime()
			"Processing Cost");

		for (int i = 0; i < cloudletSubmittedList.size(); i++) {
			cloudlet = cloudletSubmittedList.get(i);

			Log.print(indent + cloudlet.getServiceRequestId() + indent + indent);
			if (cloudlet.getServiceRequestStatus() == ServiceRequest.SUCCESS) {
				Log.print("SUCCESS");
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.READY) {
				Log.print("READY");
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.QUEUED) {
				Log.print("QUEUED");
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.INEXEC) {
				Log.print("INEXEC");    
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.FAILED) {
				Log.print("FAILED");
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.CANCELED) {
				Log.print("CANCELED");
			} else if (cloudlet.getServiceRequestStatus() == ServiceRequest.FAILED_RESOURCE_UNAVAILABLE) {
				Log.print("FAILED_RESOURCE_UNAVAILABLE");
			}
			
			//Vm vm = VmList.getById(vmlist, cloudlet.getVmId());
			DecimalFormat dft = new DecimalFormat("#00.00");
			Log.printLine(
				indent + indent 
				+ cloudlet.getResourceId()
				+ indent + indent + indent
				//+ vm.getHost().getId() 
				//+ indent + indent + indent
				+ cloudlet.getVmId()
				+ indent + indent
				+ dft.format(cloudlet.getArrivalTime())
				+ indent + indent + indent 
				+ dft.format(cloudlet.getSubmissionTime())
				+ indent + indent + indent 
				+ dft.format(cloudlet.getExecStartTime())
				+ indent + indent + indent
				+ dft.format(cloudlet.getFinishTime())
				+ indent + indent + indent
				//+ dft.format(cloudlet.getWallClockTime())
				//+ indent + indent + indent + indent
				+ dft.format(cloudlet.getActualCPUTime()) 
				+ indent + indent + indent
				+ dft.format(cloudlet.getResponseTime())
				+ indent + indent + indent 
				+ dft.format(cloudlet.getLatencyTime())
				+ indent + indent + indent
				+ dft.format(cloudlet.getProcessingCost()));
		}
		Log.printLine();
		Log.printLine();
		Log.printLine("======= ServiceRequests statistics =======");
		Log.printLine();
		Log.printLine("Submitted " + cloudletSubmittedList.size() + " ServiceRequests");
		Log.printLine("Received " + cloudletReceivedList.size() + " ServiceRequests");
		Log.printLine();
	}
	
	public static void printExperimentIntervalResults(List<ServiceRequest> clist, double lastClock, List<? extends Vm> vmlist, Datacenter datacenter){
		int size = clist.size();

		//get workload intervals
		double lastInterval = 0;
		for (ServiceRequest cloudlet : clist) {
			if (lastInterval < cloudlet.getArrivalTime()){
				lastInterval = cloudlet.getArrivalTime();
			}
		}
		
		int intervals = (int) ((lastInterval/helper.Constants.RUNTIME_INTERVAL)+2);
		
		double[] total_arrived_requests = new double[intervals];
		double[] total_serviceTime = new double[intervals];
		double[] total_responseTime = new double[intervals];
		double[] total_latency = new double[intervals];
		double[] total_throughput = new double[intervals];
		double[] total_cost = new double[intervals];
		double[] operational_cost = new double[intervals];
		double[] energy_consumption = new double[intervals];
		
		double[] arrivalRate = new double[intervals];
 		double[] avg_serviceTime = new double[intervals];
		double[] avg_responseTime = new double[intervals];
		double[] avg_latency = new double[intervals];
		double[] avg_throughput = new double[intervals];
		double[] avg_cost = new double[intervals];
		
		//get interval totals
		for (ServiceRequest cloudlet : clist) {
			int idx;
			if (cloudlet.getServiceRequestStatus() == ServiceRequest.SUCCESS){
				idx = (int) (cloudlet.getArrivalTime()/helper.Constants.RUNTIME_INTERVAL);
				total_arrived_requests[idx] ++;
				total_serviceTime[idx] += cloudlet.getActualCPUTime();
				total_responseTime[idx] += cloudlet.getResponseTime();
				total_latency[idx] += cloudlet.getLatencyTime();
				total_cost[idx] += cloudlet.getProcessingCost();
				
				idx = (int) (cloudlet.getFinishTime()/helper.Constants.RUNTIME_INTERVAL);
				if (idx <= intervals-1) {
					total_throughput[idx]++;
				}
				else {
					total_throughput[intervals-1]++;
				}
			}
		}
		//calculate interval average
		for (int i=0; i <= intervals-1; i++){
			if (total_arrived_requests[i] > 0) { 
				arrivalRate[i] = total_arrived_requests[i]/helper.Constants.RUNTIME_INTERVAL;
				avg_serviceTime[i] = total_serviceTime[i]/total_arrived_requests[i];
				avg_responseTime[i] = total_responseTime[i]/total_arrived_requests[i];
				avg_latency[i] = total_latency[i]/total_arrived_requests[i];
				avg_cost[i] = total_cost[i]/total_arrived_requests[i];
			}
			avg_throughput[i] = total_throughput[i]/helper.Constants.RUNTIME_INTERVAL;
			if (datacenter.getClass().getSimpleName().equals("AdaptiveDatacenter")) {
				operational_cost[i] =  ((AdaptiveDatacenter) datacenter).getOperationRecord(i*helper.Constants.RUNTIME_INTERVAL).getCost();
				energy_consumption[i] = ((AdaptiveDatacenter) datacenter).getOperationRecord(i*helper.Constants.RUNTIME_INTERVAL).getPower() / (3600 * 1000);
			}
			if (datacenter.getClass().getSimpleName().equals("AwareDatacenter")) {
				operational_cost[i] =  ((AwareDatacenter) datacenter).getOperationRecord(i * helper.Constants.RUNTIME_INTERVAL).getCost();
				energy_consumption[i] = ((AwareDatacenter) datacenter).getOperationRecord(i*helper.Constants.RUNTIME_INTERVAL).getPower() / (3600 * 1000);
			}
		}
				
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== Runtime Workload Interval Results ==========");
		Log.printLine();
		Log.printLine(
			"Time" + indent + 
			"Average Arrival Rate" + indent +
			"Average Service Time" + indent + 
			"Average Response Time" + indent + 
			"Average Latency" + indent +		
			"Average Throughput" + indent + 		
			"Average Request Cost" + indent +
			"Total Requests Cost" + indent +
			"Operational Cost" + indent +
			"Energy Consumption");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < intervals; i++){
			Log.printLine(
					+ i * helper.Constants.RUNTIME_INTERVAL
					+ indent + indent 
					+ dft.format(arrivalRate[i])
					+ indent + indent + indent + indent  
					+ dft.format(avg_serviceTime[i])
					+ indent + indent + indent + indent 
					+ dft.format(avg_responseTime[i])
					+ indent + indent + indent + indent 
					+ dft.format(avg_latency[i])
					+ indent + indent + indent + indent 
					+ dft.format(avg_throughput[i])
					+ indent + indent + indent + indent 
					+ dft.format(avg_cost[i])
					+ indent + indent + indent + indent 
					+ dft.format(total_cost[i])
					+ indent + indent + indent + indent 
					+ dft.format(operational_cost[i])
					+ indent + indent + indent + indent 
					+ dft.format(energy_consumption[i])
					);
		}
		Log.printLine();
	}

	/**
	 * Prints the results Data center for SAd/Saw package
	 * 
	 * @param datacenter the Datacenter
	 * @param lastClock the last clock
	 * @throws FileNotFoundException 
	 */
	public static void printDatacenterResults(Datacenter datacenter, List<? extends Vm> vms, double lastClock) throws FileNotFoundException{			
			Log.enable();
			List<Host> hosts = datacenter.getHostList();
			String datacenterName = datacenter.getName();
			int numberOfHosts = hosts.size();
			int numberOfVms = vms.size();
		
			double totalSimulationTime = lastClock;
			double energy = datacenter.getPower() / (3600 * 1000);
			double operational_cost = datacenter.getOperationalCost();
			//int numberOfMigrations = datacenter.getMigrationCount();

			Log.printLine("========== Datacenter statistics ==========");
			Log.printLine();
			Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine();
			Log.printLine(String.format("Datacenter: " + datacenterName));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			Log.printLine();			
			Log.printLine(String.format("Total energy consumption: %.2f kWh", energy));
			Log.printLine(String.format("Total operational cost: %.2f $", operational_cost));
			//Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));			
	}
	
	private static double getCost(List<? extends Vm> vms){
		double total_cost;
		
		double vm_CPUs = 0.0;
		double vm_memory = 0.0;
		double vm_storage = 0.0;
		double vm_Bw = 0.0;
		for (Vm vm : vms) {
			vm_CPUs += vm.getNumberOfPes();
			vm_memory += vm.getRam();
			vm_storage += vm.getSize();
			vm_Bw += vm.getBw();
		}
					
		total_cost = Constants.COST_CPU[1] * vm_CPUs;
		total_cost += Constants.COST_Per_Mem[1] * vm_memory;
		total_cost += Constants.COST_Per_Storage[1] * vm_storage;
		total_cost += Constants.COST_Per_Bw[1] * vm_Bw;
		
		return total_cost;
	}
	

	public static void printSelfAwarenessResults(AwareDatacenter datacenter) {
		try {
			double adaptationOverhead = SelfAwareArchitecture.getInstance().getAdaptationOverhead();
			LinkedList<AdaptationRecord> adaptationHistory = SelfAwareArchitecture.getInstance().getAdaptationHistory();

			Log.printLine();
			Log.printLine("========== Self-Awareness statistics ==========");
			Log.printLine();
			Log.printLine(String.format("Adaptation overhead: " + adaptationOverhead));
			Log.printLine();
			Log.printLine("Adaptation history:");
			Log.printLine("Time 	Adaptation		Hosts#		Vms#");
			adaptationHistory.forEach((record) -> {
			Log.printLine(record.getAdaptationTime() + " 	" 
					+ record.getAdaptationTag() + " 	" 
					+ record.getHostNo() + "		 " 
					+ record.getVmNo());
			});
			Log.printLine();
			Log.printLine("Operation hisotry:");
			
			Log.printLine("Time 	Hosts#	Vms#	Operational Cost	Energy");
			datacenter.getOperationHistory().forEach((record) -> {
				Log.printLine(
						record.getOperationTime() + " 	"
						+ record.getHostNo() + " 	"
						+ record.getVmNo() + " 	"
						+ String.format("%.2f", record.getCost()) + " 	"
						+ String.format("%.2f", record.getPower() / (3600 * 1000))
						);
	        });
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printExperimentTotalResults(
			List<ServiceRequest> clist, double lastClock, List<? extends Vm> vmlist, Datacenter datacenter) {

		int size = clist.size();

		double total_serviceTime = 0.0;
		double total_responseTime = 0.0;
		double total_latency= 0.0;
		double total_cost = 0.0;
	
		for (ServiceRequest cloudlet : clist) {
			if (cloudlet.getServiceRequestStatus() == ServiceRequest.SUCCESS){
				total_serviceTime += cloudlet.getActualCPUTime();
				total_responseTime += cloudlet.getResponseTime();
				total_latency += cloudlet.getLatencyTime();
				total_cost += cloudlet.getProcessingCost();
			}
		}
	
		double arrivalRate = size/clist.get(clist.size()-1).getArrivalTime();
		//avg_arrivalRate = size/last_submission_time;
		//avg_arrivalRate = size/(simDays*24*60*60);	
		double avg_serviceTime = total_serviceTime/size;
		double avg_responseTime = total_responseTime/size;
		double avg_latency = total_latency/size;
		double avg_throughput = size/lastClock;
		double avg_cost = total_cost/size;
	
		double total_energy = datacenter.getPower() / (3600 * 1000);
		double total_operationalCost = datacenter.getOperationalCost();
		
		DecimalFormat dft = new DecimalFormat("###.##");
		Log.printLine();
		Log.printLine("========== Experiment Results ===========");
		Log.printLine();
		Log.printLine(String.format("Average arrival rate: %.2f request/sec", arrivalRate));
		Log.printLine(String.format("Average service time: %.2f ms", avg_serviceTime));
		Log.printLine(String.format("Average response time: %.2f ms", avg_responseTime));
		Log.printLine(String.format("Average latency: %.2f ms", avg_latency));
		Log.printLine(String.format("Average throughput: %.2f request/sec", avg_throughput));
		Log.printLine(String.format("Average cost/request: %.2f $", avg_cost));
		Log.printLine(String.format("Total processing cost of requests: %.2f $", total_cost));
		Log.printLine();
		Log.printLine(String.format("Total energy: %.2f kWh", total_energy));		
		Log.printLine(String.format("Total operational cost: %.2f $", total_operationalCost));		
	}

	
}
