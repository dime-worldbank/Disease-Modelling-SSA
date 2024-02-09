package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class Testing {
	
	 
	public static Steppable CovidTesting(WorldBankCovid19Sim world) {
		return new Steppable() {
		@Override
		public void step(SimState arg0) {
			
			int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);

			// ----------------------- Filter through the population to only give tests to those who are eligible ---------------------------------
			
			// we only want to test people who are alive and administer the tests per 1000 based on this 
			Map<Boolean, Map<Boolean, List<Person>>> is_elligable_for_testing_map = world.agents.stream().collect(
									Collectors.groupingBy(
											Person::isAlive,
											Collectors.groupingBy(
													Person::isElligableForTesting,
									Collectors.toList()
									)
								)
					);
			WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;			
			// create a counter for the number of positive tests
			int number_of_positive_tests = 0;
			// ------------------------ Go through this group of people and give them a COVID-19 test -----------------------------------------------
			// generate a list of people to test today
			List<Person> people_tested = new ArrayList<>();
			try {
				people_tested = world.pickRandom(is_elligable_for_testing_map.get(true).get(true), world.number_of_covid_tests_today);
			double test_accuracy = 0.97;
			// iterate over the list of people to test and perform the tests
			for (Person person:people_tested) {
				// Show that they have had a test
				person.hasBeenTested();
				// if they have COVID.....
				if(person.hasCovid()) {
					// And if this test correctly reports that this person has COVID...
					if (world.random.nextDouble() < test_accuracy) {
						// Update the counter and update the persons properties, showing that they have tested positive
						number_of_positive_tests ++;
						person.setTestedPositive();
					}
				// assume that after recieving a test they are no longer eligible for testing
				person.notEligibleForTesting();
				}
			}
			}
			catch (Exception e) {
			}
			
			// ---------------------------------------- Report and log the results of the COVID testing ---------------------------------------------
			// Calculate the percentage of tests that were positive.
			double percent_positive = 0;
			if (world.number_of_covid_tests_today > 0) {
				percent_positive = (double) number_of_positive_tests / (double) world.number_of_covid_tests_today; 
			}
			String t = "\t";
			
			String detected_covid_output = "";
			if (time == 0) {
				detected_covid_output += "day" + t + "number_of_detected_cases" + t + "number_of_tests" + t + "fraction_positive" + "\n"+ String.valueOf(time) + t + number_of_positive_tests + t + world.number_of_covid_tests_today + t + percent_positive+ "\n";
			}
			else {
				detected_covid_output += t + number_of_positive_tests + t + world.number_of_covid_tests_today + t + percent_positive + "\n";
			}
			ImportExport.exportMe(world.detectedCovidFilename, detected_covid_output, world.timer);
//			create a list of district names to iterate over for our logging
			List <String> districtList = myWorld.params.districtNames;

			// create list to store the number of cases per district
			ArrayList <Integer> covidTestedPositiveArray = new ArrayList<Integer>();

			// create a function to group the population by location, whether they are alive and if they have covid and if this is a new case
			Map<String, Map<Boolean, Map<Boolean, Long>>> location_alive_tested_pos_for_Covid_map = world.agents.stream().collect(
					Collectors.groupingBy(
							Person::getCurrentDistrict, 
								Collectors.groupingBy(
											Person::isAlive,
											Collectors.groupingBy(
													Person::hasTestedPos,
									Collectors.counting()
									)
							)
					)
					);
//			We now iterate over the districts, to find the current state of the epidemic
			for (String district: districtList) {
				// get the current number of cases in each district
				try {
					covidTestedPositiveArray.add(location_alive_tested_pos_for_Covid_map.get(district).get(true).get(true).intValue());
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
			ImportExport.exportMe(world.spatialdetectedCovidFilename, spatialOutput, world.timer);
			
			// -------------------------------------- Finally remove the people who have been tested ------------------------------------------------
			
			if (people_tested.size() > 0) {
			for (Person person:people_tested) {
				if(person.hasTestedPos()) {
					person.removeTestedPositive();
					}
			}
			}
	}
	
	};
	}
}