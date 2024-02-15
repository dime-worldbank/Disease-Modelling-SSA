package uk.ac.ucl.protecs.sim;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.Person;

public interface DiseaseTesting {
	public static List<Person>  filterForEligibleCandidates(ArrayList <Person> population){return null;};
	public static double testAccuracy() {return 0.0;};
	public static void updatePropertiesForPositiveTest(Person p) {};
	public static void updatePropertiesForNegativeTest(Person p) {};
	public static <E> List<E> pickRandom(WorldBankCovid19Sim world, List<E> list, int n) {
	    return (List<E>)(world.random).ints(n, 0, list.size()).mapToObj(list::get).collect(Collectors.toList());
	  }
}
