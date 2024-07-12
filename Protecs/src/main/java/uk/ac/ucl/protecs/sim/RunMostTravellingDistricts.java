package uk.ac.ucl.protecs.sim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


public class RunMostTravellingDistricts {
	
	public static void main(String [] args) {
		
		double myBeta = .03;
		int numDays = 100;
		String outputfilefolder= "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/java/WorldBankCovid19/outputs/different_district_start_points/";
		
		// get current time, use to time stamp the results file, first format date time
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		// name current time 'timestamp'
		String [] paramFilenameList = {"params_d23_start", "params_d26_start", "params_d31_start"}; // "params_d2_start", "params_d18_start", "params_d23_start"

		for (String s: paramFilenameList) {
			String [] parts = s.split("_");
			for(int i = 0; i < 30; i++) {
				String paramFilename = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/" + s + ".txt";

				LocalDateTime timestamp = LocalDateTime.now();  
	
				String outputFilename = outputfilefolder + "district_start_point_" + parts[1] + "_" + timestamp + ".txt";
				String infectionsOutputFilename = outputfilefolder + "infections_district_start_point_" + parts[1] + "_" + timestamp + ".txt";
				String sim_info_filename = outputfilefolder + "sim_info_district_start_point_" + parts[1] + "_" + timestamp +  ".txt";
					
				// create Random object
			    Random random = new Random();
			    // generate random seed from 0 to 1,000,000
			    int seed = random.nextInt(1000000);
			        

			    WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(seed, new Params(paramFilename, false), outputFilename);
	
				System.out.println("Loading...");
	
				mySim.params.infection_beta = myBeta; // normalised to be per tick
				mySim.targetDuration = numDays;
				mySim.start();
				mySim.infections_export_filename = infectionsOutputFilename;
				mySim.sim_info_filename = sim_info_filename;
					
				System.out.println("Running...");
	
				while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
					mySim.schedule.step(mySim);
					double myTime = mySim.schedule.getTime();
					//System.out.println("\n*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
				}
					
				//mySim.reportOnInfected();
				//mySim.exportInfections();
				//mySim.exportSimInformation();
	
				
			}
		}


	}
	
}