package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.Person.SEX;
import uk.ac.ucl.protecs.objects.diseases.Infection;

public class Logging {
	// set up commonly used variables to avoid repetition
	// age boundaries to format log files
	private final static List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
	private final static List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
	private final static List <Integer> birthrate_upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
	private final static List <Integer> birthrate_lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
	private final static String age_categories = "<1" + "\t" + "1_4" + "\t" + "5_9" + "\t" + "10_14" + "\t" + "15_19" + "\t" + "20_24" + "\t" + "25_29" + 
			"\t" + "30_34" + "\t" + "35_39" + "\t" + "40_44" + "\t" + "45_49" + "\t" + "50_54" + "\t" + "55_59" + "\t" + "60_64" + "\t" + "65_69" + "\t" + 
			"70_74" + "\t" + "75_79" + "\t" + "80_84" + "\t" + "85_89" + "\t" + "90_94" + "\t" + "95<";
	// tab shortcut
	private final static String t = "\t";
	// age sex breakdown header
	private final static String age_sex_categories = t + "sex" + t + age_categories + "\n";

	// set up commonly used functions to avoid repetition
	// get those alive of given age and sex
	private static Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map(WorldBankCovid19Sim world) {
		Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::getSex, 
						Collectors.groupingBy(
								Person::getAge, 
								Collectors.groupingBy(
										Person::isAlive,
								Collectors.counting()
								)
						)
				)
				);
		return age_sex_alive_map;
	}
	
	// get those who alive with COVID of given age and sex
	private static Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> age_sex_has_covid_map(
			WorldBankCovid19Sim world) {
		Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_has_covid = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::getSex, 
						Collectors.groupingBy(
								Infection::getAge, 
								Collectors.groupingBy(
										Infection::isCovid,
										Collectors.groupingBy(
												Infection::hasRecovered,
										Collectors.groupingBy(
												Infection::getLogged,
												Collectors.counting()
								)
						)
				)
				)
				)
				);
		return age_sex_map_has_covid;
	}
	
	// get number of those alive of age and sex
	private static ArrayList <Integer> get_number_of_alive(WorldBankCovid19Sim world, SEX sex) {
		ArrayList <Integer> alive_ages = new ArrayList<Integer>();
		Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = age_sex_alive_map(world);
		// We now iterate over the age ranges, create a variable to keep track of the iterations
		Integer idx = 0;
		for (Integer val: upper_age_range) {
			// for each age group we begin to count the number of people who fall into each category, create variables
			// to store this information in
			Integer count = 0;
			// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
			for (int age = lower_age_range.get(idx); age < val; age++) {
				
				try {
					// try function necessary as some ages won't be present in the population
					// use the functions created earlier to calculate the number of people of each age group who fall
					// into the categories we are interested in (alive, died from covid, died from other)
					count += age_sex_alive_map_copy.get(sex).get(age).get(true).intValue();
				}
					catch (Exception e) {
						// age wasn't present in the population, skip
					}	
			}
			alive_ages.add(count);

		}
		return alive_ages;
	}
	
	// get those alive with COVID of age
	private static ArrayList <Integer> get_covid_counts_by_age(WorldBankCovid19Sim world, SEX sex) {
		Integer idx = 0;
		ArrayList <Integer> covid_by_ages = new ArrayList<Integer>();

		// create a function to group the population by sex, age and whether they have covid
		Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_has_covid = age_sex_has_covid_map(world);
				
		//	We now iterate over the age ranges, create a variable to keep track of the iterations
		for (Integer val: upper_age_range) {
			// for each age group we begin to count the number of people who fall into each category, create variables
			// to store this information in
			Integer covid_count = 0;
			// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
			for (int age = lower_age_range.get(idx); age < val; age++) {					
				try {
					// try function necessary as some ages won't be present in the population
					// use the functions created earlier to calculate the number of people of each age group who fall
					// into the categories we are interested in (alive, died from covid, died from other)
					covid_count += age_sex_map_has_covid.get(sex).get(age).get(true).get(false).get(false).intValue();
				}
					catch (Exception e) {
					// age wasn't present in the population, skip
					}
			}
			covid_by_ages.add(covid_count);
			// update the idx variable for the next iteration
			idx++;
			
		}
		return covid_by_ages;
	}
	
	// get those who died of COVID of age
	private static ArrayList <Integer> get_covid_death_counts_by_age(WorldBankCovid19Sim world, SEX sex) {
		Integer idx = 0;
		ArrayList <Integer> covid_death_by_ages = new ArrayList<Integer>();

		// create a function to group the population by sex, age and whether they have covid
		Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_died_from_covid = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::getSex, 
						Collectors.groupingBy(
								Infection::getAge, 
								Collectors.groupingBy(
										Infection::isCovid,
										Collectors.groupingBy(
												Infection::isCauseOfDeath,
												Collectors.groupingBy(
														Infection::getDeathLogged,
														Collectors.counting()
								)
						)
				)
				)
				)
				);
				
		//	We now iterate over the age ranges, create a variable to keep track of the iterations
		for (Integer val: upper_age_range) {
			// for each age group we begin to count the number of people who fall into each category, create variables
			// to store this information in
			Integer covid_death_count = 0;
			// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
			for (int age = lower_age_range.get(idx); age < val; age++) {					
				try {
					// try function necessary as some ages won't be present in the population
					// use the functions created earlier to calculate the number of people of each age group who fall
					// into the categories we are interested in (alive, died from covid, died from other)
					covid_death_count += age_sex_map_died_from_covid.get(sex).get(age).get(true).get(true).get(false).intValue();
				}
					catch (Exception e) {
					// age wasn't present in the population, skip
					}
			}
			covid_death_by_ages.add(covid_death_count);
			// update the idx variable for the next iteration
			idx++;
			
		}
		return covid_death_by_ages;
	}
	
	// get those alive at location
	private static Map<Boolean, Map<String, List<Person>>> get_alive_at_location(WorldBankCovid19Sim world) {
		// create a function to group the population by who is alive at this admin zone
		Map<Boolean, Map<String, List<Person>>> aliveAtLocation = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::isAlive,
						Collectors.groupingBy(
								Person::getCurrentAdminZone
								)
				)
			);
		return aliveAtLocation;
	}
	
	// get those alive with COVID at location
	private static Map<Boolean, Map<String, Map<Boolean, Map<Boolean, List<Infection>>>>> get_covid_at_location(
			WorldBankCovid19Sim world) {
		Map<Boolean, Map<String, Map<Boolean, Map<Boolean, List<Infection>>>>> covidAtLocation = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::isAlive,
						Collectors.groupingBy(
								Infection::getCurrentAdminZone,
								Collectors.groupingBy(
										Infection::isCovid,
										Collectors.groupingBy(
												Infection::hasRecovered
								)
						)
				)
			)
		);
		return covidAtLocation;
	}
	
	// get those who died of COVID at location
	private static Map<String, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>> get_dead_from_covid_at_location(
			WorldBankCovid19Sim world) {
		Map<String, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>> covidDeathsAtLocation = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::getCurrentAdminZone,
						Collectors.groupingBy(
								Infection::isCovid,
								Collectors.groupingBy(
										Infection::isCauseOfDeath,
										Collectors.groupingBy(
												Infection::getDeathLogged
								)
						)
				)
			)
		);
		return covidDeathsAtLocation;
	}
	// =============================== Demographic information logging =============================================================================
	// output for birthRateOutputFilename
	public class BirthRateReporter implements Steppable{
		
		WorldBankCovid19Sim world;
		boolean firstTimeReporting;
		
		public BirthRateReporter(WorldBankCovid19Sim myWorld) {
			this.world = myWorld;
			this.firstTimeReporting = true;
		}

		@Override
		public void step(SimState arg0) {
			Params params = world.params;

			// create list to store the counts of the number of females alive in each age range and the 
			// number of births in each age range.
			ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
			ArrayList <Integer> female_pregnancy_ages = new ArrayList<Integer>();
			// create a function to group the population by sex, age and whether they are alive
			Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = age_sex_alive_map(world);
			
			// create a function to group the population by sex, age and whether they gave birth
			Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_gave_birth = world.agents.stream().collect(
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
//			We now iterate over the age ranges, create a variable to keep track of the iterations
			Integer idx = 0;
			for (Integer val: birthrate_upper_age_range) {
				// for each age group we begin to count the number of people who fall into each category, create variables
				// to store this information in
				Integer female_count = 0;
				Integer female_gave_birth_count = 0;
				// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
				for (int age = birthrate_lower_age_range.get(idx); age < val; age++) {
					try {
						// try function necessary as some ages won't be present in the population
						// use the functions created earlier to calculate the number of people of each age group who fall
						// into the categories we are interested in (female, alive)
						female_count += age_sex_alive_map_copy.get(SEX.FEMALE).get(age).get(true).intValue();
					}
						catch (Exception e) {
							// age wasn't present in the population, skip
						}
					try {
						// try function necessary as some ages won't be present in the population
						// use the functions created earlier to calculate the number of people of each age group who fall
						// into the categories we are interested in (female, alive and gave birth)
						female_gave_birth_count += age_sex_map_gave_birth.get(SEX.FEMALE).get(age).get(true).get(false).intValue();
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

			String birth_rate_age_categories = t + "15_19" + t + "20_24" + t + "25_29" + t + "30_34" + t + "35_39" + t + "40_44" + t + "45_49" + "\n";
			if (this.firstTimeReporting) {
				age_dependent_birth_rate += "day" + birth_rate_age_categories + String.valueOf(time);
			}
			else {
				age_dependent_birth_rate += String.valueOf(time);
			}
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
			
			ImportExport.exportMe(world.birthRateOutputFilename, age_dependent_birth_rate, world.timer);
			// to make sure that births aren't counted more than once, update this person's properties
			for (Person p: world.agents) {
				if(p.gaveBirthLastYear()) {
					p.confirmBirthlogged();
					}
				}

			int currentYear = 1 + (int) Math.floor(time / 365);
			int nextYear = currentYear + 1;
			arg0.schedule.scheduleOnce((365 * nextYear - 1) * params.ticks_per_day, world.param_schedule_reporting, this);
			this.firstTimeReporting = false;
		}
		
	}
	
	public class CovidTestReporter implements Steppable{
		
		WorldBankCovid19Sim world;
		boolean firstTimeReporting;
		
		public CovidTestReporter(WorldBankCovid19Sim myWorld) {
			this.world = myWorld;
			this.firstTimeReporting = true;
		}

		@Override
		public void step(SimState arg0) {
			int dayOfSimulation = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
			int numberOfTestsPerDay = world.params.number_of_tests_per_day.get(dayOfSimulation);
			// create a function to group the population by sex, age and whether they gave birth
			Map<Boolean, Map<Boolean, List<Infection>>> hasTestedPositiveForCovid = (Map<Boolean, Map<Boolean,List<Infection>>>) world.infections.stream().collect(
					Collectors.groupingBy(Infection::hasTestedPositive,
											Collectors.groupingBy(
														Infection::getTestLogged,
														Collectors.toList()
								)
						)
			);
			int numberOfPositiveTests = 0;
			try {
				numberOfPositiveTests = hasTestedPositiveForCovid.get(true).get(false).size();
			}catch (Exception e) {
				numberOfPositiveTests = 0;
			}
			
			double fractionPositive = (double) numberOfPositiveTests / numberOfTestsPerDay;
			String covidTestingOutput = "";

			String t = "\t";
			String outputColumnNames = t + "numberOfTests" + t + "numberOfPositiveTests" + t + "fractionPositive" + "\n";
			if (this.firstTimeReporting) {
				covidTestingOutput += "day" + outputColumnNames + String.valueOf(dayOfSimulation);
				this.firstTimeReporting = false;
			}
			else {
				covidTestingOutput += String.valueOf(dayOfSimulation);
			}
			covidTestingOutput += t + String.valueOf(numberOfTestsPerDay) + t + String.valueOf(numberOfPositiveTests) + t + String.valueOf(fractionPositive) + "\n";
			
			
			ImportExport.exportMe(world.covidTestingOutputFilename, covidTestingOutput, world.timer);
			// to make sure that COVID tests aren't counted more than once, update this infections properties
			for (Infection i: world.infections) {
				if((i.isCovid() | i.isCovidSpuriousSymptom()) & !i.getTestLogged()) {
					i.confirmTestLogged();
					}
				}
		}
		
	}

	
	// output for populationOutputFilename
	public static Steppable ReportPopStructure (WorldBankCovid19Sim world) {
		// create a function to report the overall population structure
			return new Steppable(){					
				@Override
				public void step(SimState arg0) {
					//	create a list to define our age group search ranges

					// create list to store the counts of the number of males and females alive in each age range in each admin zone
					ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
					ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
					// create a function to group the population by sex, age and whether they are alive
					Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = age_sex_alive_map(world);
					//	We now iterate over the age ranges, create a variable to keep track of the iterations						
					Integer idx = 0;
					Integer male_count = null;
					Integer female_count = null;
					for (Integer val: upper_age_range) {
						// for each age group we begin to count the number of people who fall into each category, create variables
						// to store this information in
						male_count = 0;
						female_count = 0;
						// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
						for (int age = lower_age_range.get(idx); age < val; age++) {
							try {
								// try function necessary as some ages won't be present in the population
								// use the functions created earlier to calculate the number of people of each age group
								male_count += age_sex_alive_map_copy.get(SEX.MALE).get(age).get(true).intValue();
							}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
							try {
								// try function necessary as some ages won't be present in the population
								// use the functions created earlier to calculate the number of people of each age group
								female_count += age_sex_alive_map_copy.get(SEX.FEMALE).get(age).get(true).intValue();
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
					int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
					String population = "";

					String age_sex_categories = t + "sex" + t + age_categories + "\n";
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
					ImportExport.exportMe(world.populationOutputFilename, population, world.timer);
						
			}
		};		
	}
	
	// output for otherIncDeathOutputFilename
	public static Steppable ReportOtherIncidenceOfDeath(WorldBankCovid19Sim world) {

		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

//				calculate incidence of death in each age group by cause
				//	covid deaths, incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges

				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of covid deaths in each age range and the number of 'other' cause deaths in each age range
				ArrayList <Integer> male_other_deaths_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_other_deaths_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> male_alive_ages = get_number_of_alive(world, SEX.MALE);
				ArrayList <Integer> female_alive_ages = get_number_of_alive(world, SEX.FEMALE);

				// create a function to group the population by sex, age and whether they died from something other than covid
				Map<SEX, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_died_from_other = world.agents.stream().collect(
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
					Integer male_other_death_count = 0;
					Integer female_other_death_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = lower_age_range.get(idx); age < val; age++) {

						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							male_other_death_count += age_sex_map_died_from_other.get(SEX.MALE).get(age).get(true).get(false).intValue();
						}
						catch (Exception e) {
							// age wasn't present in the population, skip
						}
						try {
							// try function necessary as some ages won't be present in the population
							// use the functions created earlier to calculate the number of people of each age group who fall
							// into the categories we are interested in (alive, died from covid, died from other)
							female_other_death_count += age_sex_map_died_from_other.get(SEX.FEMALE).get(age).get(true).get(false).intValue();
						}
						catch (Exception e) {
							// age wasn't present in the population, skip
						}
					}

					male_other_deaths_by_ages.add(male_other_death_count);
					female_other_deaths_by_ages.add(female_other_death_count);
					// update the idx variable for the next iteration
					idx++;
				}
				// format log file
				String other_inc_death = "";

				if (time == 0) {
					other_inc_death += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					other_inc_death += String.valueOf(time);
				}
					
				// calculate incidence of other death in males this day
				other_inc_death += t + "m";
				for (int x = 0; x <male_other_deaths_by_ages.size(); x++){
					double male_other_deaths_in_age = male_other_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result = male_other_deaths_in_age / male_alive_in_age;
		            result *= 100000;
		            other_inc_death += t + String.valueOf(result);
				}
				other_inc_death += "\n";
				// calculate incidence of other death in females this day
				other_inc_death += String.valueOf(time) + t + "f";
				for (int x =0; x <female_other_deaths_by_ages.size(); x++){
					double female_other_deaths_in_age = female_other_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_other_deaths_in_age / female_alive_in_age;
		            result *= 100000;
		            other_inc_death += t +String.valueOf(result);
				}
				other_inc_death += "\n";

				// export the output files
				ImportExport.exportMe(world.otherIncDeathOutputFilename, other_inc_death, world.timer);
			}
		};
	}
	// =============================== Spatial disease logging  ====================================================================================
	// output for casesPerAdminZoneFilename
	public static Steppable ReportCovidCasesByTypeAndLocation(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;
				
				//	create a list of admin zones names to iterate over for our logging
				List <String> adminZoneList = myWorld.params.adminZoneNames;
						
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_alive_hasCovid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
											Infection::isAlive,
												Collectors.groupingBy(
														Infection::isCovid,
														Collectors.groupingBy(
																Infection::hasRecovered,
																Collectors.groupingBy(
																		Infection::getLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				
				// create a map to count the number of people who have recovered from covid in that admin zone
				
				Map<String, Map<Boolean, Long>> location_alive_recovered_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
											Collectors.groupingBy(
													Infection::hasRecovered,
										Collectors.counting()
										)
								)
						);

				
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_asympt_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
											Infection::isAlive,
												Collectors.groupingBy(
														Infection::isCovid,
															Collectors.groupingBy(
																	Infection::hasAsympt,
																		Collectors.groupingBy(
																				Infection::getAsymptLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);

				
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_mild_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
											Infection::isAlive,
												Collectors.groupingBy(
														Infection::isCovid,
															Collectors.groupingBy(
																	Infection::hasMild,
																		Collectors.groupingBy(
																				Infection::getMildLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);

				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_severe_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
											Infection::isAlive,
												Collectors.groupingBy(
														Infection::isCovid,
															Collectors.groupingBy(
																	Infection::hasSevere,
																		Collectors.groupingBy(
																				Infection::getSevereLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);

				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_critical_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
											Infection::isAlive,
												Collectors.groupingBy(
														Infection::isCovid,
															Collectors.groupingBy(
																	Infection::hasCritical,
																		Collectors.groupingBy(
																				Infection::getCriticalLogged,
										Collectors.counting()
										)
								)
						)
						)
						)
						);
				// create a function to group the population by location and count cumulative deaths
				Map<String, Map<Boolean, Map<Boolean, Long>>> location_cumulative_died_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
								Collectors.groupingBy(
										Infection::isCovid,
											Collectors.groupingBy(
													Infection::isCauseOfDeath,
										Collectors.counting()
										)
								)
							)
						);
				// create a function to group the population by location and count cumulative cases
				Map<String, Map<Boolean, Long>> location_cumulative_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
											Collectors.groupingBy(
													Infection::isCovid,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count new deaths

				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> location_new_deaths_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getCurrentAdminZone, 
									Collectors.groupingBy(
										Infection::isCovid, 
											Collectors.groupingBy(
													Infection::isCauseOfDeath,
													Collectors.groupingBy(
															Infection::getDeathLogged,
										Collectors.counting()
										)
									)
								)
							)
						);
				//	We now iterate over the admin zones, to find the current state of the epidemic
				for (String zone: adminZoneList) {
					// get the current number of cases in each admin zone
					try {
					covidCountArray.add(location_alive_hasCovid_map.get(zone).get(true).get(true).get(false).get(false).intValue());
					

					} catch (Exception e) {
						// No one in population met criteria
						covidCountArray.add(0);
					}
					// get the cumulative number of covid cases in the admin zone
					try {
						cumCovidCountArray.add(location_cumulative_covid_map.get(zone).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							cumCovidCountArray.add(0);
						}
					// get the number of asymptomatic covid cases in the admin zone
					try {
												asymptCovidCountArray.add(location_asympt_covid_map.get(zone).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							asymptCovidCountArray.add(0);
						}
					// get the number of mild covid cases in the admin zone
					try {
						mildCovidCountArray.add(location_mild_covid_map.get(zone).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							mildCovidCountArray.add(0);
						}
					// get the number of severe covid cases in the admin zone
					try {
						severeCovidCountArray.add(location_severe_covid_map.get(zone).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							severeCovidCountArray.add(0);
						}
					// get the number of critical covid cases in the admin zone
					try {
						criticalCovidCountArray.add(location_critical_covid_map.get(zone).get(true).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							criticalCovidCountArray.add(0);
						}
					// get the number of recoveries  in the admin zone
					try {
						recoveredCountArray.add(location_alive_recovered_map.get(zone).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							recoveredCountArray.add(0);
						}
					// get the cumultative number of covid deaths in the admin zone
					try {
						covidCumulativeDeathCount.add(location_cumulative_died_map.get(zone).get(true).get(true).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							covidCumulativeDeathCount.add(0);
						}
					// get the number of new covid deaths in the admin zone
					try {
						covidNewDeathCount.add(location_new_deaths_map.get(zone).get(true).get(true).get(false).intValue());
						} catch (Exception e) {
							// No one in population met criteria
							covidNewDeathCount.add(0);
						}
				}
				// report out findings
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// name the file
				String covidNumberOutput = "";
				// format the file
				String adminZoneNames = "";
				for (String zone: adminZoneList) {adminZoneNames += t + zone;}
				if (time == 0) {
					covidNumberOutput += "day" + t + "metric" + adminZoneNames + "\n" + String.valueOf(time);
				}
				else {
					covidNumberOutput += String.valueOf(time);
				}
				// store total number of cases in admin zone
				covidNumberOutput += t + "total_cases";
				for (int val: covidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of asymptomatic cases in admin zone
				covidNumberOutput += time + t + "total_asympt_cases";
				for (int val: asymptCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of mild cases in admin zone
				covidNumberOutput += time + t + "total_mild_cases";
				for (int val: mildCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of severe cases in admin zone
				covidNumberOutput += time + t + "total_severe_cases";
				for (int val: severeCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of critical cases in admin zone
				covidNumberOutput += time + t + "total_critical_cases";
				for (int val: criticalCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of recoveries in admin zone
				covidNumberOutput += time + t + "total_recovered";
				for (int val: recoveredCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store cumulative number of cases in admin zone
				covidNumberOutput += time + t + "cumulative_cases";
				for (int val: cumCovidCountArray){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store cumulative number of deaths in admin zone
				covidNumberOutput += time + t + "cumulative_deaths"; 
				for (int val: covidCumulativeDeathCount){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";
				// store total number of new deaths in admin zone
				covidNumberOutput += time + t + "new_deaths";
				for (int val: covidNewDeathCount){
					covidNumberOutput += t + String.valueOf(val);
				}
				covidNumberOutput += "\n";

				// export the file
				
				ImportExport.exportMe(world.casesPerAdminZoneFilename, covidNumberOutput, world.timer);
				}
			};
	}
	
	// output for adminZonePopSizeOutputFilename
	public static Steppable ReportAdminZonePopulationSize(WorldBankCovid19Sim world) {
		return new Steppable() {		
			@Override
			public void step(SimState arg0) {
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Integer> adminZonePopCounts = new ArrayList<Integer>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						adminZonePopCounts.add(aliveAtLocation.get(true).get(place).size());
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePopCounts.add(0);
					}
				}
				String pop_size_in_admin_zone = "";
		
				if (time == 0) {
					pop_size_in_admin_zone += "day" + t;
					for (String place: adminZones) {
						pop_size_in_admin_zone += place + t;
					}
					pop_size_in_admin_zone += "\n" + String.valueOf(time);
				}
				else {
					pop_size_in_admin_zone += "\n" + String.valueOf(time);
				}
				// store the population counts per admin zone
				for (int count: adminZonePopCounts) {
					pop_size_in_admin_zone += t + count;
				}
				// export the file
				ImportExport.exportMe(world.adminZonePopSizeOutputFilename, pop_size_in_admin_zone, world.timer);
			}
		};
	}
	
	// output for adminZoneCovidPrevalenceOutputFilename
	public static Steppable ReportPercentInAdminZoneWithCovid(WorldBankCovid19Sim world) {
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {

				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);
				// create a function to group the population by who is alive in each admin zone and has covid
				Map<Boolean, Map<String, Map<Boolean,  Map<Boolean, List<Infection>>>>> covidAtLocation = get_covid_at_location(world);

				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
	
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Integer> adminZonePopCounts = new ArrayList<Integer>();
				ArrayList <Integer> adminZoneCovidCounts = new ArrayList<Integer>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						adminZonePopCounts.add(aliveAtLocation.get(true).get(place).size());
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePopCounts.add(0);
						}
					// get covid counts in each admin zone
					try {
					adminZoneCovidCounts.add(covidAtLocation.get(true).get(place).get(true).get(false).size());
					}
					catch (Exception e) {
					// age wasn't present in the population, skip
					adminZoneCovidCounts.add(0);
					}
					
				}
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				// format the output for the percent of the admin zone with covid
				String percent_with_covid = "";
				if (time == 0) {
					percent_with_covid += "day" + t;
					for (String place: adminZones) {
						percent_with_covid += place + t;
					}
					percent_with_covid += "\n" + String.valueOf(time);
				}
				else {
					percent_with_covid += "\n" + String.valueOf(time);
				}
				int idx = 0;
				// calculate the percentage in the admin zone with covid
				for (float count: adminZoneCovidCounts) {
					float perc_with_covid = count / adminZonePopCounts.get(idx);
					percent_with_covid += t + perc_with_covid;
					idx++;
				}
				// export the file
				ImportExport.exportMe(world.adminZoneCovidPrevalenceOutputFilename, percent_with_covid, world.timer);
			}
		};
	}
	
	// output for adminZonePopBreakdownOutputFilename
	public static Steppable ReportAdminZoneAgeSexBreakdown(WorldBankCovid19Sim world) {
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {

				Map<Boolean, Map<String, Map<Integer, Map<SEX, List<Person>>>>> aliveAtLocationAgeSex = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::isAlive,
								Collectors.groupingBy(
										Person::getCurrentAdminZone,
										Collectors.groupingBy(
												Person::getAge,
												Collectors.groupingBy(
														Person::getSex
										)
						)
						)
						)
						);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				int idx = 0;

				// export the file
				String adminZoneLevelPopBreakdown = "";
				
				String admin_zone_age_sex_categories = t + "admin_zone" + t + "sex" + t + age_categories + "\n";
				if (time == 0) {
					adminZoneLevelPopBreakdown += "day" + admin_zone_age_sex_categories;
				}
				for (String place: adminZones) {
					adminZoneLevelPopBreakdown += time + t + place;
					// create lists to store the age gender breakdown of people in this admin zone
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
								male_count += aliveAtLocationAgeSex.get(true).get(place).get(age).get(SEX.MALE).size();
							}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
							try {
								// try function necessary as some ages won't be present in the population
								// use the functions created earlier to calculate the number of people of each age group who fall
								// into the categories we are interested in (alive, died from covid, died from other)
								female_count += aliveAtLocationAgeSex.get(true).get(place).get(age).get(SEX.FEMALE).size();
							}
							catch (Exception e) {
								// age wasn't present in the population, skip
							}
						}
					// store what we have found in the lists we created
					male_alive_ages.add(male_count);
					female_alive_ages.add(female_count);
				}
				adminZoneLevelPopBreakdown += t + "m";
				for (int count: male_alive_ages){
		            adminZoneLevelPopBreakdown += t + String.valueOf(count);
				}
				adminZoneLevelPopBreakdown += "\n";
				adminZoneLevelPopBreakdown += time + t + place;				
				adminZoneLevelPopBreakdown += t + "f";
				for (int count: female_alive_ages){
		            adminZoneLevelPopBreakdown += t + String.valueOf(count);
				}
				adminZoneLevelPopBreakdown += "\n";
				}
				ImportExport.exportMe(world.adminZonePopBreakdownOutputFilename, adminZoneLevelPopBreakdown, world.timer);

			}
		};
	}
	
	// output for adminZonePercentCovidCasesFatalOutputFilename
	public static Steppable ReportPercentOfCovidCasesThatAreFatalPerAdminZone(WorldBankCovid19Sim world) {
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by who is alive in each admin zone and has covid
				Map<Boolean, Map<String, Map<Boolean, Map<Boolean, List<Infection>>>>> covidAtLocation = get_covid_at_location(world);
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>> covidDeathsAtLocation = get_dead_from_covid_at_location(
						world);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Float> adminZonePercentCovidCasesFatal = new ArrayList<Float>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						// numerator = number of people at location who have died from covid, but have not had their deaths recorded
						int numerator = covidDeathsAtLocation.get(place).get(true).get(true).get(false).size();
						// denominator = number of people at location who currently are alive with covid plus those at location who have died from covid but not had their deaths recorded
						int denominator = covidAtLocation.get(true).get(place).get(true).get(false).size() + numerator;
						adminZonePercentCovidCasesFatal.add((float) numerator / denominator);
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePercentCovidCasesFatal.add(0f);
					}
				}
				// format log file
				String percent_covid_death_per_admin = "";

				if (time == 0) {
					percent_covid_death_per_admin += "day" + t;
					for (String place: adminZones) {
						percent_covid_death_per_admin += place + t;
					}
					percent_covid_death_per_admin += "\n" + String.valueOf(time);
				}
				else {
					percent_covid_death_per_admin += "\n" + String.valueOf(time);
				}
				// calculate the percentage in the admin zone with covid
				for (float percent: adminZonePercentCovidCasesFatal) {;
				percent_covid_death_per_admin += t + percent;
				}
				// export the file
				ImportExport.exportMe(world.adminZonePercentCovidCasesFatalOutputFilename, percent_covid_death_per_admin, world.timer);
				}
			};
	}
	
	// output for adminZonePercentCovidCasesFatalOutputFilename
	public static Steppable adminZonePercentDiedFromCovidOutputFilename(WorldBankCovid19Sim world) {
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by who is alive in each admin zone and has covid
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);
				// create a function to group the population by who died from covid at each admin zone
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>> covidDeathsAtLocation = get_dead_from_covid_at_location(
						world);
				// get a list of admin zone to iterate over
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				// create a list to store the number of people and who has covid in each admin zone
				ArrayList <Float> adminZonePercentCovidFatal = new ArrayList<Float>();
				// iterate over each admin zone
				for (String place: adminZones) {
					// get population counts in each admin zone
					try {
						// numerator = number of people at location who have died from covid, but have not had their deaths recorded
						int numerator = covidDeathsAtLocation.get(place).get(true).get(true).get(false).size();
						// denominator = number of people at location who currently are alive with covid plus those at location who have died from covid but not had their deaths recorded
						int denominator = aliveAtLocation.get(true).get(place).size() + numerator;
						adminZonePercentCovidFatal.add((float) numerator / denominator);
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						adminZonePercentCovidFatal.add(0f);
					}
				}
				// format log file
				String percent_covid_death_per_admin = "";

				if (time == 0) {
					percent_covid_death_per_admin += "day" + t;
					for (String place: adminZones) {
						percent_covid_death_per_admin += place + t;
					}
					percent_covid_death_per_admin += "\n" + String.valueOf(time);
				}
				else {
					percent_covid_death_per_admin += "\n" + String.valueOf(time);
				}
				// calculate the percentage in the admin zone with covid
				for (float percent: adminZonePercentCovidFatal) {;
				percent_covid_death_per_admin += t + percent;
				}
				// export the file
				ImportExport.exportMe(world.adminZonePercentDiedFromCovidOutputFilename, percent_covid_death_per_admin, world.timer);
				}
			};
	}
	// =============================== Non-spatial disease logging  ================================================================================

	// output for covidIncDeathOutputFilename
	public static Steppable ReportCovidIncidenceOfDeath(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				// calculate incidence of death in each age group by cause
				//	covid deaths, incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges

				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of covid deaths in each age range and the number of 'other' cause deaths in each age range
				ArrayList <Integer> male_covid_deaths_by_ages = get_covid_death_counts_by_age(world, SEX.MALE);
				ArrayList <Integer> female_covid_deaths_by_ages = get_covid_death_counts_by_age(world, SEX.FEMALE);
				ArrayList <Integer> male_alive_ages = get_number_of_alive(world, SEX.MALE);
				ArrayList <Integer> female_alive_ages = get_number_of_alive(world, SEX.FEMALE);

				// format log file
				String covid_inc_death = "";

				if (time == 0) {
					covid_inc_death += "day" + age_sex_categories + String.valueOf(time);
				}
				else {
					covid_inc_death += String.valueOf(time);
				}
				// calculate incidence of covid death in males this day
				covid_inc_death += t + "m";
				for (int x = 0; x <male_covid_deaths_by_ages.size(); x++){
					double male_covid_deaths_in_age = male_covid_deaths_by_ages.get(x);
					double male_alive_in_age = male_alive_ages.get(x);
					double result =  male_covid_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				// calculate incidence of covid death in females this day
				covid_inc_death += String.valueOf(time) + t + "f";
				for (int x =0; x <female_covid_deaths_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_deaths_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_covid_deaths_in_age / female_alive_in_age;	                
					result *= 100000;
	                covid_inc_death += t + String.valueOf(result);
				}
				covid_inc_death += "\n";
				

				// export the output files
				ImportExport.exportMe(world.covidIncDeathOutputFilename, covid_inc_death, world.timer);
				
				}
			};
		}
	
	// output for covidIncOutputFilename
	public static Steppable ReportIncidenceOfCovid(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

//				calculate incidence of death in each age group by cause
				//	covid deaths, incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges

				// create list to store the counts of the number of males and females alive in each age range, 
				// the number of covid deaths in each age range and the number of 'other' cause deaths in each age range
				ArrayList <Integer> male_alive_ages = get_number_of_alive(world, SEX.MALE);
				ArrayList <Integer> female_alive_ages = get_number_of_alive(world, SEX.FEMALE);
				// calculate incidence of Covid in each age group
				//	covid incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
				ArrayList <Integer> male_covid_by_ages = get_covid_counts_by_age(world, SEX.MALE);
				ArrayList <Integer> female_covid_by_ages =  get_covid_counts_by_age(world, SEX.FEMALE);
				// create a function to group the population by sex, age and whether they are alive
				
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
					double result =  male_covid_deaths_in_age / male_alive_in_age;
	                result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				// calculate the incidence of covid in females this day
				covid_inc += String.valueOf(time) + t + "f";

				for (int x =0; x <female_covid_by_ages.size(); x++){
					double female_covid_deaths_in_age = female_covid_by_ages.get(x);
					double female_alive_in_age = female_alive_ages.get(x);
					double result = female_covid_deaths_in_age / female_alive_in_age;	                
					result *= 100000;
	                covid_inc += t + String.valueOf(result);
				}
				covid_inc += "\n";
				
				// export the output file
				ImportExport.exportMe(world.covidIncOutputFilename, covid_inc, world.timer);
				}
			};
		}
	
	// output for covidCountsOutputFilename
	public static Steppable ReportCovidCounts(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				ArrayList <Integer> male_covid_by_ages = get_covid_counts_by_age(world, SEX.MALE);
				ArrayList <Integer> male_covid_deaths_by_ages = get_covid_death_counts_by_age(world, SEX.MALE);
				ArrayList <Integer> female_covid_by_ages =  get_covid_counts_by_age(world, SEX.FEMALE);
				ArrayList <Integer> female_covid_deaths_by_ages = get_covid_death_counts_by_age(world, SEX.FEMALE);

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
				ImportExport.exportMe(world.covidCountsOutputFilename, covid_number_and_deaths, world.timer);


			}
		};
	}
	
	// output for covidByEconOutputFilename
	public static Steppable ReportCovidCountsByOccupation(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

				OCCUPATION[] economic_status = OCCUPATION.values();

				ArrayList <Integer> status_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_death_counts = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a function to group the population by occupation, age and whether they have covid
				Map<OCCUPATION, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> economic_alive_has_covid = 
						world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getEconStatus, 
								Collectors.groupingBy(
										Infection::isAlive,
										Collectors.groupingBy(
												Infection::isCovid,
												Collectors.groupingBy(
														Infection::hasRecovered,
												Collectors.groupingBy(
														Infection::getLogged,
														Collectors.counting()
										)
								)
						)
						)
						)
						);
				Map<OCCUPATION, Map<Boolean, Long>> economic_alive = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
								Collectors.groupingBy(
										Person::isAlive,
												Collectors.counting()
										
								
						)
						)
						);
				// create a function to group the population by sex, age and whether they died from covid
				Map<OCCUPATION, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> econ_died_from_covid = world.infections.stream().collect(
						Collectors.groupingBy(
								Infection::getEconStatus, 
									Collectors.groupingBy(
											Infection::isCovid,
											Collectors.groupingBy(
													Infection::isCauseOfDeath,
													Collectors.groupingBy(
															Infection::getDeathLogged,
															Collectors.counting()
										)
									)
								)
						)
						);

				for (OCCUPATION status: world.occupationsInSim) {
					try {
					status_covid_counts.add(economic_alive_has_covid.get(status).get(true).get(true).get(false).get(false).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_covid_counts.add(0);
					}
					try {
						status_covid_death_counts.add(econ_died_from_covid.get(status).get(true).get(true).get(false).intValue());
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
				
				String econ_status_categories = "";
				for (OCCUPATION job: world.occupationsInSim) {
					econ_status_categories += job.name() + t;
				}
				econ_status_categories += "\n";
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
				
				ImportExport.exportMe(world.covidByEconOutputFilename, econ_status_output, world.timer);
			}
			};
	}

	
	
	// =============================== Reset the properties to avoid counting the same thing multiple times  ====================================

	public static Steppable ResetLoggedProperties(WorldBankCovid19Sim world) {
		return new Steppable() {			
			@Override
			public void step(SimState arg0) {
					// to make sure deaths and cases aren't counted multiple times, update this person's properties
					for (Infection i: world.infections) {
						if(!i.isAlive()) {
							i.confirmDeathLogged();
						}
						if(i.hasAsympt() & !i.getAsymptLogged()) {
							i.confirmAsymptLogged();
						}
						if(i.hasMild() & !i.getMildLogged()) {
							i.confirmMildLogged();
						}
						if(i.hasSevere() & !i.getSevereLogged()) {
							i.confirmSevereLogged();
						}
						if(i.hasCritical() & !i.getCriticalLogged()) {
							i.confirmCriticalLogged();
						}
						i.confirmLogged();
					} 
				}
			};
	}
		
}