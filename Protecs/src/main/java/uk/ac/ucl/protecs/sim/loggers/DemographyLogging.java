package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class DemographyLogging {
	
	static String t = LoggingHelperFunctions.tab;
	// age sex breakdown header
	private final static String age_sex_categories = t + "sex" + t + LoggingHelperFunctions.age_categories + "\n";

	// create specific age ranges for birthrate logging
	public final static List <Integer> birthrate_upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
	public final static List <Integer> birthrate_lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
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
			Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = LoggingHelperFunctions.age_sex_alive_map(world);
			
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

			this.firstTimeReporting = false;
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
					Map<SEX, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map_copy = LoggingHelperFunctions.age_sex_alive_map(world);
					//	We now iterate over the age ranges, create a variable to keep track of the iterations						
					Integer idx = 0;
					Integer male_count = null;
					Integer female_count = null;
					for (Integer val: LoggingHelperFunctions.upper_age_range) {
						// for each age group we begin to count the number of people who fall into each category, create variables
						// to store this information in
						male_count = 0;
						female_count = 0;
						// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
						for (int age = LoggingHelperFunctions.lower_age_range.get(idx); age < val; age++) {
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

					String age_sex_categories = t + "sex" + t + LoggingHelperFunctions.age_categories + "\n";
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
				ArrayList <Integer> male_alive_ages = LoggingHelperFunctions.get_number_of_alive(world, SEX.MALE);
				ArrayList <Integer> female_alive_ages = LoggingHelperFunctions.get_number_of_alive(world, SEX.FEMALE);

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
				for (Integer val: LoggingHelperFunctions.upper_age_range) {
					// for each age group we begin to count the number of people who fall into each category, create variables
					// to store this information in
					Integer male_other_death_count = 0;
					Integer female_other_death_count = 0;
					// iterate over the ages set in the age ranges (lower value from lower_age_range, upper from upper_age_range)
					for (int age = LoggingHelperFunctions.lower_age_range.get(idx); age < val; age++) {

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
	
	
	// output for adminZonePopSizeOutputFilename
	public static Steppable ReportAdminZonePopulationSize(WorldBankCovid19Sim world) {
		return new Steppable() {		
			@Override
			public void step(SimState arg0) {
				// format the output file for population counts
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
				List <String> adminZones = ((WorldBankCovid19Sim)arg0).params.adminZoneNames;
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = LoggingHelperFunctions.get_alive_at_location(world);
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
					pop_size_in_admin_zone += "day";
					for (String place: adminZones) {
						pop_size_in_admin_zone += t + place;
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
				
				String admin_zone_age_sex_categories = t + "admin_zone" + t + "sex" + t + LoggingHelperFunctions.age_categories + "\n";
				if (time == 0) {
					adminZoneLevelPopBreakdown += "day" + admin_zone_age_sex_categories;
				}
				for (String place: adminZones) {
					adminZoneLevelPopBreakdown += time + t + place;
					// create lists to store the age gender breakdown of people in this admin zone
					ArrayList <Integer> male_alive_ages = new ArrayList<Integer>();
					ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
					idx = 0;
					for (Integer val: LoggingHelperFunctions.upper_age_range) {
						// for each age group we begin to count the number of people who fall into each category, create variables
						// to store this information in
						Integer male_count = 0;
						Integer female_count = 0;
						for (int age = LoggingHelperFunctions.lower_age_range.get(idx); age < val; age++) {
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
		
}