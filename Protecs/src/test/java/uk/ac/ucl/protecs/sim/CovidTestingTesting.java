package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import java.util.Random;


public class CovidTestingTesting {
	
	// ---------------- tests that spurious symptoms are eventually turned off by the model ------------------------------------------
	@Test
	public void MakeSureSpuriousSymptomsTurnOff() {
		
		Random random = new Random();
		// generate random seed from 0 to 1,000,000
		int seed = random.nextInt(1000000);
		//Arrange
		WorldBankCovid19Sim sim = CreateDummySim(seed, "src/test/resources/covid_testing_params.txt", false, true);
		sim.start();
		// make sure no one develops spurious symptoms again
		sim.params.rate_of_spurious_symptoms = 0.0;
		// Give everyone spurious symptoms and a date to remove them
		for (Person p: sim.agents) {
			p.setSpuriousSymptoms();
			p.setSymptomRemovalDate(7);
		}
		// check that everyone has been given spurious symptoms
		for (Person p: sim.agents) {
			Assert.assertTrue(p.hasSpuriousSymptoms());
		}
		int numDays = 9; 

		while(sim.schedule.getTime() < Params.ticks_per_day * numDays && !sim.schedule.scheduleComplete()){
			sim.schedule.step(sim);
		}
		// now check that everyone who was given spurious symptoms has had them removed over the course of the simulation
		for (Person p: sim.agents) {
			Assert.assertFalse(p.hasSpuriousSymptoms());
		}
	}
	// ---------------- test that output from the model isn't produced when we turn testing off --------------------------------------
	@Test
	public void MakeSureThereIsNoTestingOutputIfTurnedOff() {
		Random random = new Random();
		// generate random seed from 0 to 1,000,000
		int seed_1 = random.nextInt(1000000);
		int seed_2 = random.nextInt(1000000);
		// create a no-testing output sim
		WorldBankCovid19Sim no_output_sim = CreateDummySim(seed_1, "src/test/resources/covid_testing_params.txt", false, false);
		// start it
		no_output_sim.start();
		// make sure there will be no output file 
		Assert.assertTrue(no_output_sim.detectedCovidFilename.length() == 0);
		// create a simulation with an output file saved
		WorldBankCovid19Sim with_output_sim = CreateDummySim(seed_2, "src/test/resources/covid_testing_params.txt", false, true);
		// start it
		with_output_sim.start();
		// make sure there will be an output file
		Assert.assertTrue(with_output_sim.detectedCovidFilename.length() != 0);
		
	}
	// ---------------- test that the covid cases predicted by the model with and without testing aren't different -------------------
	@Test
	public void MakeSurethereArenoDifferenceInCovidCases() {
		// create a no-testing output sim
		WorldBankCovid19Sim no_testing_sim = CreateDummySim(1, "src/test/resources/covid_testing_params.txt", false, false);
		// start it
		no_testing_sim.start();
		// run it for 80 days
		int numDays = 80; 

		while(no_testing_sim.schedule.getTime() < Params.ticks_per_day * numDays && !no_testing_sim.schedule.scheduleComplete()){
			no_testing_sim.schedule.step(no_testing_sim);
		}
		// make sure there will be no output file 
		// create a simulation with an output file saved
		WorldBankCovid19Sim with_testing_sim = CreateDummySim(1, "src/test/resources/covid_testing_params.txt", false, true);
		// start it
		with_testing_sim.start();
		// run it for 80 days
		while(with_testing_sim.schedule.getTime() < Params.ticks_per_day * numDays && !with_testing_sim.schedule.scheduleComplete()){
			with_testing_sim.schedule.step(with_testing_sim);
		}		
		int noTestingCovidCounts = 0; 
		int noTestingAsymptCounts = 0;
		int noTestingMildCounts = 0;
		int noTestingSevereCounts = 0;
		int noTestingCriticalCounts = 0;
		int noTestingRecoveredCounts = 0;
		int noTestingAliveCounts = 0;
		
		for (Person p: no_testing_sim.agents) {
			if (p.hasCovid()) {noTestingCovidCounts ++;}
			if (p.hasAsymptCovid()) {noTestingAsymptCounts ++;}
			if (p.hasMild()) {noTestingMildCounts ++;}
			if (p.hasSevere()) {noTestingSevereCounts ++;}
			if (p.hasCritical()) {noTestingCriticalCounts ++;}		
			if (p.isAlive()) {noTestingAliveCounts ++;}
		}
		int withTestingCovidCounts = 0; 
		int withTestingAsymptCounts = 0;
		int withTestingMildCounts = 0;
		int withTestingSevereCounts = 0;
		int withTestingCriticalCounts = 0;
		int withTestingRecoveredCounts = 0;
		int withTestingAliveCounts = 0;
		
		for (Person p: with_testing_sim.agents) {
			if (p.hasCovid()) {withTestingCovidCounts ++;}
			if (p.hasAsymptCovid()) {withTestingAsymptCounts ++;}
			if (p.hasMild()) {withTestingMildCounts ++;}
			if (p.hasSevere()) {withTestingSevereCounts ++;}
			if (p.hasCritical()) {withTestingCriticalCounts ++;}		
			if (p.isAlive()) {withTestingAliveCounts ++;}
		}
		int[] noTestingResultsArray = {noTestingCovidCounts, noTestingAsymptCounts, noTestingMildCounts, noTestingSevereCounts, 
				noTestingCriticalCounts, noTestingRecoveredCounts, noTestingAliveCounts}; 
		int[] withTestingResultsArray = {withTestingCovidCounts, withTestingAsymptCounts, withTestingMildCounts, withTestingSevereCounts, 
				withTestingCriticalCounts, withTestingRecoveredCounts, withTestingAliveCounts}; 
		Assert.assertArrayEquals(noTestingResultsArray, withTestingResultsArray);
	}
		
	private WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography, boolean testing) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography, testing);
		return myWorld;
	}
	
}