package test.java.behaviours;

import org.junit.Assert;
import org.junit.Test;

import main.java.objects.Person;
import main.java.sim.Params;
import main.java.sim.WorldBankCovid19Sim;

public class MobilityTesting {

	@Test
	public void OfficeWorkerBehaviours() {
		//Arrange
		WorldBankCovid19Sim sim = CreateDummySim(12345, "src/main/resources/params.txt", false);
		sim.start();
		sim.schedule.step(sim);
		
		Person sut = sim.agents.get(0);
		
		//Act
		sut.step(sim);
		
		//Assert
		Assert.assertFalse(sut.atWorkNow()); // it is morning - they should not be at work
	}

	private WorldBankCovid19Sim CreateDummySim(long seed, String paramsFilename, boolean demography) {
		Params p = new Params(paramsFilename, false);
		WorldBankCovid19Sim myWorld = new WorldBankCovid19Sim(seed, p, "", demography);
		return myWorld;
	}
}