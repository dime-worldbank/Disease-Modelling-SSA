package sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import behaviours.InfectiousBehaviourFramework;
import behaviours.MovementBehaviourFramework;
import ec.util.MersenneTwisterFast;
import objects.Household;
import objects.Infection;
import objects.Location;
import objects.Person;
import sim.engine.SimState;
import sim.engine.Steppable;

public class WorldBankCovid19Sim extends SimState {

	// the objects which make up the system
	public ArrayList <Person> agents;
	public ArrayList <Household> households;
	public ArrayList <Infection> infections;
	
	ArrayList <Location> districts;
	
	HashMap <Location, ArrayList<Person>> personsToDistrict; 
	
	public MovementBehaviourFramework movementFramework;
	public InfectiousBehaviourFramework infectiousFramework;
	public Params params;
	public boolean lockedDown = false;
		
	public String outputFilename;
	public String infections_export_filename;
	public String dedectedCovidFilename;
	public String spatialDedectedCovidFilename;
	public String sim_info_filename;
	int targetDuration = 0;

	
	// ordering information
	public static int param_schedule_lockdown = 0;
	public static int param_schedule_movement = 1;
	public static int param_schedule_updating_locations = 5;
	public static int param_schedule_infecting = 10;
	public static int param_schedule_reporting = 100;
	
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
		this.dedectedCovidFilename = outputFilename + "_detected_covid_cases.txt";
		this.spatialDedectedCovidFilename = outputFilename + "_spatial_detected_covid_cases.txt";
	}
	
	public void start(){
		
		// copy over the relevant information
		districts = new ArrayList <Location> (params.districts.values());
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new InfectiousBehaviourFramework(this);
		
		// load the population
		load_population(params.dataDir + params.population_filename);
		assert (agents.size() > 0) : "ERROR *** NO AGENTS LOADED";
		// if there are no agents, SOMETHING IS WRONG. Flag this issue!
		if(agents.size() == 0) {
			System.out.println("ERROR *** NO AGENTS LOADED");
			System.exit(0);
		}

		// set up the social networks
		//InteractionUtilities.create_work_bubbles(this);
		//InteractionUtilities.create_community_bubbles(this);

		// RESET SEED
		random = new MersenneTwisterFast(this.seed());

		// set up the infections
		infections = new ArrayList <Infection> ();
		ArrayList <Location> unactivatedDistricts = new ArrayList <Location> (districts);
		for(Location l: params.lineList.keySet()){
			
			// activate this location
			l.setActive(true);
			unactivatedDistricts.remove(l);
			
			// number of people to infect
			int countInfections = params.lineList.get(l) * params.lineListWeightingFactor;
			
			// list of infected people
			HashSet <Person> newlyInfected = new HashSet <Person> ();
			
			// number of people present
			ArrayList <Person> peopleHere = this.personsToDistrict.get(l);
			int numPeopleHere = peopleHere.size();//l.getPeople().size();
			assert (numPeopleHere > 0): "A location has no one in it, this can't happen";
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
				Infection inf = new Infection(p, null, infectiousFramework.getInfectedEntryPoint(l), this);
				inf.time_contagious = 0;
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
		
		// SCHEDULE testing
		Steppable testing_for_covid = new Steppable() {

					@Override
					public void step(SimState arg0) {
						// get the simulation time
						int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
						// get the index for the test numbers
						int index_for_test_number = params.test_dates.indexOf(time);
						// find the number of tests per 1000 to test today
						int number_of_tests_today = params.number_of_tests_per_day.get(index_for_test_number);
						// we only want to test people who are alive and administer the tests per 1000 based on this 
						Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>> is_elligable_for_testing_map = agents.stream().collect(
												Collectors.groupingBy(
														Person::isDead,
														Collectors.groupingBy(
																Person::isElligableForTesting,
																Collectors.groupingBy(Person::inADistrictTesting,
												Collectors.toList()
												)
											)
										)
								);
						// create a random state (I need to link this to the existing random state but don't know how)
						Random testing_random = new Random(sim.WorldBankCovid19Sim.this.seed());
						int number_of_positive_tests = 0;
						double percent_positive = 0;
						// generate a list of people to test today
						try {
							List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true).get(true), number_of_tests_today, testing_random);
						// create a counter for the number of positive tests
						double test_accuracy = 0.97;
						// iterate over the list of people to test and perform the tests
						for (Person person:people_tested) {
							if(person.hasCovid()) {
								if (random.nextDouble() < test_accuracy) {
									number_of_positive_tests ++;
									person.setTestedPositive();
									// after they have tested positive, they no longer need to be tested again
									person.notElligableForTesting();
								}
							}
						}
						if (number_of_tests_today > 0) {
							percent_positive = (double) number_of_positive_tests / (double) number_of_tests_today; 
						}}
						catch (Exception e) {
						}
						String t = "\t";
						
						String detected_covid_output = "";
						if (time == 0) {
							detected_covid_output += "day" + t + "number_of_detected_cases" + t + "number_of_tests" + t + "fraction_positive" + "\n"+ String.valueOf(time) + t + number_of_positive_tests + t + number_of_tests_today + t + percent_positive+ "\n";
						}
						else {
							detected_covid_output += String.valueOf(time) + t + number_of_positive_tests + t + number_of_tests_today + t + percent_positive + "\n";
						}
						exportMe(dedectedCovidFilename, detected_covid_output);
//						create a list of district names to iterate over for our logging
						List <String> districtList = Arrays.asList(
								"d_1", "d_2", "d_3", "d_4", "d_5", "d_6", "d_7", "d_8", "d_9", "d_10", "d_11", "d_12", "d_13", "d_14", "d_15", 
								"d_16", "d_17", "d_18", "d_19", "d_20", "d_21", "d_22", "d_23", "d_24", "d_25", "d_26", "d_27", "d_28", "d_29", 
								"d_30", "d_31", "d_32", "d_33", "d_34", "d_35", "d_36", "d_37", "d_38", "d_39", "d_40", "d_41", "d_42", "d_43", 
								"d_44", "d_45", "d_46", "d_47", "d_48", "d_49", "d_50", "d_51", "d_52", "d_53", "d_54", "d_55", "d_56", "d_57", 
								"d_58", "d_59", "d_60");
						// create list to store the number of cases per district
						ArrayList <Integer> covidTestedPositiveArray = new ArrayList<Integer>();

						// create a function to group the population by location, whether they are alive and if they have covid and if this is a new case
						Map<String, Map<Boolean, Map<Boolean, Long>>> location_alive_tested_pos_for_Covid_map = agents.stream().collect(
								Collectors.groupingBy(
										Person::getCurrentDistrict, 
											Collectors.groupingBy(
														Person::isDead,
														Collectors.groupingBy(
																Person::hasTestedPos,
												Collectors.counting()
												)
										)
								)
								);
//						We now iterate over the districts, to find the current state of the epidemic
						for (String district: districtList) {
							// get the current number of cases in each district
							try {
								covidTestedPositiveArray.add(location_alive_tested_pos_for_Covid_map.get(district).get(false).get(true).intValue());
							} catch (Exception e) {
								// No one in population met criteria
								covidTestedPositiveArray.add(0);
							}
						}
						
						String spatialOutput = "";
						// format the file
						String tabbedDistrictNames = "";
						for (String district: districtList) {tabbedDistrictNames += t + district;}
						if (time == 0) {
							spatialOutput += "day" + tabbedDistrictNames + "\n" + String.valueOf(time);
						}
						else {
							spatialOutput += String.valueOf(time);
						}
						// store total number of positive tests in district
						for (int val: covidTestedPositiveArray){
							spatialOutput += t + String.valueOf(val);
						}
						spatialOutput += "\n";
						exportMe(spatialDedectedCovidFilename, spatialOutput);
						try {
							List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true).get(true), number_of_tests_today, testing_random);
						for (Person person:people_tested) {
							if(person.hasTestedPos()) {
								person.removeTestedPositive();
								}
						}
					} catch (Exception e) {}
				}
					
				};
		schedule.scheduleRepeating(testing_for_covid, this.param_schedule_reporting, params.ticks_per_day);

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
		
		// SCHEDULE LOCKDOWNS
		
		Steppable spuriosSymptomTrigger = new Steppable() {

			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);

				// we only want to assign symptoms to people without symptomatic covid. Create a map to find those alive, without mild, severe or critical covid.
				Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>> has_non_asymptomatic_covid = agents.stream().collect(
									Collectors.groupingBy(
												Person::isDead,
												Collectors.groupingBy(
														Person::hasMild,
														Collectors.groupingBy(
																Person::hasSevere,
																Collectors.groupingBy(
																		Person::hasCritical,
										Collectors.toList()
										)
								)
						)
					)
					);
				// todo fix this
				double number_people_with_symptoms_as_double = (double) params.rate_of_spurious_symptoms * has_non_asymptomatic_covid.get(false).get(false).get(false).get(false).size();
				// create the number of people to develop symptoms
				int number_people_with_symptoms = (int) number_people_with_symptoms_as_double;
				// create a random state (I need to link this to the existing random state but don't know how)
				Random symptom_random = new Random(sim.WorldBankCovid19Sim.this.seed());
				// generate a list of people develop symptoms today
				List<Person> people_developing_symptoms = pickRandom(has_non_asymptomatic_covid.get(false).get(false).get(false).get(false), number_people_with_symptoms, symptom_random);
				
			for (Person p: people_developing_symptoms) {
				p.elligableForTesting();
				// Assume people have these symptoms for a week
				p.setSymptomRemovalDate(time + 7);
				p.hasSpuriousSymptoms();
			}
			// we also want people's spurios symptoms to dissapear over time, find out who has these symptoms
			Map<Boolean, Map<Boolean, List<Person>>> has_spurios_symptoms = agents.stream().collect(
								Collectors.groupingBy(
											Person::isDead,
											Collectors.groupingBy(
													Person::hasSpuriousSymptoms,
									Collectors.toList()
									)
							)
				);
			List<Person> people_with_symptoms = has_spurios_symptoms.get(false).get(true);
			try {
			for (Person p: people_with_symptoms) {
				if (p.timeToRemoveSymptoms < time) {
					p.notElligableForTesting();
					p.removeSpuriousSymptoms();
				}
			}
			} catch (Exception e) {
				// No one to remove spurious symptoms from
			}
			
		}
		};
		schedule.scheduleRepeating(0, this.param_schedule_lockdown, spuriosSymptomTrigger);

		
		
		String filenameSuffix = (this.params.ticks_per_day * this.params.infection_beta) + "_" 
				+ this.params.lineListWeightingFactor + "_"
				+ this.targetDuration + "_"
				+ this.seed() + ".txt";
		//outputFilename = "results_" + filenameSuffix;
		infections_export_filename = "infections_" + filenameSuffix;

		exportMe(outputFilename, Location.metricNamesToString());
		Steppable reporter = new Steppable(){

			@Override
			public void step(SimState arg0) {
				
				String s = "";
				
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				for(Location l: districts){
					s += time + "\t" + l.metricsToString() + "\n";
					l.refreshMetrics();
				}
				
				exportMe(outputFilename, s);
				
				System.out.println("Day " + time + " finished");
			}
		};
		schedule.scheduleRepeating(reporter, this.param_schedule_reporting, params.ticks_per_day);
		random = new MersenneTwisterFast(this.seed());
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

				// create and save the Person agent
				Person p = new Person(Integer.parseInt(bits[1]), // ID 
						Integer.parseInt(bits[2]), // age
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
			
	
	void reportOnInfected(){
		String makeTerribleGraphFilename = "nodes_latest_16.gexf";
		try {
			
			System.out.println("Printing out infects? from " + makeTerribleGraphFilename);
			
			// shove it out
			BufferedWriter badGraph = new BufferedWriter(new FileWriter(makeTerribleGraphFilename));

			//badGraph.write("ID;econ;age;infect;time;source");
			badGraph.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gexf xmlns=\"http://www.gexf.net/1.1draft\" version=\"1.1\">\n" + 
					"<graph mode=\"static\" defaultedgetype=\"directed\">\n" + 
					"<attributes class=\"node\" type=\"static\"> \n" +
				     "<attribute id=\"infected\" title=\"Infected\" type=\"string\"/>\n</attributes>\n");
			badGraph.write("<nodes>\n");
			for(Person p: agents){
				String myStr = p.toString();
				//myStr += ";" + p.getEconStatus() + ";" + p.getAge() + ";" + p.getInfectStatus();
				
				if(p.getInfection() != null){
					Person source = p.getInfection().getSource();
					String sourceName = null;
					if(source != null)
						sourceName = source.toString();
					//myStr += ";" + p.getInfection().getStartTime() + ";" + sourceName;
					myStr = p.getInfection().getBehaviourName();
				}
				else
					//myStr += "Susceptible;;";
					myStr = "Susceptible";
/*				for(Person op: p.getWorkBubble()){
					myStr += ";" + op.toString();
				}
	*/			
				badGraph.write("\t<node id=\"" + p.getID() + "\" label=\"" + p.toString() + 
						"\"> <attvalue for=\"infected\" value=\"" +myStr +  "\"/></node>\n");

				//badGraph.write("\n" + myStr);
			}
			badGraph.write("</nodes>\n");
			badGraph.write("<edges>\n");
			for(Person p: agents){
				int myID = p.getID();
				for(Person op: p.getWorkBubble()){
					badGraph.write("\t<edge source=\"" + myID + "\" target=\"" + op.getID() + "\" weight=\"1\" />\n");
				}
			}
			
			badGraph.write("</edges>\n");
			badGraph.write("</graph>\n</gexf>");
			badGraph.close();
		} catch (Exception e) {
			System.err.println("File input error: " + makeTerribleGraphFilename);
		}
	}
	
	void exportMe(String filename, String output){
		try {
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));
			if(timer > 0)
				exportFile.write(timer + "\n");
			exportFile.write(output);
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	void exportDailyReports(String filename){
		try {
			
			System.out.println("Printing out infects? from " + filename);
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(filename, true));

			String header = "index\t";
			for(int p = 0; p < params.exportParams.length; p++){
				header += params.exportParams[p].toString() + "\t";
			}
			exportFile.write(header);
			
			for(int i = 0; i < dailyRecord.size(); i++){
				HashMap <String, Double> myRecord = dailyRecord.get(i);
				String s = this.seed() + "\t";
				for(String paramName: params.exportParams){
					s += myRecord.get(paramName).toString() + "\t";
				}
				exportFile.write("\n" + s);
			}
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + filename);
		}
	}
	
	void exportInfections() {
		try {
			
			System.out.println("Printing out INFECTIONS to " + infections_export_filename);
			
			// shove it out
			BufferedWriter exportFile = new BufferedWriter(new FileWriter(infections_export_filename, true));
			exportFile.write("Host\tSource\tTime\tLocOfTransmission" + 
					"\tContagiousAt\tSymptomaticAt\tSevereAt\tCriticalAt\tRecoveredAt\tDiedAt\tYLD\tYLL\tDALYs"
					+ "\n");
			
			// export infection data
			for(Infection i: infections) {
				
				String rec = i.getHost().getID() + "\t";
				
				// infected by:
				
				Person source = i.getSource();
				if(source == null)
					rec += "null";
				else
					rec += source.getID();
				
				rec += "\t" + i.getStartTime() + "\t";
				
				// infected at:
				
				Location loc = i.getInfectedAtLocation();
				
				if(loc == null)
					rec += "SEEDED";
				else if(loc.getRootSuperLocation() != null)
					rec += loc.getRootSuperLocation().getId();
				else
					rec += loc.getId();
				
				// progress of disease: get rid of max vals
				
				if(i.time_contagious == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_contagious;
				
				if(i.time_start_symptomatic == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_symptomatic;
				
				if(i.time_start_severe == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_severe;
				
				if(i.time_start_critical == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_start_critical;
				
				if(i.time_recovered == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_recovered;
				
				if(i.time_died == Double.MAX_VALUE)
					rec += "\t-";
				else
					rec += "\t" + (int) i.time_died;
				// create variables to calculate DALYs, set to YLD zero as default
				double yld = 0.0;
				// DALY weights are taken from https://www.ssph-journal.org/articles/10.3389/ijph.2022.1604699/full , exact same DALY weights used 
				// here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8212397/ and here https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8844028/ , seems like these are common
				// TODO: check if these would be representative internationally
				// TODO: Find DALYs from long COVID
				double critical_daly_weight = 0.655;
				double severe_daly_weight = 0.133;
				double mild_daly_weight = 0.051;

				// calculate DALYs part 1: YLD working from the most serious level of infection
				// YLD = fraction of year with condition * DALY weight
				if (i.time_start_critical < Double.MAX_VALUE)
					// calculate yld between the onset of critical illness to death or recovery
					if (i.time_died < Double.MAX_VALUE)
						yld += ((i.time_died - i.time_start_critical) / 365) * critical_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_critical) / 365) * critical_daly_weight;
				if (i.time_start_severe < Double.MAX_VALUE)
					// calculate yld between the progression from a severe case to a critical case or recovery
					if (i.time_start_critical < Double.MAX_VALUE)
						yld += ((i.time_start_critical - i.time_start_severe) / 365) * severe_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_severe) / 365) * severe_daly_weight;
				if (i.time_start_symptomatic < Double.MAX_VALUE)
					// calculate yld between the onset of symptoms to progression to severe case or recovery
					if (i.time_start_severe < Double.MAX_VALUE)
						yld += ((i.time_start_severe - i.time_start_symptomatic) / 365) * mild_daly_weight;
					else if (i.time_recovered < Double.MAX_VALUE)
						yld += ((i.time_recovered - i.time_start_symptomatic) / 365) * mild_daly_weight;
				if(yld == 0.0)
					rec += "\t-";
				else
					rec += "\t" + (double) yld;
				// calculate YLL (basic)
				// YLL = Life expectancy in years - age at time of death, if age at death < Life expectancy else 0
				int lifeExpectancy = 62;  // according to world bank estimate https://data.worldbank.org/indicator/SP.DYN.LE00.IN?locations=ZW
				double yll = 0;
				if(i.time_died == Double.MAX_VALUE)
					rec += "\t-";
				else {
					yll = lifeExpectancy - i.getHost().getAge();
					// If this person's age is greater than the life expectancy of Zimbabwe, then assume there are no years of life lost
					if (yll < 0)
						yll = 0;
					rec += "\t" + (double) yll;
				}
				// Recored DALYs (YLL + YLD)
				if (yll + yld == 0.0)
					rec += "\t-";
				else
					rec += "\t" + (double) (yll + yld);
				
				rec += "\n";
				
				exportFile.write(rec);
				
			}
			
			exportFile.close();
		} catch (Exception e) {
			System.err.println("File input error: " + infections_export_filename);
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
	private static <E> List<E> pickRandom(List<E> list, int n, Random rand) {
		  return new Random().ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
		}
	
	/**
	 * In an ideal scenario, there would be multiple ways to run this with arguments. EITHER: <ul>
	 * 	<li> pass along a parameter file which contains all of these values or;
	 *  <li> pass along the minimal metrics required (e.g. beta, length of run).
	 *  </ul>
	 * @param args
	 */
	public static void main(String [] args){
		
		// default settings in the absence of commands!
		int numDays = 7; // by default, one week
		double myBeta = .016;
		long seed = 12345;
		String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".txt";
		String infectionsOutputFilename = ""; 
		String paramsFilename = "data/configs/params.txt";
		
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
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename), outputFilename);


		System.out.println("Loading...");

		// ensure that all parameters are set
		mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
		mySim.targetDuration = numDays;
		
		mySim.start(); // start the simulation
		
		mySim.infections_export_filename = infectionsOutputFilename; // overwrite the export filename
		
		System.out.println("Running...");

		// run the simulation
		while(mySim.schedule.getTime() < Params.ticks_per_day * numDays && !mySim.schedule.scheduleComplete()){
			mySim.schedule.step(mySim);
			double myTime = mySim.schedule.getTime();
		}
		
		mySim.exportInfections();
		
		// end of wallclock determination of time
		long endTime = System.currentTimeMillis();
		mySim.timer = endTime - startTime;
		
		System.out.println("...run finished after " + mySim.timer + " ms");
	}

	void exportSimInformation() {
		// Write the following information to a .txt file: Seed, number of agents, simulation duration
		// TODO: discuss what else would be useful for the output here
		try {
		System.out.println("Printing out SIMULATION INFORMATION to " + sim_info_filename);
		
		// Create new buffered writer to store this information in
		BufferedWriter exportFile = new BufferedWriter(new FileWriter(sim_info_filename, true));
		// write a new heading 
		exportFile.write("Seed\tNumberOfAgents\tSimuilationDuration"
				+ "\n");
		// Create variable rec to store the information
		String rec = "";
		// get and record the simulation seed
		rec += this.seed() + "\t";
		// get and record the number of agents
		rec += this.agents.size() + "\t";
		// get and record the simulation duration
		rec += this.targetDuration + "\t";
		
		exportFile.write(rec);
		exportFile.close();
		
		} catch (Exception e) {
			System.err.println("File input error: " + sim_info_filename);
		}

		
	}
}