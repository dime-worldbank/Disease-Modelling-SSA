package main.java.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import main.java.behaviours.*;
import main.java.objects.*;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import main.java.sim.ImportExport;

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
	// create a variable to determine whether the model will cause additional births and deaths	
	public boolean demography = false;
	// create a variable to determine whether seroprevalance sampling will go ahead
	public boolean seroSample = false;
	public int numDaysToSampleSeroprevalence;
	public int numSamplesPerDay;
	public Double day_to_start_writing_sero_sample_file;
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
	public String detectedCovidFilename;
	public String spatialdetectedCovidFilename;
	public String overallSeroprevalence;
	public String detectedSeroprevalenceFilename;
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
	public WorldBankCovid19Sim(long seed, Params params, String outputFilename, boolean demography) {
		super(seed);
		this.params = params;
		this.outputFilename = outputFilename + ".txt";
		this.demography = demography;
		this.covidIncOutputFilename = outputFilename + "_Incidence_Of_Covid_" + ".txt"; 
		this.populationOutputFilename = outputFilename + "_Overall_Demographics_" + ".txt";
		this.overallSeroprevalence = outputFilename + "_Overall_Seroprevalence_" + ".txt";
		this.detectedSeroprevalenceFilename = outputFilename + "_Detected_Seroprevalence_.txt";
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
		this.detectedCovidFilename = outputFilename + "_detected_covid_cases.txt";
		this.spatialdetectedCovidFilename = outputFilename + "_spatial_detected_covid_cases.txt";
		
	}
	
	public void start(){
		
		// copy over the relevant information
		districts = new ArrayList <Location> (params.districts.values());
		
		// set up the behavioural framework
		movementFramework = new MovementBehaviourFramework(this);
		infectiousFramework = new InfectiousBehaviourFramework(this);
		
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
				// update this person's properties
				
				inf.time_contagious = 0;
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
		
		// Functions that control the background demography of the model
		// Create a function to create births in the population
		Steppable createBirths = new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				// create a list of babies
				ArrayList<Person> newBirths = new ArrayList <Person> ();
				//	get a reference to the current simulation day		
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				// create an id_offset variable to assign the babies a unique id
				int id_offset = 0;
				// iterate over all people (would make sense to do this for woman only)
				for(Person p:agents) {
					// create a variable to predict the likelihood of pregnancy
					double myPregnancyLikelihood = 0.0;
					// get this person's sex
					String sex = p.getSex();
					// if this person is female and gave birth over a year ago the 9 months pregnancy period reset their 
					//ability to give birth
					if (sex.equals("female") & (p.getDateGaveBirth() < time - 365 - 9 * 30)) {
						p.ableToGiveBirth();
					}
					// get a reference to their age
					int age = p.getAge();
					// if they are a woman, are alive and didn't give birth within the last year consider whether they
					// will give birth today
					if(sex.equals("female") & p.getAlive() & !(p.gaveBirthLastYear())){
						// get the probability of giving birth at this age
						myPregnancyLikelihood = params.getBirthLikelihoodByAge(
								params.prob_birth_by_age, age);
						if(random.nextDouble() < myPregnancyLikelihood) {
							// this woman has given birth, update their birth status and note the time of birth
							p.gaveBirth(time);
							// create attributed for the newborn, id, age, sex, occupation status (lol), their 
							// household (assume this is the mothers), where the baby is, that it's not going to school
							// and a copy of the simulation, then create the person
							int new_id = agents.size() + 1 + id_offset;
							int baby_age = 0;
							List<String> sexList = Arrays.asList("male", "female");
							String sexAssigned = sexList.get(random.nextInt(sexList.size()));
							String babiesJob = "Not working, inactive, not in universe".toLowerCase();
							Household babyHousehold = p.getHouseholdAsType();
							Location babyDistrict = p.getLocation();
							boolean babySchooling = false;
							int birthday = time;
							Person baby = new Person(new_id, // ID 
									baby_age, // age
									birthday, // date of birth
									sexAssigned, // sex
									babiesJob, // lower case all of the job titles
									babySchooling,
									babyHousehold, // household
									returnSim()
									);				
							// update the household and location to include the baby
							babyHousehold.addPerson(baby);
							baby.setLocation(babyDistrict);
							// the baby has decided to go home
							baby.setActivityNode(movementFramework.getHomeNode());
							// store the baby in the newBirths array
							newBirths.add(baby);
							// Add the person to the district
							personsToDistrict.get(babyDistrict.getRootSuperLocation()).add(baby);
							// ++ the id_offset so the next baby will have an id
							id_offset ++;
						}
					}
				}
				// store the newborns in the population
				for (Person baby:newBirths) {
					agents.add(baby);
//					System.out.print("baby " + baby.getID() + " has been born \n");
					}
				
			}
			
		};
		if (this.demography) {
		schedule.scheduleRepeating(createBirths, this.param_schedule_updating_locations, params.ticks_per_day);
		}
		// create a function to report on birth rates
		Steppable reportBirthRates = new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				//	calculate the birth rate in age groups 15-19, 10-14, ..., 45-49
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
				List <Integer> lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
				// create list to store the counts of the number of females alive in each age range and the 
				// number of births in each age range.
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_pregnancy_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::getAlive,
										Collectors.counting()
										)
								)
						)
						);
				// create a function to group the population by sex, age and whether they gave birth
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_gave_birth = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::gaveBirthLastYear,
												Collectors.groupingBy(
														Person::getBirthLogged,
														Collectors.counting()
										)
								)
						)
						)
						);
				
				//	We now iterate over the age ranges, create a variable to keep track of the iterations
				Integer idx = 0;
				for (Integer val: upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer female_count = 0;
					Integer female_gave_birth_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (female, alive)
							female_count += age_sex_alive_map.get("female").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (female, alive and gave birth)
							female_gave_birth_count += age_sex_map_gave_birth.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
					}
					// store what we have found in the lists we created
					female_alive_ages.add(female_count);
					female_pregnancy_ages.add(female_gave_birth_count);
					// update the idx variable for the next iteration
					idx++;
				}
				// calculate the birth rate per 1000 this day
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				String age_dependent_birth_rate = "";

				String t = "\t";
				String age_categories = t + "15_19" + t + "20_24" + t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + "\n";
				if (time == 0) {
					age_dependent_birth_rate += "day" + age_categories + String.valueOf(time);
				}
				else {
					age_dependent_birth_rate += String.valueOf(time);
				}
				age_dependent_birth_rate += t;
				for (int x = 0; x <female_pregnancy_ages.size(); x++){
					double births_in_age = female_pregnancy_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = 0;
					if (female_alive_in_age > 0) {
						result = births_in_age / female_alive_in_age;
						}
	                result *= 1000;
	                age_dependent_birth_rate += t + String.valueOf(result);
				}
				age_dependent_birth_rate += "\n";


				// create a string to store this information in
				// get the day
				
				ImportExport.exportMe(birthRateOutputFilename, age_dependent_birth_rate, timer);
				// to make sure that births aren't counted more than once, update this person's properties
				for (Person p: agents) {
					if(p.gaveBirthLastYear()) {
						p.confirmBirthlogged();
						}
					}
				
			}
			};
		schedule.scheduleRepeating(reportBirthRates, this.param_schedule_reporting, params.ticks_per_day);
		
		// SCHEDULE CHECKS ON MORTALITY AND LOGGING
			
		Steppable checkMortality = new Steppable() {
			@Override
			public void step(SimState arg0) {
				// create a temp variable to be updated when the likelihood of mortality has been determined
				double myMortalityLikelihood = 0.0;
				// iterate over the population
				for(Person p: agents){
					// only determine mortality if this person is alive
					if (p.getAlive()){
						// get the person's age and sex
						String sex = p.getSex();
						int age = p.getAge();
						// based on their properties determine if they die today
						if(sex.equals("male")) {
							myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
									params.prob_death_by_age_male, age);
						}
						else {
							myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
									params.prob_death_by_age_female, age);
						}
						if(random.nextDouble() < myMortalityLikelihood) {
							p.die("other");
						}
					}
				}				
			}
		};
			
		if (this.demography){
		schedule.scheduleRepeating(checkMortality, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		
		// to maintain population structure over time, create a function that will update the age of the population
		Steppable updateAges = new Steppable() {
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by those who are alive and those who's birthday it is
				Map<Boolean, Map<Integer, List<Person>>> birthday_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getAlive,
								Collectors.groupingBy(
										Person::getBirthday
						)
						)
						);
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				List<Person> birthdays = birthday_map.get(true).get(time + 1 % 365);
				try {
					for(Person p: birthdays){
						// update the person's age
						p.updateAge();
					}
				}
				catch (Exception e) {
						// No one had a birthday today, skip.
				}
			}
		};
		
		if (this.demography){
		schedule.scheduleRepeating(updateAges, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		// This function tracks the epidemic over time 
		Steppable testLoggingCases = new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;
				
				//	create a list of district names to iterate over for our logging
				List <String> districtList = myWorld.params.districtNames;
						
				// create list to store the counts of each category of interest. The number of cases, the cumulative number of cases, 
				// the number of cases by type, the number of recoveries, the number of deaths etc... 
				ArrayList <Integer> covidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> cumCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> asymptCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> mildCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> severeCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> criticalCovidCountArray = new ArrayList<Integer>();
				ArrayList <Integer> recoveredCountArray = new ArrayList<Integer>();
				ArrayList<Integer> covidCumulativeDeathCount = new ArrayList<Integer>();
				ArrayList<Integer> covidNewDeathCount = new ArrayList<Integer>();

				// create a function to group the population by location, whether they are alive and if they have covid and if this is a new case
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> location_alive_hasCovid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
														Collectors.groupingBy(
																Person::covidLogCheck,
										Collectors.counting()
										)
								)
						)
						)
						);
				// create a map to count the number of people who have recovered from covid in that district
				Map<String, Map<Boolean, Long>> location_alive_recovered_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::hasRecovered,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location, whether they are alive and which type of covid infection they have
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> location_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
																Collectors.groupingBy(
																		Person::covidLogCheck,
										Collectors.counting()
										)
								)
						)
						)
						);
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_asympt_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
															Collectors.groupingBy(
																	Person::hasAsymptCovid,
																		Collectors.groupingBy(
																					Person::getAsymptCovidLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_mild_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
															Collectors.groupingBy(
																	Person::hasMild,
																		Collectors.groupingBy(
																					Person::getMildCovidLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_severe_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
															Collectors.groupingBy(
																	Person::hasSevere,
																		Collectors.groupingBy(
																					Person::getSevereCovidLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_critical_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::getAlive,
												Collectors.groupingBy(
														Person::hasCovid,
															Collectors.groupingBy(
																	Person::hasCritical,
																		Collectors.groupingBy(
																					Person::getCriticalCovidLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				// create a function to group the population by location and count cumulative deaths
				Map<String, Map<Boolean, Long>> location_cumulative_died_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::isDeadFromCovid,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count cumulative cases
				Map<String, Map<Boolean, Long>> location_cumulative_covid_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::hadCovid,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count new deaths
				Map<String, Map<Boolean, Map<Boolean, Long>>> location_new_deaths_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::isDeadFromCovid,
													Collectors.groupingBy(
															Person::getDeathLogged,
										Collectors.counting()
										)
									)
								)
						);
				//	We now iterate over the districts, to find the current state of the epidemic
				for (String district: districtList) {
					// get the current number of cases in each district
					try {
					covidCountArray.add(location_alive_hasCovid_map.get(district).get(true).get(true).get(false).intValue());
					} catch (Exception e) {
						// No one in population met criteria
						covidCountArray.add(0);
					}
					// get the cumulative number of covid cases in the district
					try {
						cumCovidCountArray.add(location_cumulative_covid_map.get(district).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							cumCovidCountArray.add(0);
						}
					// get the number of asymptomatic covid cases in the district
					try {
						asymptCovidCountArray.add(location_asympt_covid_map.get(district).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							asymptCovidCountArray.add(0);
						}
					// get the number of mild covid cases in the district
					try {
						mildCovidCountArray.add(location_mild_covid_map.get(district).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							mildCovidCountArray.add(0);
						}
					// get the number of severe covid cases in the district
					try {
						severeCovidCountArray.add(location_severe_covid_map.get(district).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							severeCovidCountArray.add(0);
						}
					// get the number of critical covid cases in the district
					try {
						criticalCovidCountArray.add(location_critical_covid_map.get(district).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							criticalCovidCountArray.add(0);
						}
					// get the number of recoveries  in the district
					try {
						recoveredCountArray.add(location_alive_recovered_map.get(district).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							recoveredCountArray.add(0);
						}
					// get the cumultative number of covid deaths in the district
					try {
						covidCumulativeDeathCount.add(location_cumulative_died_map.get(district).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							covidCumulativeDeathCount.add(0);
						}
					// get the number of new covid deaths in the district
					try {
						covidNewDeathCount.add(location_new_deaths_map.get(district).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							covidNewDeathCount.add(0);
						}
				}
				// report out findings
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				// name the file
				String covidNumberOutput = "";
				// format the file
				String t = "\t";
				String tabbedDistrictNames = "";
				for (String district: districtList) {tabbedDistrictNames += t + district;}
				if (time == 0) {
					covidNumberOutput += "day" + t + "metric" + tabbedDistrictNames + "\n" + String.valueOf(time);
				}
				else {
					covidNumberOutput += String.valueOf(time);
				}
				// store total number of cases in district
				covidNumberOutput += t + "total_cases";
				for (int val: covidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of asymptomatic cases in district
				covidNumberOutput += time + t + "total_asympt_cases";
				for (int val: asymptCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of mild cases in district
				covidNumberOutput += time + t + "total_mild_cases";
				for (int val: mildCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of severe cases in district
				covidNumberOutput += time + t + "total_severe_cases";
				for (int val: severeCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of critical cases in district
				covidNumberOutput += time + t + "total_critical_cases";
				for (int val: criticalCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of recoveries in district
				covidNumberOutput += time + t + "total_recovered";
				for (int val: recoveredCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store cumulative number of cases in district
				covidNumberOutput += time + t + "cumulative_cases";
				for (int val: cumCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store cumulative number of deaths in district
				covidNumberOutput += time + t + "cumulative_deaths"; 
				for (int val: covidCumulativeDeathCount){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of new deaths in district
				covidNumberOutput += time + t + "new_deaths";
				for (int val: covidNewDeathCount){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";

				// export the file
				
				ImportExport.exportMe(newLoggingFilename, covidNumberOutput, timer);
//				calculate incidence of death in each age group by cause
				//	covid deaths, incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
				List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of covid deaths in each age range and the number of 'other' cause deaths in each age range
				ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> male_covid_deaths_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> male_other_deaths_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_covid_deaths_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_other_deaths_by_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::getAlive,
										Collectors.counting()
										)
								)
						)
						);
				// create a function to group the population by sex, age and whether they died from covid
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_died_from_covid = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::isDeadFromCovid,
												Collectors.groupingBy(
														Person::getDeathLogged,
														Collectors.counting()
										)
								)
						)
						)
						);
				// create a function to group the population by sex, age and whether they died from something other than covid
				Map<String, Map<Integer, Map <Boolean, Map<Boolean, Long>>>> age_sex_map_died_from_other = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::isDeadFromOther,
												Collectors.groupingBy(Person::getDeathLogged,
										Collectors.counting()
										)
								)
						)
						)
						);
				//	We now iterate over the age ranges, create a variable to keep track of the iterations
				Integer idx = 0;
				for (Integer val: upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer male_count = 0;
					Integer male_covid_death_count = 0;
					Integer male_other_death_count = 0;
					Integer female_count = 0;
					Integer female_covid_death_count = 0;
					Integer female_other_death_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {
						
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							male_count += age_sex_alive_map.get("male").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_count += age_sex_alive_map.get("female").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
					
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							male_covid_death_count += age_sex_map_died_from_covid.get("male").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_covid_death_count += age_sex_map_died_from_covid.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							male_other_death_count += age_sex_map_died_from_other.get("male").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_other_death_count += age_sex_map_died_from_other.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
						}
				
						
					// store what we have found in the lists we created
					male_alive_ages.add(male_count);
					female_alive_ages.add(female_count);
					male_covid_deaths_by_ages.add(male_covid_death_count);
					female_covid_deaths_by_ages.add(female_covid_death_count);
					male_other_deaths_by_ages.add(male_other_death_count);
					female_other_deaths_by_ages.add(female_other_death_count);
					// update the idx variable for the next iteration
					idx++;
				}
				// format log file
				String covid_inc_death = "";
				String other_inc_death = "";

				String age_sex_categories = t + "sex" + t + "<1" + t + "1_4" + t + "5_9" + t + "10_14" + t + "15_19" + t + "20_24" + 
						t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + t + "50_54" + t + "55_59" + t + 
						"60_64" + t + "65_69" + t + "70_74" + t + "75_79" + t + "80_84" + t + "85_89" + t + "90_94" + t + "95<" + "\n";
				if (time == 0) {
					covid_inc_death += "day" + age_sex_categories + String.valueOf(time);
					other_inc_death += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					covid_inc_death += String.valueOf(time);
					other_inc_death += String.valueOf(time);
				}
				// calculate incidence of covid death in males this day
				covid_inc_death += t + "m";
				for (int x = 0; x <male_covid_deaths_by_ages.size(); x++){
					double male_covid_deaths_in_age = male_covid_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result = 0;
					if (male_alive_in_age > 0) {
						result =  male_covid_deaths_in_age / male_alive_in_age;
					}
	                result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				// calculate incidence of covid death in females this day
				covid_inc_death += String.valueOf(time) + t + "f";
				for (int x =0; x <female_covid_deaths_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = 0;
					if (female_alive_in_age > 0) {
						result = female_covid_deaths_in_age / female_alive_in_age;	                
					}
					result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				// calculate incidence of other death in males this day
				other_inc_death += t + "m";
				for (int x = 0; x <male_other_deaths_by_ages.size(); x++){
					double male_other_deaths_in_age = male_other_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result = 0;
					if (male_alive_in_age > 0) {
						result = male_other_deaths_in_age / male_alive_in_age;
					}
	                result *= 100000;
	                other_inc_death += t + String.valueOf(result);
				}
				other_inc_death += "\n";
				// calculate incidence of other death in females this day
				other_inc_death += String.valueOf(time) + t + "f";
				for (int x =0; x <female_other_deaths_by_ages.size(); x++){
					double female_other_deaths_in_age = female_other_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = 0;
					if (female_alive_in_age > 0) {
						result = female_other_deaths_in_age / female_alive_in_age;
					}
	                result *= 100000;
	                other_inc_death += t +String.valueOf(result);
				}
				other_inc_death += "\n";

				// export the output files
				ImportExport.exportMe(covidIncDeathOutputFilename, covid_inc_death, timer);
				ImportExport.exportMe(otherIncDeathOutputFilename, other_inc_death, timer);
//				calculate incidence of Covid in each age group
				//	covid incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
				ArrayList <Integer> male_covid_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_covid_by_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a function to group the population by sex, age and whether they have covid
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_has_covid = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::hasCovid,
												Collectors.groupingBy(
														Person::getCovidLogged,
														Collectors.counting()
										)
								)
						)
						)
						);
						
				//	We now iterate over the age ranges, create a variable to keep track of the iterations
				idx = 0;
				for (Integer val: upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer male_count = 0;
					Integer male_covid_count = 0;
					Integer female_count = 0;
					Integer female_covid_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {					
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							male_covid_count += age_sex_map_has_covid.get("male").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_covid_count += age_sex_map_has_covid.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
							// age wasn't present in the population, skip
							}
					}
					male_covid_by_ages.add(male_covid_count);
					female_covid_by_ages.add(female_covid_count);
					// update the idx variable for the next iteration
					idx++;
					
				}
				// format the output file
				String covid_inc = "";
				if (time == 0) {
					covid_inc += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					covid_inc += String.valueOf(time);
				}
				// calculate the incidence of covid in males this day
				covid_inc += t + "m";
				for (int x = 0; x <male_covid_by_ages.size(); x++){
					double male_covid_deaths_in_age = male_covid_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result = 0;
					if (male_alive_in_age > 0) {
						result =  male_covid_deaths_in_age / male_alive_in_age;
					}
	                result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				// calculate the incidence of covid in females this day
				covid_inc += String.valueOf(time) + t + "f";

				for (int x =0; x <female_covid_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = 0;
					if (female_alive_in_age > 0) {
						result = female_covid_deaths_in_age / female_alive_in_age;	                
					}
					result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				
				// export the output file
				ImportExport.exportMe(covidIncOutputFilename, covid_inc, timer);
				//	calculate the number of counts in each age group	
				String covid_number_and_deaths = "";
				if (time == 0) {
					covid_number_and_deaths += "day" + t + "metric" + age_sex_categories + String.valueOf(time);
				}
				else {
					covid_number_and_deaths += String.valueOf(time);
				}
				covid_number_and_deaths += t + "cases" + t + "m";
				for (Integer count: male_covid_by_ages){
					covid_number_and_deaths += t + count;
				}
				covid_number_and_deaths += "\n";
				// calculate the incidence of covid in females this day
				covid_number_and_deaths += String.valueOf(time) + t + "deaths" + t + "m";
				for (Integer count: male_covid_deaths_by_ages){
					covid_number_and_deaths += t + count;
				}
				covid_number_and_deaths += "\n";
				covid_number_and_deaths += String.valueOf(time) + t + "cases" + t + "f";
				for (Integer count: female_covid_by_ages){
					covid_number_and_deaths += t + count;
				}
				covid_number_and_deaths += "\n";
				// calculate the incidence of covid in females this day
				covid_number_and_deaths += String.valueOf(time) + t + "deaths" + t + "f";
				for (Integer count: female_covid_deaths_by_ages){
					covid_number_and_deaths += t + count;
				}
				covid_number_and_deaths += "\n";
				ImportExport.exportMe(covidCountsOutputFilename, covid_number_and_deaths, timer);
				List <String> economic_status = Arrays.asList("not working, inactive, not in universe", "current students", "homemakers/housework", 
						"office workers", "teachers", "service workers", "agriculture workers", "industry workers", "in the army", "disabled and not working");
				ArrayList <Integer> status_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_death_counts = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a function to group the population by occupation, age and whether they have covid
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> economic_alive_has_covid = agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
								Collectors.groupingBy(
										Person::getAlive,
										Collectors.groupingBy(
												Person::hasCovid,
												Collectors.groupingBy(
														Person::getCovidLogged,
														Collectors.counting()
										)
								)
						)
						)
						);
				Map<String, Map<Boolean, Long>> economic_alive = agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
								Collectors.groupingBy(
										Person::getAlive,
														Collectors.counting()
										
								
						)
						)
						);
				// create a function to group the population by sex, age and whether they died from covid
				Map<String, Map<Boolean, Map<Boolean, Long>>> econ_died_from_covid = agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
									Collectors.groupingBy(
											Person::isDeadFromCovid,
											Collectors.groupingBy(
													Person::getDeathLogged,
													Collectors.counting()
										)
								)
						)
						);
				for (String status: economic_status) {
					try {
					status_covid_counts.add(economic_alive_has_covid.get(status).get(true).get(true).get(false).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_covid_counts.add(0);
					}
					try {
						status_covid_death_counts.add(econ_died_from_covid.get(status).get(true).get(false).intValue());
						}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_covid_death_counts.add(0);
					}
					try {
					status_counts.add(economic_alive.get(status).get(true).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_counts.add(0);
					}
				}
				String econ_status_categories = "Not working, inactive, not in universe" + t + "Current Students" + t + "Homemakers/Housework" + t +
						"Office workers" + t + "Teachers" + t + "Service Workers" + t + "Agriculture Workers" + t + "Industry Workers" + t + "In the army" + 
						t + "Disabled and not working" + "\n";
				String econ_status_output = "";
				if (time == 0) {
					econ_status_output += "day" + t + "metric" + t + econ_status_categories + String.valueOf(time);
				}
				else {
					econ_status_output += String.valueOf(time);
				}
				econ_status_output += t + "number_in_occ";
				for (Integer count: status_counts){
					econ_status_output += t + count;
				}
				econ_status_output += "\n";
				// calculate the incidence of covid in females this day
				econ_status_output += String.valueOf(time) + t + "number_with_covid";
				for (Integer count: status_covid_counts){
					econ_status_output += t + count;
				}
				econ_status_output += "\n";
				econ_status_output += String.valueOf(time) + t + "number_died_from_covid";
				for (Integer count: status_covid_death_counts){
					econ_status_output += t + count;
				}
				econ_status_output += "\n";
				
				ImportExport.exportMe(covidByEconOutputFilename, econ_status_output, timer);
				// to make sure deaths and cases aren't counted multiple times, update this person's properties

				for (Person p: agents) {
					if(p.isDeadFromCovid()) {
						p.confirmDeathLogged();
						}
					if (p.isDeadFromOther()) p.confirmDeathLogged();
					if(p.hasCovid() & !p.covidLogCheck()) {
						p.confirmCovidLogged();
					}
					if(p.hasAsymptCovid() & !p.getAsymptCovidLogged()) {
						p.confirmAsymptLogged();
					}
					if(p.hasMild() & !p.getMildCovidLogged()) {
						p.confirmMildLogged();
					}
					if(p.hasSevere() & !p.getSevereCovidLogged()) {
						p.confirmSevereLogged();
					}
					if(p.hasCritical() & !p.getCovidLogged()) {
						p.confirmCriticalLogged();
					}
					}	
			}
			};
		
		schedule.scheduleRepeating(testLoggingCases, this.param_schedule_reporting, params.ticks_per_day);
		
		// Create a function to keep track of the population and epidemic at the scale of district level
		Steppable updateDistrictLevelInfo = new Steppable() {
			
			@Override
			public void step(SimState arg0) {

				// create a function to group the population by who is alive at this district
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = agents.stream().collect(
						Collectors.groupingBy(
								Person::getAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict
										)
						)
						);
				// create a function to group the population by who is alive in each district and has covid
				Map<Boolean, Map<String, Map<Boolean, List<Person>>>> covidAtLocation = agents.stream().collect(
						Collectors.groupingBy(
								Person::getAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict,
										Collectors.groupingBy(
												Person::hasCovid
										)
						)
						)
						);
				Map<Boolean, Map<String, Map<Integer, Map<String, List<Person>>>>> aliveAtLocationAgeSex = agents.stream().collect(
						Collectors.groupingBy(
								Person::getAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict,
										Collectors.groupingBy(
												Person::getAge,
												Collectors.groupingBy(
														Person::getSex
										)
						)
						)
						)
						);
				// get list of ages to iterate over
				List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
				List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
				// get a list of districts to iterate over
				List <String> districts = ((WorldBankCovid19Sim)arg0).params.districtNames;
						
						/*Arrays.asList(
						"d_1", "d_2", "d_3", "d_4", "d_5", "d_6", "d_7", "d_8", "d_9", "d_10", "d_11", "d_12", "d_13", "d_14", "d_15", 
						"d_16", "d_17", "d_18", "d_19", "d_20", "d_21", "d_22", "d_23", "d_24", "d_25", "d_26", "d_27", "d_28", "d_29", 
						"d_30", "d_31", "d_32", "d_33", "d_34", "d_35", "d_36", "d_37", "d_38", "d_39", "d_40", "d_41", "d_42", "d_43", 
						"d_44", "d_45", "d_46", "d_47", "d_48", "d_49", "d_50", "d_51", "d_52", "d_53", "d_54", "d_55", "d_56", "d_57", 
						"d_58", "d_59", "d_60");*/
				// create a list to store the number of people and who has covid in each district
				ArrayList <Integer> districtPopCounts = new ArrayList<Integer>();
				ArrayList <Integer> districtCovidCounts = new ArrayList<Integer>();
				// iterate over each district
				for (String place: districts) {
					// get population counts in each district
					districtPopCounts.add(aliveAtLocation.get(true).get(place).size());
					// get covid counts in each district
					try {
					districtCovidCounts.add(covidAtLocation.get(true).get(place).get(true).size());
					}
					catch (Exception e) {
					// age wasn't present in the population, skip
					districtCovidCounts.add(0);
					}
					
				}
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);

				String pop_size_in_district = "";
				
				String t = "\t";
				if (time == 0) {
					pop_size_in_district += "day" + t;
					for (String place: districts) {
						pop_size_in_district += place + t;
					}
					pop_size_in_district += "\n" + String.valueOf(time);
				}
				else {
					pop_size_in_district += "\n" + String.valueOf(time);
				}
				// store the population counts per district
				for (int count: districtPopCounts) {
					pop_size_in_district += t + count;
				}
				// export the file
				ImportExport.exportMe(distPopSizeOutputFilename, pop_size_in_district, timer);
				// format the output for the percent of the district with covid
				String percent_with_covid = "";
				if (time == 0) {
					percent_with_covid += "day" + t;
					for (String place: districts) {
						percent_with_covid += place + t;
					}
					percent_with_covid += "\n" + String.valueOf(time);
				}
				else {
					percent_with_covid += "\n" + String.valueOf(time);
				}
				int idx = 0;
				// calculate the percentage in the district with covid
				for (float count: districtCovidCounts) {
					float perc_with_covid = count / districtPopCounts.get(idx);
					percent_with_covid += t + perc_with_covid;
					idx++;
				}
				// export the file
				ImportExport.exportMe(distCovidPrevalenceOutputFilename, percent_with_covid, timer);
				String districtLevelPopBreakdown = "";
				
				String district_age_sex_categories = t + "district" + t + "sex" + t + "<1" + t + "1_4" + t + "5_9" + t + "10_14" + t + "15_19" + t + "20_24" + 
						t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + t + "50_54" + t + "55_59" + t + 
						"60_64" + t + "65_69" + t + "70_74" + t + "75_79" + t + "80_84" + t + "85_89" + t + "90_94" + t + "95<" + "\n";
				if (time == 0) {
					districtLevelPopBreakdown += "day" + district_age_sex_categories;
				}
				for (String place: districts) {
					districtLevelPopBreakdown += time + t + place;
					// create lists to store the age gender breakdown of people in this district
					ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
					ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
					idx = 0;
					for (Integer val: upper_age_range) {
						// for each age group we begin to count the number of people who fall into each category, create variables
						// to store this information in
						Integer male_count = 0;
						Integer female_count = 0;
						for (int age = lower_age_range.get(idx); age < val; age++) {
					
							try {
								// try function necessary as some ages won't be present in the population
								// use the functions created earlier to calculate the number of people of each age group who fall
								// into the categories we are interested in (alive, died from covid, died from other)
							male_count += aliveAtLocationAgeSex.get(true).get(place).get(age).get("male").size();
							}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
							try {
								// try function necessary as some ages won't be present in the population
								// use the functions created earlier to calculate the number of people of each age group who fall
								// into the categories we are interested in (alive, died from covid, died from other)
								female_count += aliveAtLocationAgeSex.get(true).get(place).get(age).get("female").size();
							}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						}
					// store what we have found in the lists we created
					male_alive_ages.add(male_count);
					female_alive_ages.add(female_count);
				}
				districtLevelPopBreakdown += t + "m";
				for (int count: male_alive_ages){
		            districtLevelPopBreakdown += t + String.valueOf(count);
				}
				districtLevelPopBreakdown += "\n";
				districtLevelPopBreakdown += time + t + place;				
				districtLevelPopBreakdown += t + "f";
				for (int count: female_alive_ages){
		            districtLevelPopBreakdown += t + String.valueOf(count);
				}
				districtLevelPopBreakdown += "\n";
				}
				ImportExport.exportMe(distPopBreakdownOutputFilename, districtLevelPopBreakdown, timer);

			}
		};
		
		schedule.scheduleRepeating(updateDistrictLevelInfo, this.param_schedule_reporting, params.ticks_per_day);
		
		
				
		// create a function to report the overall population structure
		Steppable reportPopStructure = new Steppable(){
			
			@Override
			public void step(SimState arg0) {
				//	calculate the number of people in each age group 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
				List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
				// create list to store the counts of the number of males and females alive in each age range in each district
				ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getSex, 
								Collectors.groupingBy(
										Person::getAge, 
										Collectors.groupingBy(
												Person::getAlive,
										Collectors.counting()
										)
								)
						)
						);
						
				//	We now iterate over the age ranges, create a variable to keep track of the iterations
				Integer idx = 0;
				for (Integer val: upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer male_count = 0;
					Integer female_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group
							male_count += age_sex_alive_map.get("male").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group
							female_count += age_sex_alive_map.get("female").get(age).get(true).intValue();
						}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
					}
						
					// store what we have found in the lists we created
					male_alive_ages.add(male_count);
					female_alive_ages.add(female_count);
					// update the idx variable for the next iteration
					idx++;
					
				}
				// format the output file
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				String population = "";

				String t = "\t";
				String age_sex_categories = t + "sex" + t + "<1" + t + "1_4" + t + "5_9" + t + "10_14" + t + "15_19" + t + "20_24" + 
						t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + t + "50_54" + t + "55_59" + t + 
						"60_64" + t + "65_69" + t + "70_74" + t + "75_79" + t + "80_84" + t + "85_89" + t + "90_94" + t + "95<" + "\n";
				if (time == 0) {
					population += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					population += String.valueOf(time);
				}
				// get the number of males in each age group
				population += t + "m";

				for (int x = 0; x <male_alive_ages.size(); x++){
					int male_alive_in_age = male_alive_ages.get(x);
					population += t + String.valueOf(male_alive_in_age);
				}
				population += "\n";
				// get the number of females in each age group
				population += String.valueOf(time) + t + "f";
				for (int x = 0; x <female_alive_ages.size(); x++){
					int female_alive_in_age = female_alive_ages.get(x);
					population += t + String.valueOf(female_alive_in_age);
				}
				population += "\n";

				// export the file
				ImportExport.exportMe(populationOutputFilename, population, timer);
				
			}
		};
		schedule.scheduleRepeating(reportPopStructure, this.param_schedule_reporting, params.ticks_per_day);
		// create a function to report the overall population structure
		Steppable reportSeroprevalence = new Steppable(){
					
					@Override
					public void step(SimState arg0) {
						// get a list of locations
						List <String> districts = ((WorldBankCovid19Sim)arg0).params.districtNames;

						// create a function to group the population by who is alive at this district
						Map<Boolean, Map<String, List<Person>>> aliveAtLocation = agents.stream().collect(
								Collectors.groupingBy(
										Person::getAlive,
										Collectors.groupingBy(
												Person::getCurrentDistrict
												)
								)
								);
						// create a function to group the population by who is alive in each district and has covid antibodies
						Map<Boolean, Map<String, Map<Boolean, List<Person>>>> covidAntibodiesAtLocation = agents.stream().collect(
								Collectors.groupingBy(
										Person::getAlive,
										Collectors.groupingBy(
												Person::getCurrentDistrict,
												Collectors.groupingBy(
														Person::hasAntibodies
												)
								)
								)
								);
						
						ArrayList <Double> districtSeroprevalence = new ArrayList<Double>();
						// iterate over each district
						double population;
						double antibodyCount;
						double seroprevalence;
						for (String place: districts) {
							// get population counts in each district
							population = (double) aliveAtLocation.get(true).get(place).size();
							// get covid counts in each district
							try {
								antibodyCount = (double) covidAntibodiesAtLocation.get(true).get(place).get(true).size();
							}
							catch (Exception e) {
							// antibodies weren't present in the population, skip
								antibodyCount = 0.0;
							}
							
							districtSeroprevalence.add(antibodyCount / population);
						}
						// format the output file for population counts
						int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);

						String seroprev_in_district = "";
						
						String t = "\t";
						if (time == 0) {
							seroprev_in_district += "day" + t;
							for (String place: districts) {
								seroprev_in_district += place + t;
							}
							seroprev_in_district += "\n" + String.valueOf(time);
						}
						else {
							seroprev_in_district += "\n" + String.valueOf(time);
						}
						// store the population counts per district
						for (double val: districtSeroprevalence) {
							seroprev_in_district += t + val;
						}
						

						// export the file
						ImportExport.exportMe(overallSeroprevalence, seroprev_in_district, timer);
						
					}
				};
		schedule.scheduleRepeating(reportSeroprevalence, this.param_schedule_reporting, params.ticks_per_day);
		// Schedule seroprevalence sampling
		Steppable covidSeroprevalenceSampling = new Steppable() {

					@Override
					public void step(SimState arg0) {
						if (seroSample) {
							WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;

							// get the simulation time
							int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
							
							// we only want to sample from people who are alive and in the correct districts
							Map<Boolean, Map<Boolean, List<Person>>> is_elligable_for_testing_map = agents.stream().collect(
													Collectors.groupingBy(
															Person::isDead,
															Collectors.groupingBy(
																	Person::inASamplingDistrict,
													Collectors.toList()
													)
												)
									);
							Random testing_random = new Random(myWorld.seed());
							int number_of_positive_tests = 0;
							double percent_positive = 0;
							// generate a list of people to test today
							try {
								List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true), numSamplesPerDay, testing_random);

							// iterate over the list of people to test and perform the tests
							for (Person person:people_tested) {
								if(person.hasAntibodies()) {
									number_of_positive_tests ++;
									}
								}
							percent_positive = (double) number_of_positive_tests / numSamplesPerDay;
							}
							catch (Exception e) {
							}
						
						String t = "\t";
						
						String detected_sero_output = "";
						
						if (time == day_to_start_writing_sero_sample_file) {
							detected_sero_output += "day" + t + "number_of_tests"  + t + "num_with_antibodies"  + t + "seroprevalence"+ "\n"+ String.valueOf(time) + t + numSamplesPerDay + t + number_of_positive_tests + t + percent_positive+ "\n";
						}
						else {
							detected_sero_output += t + numSamplesPerDay + t + number_of_positive_tests + t + percent_positive + "\n";
						}
						ImportExport.exportMe(detectedSeroprevalenceFilename, detected_sero_output, time);
						
				}
				}
				};
		schedule.scheduleRepeating(covidSeroprevalenceSampling, this.param_schedule_reporting, params.ticks_per_day);
		
				
		// SCHEDULE testing
				Steppable testing_for_covid = new Steppable() {

							@Override
							public void step(SimState arg0) {
								// get the simulation time
								int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
								// get the index for the test numbers
								int index_for_test_number = params.test_dates.indexOf(time);
								// find the number of tests per 1000 to test today
								int number_of_tests_today = 0;
								try {
									number_of_tests_today = params.number_of_tests_per_day.get(index_for_test_number);
									}
								catch (Exception e) {
								}
								// we only want to test people who are alive and administer the tests per 1000 based on this 
								Map<Boolean, Map<Boolean, List<Person>>> is_elligable_for_testing_map = agents.stream().collect(
														Collectors.groupingBy(
																Person::isDead,
																Collectors.groupingBy(
																		Person::isElligableForTesting,
														Collectors.toList()
														)
													)
										);
								WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;								// create a random state (I need to link this to the existing random state but don't know how)
								Random testing_random = new Random(myWorld.seed());
								int number_of_positive_tests = 0;
								double percent_positive = 0;
								// generate a list of people to test today
								try {
									List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true), number_of_tests_today, testing_random);
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
									detected_covid_output += t + number_of_positive_tests + t + number_of_tests_today + t + percent_positive + "\n";
								}
								ImportExport.exportMe(detectedCovidFilename, detected_covid_output, time);
