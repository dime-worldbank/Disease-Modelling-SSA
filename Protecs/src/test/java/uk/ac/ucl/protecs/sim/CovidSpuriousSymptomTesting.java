package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

public class CovidSpuriousSymptomTesting{
	
	@Test
	public void CheckPeopleWithSymptomaticCovidDoNotGetSpuriousSymptoms() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 1;
		// Change the rate of setting Covid spurious symptoms so everyone will who is eligible will develop symptoms
		sim.params.rate_of_spurious_symptoms = 1.0;
		// Give half the population mild Covid to make test that there are no people with symptomatic Covid and Covid spurious symptoms
		helperFunctions.SetFractionInfectionsWithCertainNode(0.5, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// we need people with symptoms to be alive and not have any Covid infections of any severity. Use streams to search over the population to 
		// find get the number of people with spurious symptoms who match this criteria
		List<Person> peopleWithPropertiesAssignedWhenSpuriousSymptomsAreAssigned = getPopulationWithSpuriousSymptomAssignmentCriteria(sim);
		List<Person> peopleWithSpuriousSymptoms = getPopulationWithSpuriousSymptoms(sim);
		Assert.assertTrue(peopleWithPropertiesAssignedWhenSpuriousSymptomsAreAssigned.size() == peopleWithSpuriousSymptoms.size());
	}
	@Test
	public void CheckPeopleCanHaveAsymptomaticCovidAndSpuriousSymptoms() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 1;
		// Change the rate of setting Covid spurious symptoms so everyone will who is eligible will develop symptoms
		sim.params.rate_of_spurious_symptoms = 1.0;
		// Give everyone asymptomatic Covid
		helperFunctions.SetFractionInfectionsWithCertainNode(1, sim, sim.infectiousFramework.setNodeForTesting("asymptomatic"));
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// we need people with symptoms to be alive and not have any Covid infections of any severity. Use streams to search over the population to 
		// find get the number of people with spurious symptoms who match this criteria
		List<Person> peopleWithSpuriousSymptomsAndAsympt = getPopulationWithSpuriousSymptomsAndAsymptomaticCovid(sim);
		Assert.assertTrue(peopleWithSpuriousSymptomsAndAsympt.size() > 0);
	}
	
	@Test
	public void CheckSettingSymptomsAreBeingRemoved() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 3;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;
		// Stop new Covid infections from developing
		sim.params.infection_beta = 0.0;
		Random rand = new Random();
		// remove and existing infections from the population and assign half the population spurious symptoms
		for (Person p: sim.agents) {
			if (p.hadCovid()) {
				p.die("");
				}
			else if (rand.nextDouble() < 0.5) {
				p.setCovidSpuriousSymptoms();
				p.setCovidSpuriousSymptomRemovalDate(1);
			}
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// Check that there are no spurious symptoms remaining in the population
		List<Person> peopleWithSpuriousSymptoms = getPopulationWithSpuriousSymptoms(sim);
		Assert.assertTrue(peopleWithSpuriousSymptoms == null);	
	}
	
	@Test
	public void CheckPropertiesAreBeingSet() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 1;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 100.0;		
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		// run the simulation
		for (Person p: sim.agents) { if (p.hadCovid()) {p.die("");}}
		helperFunctions.runSimulation(sim, numDays);
		int sizeThatShouldHaveBeenGivenSymptoms = helperFunctions.GetNumberAlive(sim);
		// Check that there are no spurious symptoms remaining in the population
		List<Person> peopleWithPropertiesAssigned = peopleWithPropertiesAssigned(sim);		
		// Make sure that a day to remove symptoms has been set
		boolean hadAdateSetForRemoval = true;
		for (Person p: sim.agents) {
			if (!p.hasCovid()) {
				if (p.getCovidSpuriousSymptomRemovalDate() == Integer.MAX_VALUE) {
					hadAdateSetForRemoval = false;
				}
			}
		}
		Assert.assertTrue((peopleWithPropertiesAssigned.size() == sizeThatShouldHaveBeenGivenSymptoms) & hadAdateSetForRemoval);	
	}
	
	@Test
	public void CheckPropertiesAreBeingRemoved() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 8;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;		
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		for (Person p: sim.agents) {
			p.setCovidSpuriousSymptoms();
			p.setCovidSpuriousSymptomRemovalDate(7);
			
		}
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		List<Person> peopleWithoutPropertiesAssigned = peopleWithoutPropertiesAssigned(sim);
		boolean hadAdateSetForRemoval = false;
		for (Person p: sim.agents) {
			if (!p.hasCovid()) {
				if (p.getCovidSpuriousSymptomRemovalDate() < Integer.MAX_VALUE) {
					hadAdateSetForRemoval = true;
				}
			}
		}
		Assert.assertTrue((peopleWithoutPropertiesAssigned.size() == sim.agents.size()) & !hadAdateSetForRemoval);	


	}
	
	public List<Person> getPopulationWithSpuriousSymptomAssignmentCriteria(WorldBankCovid19Sim world){
	
	Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>> meetsSymptomAssignmentCriteria = (Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, Map<Boolean, List<Person>>>>>>) world.agents.stream().collect(
            Collectors.groupingBy(
              Person::isAlive, 
              Collectors.groupingBy(
                Person::hasMild, 
                Collectors.groupingBy(
                  Person::hasSevere, 
                  Collectors.groupingBy(
                    Person::hasCritical, 
                    Collectors.groupingBy(
                            Person::hasCovidSpuriousSymptoms, 
                    Collectors.toList()
                    )
                  )
                )
              )
            )
            );
	
	return meetsSymptomAssignmentCriteria.get(true).get(false).get(false).get(false).get(true);
	}
	public List<Person> getPopulationWithSpuriousSymptoms(WorldBankCovid19Sim world){
		
		Map<Boolean, List<Person>> hasSpuriousSymptoms = (Map<Boolean, List<Person>>) world.agents.stream().collect(
	            Collectors.groupingBy(
	              Person::hasCovidSpuriousSymptoms, 
	                    Collectors.toList()
	                    )
	                  );
		
		return hasSpuriousSymptoms.get(true);
		}
	public List<Person> getPopulationWithSpuriousSymptomsAndAsymptomaticCovid(WorldBankCovid19Sim world){
		
		Map<Boolean, Map<Boolean, List<Person>>> hasSpuriousSymptomsAndAsympt = (Map<Boolean, Map<Boolean, List<Person>>>) world.agents.stream().collect(
	            Collectors.groupingBy(
	              Person::hasCovidSpuriousSymptoms, 
		            Collectors.groupingBy(
		            	Person::hasAsymptCovid,
	                    Collectors.toList()
	                    )
		            )
	               );
		
		return hasSpuriousSymptomsAndAsympt.get(true).get(true);
		}
	public List<Person> peopleWithPropertiesAssigned(WorldBankCovid19Sim world){
		
		Map<Boolean, Map<Boolean, List<Person>>> propertiesChecked = (Map<Boolean, Map<Boolean, List<Person>>>) world.agents.stream().collect(
	            Collectors.groupingBy(
	              Person::hasCovidSpuriousSymptoms, 
		            Collectors.groupingBy(
		            	Person::isEligibleForCovidTesting,
	                    Collectors.toList()
	                    )
		            )
	               );
		
		return propertiesChecked.get(true).get(true);
		}
	public List<Person> peopleWithoutPropertiesAssigned(WorldBankCovid19Sim world){
		
		Map<Boolean, Map<Boolean, List<Person>>> propertiesChecked = (Map<Boolean, Map<Boolean, List<Person>>>) world.agents.stream().collect(
	            Collectors.groupingBy(
	              Person::hasCovidSpuriousSymptoms, 
		            Collectors.groupingBy(
		            	Person::isEligibleForCovidTesting,
	                    Collectors.toList()
	                    )
		            )
	               );
		
		return propertiesChecked.get(false).get(false);
		}
}