package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.Infection;
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
		List <Infection> infections_to_test_today = filterForEligibleCandidates(world, dayOfSimulation);
		// test each infection, check the results of the test and update the infections properties
		for (Infection i: infections_to_test_today) {
			double random_to_check_if_test_is_accurate = world.random.nextDouble();
			if (random_to_check_if_test_is_accurate < testAccuracy()){
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

	public static void updatePropertiesForPositiveTest(Infection i) {
		i.setTested();
		i.setTestedPositive();
	}


	public static void updatePropertiesForNegativeTest(Infection i) {
		i.setTested();
	}


	public static List<Infection> filterForEligibleCandidates(WorldBankCovid19Sim world, int time) {
		// At this stage we want to filter the population to give tests to those who are:
		// 1) Alive
		// 2) Have symptomatic COVID (mild, severe and critical)
		// 3) Haven't been tested before
		// To do this we will use streams to search over a list of objects and draw those that have these properties
		// create a function to group the population by location and count new deaths
		Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>>> is_symptomatic = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::isAlive,
						Collectors.groupingBy(
								Infection::isSymptomatic,
								Collectors.groupingBy(
								Infection::inATestingAdminZone,
									Collectors.groupingBy(
											Infection::isEligibleForTesting,
											Collectors.groupingBy(
													Infection::hasBeenTested,
									Collectors.toList()
									
								)
						)
				)
			)
			)
			);
		
		Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>>>> is_symptomatic_covid = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::isAlive,
						Collectors.groupingBy(
								Infection::isSymptomatic,
								Collectors.groupingBy(
										Infection::isCovid,
								Collectors.groupingBy(
								Infection::inATestingAdminZone,
									Collectors.groupingBy(
											Infection::isEligibleForTesting,
											Collectors.groupingBy(
													Infection::hasBeenTested,
									Collectors.toList()
									
								)
						)
					)
				)
			)
			)
			);
		Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Infection>>>>>>> is_covid_spurious_symptom = world.infections.stream().collect(
				Collectors.groupingBy(
						Infection::isAlive,
						Collectors.groupingBy(
								Infection::isSymptomatic,
								Collectors.groupingBy(
										Infection::isCovidSpuriousSymptom,
								Collectors.groupingBy(
								Infection::inATestingAdminZone,
									Collectors.groupingBy(
											Infection::isEligibleForTesting,
											Collectors.groupingBy(
													Infection::hasBeenTested,
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
		List <Infection> eligible_for_testing = new ArrayList<>();
		List <Infection> eligible_for_testing_covid = new ArrayList<>();
		List <Infection> eligible_for_testing_covid_spurious_symptom = new ArrayList<>();
		// add potential COVID-19 infections to be tested
		try {
			eligible_for_testing_covid = is_symptomatic_covid.get(true).get(true).get(true).get(true).get(true).get(false);
			eligible_for_testing.addAll(eligible_for_testing_covid);
		}
		catch (NullPointerException e) {}
		// add potential COVID-19 spurious symptoms to be tested

		try {
			eligible_for_testing_covid_spurious_symptom = is_covid_spurious_symptom.get(true).get(true).get(true).get(true).get(true).get(false);
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
	