package uk.ac.ucl.protecs.sim.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.OCCUPATION;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;
import uk.ac.ucl.protecs.sim.ImportExport;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class CovidLogging {
	private final static List <Integer> upper_age_range = Arrays.asList(1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 120);
	private final static List <Integer> lower_age_range = Arrays.asList(0, 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95);
	private final static String age_categories = "<1" + "\t" + "1_4" + "\t" + "5_9" + "\t" + "10_14" + "\t" + "15_19" + "\t" + "20_24" + "\t" + "25_29" + 
			"\t" + "30_34" + "\t" + "35_39" + "\t" + "40_44" + "\t" + "45_49" + "\t" + "50_54" + "\t" + "55_59" + "\t" + "60_64" + "\t" + "65_69" + "\t" + 
			"70_74" + "\t" + "75_79" + "\t" + "80_84" + "\t" + "85_89" + "\t" + "90_94" + "\t" + "95<";
	// tab shortcut
	private final static String t = "\t";
	// age sex breakdown header
	private final static String age_sex_categories = t + "sex" + t + age_categories + "\n";
	
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
	private static Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, List<Disease>>>>> get_covid_at_location(WorldBankCovid19Sim world) {
		Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, List<Disease>>>>> covidAtLocation = world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::isHostAlive,
						Collectors.groupingBy(
								Disease::getCurrentAdminZone,
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.groupingBy(
												Disease::hasRecovered
												)
										)
								)
						)
				);
		return covidAtLocation;
	}
	
	// get those who died of COVID at location
	private static Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>> get_dead_from_covid_at_location(
			WorldBankCovid19Sim world) {
		Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>> covidDeathsAtLocation = world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::getCurrentAdminZone,
						Collectors.groupingBy(
								Disease::getDiseaseType,
								Collectors.groupingBy(
										Disease::isCauseOfDeath,
										Collectors.groupingBy(
												Disease::getDeathLogged
								)
						)
				)
			)
		);
		return covidDeathsAtLocation;
	}
	
	// get those who died of COVID of age
	private static ArrayList <Integer> get_covid_death_counts_by_age(WorldBankCovid19Sim world, SEX sex) {
		Integer idx = 0;
		ArrayList <Integer> covid_death_by_ages = new ArrayList<Integer>();

		// create a function to group the population by sex, age and whether they have covid
		Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_died_from_covid = world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::getHostSex, 
						Collectors.groupingBy(
								Disease::getHostAge, 
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.groupingBy(
												Disease::isCauseOfDeath,
												Collectors.groupingBy(
														Disease::getDeathLogged,
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
					covid_death_count += age_sex_map_died_from_covid.get(sex).get(age).get(DISEASE.COVID).get(true).get(false).intValue();
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

	// get those who alive with COVID of given age and sex
	private static Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_has_covid_map(
			WorldBankCovid19Sim world) {
		Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_has_covid = world.infections.stream().collect(
				Collectors.groupingBy(
						Disease::getHostSex, 
						Collectors.groupingBy(
								Disease::getHostAge, 
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.groupingBy(
												Disease::hasRecovered,
										Collectors.groupingBy(
												Disease::getLogged,
												Collectors.counting()
								)
						)
				)
				)
				)
				);
		return age_sex_map_has_covid;
	}
	// get those alive with COVID of age
	private static ArrayList <Integer> get_covid_counts_by_age(WorldBankCovid19Sim world, SEX sex) {
		Integer idx = 0;
		ArrayList <Integer> covid_by_ages = new ArrayList<Integer>();

		// create a function to group the population by sex, age and whether they have covid
		Map<SEX, Map<Integer, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> age_sex_map_has_covid = age_sex_has_covid_map(world);
				
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
					covid_count += age_sex_map_has_covid.get(sex).get(age).get(DISEASE.COVID).get(false).get(false).intValue();
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
			Map<Boolean, Map<Boolean, List<Disease>>> hasTestedPositiveForCovid = (Map<Boolean, Map<Boolean,List<Disease>>>) world.infections.stream().collect(
					Collectors.groupingBy(Disease::hasTestedPositive,
											Collectors.groupingBy(
														Disease::getTestLogged,
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
			for (Disease i: world.infections) {
				if((i.getDiseaseType().equals(DISEASE.COVID))) {
					i.confirmTestLogged();
					}
				}
		}
		
	}
	
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
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_alive_hasCovid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
									Collectors.groupingBy(
											Disease::isHostAlive,
												Collectors.groupingBy(
														Disease::getDiseaseType,
														Collectors.groupingBy(
																Disease::hasRecovered,
																Collectors.groupingBy(
																		Disease::getLogged,
																		Collectors.counting()
																		)
																)
														)
												)
									)
						);
					
				// create a map to count the number of people who have recovered from covid in that admin zone
					
				Map<String, Map<DISEASE, Map<Boolean, Long>>> location_alive_recovered_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone,
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.groupingBy(
													Disease::hasRecovered,
										Collectors.counting()
										)
									)
								)
							);

					
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_asympt_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
									Collectors.groupingBy(
											Disease::isHostAlive,
											Collectors.groupingBy(
														Disease::getDiseaseType,
															Collectors.groupingBy(
																	Disease::hasAsympt,
																		Collectors.groupingBy(
																				Disease::getAsymptLogged,
																				Collectors.counting()
																				)
																		)
															)
											)
									)
						);

					
				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_mild_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
									Collectors.groupingBy(
											Disease::isHostAlive,
												Collectors.groupingBy(
														Disease::getDiseaseType,
															Collectors.groupingBy(
																	Disease::hasMild,
																		Collectors.groupingBy(
																				Disease::getMildLogged,
																				Collectors.counting()
																				)
																		)
															)
												)
									)
						);

				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_severe_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
									Collectors.groupingBy(
											Disease::isHostAlive,
												Collectors.groupingBy(
														Disease::getDiseaseType,
															Collectors.groupingBy(
																	Disease::hasSevere,
																		Collectors.groupingBy(
																				Disease::getSevereLogged,
																				Collectors.counting()
																				)
																		)
															)
												)
									)
						);

				Map<String, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> location_critical_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
									Collectors.groupingBy(
											Disease::isHostAlive,
												Collectors.groupingBy(
														Disease::getDiseaseType,
															Collectors.groupingBy(
																	Disease::hasCritical,
																		Collectors.groupingBy(
																				Disease::getCriticalLogged,
																				Collectors.counting()
																				)
																		)
															)
												)
									)
						);
				// create a function to group the population by location and count cumulative deaths
				Map<String, Map<DISEASE, Map<Boolean, Long>>> location_cumulative_died_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.groupingBy(
												Disease::isCauseOfDeath,
												Collectors.counting()
												)
										)
								)
						);
				// create a function to group the population by location and count cumulative cases
				Map<String, Map<DISEASE, Long>> location_cumulative_covid_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone, 
								Collectors.groupingBy(
										Disease::getDiseaseType,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count new deaths

				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>> location_new_deaths_map = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getCurrentAdminZone,
								Collectors.groupingBy(
											Disease::getDiseaseType, 
												Collectors.groupingBy(
														Disease::isCauseOfDeath,
														Collectors.groupingBy(
																Disease::getDeathLogged,
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
					covidCountArray.add(location_alive_hasCovid_map.get(zone).get(true).get(DISEASE.COVID).get(false).get(false).intValue());						
					} 
					catch (Exception e) {
						// No one in population met criteria
						covidCountArray.add(0);
					}
					// get the cumulative number of covid cases in the admin zone
					try {
						cumCovidCountArray.add(location_cumulative_covid_map.get(zone).get(DISEASE.COVID).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						cumCovidCountArray.add(0);
					}
					// get the number of asymptomatic covid cases in the admin zone
					try {
						asymptCovidCountArray.add(location_asympt_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						asymptCovidCountArray.add(0);
					}
					// get the number of mild covid cases in the admin zone
					try {
						mildCovidCountArray.add(location_mild_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						mildCovidCountArray.add(0);
					}
						// get the number of severe covid cases in the admin zone
					try {
						severeCovidCountArray.add(location_severe_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						severeCovidCountArray.add(0);
					}
					// get the number of critical covid cases in the admin zone
					try {
						criticalCovidCountArray.add(location_critical_covid_map.get(zone).get(true).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						criticalCovidCountArray.add(0);
					}
					// get the number of recoveries  in the admin zone
					try {
						recoveredCountArray.add(location_alive_recovered_map.get(zone).get(DISEASE.COVID).get(true).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						recoveredCountArray.add(0);
					}
					// get the cumultative number of covid deaths in the admin zone
					try {
						covidCumulativeDeathCount.add(location_cumulative_died_map.get(zone).get(DISEASE.COVID).get(true).intValue());
					} 
					catch (Exception e) {
						// No one in population met criteria
						covidCumulativeDeathCount.add(0);
					}
					// get the number of new covid deaths in the admin zone
					try {
						covidNewDeathCount.add(location_new_deaths_map.get(zone).get(DISEASE.COVID).get(true).get(false).intValue());
					} 
					catch (Exception e) {
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
	// output for adminZoneCovidPrevalenceOutputFilename
	public static Steppable ReportPercentInAdminZoneWithCovid(WorldBankCovid19Sim world) {
		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {

				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = get_alive_at_location(world);
				// create a function to group the population by who is alive in each admin zone and has covid
				Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, List<Disease>>>>> covidAtLocation = get_covid_at_location(world);

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
	// output for adminZonePercentCovidCasesFatalOutputFilename
	public static Steppable ReportPercentOfCovidCasesThatAreFatalPerAdminZone(WorldBankCovid19Sim world) {
		return new Steppable() {
				
			@Override
			public void step(SimState arg0) {
				// create a function to group the population by who is alive in each admin zone and has covid
				Map<Boolean, Map<String, Map<DISEASE, Map<Boolean, List<Disease>>>>> covidAtLocation = get_covid_at_location(world);
				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>> covidDeathsAtLocation = get_dead_from_covid_at_location(world);
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
				for (float percent: adminZonePercentCovidCasesFatal) {
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
				Map<String, Map<DISEASE, Map<Boolean, Map<Boolean, List<Disease>>>>> covidDeathsAtLocation = get_dead_from_covid_at_location(world);
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
				for (float percent: adminZonePercentCovidFatal) {
					percent_covid_death_per_admin += t + percent;
				}
				// export the file
				ImportExport.exportMe(world.adminZonePercentDiedFromCovidOutputFilename, percent_covid_death_per_admin, world.timer);
				}
			};
		}

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
				Map<OCCUPATION, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>>> economic_alive_has_covid = 
						world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getHostEconStatus, 
								Collectors.groupingBy(
										Disease::isHostAlive,
										Collectors.groupingBy(
												Disease::getDiseaseType,
												Collectors.groupingBy(
														Disease::hasRecovered,
												Collectors.groupingBy(
														Disease::getLogged,
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
				Map<OCCUPATION, Map<DISEASE, Map<Boolean, Map<Boolean, Long>>>> econ_died_from_covid = world.infections.stream().collect(
						Collectors.groupingBy(
								Disease::getHostEconStatus, 
									Collectors.groupingBy(
											Disease::getDiseaseType,
											Collectors.groupingBy(
													Disease::isCauseOfDeath,
													Collectors.groupingBy(
															Disease::getDeathLogged,
															Collectors.counting()
										)
									)
								)
						)
						);

				for (OCCUPATION status: world.occupationsInSim) {
					try {
					status_covid_counts.add(economic_alive_has_covid.get(status).get(true).get(DISEASE.COVID).get(false).get(false).intValue());
					}
					catch (Exception e) {
						// no one in population met criteria, skip
						status_covid_counts.add(0);
					}
					try {
						status_covid_death_counts.add(econ_died_from_covid.get(status).get(DISEASE.COVID).get(true).get(false).intValue());
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
	
	public static Steppable ResetCovidLoggedProperties(WorldBankCovid19Sim world) {
		return new Steppable() {			
			@Override
			public void step(SimState arg0) {
					// to make sure deaths and cases aren't counted multiple times, update this person's properties
					for (Disease i: world.infections) {
							if (i.isOfType(DISEASE.COVID)) {
							if(!i.isHostAlive()) {
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
				}
			};
	}

}