package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.HelperFunctions.birthsOrDeaths;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;

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

	private final static String paramsDir = "src/test/resources/";

	@Test
	public void testBirthsAreIncreasingPopSize() {
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths to only focus on births.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Increase the birth rate to ensure births take place
		HelperFunctions.setParameterListsToValue(sim, sim.demographyFramework.getProb_birth_by_age(), 1.0);
		
		// Run the simulation for 100 days
		int numDays = 100; 
		
		int original_number_of_agents = sim.agents.size();
		HelperFunctions.runSimulation(sim, numDays);
		int final_number_of_agents = sim.agents.size();
		
		Assert.assertTrue(final_number_of_agents > original_number_of_agents);
	}
	@Test
	public void testBirthsDoNotOccurInMen() {		
		// Create the simulation object
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths to only focus on births.
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		// Remove women in the simulation
		for (Person p: sim.agents) {
			if (p.getSex().equals(SEX.FEMALE)) {p.die("");}
		}
		// Run the simulation for 100 days
		int numDays = 100;
				
		int original_number_of_agents = sim.agents.size();
		HelperFunctions.runSimulation(sim, numDays);
		int final_number_of_agents = sim.agents.size();
		Assert.assertTrue(final_number_of_agents == original_number_of_agents);

	}
	@Test
	public void testDeathRatesAreSexDependent() {
		Random rand = new Random();
		int seed = rand.nextInt(1000000000);
		// Create the simulation objects
		WorldBankCovid19Sim sim_with_male_mortality = HelperFunctions.CreateDummySimWithSeed(seed, "src/test/resources/params_demography.txt");
		sim_with_male_mortality.start();
		// turn off female mortality in this simulation
		HelperFunctions.setParameterListsToValue(sim_with_male_mortality, sim_with_male_mortality.demographyFramework.getProb_death_by_age_female(), 0.0);
		HelperFunctions.setParameterListsToValue(sim_with_male_mortality, sim_with_male_mortality.demographyFramework.getProb_death_by_age_male(), 0.5);

		WorldBankCovid19Sim sim_with_female_mortality = HelperFunctions.CreateDummySimWithSeed(seed, "src/test/resources/params_demography.txt");
		sim_with_female_mortality.start();
		// turn off female mortality in this simulation
		HelperFunctions.setParameterListsToValue(sim_with_female_mortality, sim_with_female_mortality.demographyFramework.getProb_death_by_age_male(), 0.0);
		HelperFunctions.setParameterListsToValue(sim_with_female_mortality, sim_with_female_mortality.demographyFramework.getProb_death_by_age_female(), 0.5);

	
		// Make sure there are no births in either simulation
		HelperFunctions.turnOffBirthsOrDeaths(sim_with_male_mortality, birthsOrDeaths.births);
		HelperFunctions.turnOffBirthsOrDeaths(sim_with_female_mortality, birthsOrDeaths.births);
		// Get initial counts of the number of males and females that are alive in each simulation
		
		int with_male_mortality_initial_male_counts = getAliveCountsBySex(sim_with_male_mortality, SEX.MALE, true);
		int with_male_mortality_initial_female_counts = getAliveCountsBySex(sim_with_male_mortality, SEX.FEMALE, true);
		int with_female_mortality_initial_male_counts = getAliveCountsBySex(sim_with_female_mortality, SEX.MALE, true);
		int with_female_mortality_initial_female_counts = getAliveCountsBySex(sim_with_female_mortality, SEX.FEMALE, true);

		// Run the simulation for 100 days
		int numDays = 100;
						
		HelperFunctions.runSimulation(sim_with_male_mortality, numDays);
		HelperFunctions.runSimulation(sim_with_female_mortality, numDays);	
		// Get the counts of the number of males and females that are alive at the end of the simulation
		int with_male_mortality_final_male_counts = getAliveCountsBySex(sim_with_male_mortality, SEX.MALE, true);
		int with_male_mortality_final_female_counts = getAliveCountsBySex(sim_with_male_mortality, SEX.FEMALE, true);
		int with_female_mortality_final_male_counts = getAliveCountsBySex(sim_with_female_mortality, SEX.MALE, true);
		int with_female_mortality_final_female_counts = getAliveCountsBySex(sim_with_female_mortality, SEX.FEMALE, true);
		
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
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_demography.txt");
		sim.start();
		// turn off deaths births and deaths
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.deaths);
		HelperFunctions.turnOffBirthsOrDeaths(sim, birthsOrDeaths.births);
		ArrayList <Integer> originalAges = new ArrayList <Integer> ();
		for (Person p: sim.agents) {
			originalAges.add(p.getAge());
		}
		// run for a year so that every one has had a birthday
		int numDays = 364;
				
		HelperFunctions.runSimulation(sim, numDays);
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
	
	public int getAliveCountsBySex(WorldBankCovid19Sim world, SEX sex, boolean alive) {
		Map<SEX, Map<Boolean, Long>> sex_alive_map = world.agents.stream().collect(
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