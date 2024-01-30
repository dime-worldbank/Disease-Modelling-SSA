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
	
	// create function to randomly select a person to perform the test on	
	private static <E> List<E> pickRandom(List<E> list, int n, Random rand) {
	    return (List<E>)(new Random()).ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
	  }
	
	public static Steppable CovidTesting(WorldBankCovid19Sim world) {
		return new Steppable() {
		@Override
		public void step(SimState arg0) {
			// get the simulation time
			int time = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
			// get the index for the test numbers
			int index_for_test_number = world.params.test_dates.indexOf(time);
			// find the number of tests per 1000 to test today
			int number_of_tests_today = 0;
			try {
				number_of_tests_today = world.params.number_of_tests_per_day.get(index_for_test_number);
				}
			catch (Exception e) {
			}
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
			WorldBankCovid19Sim myWorld = (WorldBankCovid19Sim) arg0;								// create a random state (I need to link this to the existing random state but don't know how)
			Random testing_random = new Random(myWorld.seed());
			int number_of_positive_tests = 0;
			double percent_positive = 0;
			// generate a list of people to test today
			try {
				List<Person> people_tested = pickRandom(is_elligable_for_testing_map.get(true).get(true), number_of_tests_today, testing_random);
			// create a counter for the number of positive tests
			double test_accuracy = 0.97;
			// iterate over the list of people to test and perform the tests
			for (Person person:people_tested) {
				if(person.hasCovid()) {
					if (world.random.nextDouble() < test_accuracy) {
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
	}
}