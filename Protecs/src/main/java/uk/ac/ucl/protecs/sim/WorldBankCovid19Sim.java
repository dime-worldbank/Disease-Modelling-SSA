package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import uk.ac.ucl.protecs.behaviours.*;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusInfection;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.DummyDiseaseProgressionFramework;
import uk.ac.ucl.protecs.objects.diseases.DummyNonCommunicableDisease;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location;
import uk.ac.ucl.protecs.objects.locations.Workplace;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.SpuriousSymptomDiseaseProgressionFramework;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CoronavirusDiseaseProgressionFramework;
import sim.engine.SimState;
import sim.engine.Steppable;

public class WorldBankCovid19Sim extends SimState {
	// Create a boolean for developing disease modularity
	public boolean developingModularity = false;

	// the objects which make up the system
	public ArrayList <Person> agents = null;
	public ArrayList <Household> households = null;
	public ArrayList <Workplace> workplaces = null;

	public ArrayList <Disease> infections = null;
	public HashSet <OCCUPATION> occupationsInSim = null;
	public Random random;
	
	ArrayList <Location> adminBoundaries = null;
	
	HashMap <Location, ArrayList<Person>> personsToAdminBoundary = null; 
	
	public MovementBehaviourFramework movementFramework = null;
	public CoronavirusDiseaseProgressionFramework infectiousFramework = null;
	public SpuriousSymptomDiseaseProgressionFramework spuriousFramework = null;
	public DummyDiseaseProgressionFramework dummyFramework = null;
	public Params params = null;
	public boolean lockedDown = false;
	// the names of file names of each output filename		
	public String outputFilename = null;
	public String covidIncOutputFilename = null; 
	public String populationOutputFilename = null;
	public String covidIncDeathOutputFilename = null;
	public String otherIncDeathOutputFilename = null;
	public String birthRateOutputFilename = null;
	public String adminZonePopSizeOutputFilename = null;
	public String casesPerAdminZoneFilename = null; 
	public String infections_export_filename = null;
	public String adminZoneCovidPrevalenceOutputFilename = null;
	public String adminZonePercentDiedFromCovidOutputFilename = null;
	public String adminZonePercentCovidCasesFatalOutputFilename = null;
	public String adminZonePopBreakdownOutputFilename = null;
	public String sim_info_filename = null;
	public String covidCountsOutputFilename = null;
	public String covidByEconOutputFilename = null;
	public String covidTestingOutputFilename = null;
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
	
	// Create a enum list of diseases modelled currently, these will be used to categorise any infections a person may get over the course of the simulation.
	public enum DISEASE{
		DUMMY_NCD("DUMMY_NCD"), DUMMY_INFECTIOUS("DUMMY_INFECTIOUS"), COVID("COVID-19"), COVIDSPURIOUSSYMPTOM("COVID-19_SPURIOUS_SYMPTOM");

        public String key;
     
        DISEASE(String key) { this.key = key; }
    
        public static DISEASE getValue(String x) {
        	switch (x) {
        	case "DUMMY_NCD":
        		return DUMMY_NCD;
        	case "DUMMY_INFECTIOUS":
        		return DUMMY_INFECTIOUS;
        	case "COVID-19":
        		return COVID;
        	case "COVID-19_SPURIOUS_SYMPTOM":
        		return COVIDSPURIOUSSYMPTOM;
        	default:
        		throw new IllegalArgumentException();
        	}
        }
	}

	
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
		this.random = new Random(this.seed());
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
		infectiousFramework = new CoronavirusDiseaseProgressionFramework(this);
		spuriousFramework = new SpuriousSymptomDiseaseProgressionFramework(this);
		if (developingModularity) {
			dummyFramework = new DummyDiseaseProgressionFramework(this);
		}
		// RESET SEED
		random = new Random(this.seed());

		// initialise the agent storage
		// holders for construction
		agents = new ArrayList <Person> ();
		households = new ArrayList <Household> ();
		workplaces = new ArrayList <Workplace> ();

		// initialise the holder
		personsToAdminBoundary = new HashMap <Location, ArrayList<Person>>();
		// initialise occupations in sim
		occupationsInSim = new HashSet <OCCUPATION>(); 
		
		// load the population
		LoadPopulation.load_population(params.dataDir + params.population_filename, this);
		
		// if there are no agents, SOMETHING IS WRONG. Flag this issue!
		if(agents.size() == 0) {
			System.out.println("ERROR *** NO AGENTS LOADED");
			System.exit(0);
		}

		// set up the social networks
		//InteractionUtilities.create_work_bubbles(this);
		//InteractionUtilities.create_community_bubbles(this);

		// RESET SEED

		// set up the infections
		infections = new ArrayList <Disease> ();
		// TODO expand this to include all infection types
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
				CoronavirusInfection inf = new CoronavirusInfection(p, null, infectiousFramework.getInfectedEntryPoint(l), this, 0);
				// update this person's properties
				
				// update this person's properties so we can keep track of the number of cases etc				
				if (inf.getBehaviourName().equals("asymptomatic")) {
					inf.setAsympt();
				}
				else {
					inf.setMild();
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
		if (developingModularity) {
			double num_to_seed = agents.size() / 10;
			double i = 0.0;
			for (Person a: agents) {
				if (i < num_to_seed) {
				DummyNonCommunicableDisease inf = new DummyNonCommunicableDisease(a, null, dummyFramework.getStandardEntryPoint(), this, 0);
				schedule.scheduleOnce(1, param_schedule_infecting, inf);
				i ++ ;
				}
				else break;
			}
		}

		if (this.params.covidTesting) {
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
				int currentTime = (int) (arg0.schedule.getTime() / params.ticks_per_day); 
				if(params.lockdownChangeList.size() == 0)
					return;
				double nextChange = params.lockdownChangeList.get(0);
				if(currentTime >= nextChange) {
					params.lockdownChangeList.remove(0);
					lockedDown = !lockedDown;
					if (lockedDown) System.out.println("Going into lockdown at day " + currentTime);
					else System.out.println("Exiting lockdown at day " + currentTime); 
//					return;
				}
				
			}
			
		};
		schedule.scheduleRepeating(0, this.param_schedule_lockdown, lockdownTrigger);
		

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

		// set up the simulation

		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename, true), outputFilename);


		System.out.println("Loading...");

		// ensure that all parameters are set
		mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
		mySim.targetDuration = numDays;
		mySim.start(); // start the simulation
				
		System.out.println("How many agents? " + mySim.agents.size());
		System.out.println("Running...");

		// run the simulation
		while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
		}
				
		// end of wallclock determination of time
		long endTime = System.currentTimeMillis();
		mySim.timer = endTime - startTime;
		
		System.out.println("...run finished after " + mySim.timer + " ms");
		ImportExport.exportInfections(outputFilename + "_infections.txt", mySim.infections);

	}


}