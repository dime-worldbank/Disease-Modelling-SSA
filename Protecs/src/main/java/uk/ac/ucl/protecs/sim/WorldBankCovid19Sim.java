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
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.Person.SEX;
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
	public ArrayList <CoronavirusSpuriousSymptom> CovidSpuriousSymptomsList;
	public Random random;
	
	ArrayList <Location> adminBoundaries;
	
	HashMap <Location, ArrayList<Person>> personsToAdminBoundary; 
	
	public MovementBehaviourFramework movementFramework;
	public CoronavirusBehaviourFramework infectiousFramework;
	public SpuriousSymptomBehaviourFramework spuriousFramework;
	public Params params;
	public boolean lockedDown = false;
	// create a variable to determine if COVID testing will take place
	public boolean covidTesting = false;
	// the names of file names of each output filename		
	public String outputFilename;
	public String covidIncOutputFilename; 
	public String populationOutputFilename;
	public String covidIncDeathOutputFilename;
	public String otherIncDeathOutputFilename;
	public String birthRateOutputFilename;
	public String adminZonePopSizeOutputFilename;
	public String casesPerAdminZoneFilename; 
	public String infections_export_filename;
	public String adminZoneCovidPrevalenceOutputFilename;
	public String adminZonePercentDiedFromCovidOutputFilename;
	public String adminZonePercentCovidCasesFatalOutputFilename;
	public String adminZonePopBreakdownOutputFilename;
	public String sim_info_filename;
	public String covidCountsOutputFilename;
	public String covidByEconOutputFilename;
	public String covidTestingOutputFilename;
	int targetDuration = 0;
	
	// ordering information
	public static int param_schedule_lockdown = 0;
	public static int param_schedule_movement = 1;
	public static int param_schedule_updating_locations = 5;
	public static int param_schedule_infecting = 10;
	public static int param_schedule_reporting = 100;
	public static int param_schedule_COVID_SpuriousSymptoms = 98;
	public static int param_schedule_COVID_Testing = 99;
	public static int param_schedule_reporting_reset = param_schedule_reporting + 1;
	
	public ArrayList <Integer> testingAgeDist = new ArrayList <Integer> ();	
	// record-keeping
	
	ArrayList <HashMap <String, Double>> dailyRecord = new ArrayList <HashMap <String, Double>> ();

	// meta
	public long timer = -1;
	
	/**
	 * Constructor function
	 * @param seed
	 */

	public WorldBankCovid19Sim(long seed, Params params, String outputFilename) {
		super(seed);
		this.params = params;
		this.outputFilename = outputFilename + ".txt";
		this.covidIncOutputFilename = outputFilename + "_Incidence_Of_Covid.txt"; 
		this.populationOutputFilename = outputFilename + "_Overall_Demographics.txt";
		this.covidIncDeathOutputFilename = outputFilename + "_Incidence_Of_Covid_Death.txt";
		this.otherIncDeathOutputFilename = outputFilename + "_Incidence_Of_Other_Death.txt";
		this.birthRateOutputFilename = outputFilename + "_Birth_Rate.txt";
		this.adminZonePopSizeOutputFilename = outputFilename + "_Admin_Zone_Level_Population_Size.txt";
		this.casesPerAdminZoneFilename = outputFilename + "_Cases_Per_Admin_Zone.txt"; 
		this.infections_export_filename = outputFilename + "_Infections.txt";
		this.adminZoneCovidPrevalenceOutputFilename = outputFilename + "_Percent_In_Admin_Zone_With_Covid.txt";
		this.adminZonePopBreakdownOutputFilename = outputFilename + "_Admin_Zone_level_Demographics.txt";
		this.sim_info_filename = outputFilename + "_Sim_Information.txt";
		this.covidCountsOutputFilename = outputFilename + "_Age_Gender_Demographics_Covid.txt";
		this.covidByEconOutputFilename = outputFilename + "_Economic_Status_Covid.txt";
		this.adminZonePercentDiedFromCovidOutputFilename = outputFilename + "_Percent_In_Admin_Zone_Died_From_Covid.txt";
		this.adminZonePercentCovidCasesFatalOutputFilename = outputFilename + "_Percent_Covid_Cases_Fatal_In_Admin_Zone.txt";
		this.covidTestingOutputFilename = outputFilename + "_Covid_Testing.txt";
	}
	
	public void start(){
		
		// copy over the relevant information
		adminBoundaries = new ArrayList <Location> (params.adminZones.values());
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new CoronavirusBehaviourFramework(this);
		spuriousFramework = new SpuriousSymptomBehaviourFramework(this);
		// RESET SEED
		random = new Random(this.seed());

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
			ArrayList <Person> peopleHere = this.personsToAdminBoundary.get(l);
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
				for(Location l: adminBoundaries) {
					l.updatePersonsHere();
					}
			}
			
		};
		schedule.scheduleRepeating(0, this.param_schedule_updating_locations, updateLocationLists);
		
		if (this.params.covidTesting) {
			CovidSpuriousSymptomsList = new ArrayList <CoronavirusSpuriousSymptom> ();
			schedule.scheduleRepeating(CovidSpuriousSymptoms.createSymptomObject(this));
			schedule.scheduleRepeating(CovidTesting.Testing(this), this.param_schedule_COVID_Testing, params.ticks_per_day);
			
			Logging CovidTestLogger = new Logging();
			Logging.CovidTestReporter CovidTestReporter = CovidTestLogger.new CovidTestReporter(this);
			schedule.scheduleRepeating(CovidTestReporter, this.param_schedule_reporting, params.ticks_per_day);
			}
		// =============================== Schedule demography events if using ============================================================
		if (this.params.demography) {
			Demography myDemography = new Demography();
			for(Person a: agents) {
				// Trigger the aging process for this person
				Demography.Aging agentAging = myDemography.new Aging(a, params.ticks_per_day);
				schedule.scheduleOnce(a.getBirthday()*params.ticks_per_day, this.param_schedule_reporting, agentAging);
				// Trigger the process to determine mortality each year
				Demography.Mortality agentMortality = myDemography.new Mortality(a, params.ticks_per_day, this);
				schedule.scheduleOnce(0, this.param_schedule_reporting, agentMortality);
				// if biologically female, trigger checks for giving birth each year
				if (a.getSex().equals(SEX.FEMALE)) {
					Demography.Births agentBirths = myDemography.new Births(a, params.ticks_per_day, this);
					schedule.scheduleOnce(0, this.param_schedule_reporting, agentBirths);
				}
			}
			Logging logger = new Logging();
			Logging.BirthRateReporter birthRateLog = logger.new BirthRateReporter(this);
			// schedule the birth rate reporter (birthRateOutputFilename)
			schedule.scheduleOnce(params.ticks_per_year, this.param_schedule_reporting, birthRateLog);
			// schedule the 'other deaths' reporter (otherIncDeathOutputFilename)
			schedule.scheduleRepeating(Logging.ReportOtherIncidenceOfDeath(this), this.param_schedule_reporting, params.ticks_per_day);
		}
		// =============================== Schedule core logging events ==================================================================
		// Report on the age sex breakdown of the population (populationOutputFilename)
		schedule.scheduleRepeating(Logging.ReportPopStructure(this), this.param_schedule_reporting, params.ticks_per_day);
		
		// Report on the number of cases by type and their location (casesPerAdminZoneFilename)
		schedule.scheduleRepeating(Logging.ReportCovidCasesByTypeAndLocation(this), this.param_schedule_reporting, params.ticks_per_day);

		// Report on the breakdown of population size by space (adminZonePopSizeOutputFilename)
		schedule.scheduleRepeating(Logging.ReportAdminZonePopulationSize(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the percent of the population with COVID by space (adminZoneCovidPrevalenceOutputFilename)
		schedule.scheduleRepeating(Logging.ReportPercentInAdminZoneWithCovid(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the age-sex structure of each admin zone (adminZonePopBreakdownOutputFilename)
		schedule.scheduleRepeating(Logging.ReportAdminZoneAgeSexBreakdown(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the incidence of COVID death (covidIncDeathOutputFilename)
		schedule.scheduleRepeating(Logging.ReportCovidIncidenceOfDeath(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the incidence of COVID (covidIncOutputFilename)
		schedule.scheduleRepeating(Logging.ReportIncidenceOfCovid(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the number of COVID counts in each area (covidCountsOutputFilename)
		schedule.scheduleRepeating(Logging.ReportCovidCounts(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the number of COVID counts in each occupation (covidByEconOutputFilename)
		schedule.scheduleRepeating(Logging.ReportCovidCountsByOccupation(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the percent of COVID cases that are fatal per admin zone (adminZonePercentCovidCasesFatalOutputFilename)
		schedule.scheduleRepeating(Logging.ReportPercentOfCovidCasesThatAreFatalPerAdminZone(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Report on the prevalence of COVID death per admin zone (adminZonePercentDiedFromCovidOutputFilename)
		schedule.scheduleRepeating(Logging.adminZonePercentDiedFromCovidOutputFilename(this), this.param_schedule_reporting, params.ticks_per_day);
				
		// Schedule the resetting of COVID reporting properties in the agents 
		schedule.scheduleRepeating(Logging.ResetLoggedProperties(this), this.param_schedule_reporting_reset, params.ticks_per_day);

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
				
				for(Location l: adminBoundaries){
					s += time + "\t" + l.metricsToString() + "\n";
					l.refreshMetrics();
				}
				
				ImportExport.exportMe(outputFilename, s, timer);
				
				System.out.println("Day " + time + " finished");
			}
		};
		schedule.scheduleRepeating(reporter, this.param_schedule_reporting, params.ticks_per_day);

	}
	
	public void load_population(String agentsFilename){
		try {
			
			// holders for construction
			agents = new ArrayList <Person> ();
			households = new ArrayList <Household> ();
			
			// initialise the holder
			personsToAdminBoundary = new HashMap <Location, ArrayList<Person>>();
			for(Location l: adminBoundaries){
				personsToAdminBoundary.put(l, new ArrayList <Person> ());
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

				// target admin zone
				String myAdminZoneName = bits[5]; // TODO AN ABOMINATION, STANDARDISE IT
				Location myAdminZone = params.adminZones.get(myAdminZoneName);

				boolean schoolGoer = bits[8].equals("1");
				
				// if the Household doesn't already exist, create it and save it
				if(h == null){
					
					// set up the Household
					h = new Household(hhName, myAdminZone);
					rawHouseholds.put(hhName, h);
					households.add(h);
				}
				
				// identify the location in which the person, possibly, works
				
				// set up the person
				// create a random birthday
				int birthday = this.random.nextInt(365);

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
						birthday, // birthday to update population
						SEX.getValue(bits[3].toLowerCase()), // sex
						OCCUPATION.getValue(bits[6].toLowerCase()), // lower case all of the job titles
						schoolGoer,
						h,
						this
						);
				h.addPerson(p);
				p.setActivityNode(movementFramework.getHomeNode());
				agents.add(p);
				personsToAdminBoundary.get(myAdminZone).add(p);
				
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
		int numDays = 70; // by default, one week
		double myBeta = .3;

		long seed = 12345;
		String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed;
		String infectionsOutputFilename = "infections_" + myBeta + "_" + numDays + "_" + seed + ".txt"; 
		String paramsFilename = "src/main/resources/params.txt";

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
		}
				
		
		long startTime = System.currentTimeMillis(); // wallclock measurement of time - embarrassing.
				
		/*
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = s + outputPrefix + i + outputSuffix;
				
		 */

		// set up the simulation

		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename, true), outputFilename);


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