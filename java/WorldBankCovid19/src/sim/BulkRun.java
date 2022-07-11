package sim;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BulkRun {
	
	public static void main(String [] args) {
		
		double myBeta = .03;
		int numDays = 100;
		String outputfilefolder= "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/java/WorldBankCovid19/outputs/bulk_run/";
		
		// get current time, use to time stamp the results file, first format date time
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		// name current time 'timestamp'
		String paramFilename = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/params_robbie_test.txt";


		for(int i = 0; i < 10; i++) {
			LocalDateTime timestamp = LocalDateTime.now();  

			String outputFilename = outputfilefolder + "bulkrun" + timestamp + ".txt";
			String infectionsOutputFilename = outputfilefolder + "infections_" + timestamp + ".txt";
			String sim_info_filename = outputfilefolder + "sim_info_" + timestamp +  ".txt";
				
			// create Random object
		    Random random = new Random();
		    // generate random seed from 0 to 1,000,000
		    int seed = random.nextInt(1000000);
		        
		    WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(seed, new Params(paramFilename), outputFilename);

			System.out.println("Loading...");

			mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
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
			mySim.exportInfections();
			mySim.exportSimInformation();

			
		}


	}
	
}