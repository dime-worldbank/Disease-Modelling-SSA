package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class BulkRun {
	
	public static void main(String [] args) {
		
		String filenameBase = "/Users/swise/workspace/worldbank/Disease-Modelling-SSA/data/verification/", 
				filenameSuffix = ".txt";
		String [] paramsFilenames = {"params_default1Dist"};//, "params_defaultMultiDist", "params_multiStatusMultiDist"};   
		double myBeta = .2;
		int numDays = 30;

		String outputPrefix = "_bulkTest_" + myBeta + "_" + numDays + "_", outputSuffix = ".txt";

		for(String s: paramsFilenames) {
			
			for(int i = 0; i < 1; i++) {
				
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = s + outputPrefix + i + outputSuffix;
				String infectionsOutputFilename = "infections_" + s + outputPrefix + i + outputSuffix;
				
				WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(i, new Params(paramFilename), outputFilename);
				
				System.out.println("Loading...");

				mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
				mySim.targetDuration = numDays;
				mySim.start();
				mySim.infections_export_filename = infectionsOutputFilename;
				
				System.out.println("Running...");

				while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
					mySim.schedule.step(mySim);
					double myTime = mySim.schedule.getTime();
					//System.out.println("\n*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
				}
				
				//mySim.reportOnInfected();
				mySim.exportInfections();
				
				/*
				String agesOutputFilename = "ages_" + s + "_" + i + outputSuffix;
				try {
					
					// shove it out
					BufferedWriter exportFile = new BufferedWriter(new FileWriter(agesOutputFilename, true));
					for(int j: mySim.testingAgeDist)
						exportFile.write(j + ", ");

				 	exportFile.close();
				 	System.out.println("written out to " + agesOutputFilename);
				} catch (Exception e) {
					System.err.println("File input error: " + agesOutputFilename);
				}
*/
			}
		}


	}
	
}