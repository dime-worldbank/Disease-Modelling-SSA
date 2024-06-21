package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class Logging {
	
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

			//	calculate the birth rate in age groups 15-19, 10-14, ..., 45-49
			//	create a list to define our age group search ranges
			List <Integer> upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
			List <Integer> lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
			// create list to store the counts of the number of females alive in each age range and the 
			// number of births in each age range.
			ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
			ArrayList <Integer> female_pregnancy_ages = new ArrayList<Integer>();
			// create a function to group the population by sex, age and whether they are alive
			Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
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
			// create a function to group the population by sex, age and whether they gave birth
			Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_gave_birth = world.agents.stream().collect(
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
			if (this.firstTimeReporting) {
				age_dependent_birth_rate += "day" + age_categories + String.valueOf(time);
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
			Params params = world.params;
			int dayOfSimulation = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
			int numberOfTestsPerDay = world.params.number_of_tests_per_day.get(dayOfSimulation);
			// create a function to group the population by sex, age and whether they gave birth
			Map<Boolean, Map<Boolean, List<Person>>> hasTestedPositiveForCovid = (Map<Boolean, Map<Boolean,List<Person>>>) world.agents.stream().collect(
					Collectors.groupingBy(Person::hasTestedPositiveForCovid,
											Collectors.groupingBy(
														Person::getCovidTestLogged,
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
			// to make sure that births aren't counted more than once, update this person's properties
			for (Person p: world.agents) {
				if(!p.getCovidTestLogged()) {
					p.confirmCovidTestingLogged();
					}
				}
		}
		
	}

	
	public static Steppable TestLoggingCase(WorldBankCovid19Sim world) {

		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;
				
				//	create a list of district names to iterate over for our logging
				List <String> districtList = myWorld.params.districtNames;
						
/*						Arrays.asList(
						"d_1", "d_2", "d_3", "d_4", "d_5", "d_6", "d_7", "d_8", "d_9", "d_10", "d_11", "d_12", "d_13", "d_14", "d_15", 
						"d_16", "d_17", "d_18", "d_19", "d_20", "d_21", "d_22", "d_23", "d_24", "d_25", "d_26", "d_27", "d_28", "d_29", 
						"d_30", "d_31", "d_32", "d_33", "d_34", "d_35", "d_36", "d_37", "d_38", "d_39", "d_40", "d_41", "d_42", "d_43", 
						"d_44", "d_45", "d_46", "d_47", "d_48", "d_49", "d_50", "d_51", "d_52", "d_53", "d_54", "d_55", "d_56", "d_57", 
						"d_58", "d_59", "d_60"); */
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> location_alive_hasCovid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Long>> location_alive_recovered_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::hasRecovered,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location, whether they are alive and which type of covid infection they have
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> location_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_asympt_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_mild_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_severe_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>>> location_critical_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
									Collectors.groupingBy(
												Person::isAlive,
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
				Map<String, Map<Boolean, Long>> location_cumulative_died_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::isDeadFromCovid,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count cumulative cases
				Map<String, Map<Boolean, Long>> location_cumulative_covid_map = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getCurrentDistrict, 
											Collectors.groupingBy(
													Person::hadCovid,
										Collectors.counting()
										)
								)
						);
				// create a function to group the population by location and count new deaths
				Map<String, Map<Boolean, Map<Boolean, Long>>> location_new_deaths_map = world.agents.stream().collect(
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
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
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
				
				ImportExport.exportMe(world.newLoggingFilename, covidNumberOutput, world.timer);
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
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
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
				// create a function to group the population by sex, age and whether they died from covid
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_died_from_covid = world.agents.stream().collect(
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
				Map<String, Map<Integer, Map <Boolean, Map<Boolean, Long>>>> age_sex_map_died_from_other = world.agents.stream().collect(
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
				ImportExport.exportMe(world.covidIncDeathOutputFilename, covid_inc_death, world.timer);
				ImportExport.exportMe(world.otherIncDeathOutputFilename, other_inc_death, world.timer);
//				calculate incidence of Covid in each age group
				//	covid incidence in age groups 0-1, 1-4, 5-9, 10-14, ..., 95+
				//	create a list to define our age group search ranges
				// create list to store the counts of the number of males and females alive in each age range and
				// the number of covid cases in each age range 
				ArrayList <Integer> male_covid_by_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_covid_by_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a function to group the population by sex, age and whether they have covid
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_has_covid = world.agents.stream().collect(
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

				ArrayList <Integer> status_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_counts = new ArrayList<Integer>();
				ArrayList <Integer> status_covid_death_counts = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				
				// create a function to group the population by occupation, age and whether they have covid
				Map<String, Map<Boolean, Map<Boolean, Map<Boolean, Long>>>> economic_alive_has_covid = 
						world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
								Collectors.groupingBy(
										Person::isAlive,
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
				Map<String, Map<Boolean, Long>> economic_alive = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::getEconStatus, 
								Collectors.groupingBy(
										Person::isAlive,
														Collectors.counting()
										
								
						)
						)
						);
				// create a function to group the population by sex, age and whether they died from covid
				Map<String, Map<Boolean, Map<Boolean, Long>>> econ_died_from_covid = world.agents.stream().collect(
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
				for (String status: world.occupations_in_sim) {
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
				String econ_status_categories = "";
				for (String occ: world.occupations_in_sim) econ_status_categories += occ + t;
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
				// to make sure deaths and cases aren't counted multiple times, update this person's properties

				for (Person p: world.agents) {
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
	}

	
	public static Steppable UpdateDistrictLevelInfo(WorldBankCovid19Sim world) {
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {

				// create a function to group the population by who is alive at this district
				Map<Boolean, Map<String, List<Person>>> aliveAtLocation = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::isAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict
										)
						)
						);
				// create a function to group the population by who is alive in each district and has covid
				Map<Boolean, Map<String, Map<Boolean, List<Person>>>> covidAtLocation = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::isAlive,
								Collectors.groupingBy(
										Person::getCurrentDistrict,
										Collectors.groupingBy(
												Person::hasCovid
										)
						)
						)
						);
				Map<Boolean, Map<String, Map<Integer, Map<String, List<Person>>>>> aliveAtLocationAgeSex = world.agents.stream().collect(
						Collectors.groupingBy(
								Person::isAlive,
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
						
				// create a list to store the number of people and who has covid in each district
				ArrayList <Integer> districtPopCounts = new ArrayList<Integer>();
				ArrayList <Integer> districtCovidCounts = new ArrayList<Integer>();
				// iterate over each district
				for (String place: districts) {
					// get population counts in each district
					try {
						districtPopCounts.add(aliveAtLocation.get(true).get(place).size());
					}
					catch (Exception e) {
						// age wasn't present in the population, skip
						districtPopCounts.add(0);
						}
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
				int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

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
				ImportExport.exportMe(world.distPopSizeOutputFilename, pop_size_in_district, world.timer);
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
				ImportExport.exportMe(world.distCovidPrevalenceOutputFilename, percent_with_covid, world.timer);
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
				ImportExport.exportMe(world.distPopBreakdownOutputFilename, districtLevelPopBreakdown, world.timer);

			}
		};
		
	}
	
	
	public static Steppable ReportBirthRatesOLD(WorldBankCovid19Sim world) {
		// create a function to report on birth rates
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				Params params = world.params;

				//	calculate the birth rate in age groups 15-19, 10-14, ..., 45-49
				//	create a list to define our age group search ranges
				List <Integer> upper_age_range = Arrays.asList(20, 25, 30, 35, 40, 45, 50);
				List <Integer> lower_age_range = Arrays.asList(15, 20, 25, 30, 35, 40, 45);
				// create list to store the counts of the number of females alive in each age range and the 
				// number of births in each age range.
				ArrayList <Integer> female_alive_ages = new ArrayList<Integer>();
				ArrayList <Integer> female_pregnancy_ages = new ArrayList<Integer>();
				// create a function to group the population by sex, age and whether they are alive
				Map<String, Map<Integer, Map<Boolean, Long>>> age_sex_alive_map = world.agents.stream().collect(
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
				// create a function to group the population by sex, age and whether they gave birth
				Map<String, Map<Integer, Map<Boolean, Map<Boolean, Long>>>> age_sex_map_gave_birth = world.agents.stream().collect(
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
				
				
			}
			};
			
			
	}
	
}