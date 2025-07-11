package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.diseases.Disease;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;

public class CovidTesting implements DiseaseTesting {

	@SuppressWarnings("serial")
	public static Steppable Testing(WorldBankCovid19Sim world) {
		
		return new Steppable() {
		@Override
		public void step(SimState arg0) {
			runTests(arg0, world);
			}
		};
	}
	
	public static void runTests(SimState arg0, WorldBankCovid19Sim world) {
		// handles the administration of the tests and directs consequences of the tests
		int dayOfSimulation = (int) (arg0.schedule.getTime() / world.params.ticks_per_day);
		// get a list of infections to test today
		List <Disease> infections_to_test_today = filterForEligibleCandidates(world, dayOfSimulation);
		// test each infection, check the results of the test and update the infections properties
		for (Disease i: infections_to_test_today) {
			double random_to_check_if_test_is_accurate = world.random.nextDouble();
			if ((random_to_check_if_test_is_accurate < testAccuracy()) & (i.getDiseaseType().equals(DISEASE.COVID))){
				updatePropertiesForPositiveTest(i);
				} 
			else {
				updatePropertiesForNegativeTest(i);
				}
			
			}
		
	}
		
	public static double testAccuracy() {
		return 0.97;
	}

	public static void updatePropertiesForPositiveTest(Disease i) {
		i.setTested();
		i.setTestedPositive();
	}


	public static void updatePropertiesForNegativeTest(Disease i) {
		i.setTested();
	}


	public static List<Disease> filterForEligibleCandidates(WorldBankCovid19Sim world, int time) {
		// At this stage we want to filter the population to give tests to those who are:
		// 1) Alive
		// 2) Have symptomatic COVID (mild, severe and critical)
		// 3) Haven't been tested before
		// To do this we will use streams to search over a list of objects and draw those that have these properties
		// create a function to group the population by location and count new deaths
		Map<Boolean, Map<Boolean, Map<DISEASE, Map<Boolean, Map<Boolean, Map<Boolean, List<Disease>>>>>>> is_symptomatic_covid_or_covid_symptom = world.human_infections.stream().collect(
				Collectors.groupingBy(
						Disease::isHostAlive,
						Collectors.groupingBy(
								Disease::isSymptomatic,
								Collectors.groupingBy(
										Disease::getDiseaseType,
								Collectors.groupingBy(
								Disease::inATestingAdminZone,
									Collectors.groupingBy(
											Disease::isEligibleForTesting,
											Collectors.groupingBy(
													Disease::hasBeenTested,
									Collectors.toList()
									
								)
						)
					)
				)
			)
			)
			);
		// We also need to only give out the correct number of tests available for the disease this day.
		int number_of_tests_today = world.params.number_of_tests_per_day.get(time);
		List <Disease> eligible_for_testing = new ArrayList<>();
		List <Disease> eligible_for_testing_covid = new ArrayList<>();
		List <Disease> eligible_for_testing_covid_spurious_symptom = new ArrayList<>();
		// add potential COVID-19 infections to be tested
		try {
			eligible_for_testing_covid = is_symptomatic_covid_or_covid_symptom.get(true).get(true).get(DISEASE.COVID).get(true).get(true).get(false);
			eligible_for_testing.addAll(eligible_for_testing_covid);
		}
		catch (NullPointerException e) {}
		// add potential COVID-19 spurious symptoms to be tested

		try {
			eligible_for_testing_covid_spurious_symptom = is_symptomatic_covid_or_covid_symptom.get(true).get(true).get(DISEASE.COVIDSPURIOUSSYMPTOM).get(true).get(true).get(false);
			eligible_for_testing.addAll(eligible_for_testing_covid_spurious_symptom);
		}
		catch (NullPointerException e) {}
		// filter through the potential causes of Covid-19 symptoms and add to those eligible for testing if there are eligible persons
		if (eligible_for_testing.size() > 0) {
		try {
		if (eligible_for_testing.size() < number_of_tests_today) {
			eligible_for_testing = DiseaseTesting.pickRandom(world, eligible_for_testing, eligible_for_testing.size());
			}
		else{ 
			eligible_for_testing = DiseaseTesting.pickRandom(world, eligible_for_testing, number_of_tests_today);
			}
		}
		catch (NullPointerException e) {eligible_for_testing = Collections.emptyList();}
		}
		return eligible_for_testing;
	}

	
	
}
	