package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class CovidTesting implements DiseaseTesting {

	public static Steppable Testing(WorldBankCovid19Sim world) {
		return new Steppable() {
		@Override
		public void step(SimState arg0) {
			try {
			List <Person> people_to_test = filterForEligibleCandidates(world.agents);
			for (Person p: people_to_test) {
				double random_double = world.random.nextDouble();
				if (random_double > testAccuracy()) {
					updatePropertiesForNegativeTest(p);
					} 
				else {
					updatePropertiesForPositiveTest(p);
					}
				
			}
			} catch (NullPointerException e) {
				System.out.println("No one to test today");
			}
			}
		};
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

	public static List<Person> filterForEligibleCandidates(ArrayList<Person> population) {
		// At this stage we want to filter the population to give tests to those who are:
		// 1) Alive
		// 2) Have symptomatic COVID (mild, severe and critical)
		// 3) Haven't been tested before
		// To do this we will use streams to search over a list of objects and draw those that have these properties
		// create a function to group the population by location and count new deaths
		Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>> is_eligible_for_testing_map = population.stream().collect(
				Collectors.groupingBy(
						Person::isAlive,
						Collectors.groupingBy(
								Person::isEligibleForCovidTesting,
								Collectors.groupingBy(
										Person::hasBeenTestedForCovid,
								Collectors.toList()
								)
						)
				)
			);
		return is_eligible_for_testing_map.get(true).get(true).get(false);
	}

}
	