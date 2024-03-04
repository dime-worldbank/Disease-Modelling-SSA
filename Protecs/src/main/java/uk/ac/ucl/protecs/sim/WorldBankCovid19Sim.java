package uk.ac.ucl.protecs.sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import uk.ac.ucl.protecs.behaviours.*;
import uk.ac.ucl.protecs.objects.*;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;
import uk.ac.ucl.protecs.objects.diseases.Infection;
import uk.ac.ucl.protecs.objects.diseases.SpuriousSymptomBehaviourFramework;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusBehaviourFramework;
import sim.engine.SimState;
import sim.engine.Steppable;

public class WorldBankCovid19Sim extends SimState {

	// the objects which make up the system
	public ArrayList <Person> agents;
	public ArrayList <Household> households;
	public ArrayList <Infection> infections;
	public ArrayList <CoronavirusSpuriousSymptom> CovidSpuriousSymptoms;
	public Random random;
	
	ArrayList <Location> districts;
	
	HashMap <Location, ArrayList<Person>> personsToDistrict; 
	
	public MovementBehaviourFramework movementFramework;
	public CoronavirusBehaviourFramework infectiousFramework;
	public SpuriousSymptomBehaviourFramework spuriousFramework;
	public Params params;
	public boolean lockedDown = false;
	// create a variable to determine whether the model will cause additional births and deaths	
	public boolean demography = false;
	// create a variable to determine if COVID testing will take place
	public boolean covidTesting = false;
	
	// the names of file names of each output filename		
	public String outputFilename;
	public String covidIncOutputFilename; 
	public String populationOutputFilename;
	public String covidIncDeathOutputFilename;
	public String otherIncDeathOutputFilename;
	public String birthRateOutputFilename;
	public String distPopSizeOutputFilename;
	public String newLoggingFilename; 
	public String infections_export_filename;
	public String distCovidPrevalenceOutputFilename;
	public String distPopBreakdownOutputFilename;
	public String sim_info_filename;
	public String covidCountsOutputFilename;
	public String covidByEconOutputFilename;
	int targetDuration = 0;
	
	// ordering information
	public static int param_schedule_lockdown = 0;
	public static int param_schedule_movement = 1;
	public static int param_schedule_updating_locations = 5;
	public static int param_schedule_infecting = 10;
	public static int param_schedule_reporting = 100;
	public static int param_schedule_COVID_SpuriousSymptoms = 101;
	public static int param_schedule_COVID_Testing = 102;
	
	public ArrayList <Integer> testingAgeDist = new ArrayList <Integer> ();	
	// record-keeping
	
	ArrayList <HashMap <String, Double>> dailyRecord = new ArrayList <HashMap <String, Double>> ();

	// meta
	public long timer = -1;
	
	/**
	 * Constructor function
	 * @param seed
	 */
	public WorldBankCovid19Sim(long seed, Params params, String outputFilename, boolean demography, boolean covidTesting) {
		super(seed);
		this.params = params;
		this.outputFilename = outputFilename + ".txt";
		this.demography = demography;
		this.covidTesting = covidTesting;
		this.random = new Random(this.seed());
		this.covidIncOutputFilename = outputFilename + "_Incidence_Of_Covid_" + ".txt"; 
		this.populationOutputFilename = outputFilename + "_Overall_Demographics_" + ".txt";
		this.covidIncDeathOutputFilename = outputFilename + "_Incidence_Of_Covid_Death_" + ".txt";
		this.otherIncDeathOutputFilename = outputFilename + "_Incidence_Of_Other_Death_" + ".txt";
		this.birthRateOutputFilename = outputFilename + "_Birth_Rate_" + ".txt";
		this.distPopSizeOutputFilename = outputFilename + "_District_Level_Population_Size_" + ".txt";
		this.newLoggingFilename = outputFilename + "_Cases_Per_District_" + ".txt"; 
		this.infections_export_filename = outputFilename + "_Infections_" + ".txt";
		this.distCovidPrevalenceOutputFilename = outputFilename + "_Percent_In_District_With_Covid_" + ".txt";
		this.distPopBreakdownOutputFilename = outputFilename + "_Overall_Demographics_" + ".txt";
		this.sim_info_filename = outputFilename + "_Sim_Information_" + ".txt";
		this.covidCountsOutputFilename = outputFilename + "_Age_Gender_Demographics_Covid_" + ".txt";
		this.covidByEconOutputFilename = outputFilename + "_Economic_Status_Covid_.txt";
	}
	
