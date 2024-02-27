package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import java.util.Map;
import java.util.stream.Collectors;

public class CovidTestingTesting {

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
	
}
