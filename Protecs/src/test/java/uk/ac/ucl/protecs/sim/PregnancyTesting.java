package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.birthsOrDeaths;

import org.junit.Test;

import uk.ac.ucl.protecs.objects.hosts.Person;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PregnancyTesting {
	
	private final static String paramsDir = "src/test/resources/";
	

	@Test
	public void testPregnancyIsReset() {
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.demographyFramework.getProb_birth_by_age(), 0.05);
		
		// Run the simulation for one day to set up the births
		int numDays = 1; 		
		HelperFunctions.runSimulation(sim, numDays);
		// Get the original set of pregnant women
		List<Person> currently_pregnant = get_pregnant(sim).get(true).get(true);
		
		int original_number_of_pregnant_women = currently_pregnant.size();
		// keep running the simulation for 8 months so that some pregnancies will have resolved
		numDays = 8 * 30;
		HelperFunctions.runSimulation(sim, numDays);
		// check the number of people currently pregnant after this year's births are done
		currently_pregnant = get_pregnant(sim).get(true).get(true);
		int final_number_of_pregnant_women = 0;
		try {
			final_number_of_pregnant_women = currently_pregnant.size();
		}
		catch (Exception e) {
			// no pregnant woman
		}		
		// As some of the initially set up pregnancies should have resolved, the final number of pregnant people should be less than the initial number of 
		// pregnant people
		Assert.assertTrue(original_number_of_pregnant_women > final_number_of_pregnant_women);
		
	}
	
	@Test
	public void testPregnancyLastsNineMonths() {
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.demographyFramework.getProb_birth_by_age(), 0.05);
		// Run the simulation for one day to set up the births
		int numDays = 1; 		
		HelperFunctions.runSimulation(sim, numDays);
		// Get the original set of pregnant women
		List<Person> currently_pregnant = get_pregnant(sim).get(true).get(true);
		
		// keep running the simulation for 10 months so that the births scheduled for the initial 9 months and those starting during the first month
		// of simulation time will have resolved
		numDays = 10 * 30;
		HelperFunctions.runSimulation(sim, numDays);
		// iterate over the set of original pregnant woman and check that none are still pregnant
		for (Person p: currently_pregnant) {
			Assert.assertFalse(p.isPregnant());

		}
	}
	
	@Test
	public void pregnantPeopleDoNotImmediatelyGetPregnantAgain() {
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.demographyFramework.getProb_birth_by_age(), 0.05);
		// Run the simulation for one day to set up the births
		int numDays = 1; 		
		HelperFunctions.runSimulation(sim, numDays);
		// Get the original set of pregnant women
		List<Person> currently_pregnant = get_pregnant(sim).get(true).get(true);
		
		// keep running the simulation for one year (the minimum time that those originally pregnant will need to have the option for births again)
		numDays = 365;
		HelperFunctions.runSimulation(sim, numDays);
		// iterate over the set of original pregnant woman and check that none are still pregnant
		for (Person p: currently_pregnant) {
			Assert.assertFalse(p.isPregnant());

		}
	}
	
	@Test
	public void whenWeTurnTheBirthRateOffThereAreNoBirths() {
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		int initialNumberOfPeople = HelperFunctions.GetNumberAlive(sim);
		// turn off deaths
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.demographyFramework.getProb_birth_by_age(), 0.00);
		// Run the simulation for one day to set up the births
		int numDays = 365; 		
		HelperFunctions.runSimulation(sim, numDays);
		int finalNumberOfPeople = HelperFunctions.GetNumberAlive(sim);

		Assert.assertTrue(initialNumberOfPeople == finalNumberOfPeople);

		
	}
	
	// ================================================ Helper functions =======================================================================

	// get those who are currently pregnant
	public static Map<Boolean, Map<Boolean, List<Person>>> get_pregnant(WorldBankCovid19Sim world) {
		// create a function to group the population by who is alive and pregnant
		Map<Boolean, Map<Boolean, List<Person>>> pregnant = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::isAlive,
						Collectors.groupingBy(
								Person::isPregnant
								)
						)
				);
		return pregnant;
	}

}
