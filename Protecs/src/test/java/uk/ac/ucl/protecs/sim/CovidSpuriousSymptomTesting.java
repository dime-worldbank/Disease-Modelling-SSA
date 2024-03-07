package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.diseases.CoronavirusSpuriousSymptom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CovidSpuriousSymptomTesting{
	
	@Test
	public void CheckPeopleWithSymptomaticCovidDoNotGetSpuriousSymptoms() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 8;
		// Give the population mild Covid and spurious symptoms to see if those with mild covid have their spurious symptoms resolved 
		helperFunctions.SetFractionInfectionsWithCertainNode(1.0, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		// make sure no one recovers or progresses from their mild covid
		helperFunctions.StopRecoveryHappening(sim);
		helperFunctions.HaltDiseaseProgressionAtStage(sim, "Mild");
		// make sure there are no new Covid infections
		sim.params.infection_beta = 0.0;
		// make sure that after the initial bout of symptoms, no one develops new spurious symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;
		// create a bunch of spurious symptoms for each person
		giveAFractionASpuriousSymptom(1, sim);
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		// In this scenario we would expect that no one has spurious symptoms
		int numberOfPeopleWithSpuriousSymptoms = 0; 
		
		try {
			numberOfPeopleWithSpuriousSymptoms = peopleWithPropertiesAssigned(sim).size();
			}
		catch (Exception e) {}
		System.out.println(numberOfPeopleWithSpuriousSymptoms);
		Assert.assertTrue(numberOfPeopleWithSpuriousSymptoms == 0);
	}
	
	@Test
	public void CheckPeopleCanHaveAsymptomaticCovidAndSpuriousSymptoms() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 1;
		giveAFractionASpuriousSymptom(1, sim);
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
		int numDays = 8;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.0;
		// Stop new Covid infections from developing
		sim.params.infection_beta = 0.0;
		// remove and existing infections from the population and assign half the population spurious symptoms
		for (Person p: sim.agents) {
			if (p.hadCovid()) {
				p.die("");
				}
		}
		giveAFractionASpuriousSymptom(0.5, sim);
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
		int numDays = 7;	
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		// remove all people with covid
		for (Person p: sim.agents) { if (p.hadCovid()) {p.die("");}}
		// create spurious symptoms
		giveAFractionASpuriousSymptom(1, sim);
		helperFunctions.runSimulation(sim, numDays);
		int sizeThatShouldHaveBeenGivenSymptoms = helperFunctions.GetNumberAlive(sim);
		// Check that there are no spurious symptoms remaining in the population
		List<Person> peopleWithPropertiesAssigned = peopleWithPropertiesAssigned(sim);		
		Assert.assertTrue(peopleWithPropertiesAssigned.size() == sizeThatShouldHaveBeenGivenSymptoms);	
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
		giveAFractionASpuriousSymptom(1, sim);
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		List<Person> peopleWithoutPropertiesAssigned = peopleWithoutPropertiesAssigned(sim);
		System.out.println(peopleWithoutPropertiesAssigned.size());
		Assert.assertTrue((peopleWithoutPropertiesAssigned.size() == sim.agents.size()));
	}
	@Test
	public void CheckSpuriousObjectsAreCreated() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySimWithRandomSeed("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 8;
		// Change the rate of setting Covid spurious symptoms so we have control the number of people who get given symptoms
		sim.params.rate_of_spurious_symptoms = 0.5;		
		// Remove the development of new symptoms
		sim.params.infection_beta = 0.0;
		// run the simulation
		helperFunctions.runSimulation(sim, numDays);
		List<Person> peopleWithPropertiesAssigned = peopleWithPropertiesAssigned(sim);
		Assert.assertTrue((peopleWithPropertiesAssigned.size() > 0) & (peopleWithPropertiesAssigned.size() < sim.agents.size()));	


	}
	public void giveAFractionASpuriousSymptom(double fraction, WorldBankCovid19Sim sim) {
		for (Person p: sim.agents) {
			if (sim.random.nextDouble() <= fraction) {
			CoronavirusSpuriousSymptom CovSpuriousSymptoms = new CoronavirusSpuriousSymptom(p, sim, sim.spuriousFramework.getStandardEntryPoint(), 0);
			p.setHasSpuriousObject();
			sim.CovidSpuriousSymptomsList.add(CovSpuriousSymptoms);
			sim.schedule.scheduleOnce(1, sim.param_schedule_infecting, CovSpuriousSymptoms);
		}
		}
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