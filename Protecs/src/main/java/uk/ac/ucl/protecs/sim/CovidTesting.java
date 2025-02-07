package uk.ac.ucl.protecs.sim;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;
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
		// get people to test today
		List <Person> people_to_test_today = filterForEligibleCandidates(world, dayOfSimulation);
		// test each person, check the results of the test and update the person's properties
		for (Person p: people_to_test_today) {
			double random_to_check_if_test_is_accurate = world.random.nextDouble();
			if (p.getInfectionSet().containsKey(DISEASE.COVID.key) && random_to_check_if_test_is_accurate < testAccuracy()){
				updatePropertiesForPositiveTest(p);
				} 
			else {
				updatePropertiesForNegativeTest(p);
				}
			
			}
		
	}
		
	public static double testAccuracy() {
		return 0.97;
	}

	public static void updatePropertiesForPositiveTest(Person p) {
		p.setHasBeenTestedForCovid();
		p.setTestedPositiveForCovid();
	}

	public static void updatePropertiesForNegativeTest(Person p) {
		p.setHasBeenTestedForCovid();
	}

	public static List<Person> filterForEligibleCandidates(WorldBankCovid19Sim world, int time) {
		// At this stage we want to filter the population to give tests to those who are:
		// 1) Alive
		// 2) Have symptomatic COVID (mild, severe and critical)
		// 3) Haven't been tested before
		// To do this we will use streams to search over a list of objects and draw those that have these properties
		// create a function to group the population by location and count new deaths
		Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>> is_eligible_for_testing_map = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::isAlive,
						Collectors.groupingBy(
								Person::isEligibleForCovidTesting,
								Collectors.groupingBy(
									Person::inADistrictTesting,
									Collectors.groupingBy(
											Person::hasBeenTestedForCovid,
									Collectors.toList()
									)
								)
						)
				)
			);
		// We also need to only give out the correct number of tests available for the disease this day.
		int number_of_tests_today = world.params.number_of_tests_per_day.get(time);
		List <Person> eligible_for_testing = Collections.emptyList();
		try {
			eligible_for_testing = is_eligible_for_testing_map.get(true).get(true).get(true).get(false);

			if (eligible_for_testing.size() < number_of_tests_today) {
				eligible_for_testing = DiseaseTesting.pickRandom(world, eligible_for_testing, eligible_for_testing.size());
				}
			else{ 
				eligible_for_testing = DiseaseTesting.pickRandom(world, eligible_for_testing, number_of_tests_today);
				}
			}
		catch (NullPointerException e) {eligible_for_testing = Collections.emptyList();}
		
		return eligible_for_testing;
	}


	
	
}
	