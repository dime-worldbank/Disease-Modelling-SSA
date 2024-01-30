package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;


public class CovidTestingTesting {

	@Test
	public void MakeSureSpuriousSymptomsTurnOff() {
		//Arrange
		WorldBankCovid19Sim sim = CreateDummySim(12345, "src/test/resources/covid_testing_params.txt", false, true);
		sim.start();
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
	@Test
	public void MakeSureThereIsNoTestingOutputIfTurnedOff() {
		// create a no-testing output sim
		WorldBankCovid19Sim no_output_sim = CreateDummySim(12345, "src/test/resources/covid_testing_params.txt", false, false);
		// start it
		no_output_sim.start();
		// make sure there will be no output file 
		Assert.assertTrue(no_output_sim.detectedCovidFilename.length() == 0);
		// create a simulation with an output file saved
		WorldBankCovid19Sim with_output_sim = CreateDummySim(12345, "src/test/resources/covid_testing_params.txt", false, true);
		// start it
		with_output_sim.start();
		// make sure there will be an output file
		Assert.assertTrue(with_output_sim.detectedCovidFilename.length() != 0);
		
	}

	private WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography, boolean testing) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography, testing);
		return myWorld;
	}
	
}