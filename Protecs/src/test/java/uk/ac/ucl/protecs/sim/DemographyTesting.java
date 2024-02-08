package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.helperFunctions.*;

public class DemographyTesting {
	@Test
	public void testBirthsAreIncreasingPopSize() {
		// Create the simulation object
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(12345, "src/main/resources/demography_params.txt", true);
		sim.start();
		// turn off deaths to only focus on births.
		turnOffBirthsOrDeaths(sim, "Deaths");
		// Increase the birth rate to ensure births take place
		helperFunctions.setParameterListsToValue(sim, sim.params.prob_birth_by_age, 1.0);
		// Run the simulation for 100 days
		int numDays = 100;
		
		int original_number_of_agents = sim.agents.size();
		while(sim.schedule.getTime() < Params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}	
		int final_number_of_agents = sim.agents.size();
		
		Assert.assertTrue(final_number_of_agents > original_number_of_agents);
			
	}
	
	public void turnOffBirthsOrDeaths(WorldBankCovid19Sim world, String whatToTurnOff) {
		switch (whatToTurnOff) {
		case "Births":
			helperFunctions.setParameterListsToValue(world, world.params.prob_birth_by_age, 0.0);
			break;
		case "Deaths":
			helperFunctions.setParameterListsToValue(world, world.params.prob_death_by_age_male, 0.0);
			helperFunctions.setParameterListsToValue(world, world.params.prob_death_by_age_female, 0.0);
			break;
		default:
			System.out.println("No part of the demography has been turned off");
		}
		
	}
	
}