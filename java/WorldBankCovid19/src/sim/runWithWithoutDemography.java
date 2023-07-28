package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class runWithWithoutDemography {
	
	public static void main(String [] args) {
		
		String filenameBase = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/", 
				filenameSuffix = ".txt";
		String [] paramsFilenames = {"params_robbie_test"};//, "params_defaultMultiDist", "params_multiStatusMultiDist"};   
		double myBeta = 0.3;
		int numDays = 100;
		boolean demography = true;
		String outputPath = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/java/WorldBankCovid19/outputs/withdemog/";
		String outputPrefix = "_withDemography_" + myBeta + "_" + numDays + "_", outputSuffix = ".txt";

		for(String s: paramsFilenames) {
			
			for(int i = 0; i < 3; i++) {
				
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = outputPath + s + outputPrefix + i;
				WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(i, new Params(paramFilename), outputFilename, demography);
				
				System.out.println("Loading...");

				mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
				mySim.targetDuration = numDays;
				mySim.start();
				
				System.out.println("Running...");

				while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
					mySim.schedule.step(mySim);
					double myTime = mySim.schedule.getTime();
					//System.out.println("\n*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
				}
//				
//				mySim.reportOnInfected();
				mySim.exportInfections();

			}
		}
		
		boolean demography_off = false;
		String outputPath_2 = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/java/WorldBankCovid19/outputs/withoutdemog/";

		String outputPrefix_2 = "_withoutDemography_" + myBeta + "_" + numDays + "_";

		for(String s: paramsFilenames) {
			
			for(int i = 0; i < 3; i++) {
				
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = outputPath_2 + s + outputPrefix_2 + i;
				

				WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(i, new Params(paramFilename), outputFilename, demography_off);
				
				System.out.println("Loading...");

				mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
				mySim.targetDuration = numDays;
				mySim.start();

				
				System.out.println("Running...");

				while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
					mySim.schedule.step(mySim);
					double myTime = mySim.schedule.getTime();
					//System.out.println("\n*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
				}
				
				//mySim.reportOnInfected();
				mySim.exportInfections();

			}}


	}
	
}