	public void start(){
		
		// copy over the relevant information
		districts = new ArrayList <Location> (params.districts.values());
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new CoronavirusBehaviourFramework(this);
		spuriousFramework = new SpuriousSymptomBehaviourFramework(this);
		
		// load the population
		load_population(params.dataDir + params.population_filename);
		
		// if there are no agents, SOMETHING IS WRONG. Flag this issue!
		if(agents.size() == 0) {
			System.out.println("ERROR *** NO AGENTS LOADED");
			System.exit(0);
		}

		// set up the social networks
		//InteractionUtilities.create_work_bubbles(this);
		//InteractionUtilities.create_community_bubbles(this);

		// RESET SEED
//		random = new MersenneTwisterFast(this.seed());
		random = new Random();

		// set up the infections
		infections = new ArrayList <Infection> ();
		for(Location l: params.lineList.keySet()){
			
			// activate this location
			l.setActive(true);
			
			// number of people to infect
			int countInfections = params.lineList.get(l) * params.lineListWeightingFactor;
			
			// list of infected people
			HashSet <Person> newlyInfected = new HashSet <Person> ();
			
			// number of people present
			ArrayList <Person> peopleHere = this.personsToDistrict.get(l);
			int numPeopleHere = peopleHere.size();//l.getPeople().size();
			if(numPeopleHere == 0){ // if there is no one there, don't continue
				System.out.println("WARNING: attempting to initialise infection in Location " + l.getId() + " but there are no People present. Continuing without successful infection...");
				continue;
			}

			// schedule people here
			//for(Person p: peopleHere)
			//	schedule.scheduleRepeating(0, p);
			
			int collisions = 100; // to escape while loop in case of troubles

			// infect until you have met the target number of infections
			while(newlyInfected.size() < countInfections && collisions > 0){
				Person p = peopleHere.get(random.nextInt(numPeopleHere));
				
				// check for duplicates!
				if(newlyInfected.contains(p)){
					collisions--;
					continue;
				}
				else // otherwise record that we're infecting this person
					newlyInfected.add(p);
				
				// create new person
				Infection inf = new CoronavirusInfection(p, null, infectiousFramework.getInfectedEntryPoint(l), this, 0);
				// update this person's properties
				
				// update this person's properties so we can keep track of the number of cases etc				
				p.storeCovid();
				if (inf.getBehaviourName().equals("asymptomatic")) {
					p.setAsympt();
				}
				else {
					p.setMild();
				}
				schedule.scheduleOnce(1, param_schedule_infecting, inf);
			}
						
		}

		// SCHEDULE UPDATING OF LOCATIONS
		Steppable updateLocationLists = new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				for(Location l: districts) {
					l.updatePersonsHere();
					}
			}
			
		};
		schedule.scheduleRepeating(0, this.param_schedule_updating_locations, updateLocationLists);
		
		if (this.covidTesting) {
			CovidSpuriousSymptoms = new ArrayList <CoronavirusSpuriousSymptom> ();
			for (Person a: agents) {
				// create spurious symptom objects
				CoronavirusSpuriousSymptom CovSpuriousSymptoms = new CoronavirusSpuriousSymptom(a, this, 0);
				// make the object do things
				schedule.scheduleOnce(1, param_schedule_infecting, CovSpuriousSymptoms);
			}
//			schedule.scheduleRepeating(CovidSpuriousSymptoms.manageSymptoms(this), this.param_schedule_COVID_SpuriousSymptoms, params.ticks_per_day);
//			schedule.scheduleRepeating(CovidTesting.Testing(this), this.param_schedule_COVID_Testing, params.ticks_per_day);
			}
		if (this.demography) {
			Demography myDemography = new Demography();
			for(Person a: agents) {
				// Trigger the aging process for this person
				Demography.Aging agentAging = myDemography.new Aging(a, params.ticks_per_day);
				schedule.scheduleOnce(a.getBirthday()*params.ticks_per_day, this.param_schedule_reporting, agentAging);
				// Trigger the process to determine mortality each year
				Demography.Mortality agentMortality = myDemography.new Mortality(a, params.ticks_per_day, this);
				schedule.scheduleOnce(0, this.param_schedule_reporting, agentMortality);
				// if biologically female, trigger checks for giving birth each year
				if (a.getSex().equals("female")) {
					Demography.Births agentBirths = myDemography.new Births(a, params.ticks_per_day, this);
					schedule.scheduleOnce(0, this.param_schedule_reporting, agentBirths);
				}
			}
			Logging logger = new Logging();
			Logging.BirthRateReporter birthRateLog = logger.new BirthRateReporter(this);
			schedule.scheduleOnce(364 * params.ticks_per_day, this.param_schedule_reporting, birthRateLog);

			
		}

		// This function tracks the epidemic over time 
		schedule.scheduleRepeating(Logging.TestLoggingCase(this), this.param_schedule_reporting, params.ticks_per_day);
		
		// Create a function to keep track of the population and epidemic at the scale of district level
		schedule.scheduleRepeating(Logging.UpdateDistrictLevelInfo(this), this.param_schedule_reporting, params.ticks_per_day);

		schedule.scheduleRepeating(Logging.ReportPopStructure(this), this.param_schedule_reporting, params.ticks_per_day);
		
		
		// SCHEDULE LOCKDOWNS
		Steppable lockdownTrigger = new Steppable() {

			@Override
			public void step(SimState arg0) {
				double currentTime = arg0.schedule.getTime();
				if(params.lockdownChangeList.size() == 0)
					return;
				double nextChange = params.lockdownChangeList.get(0);
				if(currentTime >= nextChange) {
					params.lockdownChangeList.remove(0);
					lockedDown = !lockedDown;
				}
				
			}
			
		};
		schedule.scheduleRepeating(0, this.param_schedule_lockdown, lockdownTrigger);
		
		String filenameSuffix = (this.params.ticks_per_day * this.params.infection_beta) + "_" 
				+ this.params.lineListWeightingFactor + "_"
				+ this.targetDuration + "_"
				+ this.seed() + ".txt";
		//outputFilename = "results_" + filenameSuffix;
