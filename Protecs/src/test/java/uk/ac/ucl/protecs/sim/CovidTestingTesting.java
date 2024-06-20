package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CovidTestingTesting {
	
	@Test
	public void CheckTestsOnlyHappenForThoseWithSymptomsOfCovid() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/covid_testing_params.txt", false, true);
		sim.start();
		int numDays = 1;
		sim.params.infection_beta = 0;
		helperFunctions.SetFractionInfectionsWithCertainNode(0.5, sim, sim.infectiousFramework.setNodeForTesting("mild"));
		helperFunctions.StopRecoveryHappening(sim);
		helperFunctions.runSimulation(sim, numDays);
		List<Person> hasBeenTested = peopleWhoHaveBeenTested(sim);
		int numWithSpurious = 0;
		int numWithSymptomaticCovid = 0;
		for (Person p: hasBeenTested) {
			if (p.hasCovidSpuriousSymptoms()) {numWithSpurious++;}
			if (p.hasSymptomaticCovid()) {numWithSymptomaticCovid++;}
		}
		System.out.println("num tested: " + hasBeenTested.size());
		System.out.println("num spurious: " + numWithSpurious);
		System.out.println("num symptomatic: " + numWithSymptomaticCovid);
		Assert.assertTrue(hasBeenTested.size() == numWithSpurious + numWithSymptomaticCovid);
		
	}
	

//	@Test
//	public void CheckThereAreNoDifferencesInCaseNumbers() {
//		//Arrange
//		WorldBankCovid19Sim sim_without_testing = helperFunctions.CreateDummySimWithChosenSeed(12, "src/main/resources/covid_testing_params.txt", false, false);
//		sim_without_testing.start();
//		int numDays = 50;
//		while(sim_without_testing.schedule.getTime() < Params.ticks_per_day * numDays && !sim_without_testing.schedule.scheduleComplete()){
//			sim_without_testing.schedule.step(sim_without_testing);
//		}
//		WorldBankCovid19Sim sim_with_testing = helperFunctions.CreateDummySimWithChosenSeed(12, "src/main/resources/covid_testing_params.txt", false, true);
//
//		sim_with_testing.start();
//		while(sim_with_testing.schedule.getTime() < Params.ticks_per_day * numDays && !sim_with_testing.schedule.scheduleComplete()){
//			sim_with_testing.schedule.step(sim_with_testing);
//		}
//		int numWithoutTesting = sim_without_testing.infections.size();
//		System.out.println(sim_without_testing.random.nextDouble());
//		int numWithTesting = sim_with_testing.infections.size();
//		System.out.println(sim_with_testing.random.nextDouble());
//		System.out.println("number without = " + String.valueOf(numWithoutTesting));
//		System.out.println("number with = " + String.valueOf(numWithTesting));
//		Assert.assertTrue(numWithoutTesting == numWithTesting);
//
//	}
	public List<Person> peopleWhoHaveBeenTested(WorldBankCovid19Sim world){
		Map<Boolean, List<Person>> propertiesChecked = (Map<Boolean,List<Person>>) world.agents.stream().collect(
	            Collectors.groupingBy(
	              Person::hasBeenTestedForCovid,
	                    Collectors.toList()
		            )
	               );
		return propertiesChecked.get(true);
	}
}
