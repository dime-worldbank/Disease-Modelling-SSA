package uk.ac.ucl.protecs.sim;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ucl.protecs.objects.Person;

public class CovidTesting implements DiseaseTesting {
	public static Steppable Testing(WorldBankCovid19Sim world) {
		
		// Functions that control the background demography of the model
		// Create a function to create births in the population
		
		return new Steppable() {
			
			@Override
			public void step(SimState arg0) {
				System.out.println("I've been called");
				}
			};
		}

	@Override
	public void filterForEligibleCandidates(ArrayList<Person> population) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testAccuracy(double fraction_accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateProperties() {
		// TODO Auto-generated method stub
		
	}

}
	