//		infections_export_filename = "infections_" + filenameSuffix;

		ImportExport.exportMe(outputFilename, Location.metricNamesToString(), timer);
		Steppable reporter = new Steppable(){

			@Override
			public void step(SimState arg0) {
				
				String s = "";
				
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				for(Location l: districts){
					s += time + "\t" + l.metricsToString() + "\n";
					l.refreshMetrics();
				}
				
				ImportExport.exportMe(outputFilename, s, timer);
				
				System.out.println("Day " + time + " finished");
			}
		};
		schedule.scheduleRepeating(reporter, this.param_schedule_reporting, params.ticks_per_day);
//		random = new MersenneTwisterFast(this.seed());
		random = new Random(this.seed());

	}
	
	public void load_population(String agentsFilename){
		try {
			
			// holders for construction
			agents = new ArrayList <Person> ();
			households = new ArrayList <Household> ();
			
			// initialise the holder
			personsToDistrict = new HashMap <Location, ArrayList<Person>>();
			for(Location l: districts){
				personsToDistrict.put(l, new ArrayList <Person> ());
			}

			
			// use a helpful holder to find households by their names
			HashMap <String, Household> rawHouseholds = new HashMap <String, Household> ();
			
			System.out.println("Reading in agents from " + agentsFilename);
			
			// Open the file
			FileInputStream fstream = new FileInputStream(agentsFilename);

			// Convert our input stream to a BufferedReader
			BufferedReader agentData = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// get rid of the header
			s = agentData.readLine();
			//Params.parseHeader(s.split(',')); TODO fix meeee
			
			System.out.print("BEGIN READING IN PEOPLE...");
			
			// read in the raw data
			//int myIndex = 10;
			while ((s = agentData.readLine()) != null ){//&& myIndex > 0) {
				//myIndex--;
				
				// separate the columns from the raw text
				String[] bits = Params.splitRawCSVString(s);
				
				// make sure the larger units are set up before we create the individual

				// set up the Household for the Person
				String hhName = bits[4];
				Household h = rawHouseholds.get(hhName);

				// target district
				String myDistrictName = "d_" + bits[5]; // TODO AN ABOMINATION, STANDARDISE IT
				Location myDistrict = params.districts.get(myDistrictName);

				boolean schoolGoer = bits[8].equals("1");
				
				// if the Household doesn't already exist, create it and save it
				if(h == null){
					
					// set up the Household
					h = new Household(hhName, myDistrict);
					rawHouseholds.put(hhName, h);
					households.add(h);
				}
				
				// identify the location in which the person, possibly, works
				
				/*String econLocBase = bits[7];
				int econLocBaseBits = (int) Double.parseDouble(econLocBase);
				String economicActivityLocationName = "d_" + econLocBaseBits;
				Location econLocation = params.districts.get(economicActivityLocationName);
				// TODO: they might not work anywhere! Further, they might work in a particular subset of the location! Specify here further!
				*/
				
				// set up the person
				// create a random birthday
				int birthday = this.random.nextInt(365);

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
						birthday, // birthday to update population
						bits[3], // sex
						bits[6].toLowerCase(), // lower case all of the job titles
						schoolGoer,
						h,
						this
						);
				h.addPerson(p);
				//p.setLocation(myDistrict);
				p.setActivityNode(movementFramework.getHomeNode());
				agents.add(p);
				personsToDistrict.get(myDistrict).add(p);
				
				// schedule the agent to run at the beginning of the simulation
				this.schedule.scheduleOnce(0, this.param_schedule_movement, p);
				//this.schedule.scheduleRepeating(p);
			}
			
			// clean up after ourselves!
			agentData.close();
							
			System.out.println("FINISHED READING PEOPLE");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("File input error: " + agentsFilename);
		}
	}
	


	
	// thanks to THIS FRIEND: https://blogs.sas.com/content/iml/2014/06/04/simulate-lognormal-data-with-specified-mean-and-variance.html <3 to you Rick
	public double nextRandomLognormal(double mean, double std){

		// setup
		double m2 = mean * mean;
		double phi = Math.sqrt(m2 + std);
		double mu = Math.log(m2 / phi);
		double sigma = Math.sqrt(Math.log(phi * phi / m2));
		
		double x = random.nextDouble() * sigma + mu;
		
		return Math.exp(x);
		
	}
	public WorldBankCovid19Sim returnSim() {return this;}
	
	/**
	 * In an ideal scenario, there would be multiple ways to run this with arguments. EITHER: <ul>
	 * 	<li> pass along a parameter file which contains all of these values or;
	 *  <li> pass along the minimal metrics required (e.g. beta, length of run).
	 *  </ul>
	 * @param args
	 */
	public static void main(String [] args){
		
		// default settings in the absence of commands!
		int numDays = 400; // by default, one week
		double myBeta = .016;
		long seed = 12345;
		String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".txt";
		String infectionsOutputFilename = "infections_" + myBeta + "_" + numDays + "_" + seed + ".txt"; 
		String paramsFilename = "src/main/resources/params.txt";
		boolean demography = false;
		boolean covidTesting = true;
		// read in any extra settings from the command line
		if(args.length < 0){
			System.out.println("usage error");
			System.exit(0);
		}
		else if(args.length > 0){
			
			numDays = Integer.parseInt(args[0]);
			myBeta = Double.parseDouble(args[2]);
			if(args.length > 3) {
				seed = Long.parseLong(args[3]);
				outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".tsv";
			}
			if(args.length > 4)
				outputFilename = args[4];
			if(args.length > 5)
				paramsFilename = args[5];
			if(args.length > 6)
				infectionsOutputFilename = args[6];
		}
		
		long startTime = System.currentTimeMillis(); // wallclock measurement of time - embarrassing.
				
		/*
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = s + outputPrefix + i + outputSuffix;
				
		 */

		// set up the simulation
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename, true), outputFilename, demography, covidTesting);


		System.out.println("Loading...");

		// ensure that all parameters are set
		mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
		mySim.targetDuration = numDays;
		
		mySim.start(); // start the simulation
		
		mySim.infections_export_filename = infectionsOutputFilename; // overwrite the export filename
		
		System.out.println("How many agents? " + mySim.agents.size());
		System.out.println("Running...");

		// run the simulation
		while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
		}
		
		ImportExport.exportInfections(infectionsOutputFilename, mySim.infections);
		
		// end of wallclock determination of time
		long endTime = System.currentTimeMillis();
		mySim.timer = endTime - startTime;
		
		System.out.println("...run finished after " + mySim.timer + " ms");
	}


}