package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class run_diff_pop_size {
	
	public static void main(String [] args) {
		
		String filenameBase = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/", 
				filenameSuffix = ".txt";
		String [] paramsFilenames = {"params_15_perc"}; //,"params_5_perc", "params_10_perc", "params_15_perc", "params_20_perc", "params_25_perc", "params_30_perc"  
		double myBeta = 0.3;
		int numDays = 100;
		boolean demography = false;
		String outputPath = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/java/WorldBankCovid19/outputs/diffPopSize/";

		String outputPrefix = "_bulkTest_" + myBeta + "_" + numDays + "_";

		for(String s: paramsFilenames) {
			
			for(int i = 1; i < 2; i++) {
				
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = outputPath + s + outputPrefix + i;
				

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