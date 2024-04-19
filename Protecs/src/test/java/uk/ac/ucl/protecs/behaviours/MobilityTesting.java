package uk.ac.ucl.protecs.behaviours;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.*;

public class MobilityTesting {

	@Test
	public void OfficeWorkerBehaviours() {
		//Arrange
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/test/resources/params.txt", false, false);
		sim.start();
		sim.schedule.step(sim);
		
		Person sut = sim.agents.get(0);
		
		//Act
		sut.step(sim);
		
		//Assert
		Assert.assertFalse(sut.atWorkNow()); // it is morning - they should not be at work
	}
	
}
