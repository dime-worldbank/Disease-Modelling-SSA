package uk.ac.ucl.protecs.sim;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.protecs.objects.Person;

public interface DiseaseTesting {
	public static List<Person>  filterForEligibleCandidates(ArrayList <Person> population){return null;};
	public static double testAccuracy() {return 0.0;};
	public static void updatePropertiesForPositiveTest(Person p) {};
	public static void updatePropertiesForNegativeTest(Person p) {};
}
