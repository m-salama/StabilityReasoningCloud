package org.cloudbus.cloudsim.adaptive.stability.evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.distributions.ExponentialDistr;

import helper.Constants;

public class WorkloadAnalyzerWorldCup extends WorkloadAnalyzer {
	int startFrom;
	int validLines;
	long previousTime;
	double sumOfinterarrival;
	double sumOfSqrIntearrival;
	double sumOfReqs;
	double sumOfSqrReqs;
	double meanInterarrival;
	double stddevInterarrival;
	double meanRequests;
	double stddevReqs;
	
	ExponentialDistr reqGenerator;
	ExponentialDistr intearrivalGenerator;
	
	public WorkloadAnalyzerWorldCup() {
		String workingDir = System.getProperty("user.dir");
		String inputFolder_workload = workingDir + "//experiments//workload//";
		String fileName = inputFolder_workload + "worldcup98//" + "workload_worldcup98_minimal.txt";
		
		this.startFrom = 1;
		this.previousTime=0;
		this.validLines=0;
		this.sumOfinterarrival=0.0;
		this.sumOfSqrIntearrival=0.0;
		this.sumOfReqs=0.0;
		this.sumOfSqrReqs=0.0;
		this.meanInterarrival=0.0;
		this.stddevInterarrival=0.0;
		this.meanRequests=0.0;
		this.stddevReqs=0.0;

		processWorkload(fileName);
	}
	
	private void processWorkload(String fileName) {
		BufferedReader reader = null;

		try {
        	FileInputStream file = new FileInputStream(fileName);
		    InputStreamReader input = new InputStreamReader(file);
		    reader = new BufferedReader(input); 
		    int line=0;
		    while (reader.ready()) { 
		    	boolean validLine = parseLine(reader.readLine(), line);
		    	if (validLine) line++;
		    }	    
		    reader.close();
		    
		    generateStatistics();		    
        } catch (Exception e) {
        	Log.printLine("Error parsing trace file");
        	e.printStackTrace();
		} finally {
		    if (reader != null) {
		    	try {
		    		reader.close();
		        } catch (IOException ioe) {
		        	Log.printLine("Error parsing trace file");
		        	ioe.printStackTrace();
		        }
		    }
		}
    }
	
	private boolean parseLine(String line, int lineNum) {
		if (line.startsWith(";") || line.startsWith("#")) {
			return false;
		}
		
		long submitTime = previousTime + Constants.RUNTIME_INTERVAL;
		
		this.validLines++;
		
		double interarrival = submitTime - previousTime;
		
		this.sumOfinterarrival += interarrival;
		this.sumOfSqrIntearrival += (interarrival*interarrival);
		
		int numProc = Integer.parseInt(line);
		if(numProc <= 0) numProc = 1;
		this.sumOfReqs += numProc;
		this.sumOfSqrReqs += (numProc*numProc);
				
		this.previousTime += Constants.RUNTIME_INTERVAL;
		return true;
	}
	
	private void generateStatistics(){
		meanInterarrival = this.sumOfinterarrival/this.validLines;
		double meanOfSqrIntearrival = this.sumOfSqrIntearrival/this.validLines;
		stddevInterarrival = Math.sqrt(meanOfSqrIntearrival-meanInterarrival*meanInterarrival);
		meanRequests = this.sumOfReqs/this.validLines;
		double meanOfSqrRequests = this.sumOfSqrReqs/this.validLines;
		stddevReqs = Math.sqrt(meanOfSqrRequests-meanRequests*meanRequests);
		this.intearrivalGenerator = new ExponentialDistr(meanInterarrival);
		this.reqGenerator = new ExponentialDistr(meanRequests);
	}

	@Override
	public double delayToNextChangeInModel(double currentTime) {
		double delay =  intearrivalGenerator.sample();
		if (delay<60) return 60*60;
		return delay;
	}

	@Override
	public double getEstimatedArrivalRate(double currentTime) {
		double reqs = reqGenerator.sample();
		double delay =  intearrivalGenerator.sample();
		if (delay<60) delay=60*60;
		if(reqs<0) reqs=0;
		return reqs/delay;
	}
}
