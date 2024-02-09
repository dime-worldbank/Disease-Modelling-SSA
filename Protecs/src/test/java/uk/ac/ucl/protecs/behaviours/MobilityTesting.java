package uk.ac.ucl.protecs.behaviours;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

public class MobilityTesting {

	@Test
	public void OfficeWorkerBehaviours() {
		//Arrange
		WorldBankCovid19Sim sim = CreateDummySim(12345, "src/main/resources/params.txt", false, false);
		sim.start();
		sim.schedule.step(sim);
		
		Person sut = sim.agents.get(0);
		
		//Act
		sut.step(sim);
		
		//Assert
		Assert.assertFalse(sut.atWorkNow()); // it is morning - they should not be at work
	}

	private WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography, boolean covidTesting) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography, covidTesting);
		return myWorld;
	}
	
}
