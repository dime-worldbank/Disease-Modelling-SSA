package uk.ac.ucl.protecs.sim;

public class BulkRun {
	
	public static void main(String [] args) {
		
		String filenameBase = "/Users/robbiework/Desktop/local_eclipse_runs/", 
				filenameSuffix = ".txt";
		String outputFilepath = "/Users/robbiework/Desktop/local_eclipse_runs/";
		
		String [] paramsFilenames = {"demography_params_local"};//, "params_defaultMultiDist", "params_multiStatusMultiDist"};   
		boolean [] demography = {true};//, false};
		
		double myBeta = 0;
		int numDays = 400;

		String outputPrefix = "_bulkTest_" + myBeta + "_" + numDays + "_";
			
		for(Boolean b: demography) {
			
			for(String s: paramsFilenames) {
				
				for(int i = 0; i < 3; i++) {
					
					String paramFilename = filenameBase + s + filenameSuffix;
					String outputFilename = outputFilepath + "third_DEM" + outputPrefix + i;
					String infectionsFilename = outputFilepath + "infections_" + s + outputPrefix + i; 
					
					// include demography in filename if we're testing more than one
					if (demography.length > 1) {
						outputFilename += "_" + b.toString();
						infectionsFilename += "_" + b.toString();
					}

					WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(i, new Params(paramFilename, true), outputFilename, b, false);
					
					System.out.println("Loading...");

					mySim.params.infection_beta = myBeta; // normalised to be per tick
					mySim.targetDuration = numDays;
					mySim.start();
					
					System.out.println("Running...");

					while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
						mySim.schedule.step(mySim);
						double myTime = mySim.schedule.getTime();
					}
					
					ImportExport.exportInfections(infectionsFilename + filenameSuffix, mySim.infections);
					
				}
			}
		}



	}
	
	
}
