package sim;

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
	public boolean demography;
	// Create variable to log the population structure over time
	public boolean logPopStructure;
		
	public String outputFilename;
	public String covidIncOutputFilename; 
	public String populationOutputFilename;
	public String covidIncDeathOutputFilename;
	public String otherIncDeathOutputFilename;
	public String birthRateOutputFilename;
	public String distPopSizeOutputFilename;
	public String infections_export_filename;
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
	public WorldBankCovid19Sim(long seed, Params params, String outputFilename, String covidIncOutputFilename, String covidIncDeathOutputFilename, String otherIncDeathOutputFilename, String birthRateOutputFilename, String populationOutputFilename, String distPopSizeOutputFilename, boolean demography) {
		super(seed);
		this.params = params;
		this.outputFilename = outputFilename;
		this.covidIncOutputFilename = covidIncOutputFilename;
		this.covidIncDeathOutputFilename = covidIncDeathOutputFilename;
		this.otherIncDeathOutputFilename = otherIncDeathOutputFilename;
		this.birthRateOutputFilename = birthRateOutputFilename;
		this.distPopSizeOutputFilename = distPopSizeOutputFilename;
		this.populationOutputFilename = populationOutputFilename;
		this.demography = demography;
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
		
		// SCHEDULE BIRTHS AND LOG BIRTHS
		Steppable createBirths = new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				// create a list of babies
				ArrayList<Person> newBirths = new ArrayList <Person> ();
				//	get a reference to the current simulaiton day		
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
		schedule.scheduleRepeating(createBirths, this.param_schedule_updating_locations, params.ticks_per_day);
		
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
				// calculate incidence of covid death this day
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
					double result = births_in_age / female_alive_in_age;
	                result *= 1000;
	                age_dependent_birth_rate += t + String.valueOf(result);
				}
				age_dependent_birth_rate += "\n";


				// create a string to store this information in
				// get the day
				
				exportMe(birthRateOutputFilename, age_dependent_birth_rate);
				for (Person p: agents) {
					if(p.gaveBirthLastYear()) {
						p.confirmBirthlogged();
						}
					}
				
			}
			};
		
		if (this.demography) {
		schedule.scheduleRepeating(reportBirthRates, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		Steppable updateAges = new Steppable() {
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by sex, age and whether they are alive
				Map<Integer, List<Person>> birthday_map = agents.stream().collect(
						Collectors.groupingBy(
								Person::getBirthday
						)
						);
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				List<Person> birthdays = birthday_map.get(time + 1 % 365);
				try {
					for(Person p: birthdays){
//						System.out.print("person " + p.getID() + " is going from age " + p.getAge() + " to ");
							p.updateAge();
//						System.out.print(p.getAge() + "\n");
					}
				}
				catch (Exception e) {
						// No one had a birthday today, skip.
				}
				
				
//				exportMe(outputFilename, s);
				
			}
		};
		
		if (this.demography){
		schedule.scheduleRepeating(updateAges, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		Steppable updateDistrictPopSize = new Steppable() {
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by sex, age and whether they are alive
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = agents.stream().collect(
						Collectors.groupingBy(
								Person::getAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict
										)
						)
						);
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				List <String> districts = Arrays.asList(
						"d_1", "d_2", "d_3", "d_4", "d_5", "d_6", "d_7", "d_8", "d_9", "d_10", "d_11", "d_12", "d_13", "d_14", "d_15", 
						"d_16", "d_17", "d_18", "d_19", "d_20", "d_21", "d_22", "d_23", "d_24", "d_25", "d_26", "d_27", "d_28", "d_29", 
						"d_30", "d_31", "d_32", "d_33", "d_34", "d_35", "d_36", "d_37", "d_38", "d_39", "d_40", "d_41", "d_42", "d_43", 
						"d_44", "d_45", "d_46", "d_47", "d_48", "d_49", "d_50", "d_51", "d_52", "d_53", "d_54", "d_55", "d_56", "d_57", 
						"d_58", "d_59", "d_60");
				ArrayList <Integer> districtPopCounts = new ArrayList<Integer>();
				for (String place: districts) {
					districtPopCounts.add(aliveAtLocation.get(true).get(place).size());
				}
				
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
				for (int count: districtPopCounts) {
					pop_size_in_district += t + count;
				}
				
				
				exportMe(distPopSizeOutputFilename, pop_size_in_district);

//				exportMe(outputFilename, s);
				
			}
		};
		
		if (this.demography){
		schedule.scheduleRepeating(updateDistrictPopSize, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		// SCHEDULE CHECKS ON MORTALITY AND LOGGING
		
		Steppable checkMortality = new Steppable() {
			@Override
			public void step(SimState arg0) {
				
				double myMortalityLikelihood = 0.0;
				// TODO something is going wrong in either causing the deaths or calculating the logging
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				
				for(Person p: agents){
					String sex = p.getSex();
					int age = p.getAge();

					if(sex.equals("male")) {
						myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
								params.prob_death_by_age_male, age);
					}
					else {
						myMortalityLikelihood = params.getAllCauseLikelihoodByAge(
								params.prob_death_by_age_female, age);
					}
					if(random.nextDouble() < myMortalityLikelihood) {
//						System.out.println("person " + p + "should die on day" + time);
						p.die("other");
					}
					

				}
				
//				exportMe(outputFilename, s);
				
			}
		};
		
		if (this.demography){
		schedule.scheduleRepeating(checkMortality, this.param_schedule_reporting, params.ticks_per_day);
		}
		
		Steppable reportIncidenceOfDeath = new Steppable(){
			
			@Override
			public void step(SimState arg0) {
				//	calculate incidence of death in each age group by cause
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
//								System.out.print(e);
							}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_other_death_count += age_sex_map_died_from_other.get("female").get(age).get(true).get(false).intValue();
						}
							catch (Exception e) {
//								System.out.print(e);
							// age wasn't present in the population, skip
							}
//						System.out.print(female_other_death_count);
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
				// calculate incidence of covid death this day
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				String covid_inc_death = "";
				String other_inc_death = "";

				String t = "\t";
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
				covid_inc_death += t + "m";
				for (int x = 0; x <male_covid_deaths_by_ages.size(); x++){
					double male_covid_deaths_in_age = male_covid_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result =  male_covid_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				covid_inc_death += String.valueOf(time) + t + "f";

				for (int x =0; x <female_covid_deaths_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_covid_deaths_in_age / female_alive_in_age;	                
					result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				other_inc_death += t + "m";
				for (int x = 0; x <male_other_deaths_by_ages.size(); x++){
					double male_other_deaths_in_age = male_other_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result = male_other_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                other_inc_death += t + String.valueOf(result);
				}
				other_inc_death += "\n";
				other_inc_death += String.valueOf(time) + t + "f";
				for (int x =0; x <female_other_deaths_by_ages.size(); x++){
					double female_other_deaths_in_age = female_other_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_other_deaths_in_age / female_alive_in_age;
	                result *= 100000;
	                other_inc_death += t +String.valueOf(result);
				}
				other_inc_death += "\n";
				for (Person p: agents) {
					if(p.isDeadFromCovid() | p.isDeadFromOther()) {
						p.confirmDeathLogged();
						}
					}

				// create a string to store this information in
				// get the day
				
				exportMe(covidIncDeathOutputFilename, covid_inc_death);
				exportMe(otherIncDeathOutputFilename, other_inc_death);
				
			}
		};
		
		schedule.scheduleRepeating(reportIncidenceOfDeath, this.param_schedule_reporting, params.ticks_per_day);
		
		Steppable reportIncidenceOfCovid = new Steppable(){
			
			@Override
			public void step(SimState arg0) {
				//	calculate incidence of Covid in each age group
				//	covid incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
				List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
				ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> male_covid_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_covid_by_ages = new ArrayList<Integer>();
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
				Integer idx = 0;
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
						
					// store what we have found in the lists we created
					male_alive_ages.add(male_count);
					female_alive_ages.add(female_count);
					male_covid_by_ages.add(male_covid_count);
					female_covid_by_ages.add(female_covid_count);
					// update the idx variable for the next iteration
					idx++;
					
				}
				// calculate incidence of covid death this day
				int time = (int) (arg0.schedule.getTime() / params.ticks_per_day);
				String covid_inc = "";

				String t = "\t";
				String age_sex_categories = t + "sex" + t + "<1" + t + "1_4" + t + "5_9" + t + "10_14" + t + "15_19" + t + "20_24" + 
						t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + t + "50_54" + t + "55_59" + t + 
						"60_64" + t + "65_69" + t + "70_74" + t + "75_79" + t + "80_84" + t + "85_89" + t + "90_94" + t + "95<" + "\n";
				if (time == 0) {
					covid_inc += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					covid_inc += String.valueOf(time);
				}
				covid_inc += t + "m";
				for (int x = 0; x <male_covid_by_ages.size(); x++){
					double male_covid_deaths_in_age = male_covid_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result =  male_covid_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				covid_inc += String.valueOf(time) + t + "f";

				for (int x =0; x <female_covid_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_covid_deaths_in_age / female_alive_in_age;	                
					result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				
				for (Person p: agents) {
					if (p.hasCovid()) {
						p.confirmCovidLogged();
						}
					}
				// create a string to store this information in
				// get the day
				
				exportMe(covidIncOutputFilename, covid_inc);
				
			}
		};
		
		schedule.scheduleRepeating(reportIncidenceOfCovid, this.param_schedule_reporting, params.ticks_per_day);
		
		Steppable reportPopStructure = new Steppable(){
			
			@Override
			public void step(SimState arg0) {
				//	calculate the number of people in each age group 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
				List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
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
				// calculate incidence of covid death this day
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
				population += t + "m";

				for (int x = 0; x <male_alive_ages.size(); x++){
					int male_alive_in_age = male_alive_ages.get(x);
					population += t + String.valueOf(male_alive_in_age);
				}
				population += "\n";
				population += String.valueOf(time) + t + "f";
				for (int x = 0; x <female_alive_ages.size(); x++){
					int female_alive_in_age = female_alive_ages.get(x);
					population += t + String.valueOf(female_alive_in_age);
				}
				population += "\n";

				
				exportMe(populationOutputFilename, population);
				
			}
		};
		if (this.demography){
		schedule.scheduleRepeating(reportPopStructure, this.param_schedule_reporting, params.ticks_per_day);
		}
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
		int numDays = 7; // by default, one week
		double myBeta = .016;
		long seed = 12345;
		String outputFilename = "dailyReport_" + myBeta + "_" + numDays + "_" + seed + ".txt";
		String incCovidDeathFilename = "";
		String incCovidFilename = "";
		String incOtherDeathFilename = "";
		String birthOutputFileneame = "";
		String infectionsOutputFilename = ""; 
		String popStructureFilename = "";
		String PopSizeOutputFilename = "";
		String paramsFilename = "data/configs/params.txt";
		boolean demography = false;
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
		WorldBankCovid19Sim mySim = new WorldBankCovid19Sim( seed, new Params(paramsFilename), outputFilename, incCovidFilename, incCovidDeathFilename, incOtherDeathFilename, birthOutputFileneame, popStructureFilename, PopSizeOutputFilename, demography);


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