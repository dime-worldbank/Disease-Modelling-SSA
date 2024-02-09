package uk.ac.ucl.protecs.sim;
import java.util.ArrayList;
import uk.ac.ucl.protecs.objects.Person;

public interface DiseaseTesting {
	public void filterForEligibleCandidates(ArrayList <Person> population);
	public void testAccuracy(double fraction_accuracy);
	public void updateProperties();
}
