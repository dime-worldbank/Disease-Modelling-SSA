package sim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SingleRun {
	
	public static void main(String [] args) {
		double beta = 0.016;
		// set the number of days the simulation will run
		int numDays = 100;
		// get current time, use to time stamp the results file, first format date time
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		// name current time 'timestamp'
		LocalDateTime timestamp = LocalDateTime.now();  
		// Get location of parameter file

		String paramFilename = "/Users/robbiework/eclipse-workspace/Disease-Modelling-SSA/data/verification/params_robbie_test.txt";
		// Create a name for the output file
		String outputFilename = "single_run_" + timestamp.toString() + ".txt";
		// create a name for the infections output file
		String infectionsOutputFilename = "single_run_infections_" + timestamp.toString() + ".txt";
		// create a name for the simulation information file
		String sim_info_filename = "single_run_sim_info_" + timestamp.toString() + ".txt";
		
		// create Random object
        Random random = new Random();
        // generate random seed from 0 to 1,000,000
        int seed = random.nextInt(1000000);
        // create the simulation object
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim(seed, new Params(paramFilename), outputFilename);

		// Set how long the simulation should run for
		mySim.targetDuration = numDays;
		// Begin the simulation
		mySim.start();
		// Update the file names of where we will import the infecitons output and the general simulation information output
		mySim.infections_export_filename = infectionsOutputFilename;
		mySim.sim_info_filename = sim_info_filename;
				
		// run the simulation step by step
		while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
					//System.out.println("\n*****END TIME: DAY " + (int)(myTime / 6) + " HOUR " + (int)((myTime % 6) * 4) + " RAWTIME: " + myTime);
		}
		// export the infections and simulation information to .txt files
		mySim.exportInfections();
		mySim.exportSimInformation();

	}
	
}