//								create a list of district names to iterate over for our logging
								List <String> districtList = myWorld.params.districtNames;

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
//								We now iterate over the districts, to find the current state of the epidemic
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
								// store total number of positive tests in district
								for (int val: covidTestedPositiveArray){
									spatialOutput += t + String.valueOf(val);
								}
								spatialOutput += "\n";
								ImportExport.exportMe(spatialdetectedCovidFilename, spatialOutput, time);
								try {
									List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(false).get(true), number_of_tests_today, testing_random);
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
		Steppable seroSamplingTrigger = new Steppable() {

			@Override
			public void step(SimState arg0) {
				double currentTime = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				if(params.seroChangeList.size() == 0)
					return;
				double nextChange = params.seroChangeList.get(0);
				
				if(currentTime >= nextChange) {
					// assign the sample number today
					if (params.seroChangeList.size() > 1) {
						numDaysToSampleSeroprevalence = (int) (params.seroChangeList.get(1) - params.seroChangeList.get(0));
						numSamplesPerDay = params.seroSampleNumbers.get(0) / numDaysToSampleSeroprevalence;
						day_to_start_writing_sero_sample_file = params.seroChangeList.get(0);
					}
					params.seroChangeList.remove(0);
					seroSample = !seroSample;
					}	
				}	
			};
		schedule.scheduleRepeating(0, this.param_schedule_lockdown, seroSamplingTrigger);

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
				// create a random birthday
				int birthday = this.random.nextInt(365) + 1;

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
			// calculate and store the sample size of the population, involves some type casting, may be able to be improved
			double model_pop_size = (double) agents.size();
			double model_pop_fraction = model_pop_size / this.params.pop_size_2020;
			double model_sample_size = model_pop_fraction * 100;
			this.params.sample_size =  (int) model_sample_size;
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
	
	private static <E> List<E> pickRandom(List<E> list, int n, Random rand) {
		  return new Random().ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
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
		// -----------------------------------------------------------------------------------------------------
		// ------- intended feed into this from a shell script -------------------------------------------------
		// args[0] - number of days the simulation runs
		// args[1] - currently not used to execute jar, but this is typically the data directory
		// args[2] - Value of beta
		// args[3] - NEW AS OF TESTING INCLUSION the value of spurious symptom rate 
		// args[4] - seed of the run
		// args[5] - the name of the output file
		// args[6] - the name of the parameter file (directory of files used)
		// args[7] - the name of the infection output file (should no longer be necessary)
		// -----------------------------------------------------------------------------------------------------

		// default settings in the absence of commands!
		int numDays = 7; // by default, one week
		double myBeta = .016;
		double mySpuriousRate = 0.01;
		long seed = 12345;
		String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".txt";
		String infectionsOutputFilename = "infections_" + myBeta + "_" + numDays + "_" + seed + ".txt"; 
		String paramsFilename = "src/main/resources/params.txt";
		boolean demography = false;
		// read in any extra settings from the command line
		if(args.length < 0){
			System.out.println("usage error");
			System.exit(0);
		}
		else if(args.length > 0){
			
			numDays = Integer.parseInt(args[0]);
			myBeta = Double.parseDouble(args[2]);
			mySpuriousRate = Double.parseDouble(args[3]);
			if(args.length > 4) {
				seed = Long.parseLong(args[4]);
				outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".tsv";
			}
			if(args.length > 5)
				outputFilename = args[5];
			if(args.length > 6)
				paramsFilename = args[6];
			if(args.length > 7)
				infectionsOutputFilename = args[7];
		}
		
		long startTime = System.currentTimeMillis(); // wallclock measurement of time - embarrassing.
				
		/*
				String paramFilename = filenameBase + s + filenameSuffix;
				String outputFilename = s + outputPrefix + i + outputSuffix;
				
		 */

		// set up the simulation
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename, true), outputFilename, demography);


		System.out.println("Loading...");

		// ensure that all parameters are set
		mySim.params.infection_beta = myBeta / mySim.params.ticks_per_day; // normalised to be per tick
		mySim.targetDuration = numDays;
		mySim.params.rate_of_spurious_symptoms = mySpuriousRate;	
		mySim.start(); // start the simulation
				
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