package main.java.sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class BulkRun {
	
	public static void main(String [] args) {
		
		String filenameBase = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/", 
				filenameSuffix = ".txt";
		String [] paramsFilenames = {"params_robbie_test"};//, "params_defaultMultiDist", "params_multiStatusMultiDist"};   
		double myBeta = 0.3;
		int numDays = 100;
		boolean demography = true;

		String outputPrefix = "_bulkTest_" + myBeta + "_" + numDays + "_";

		for(String s: paramsFilenames) {
			
			for(int i = 0; i < 3; i++) {
				
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = s + outputPrefix + i;
				

				WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(i, new Params(paramFilename), outputFilename, demography);
				
				System.out.println("Loading...");

				mySim.params.infection_beta = myBeta; // normalised to be per tick
				mySim.targetDuration = numDays;
				mySim.start();
				
				System.out.println("Running...");

				while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
					mySim.schedule.step(mySim);
					double myTime = mySim.schedule.getTime();
				}
				
				mySim.exportInfections();
				
			}
		}


	}
	
}
