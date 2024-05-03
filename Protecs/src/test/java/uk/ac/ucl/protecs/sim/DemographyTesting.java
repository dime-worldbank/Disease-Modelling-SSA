package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.helperFunctions.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

// ================================================ Testing =======================================================================
// ===== Here we test that each of the functions in the demography testing are running as intended. Specifically we test that the =
// ===== birth functions increase the population size, births do not occur in men, death rates work and are dependent on biological
// ===== sex and that over the course of the simulation, people have their age updated when they have a birthday. =================
// ================================================================================================================================

public class DemographyTesting {
	@Test
	public void testBirthsAreIncreasingPopSize() {
		// Create the simulation object
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/demography_params.txt", true);
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
	@Test
	public void testBirthsDoNotOccurInMen() {		
		// Create the simulation object
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/demography_params.txt", true);
		sim.start();
		// turn off deaths to only focus on births.
		turnOffBirthsOrDeaths(sim, "Deaths");
		// Remove women in the simulation
		for (Person p: sim.agents) {
			if (p.getSex().equals("female")) {p.die("");}
		}
		// Run the simulation for 100 days
		int numDays = 100;
				
		int original_number_of_agents = sim.agents.size();
		while(sim.schedule.getTime() < Params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}	
		int final_number_of_agents = sim.agents.size();
		Assert.assertTrue(final_number_of_agents == original_number_of_agents);

	}
	@Test
	public void testDeathRatesAreSexDependent() {
		Random rand = new Random();
		int seed = rand.nextInt(1000000000);
		// Create the simulation objects
		WorldBankCovid19Sim sim_with_male_mortality = helperFunctions.CreateDummySimWithSeed(seed, "src/main/resources/demography_params.txt", true);
		sim_with_male_mortality.start();
		// turn off female mortality in this simulation
		helperFunctions.setParameterListsToValue(sim_with_male_mortality, sim_with_male_mortality.params.prob_death_by_age_female, 0.0);
		helperFunctions.setParameterListsToValue(sim_with_male_mortality, sim_with_male_mortality.params.prob_death_by_age_male, 0.5);

		WorldBankCovid19Sim sim_with_female_mortality = helperFunctions.CreateDummySimWithSeed(seed, "src/main/resources/demography_params.txt", true);
		// turn off female mortality in this simulation
		helperFunctions.setParameterListsToValue(sim_with_female_mortality, sim_with_female_mortality.params.prob_death_by_age_male, 0.0);
		helperFunctions.setParameterListsToValue(sim_with_female_mortality, sim_with_female_mortality.params.prob_death_by_age_female, 0.5);

	
		sim_with_female_mortality.start();
		// Make sure there are no births in either simulation
		turnOffBirthsOrDeaths(sim_with_male_mortality, "Births");
		turnOffBirthsOrDeaths(sim_with_female_mortality, "Births");
		// Get initial counts of the number of males and females that are alive in each simulation
		
		int with_male_mortality_initial_male_counts = getAliveCountsBySex(sim_with_male_mortality, "male", true);
		int with_male_mortality_initial_female_counts = getAliveCountsBySex(sim_with_male_mortality, "female", true);
		int with_female_mortality_initial_male_counts = getAliveCountsBySex(sim_with_female_mortality, "male", true);
		int with_female_mortality_initial_female_counts = getAliveCountsBySex(sim_with_female_mortality, "female", true);

		// Run the simulation for 100 days
		int numDays = 100;
						
		while(sim_with_male_mortality.schedule.getTime() < Params.ticks_per_day * numDays && !sim_with_male_mortality.schedule.scheduleComplete()){
			sim_with_male_mortality.schedule.step(sim_with_male_mortality);
		}	
		while(sim_with_female_mortality.schedule.getTime() < Params.ticks_per_day * numDays && !sim_with_female_mortality.schedule.scheduleComplete()){
			sim_with_female_mortality.schedule.step(sim_with_female_mortality);
		}	
		// Get the counts of the number of males and females that are alive at the end of the simulation
		int with_male_mortality_final_male_counts = getAliveCountsBySex(sim_with_male_mortality, "male", true);
		int with_male_mortality_final_female_counts = getAliveCountsBySex(sim_with_male_mortality, "female", true);
		int with_female_mortality_final_male_counts = getAliveCountsBySex(sim_with_female_mortality, "male", true);
		int with_female_mortality_final_female_counts = getAliveCountsBySex(sim_with_female_mortality, "female", true);
		
		// for the simulation with male mortality check that the number of alive men has decreased and the number of alive females is the same
		Assert.assertTrue(
				(with_male_mortality_initial_male_counts > with_male_mortality_final_male_counts) && 
				(with_male_mortality_initial_female_counts == with_male_mortality_final_female_counts)
		);
		// for the simulation with female mortality check that the number of alive women has decreased and the number of alive males is the same
		Assert.assertTrue(
				(with_female_mortality_initial_female_counts > with_female_mortality_final_female_counts) && 
				(with_female_mortality_initial_male_counts == with_female_mortality_final_male_counts)
		);
	}
	@Test
	public void testUpdateAges() {		
		// Create the simulation object
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/params.txt", true);
		sim.start();
		// turn off deaths births and deaths
		turnOffBirthsOrDeaths(sim, "Deaths");
		turnOffBirthsOrDeaths(sim, "Births");
		ArrayList <Integer> originalAges = new ArrayList <Integer> ();
		for (Person p: sim.agents) {
			originalAges.add(p.getAge());
		}
		// run for a year so that every one has had a birthday
		int numDays = 365;
				
		while(sim.schedule.getTime() < Params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}	
		ArrayList <Integer> finalAges = new ArrayList <Integer> ();
		for (Person p: sim.agents) {
			finalAges.add(p.getAge());
		}
		int number_of_people_in_sim = sim.agents.size();
		int idx = 0;
		int years_aged = 0;
		for (int final_age: finalAges) {
			int original_age = originalAges.get(idx);
			years_aged += final_age - original_age;
			idx++;
		}
		// As everyone in the simulation should have increased in age only once, the number of years aged in the simulation should be equal to the number of people in the simulation
		Assert.assertTrue(years_aged == number_of_people_in_sim);
	}

	// ================================================ Helper functions =======================================================================

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
	
	public int getAliveCountsBySex(WorldBankCovid19Sim world, String sex, boolean alive) {
		Map<String, Map<Boolean, Long>> sex_alive_map = world.agents.stream().collect(
				Collectors.groupingBy(
						Person::getSex,
								Collectors.groupingBy(
										Person::isAlive,
								Collectors.counting()
								)
						)
				);
		return sex_alive_map.get(sex).get(alive).intValue();
	}
	
}