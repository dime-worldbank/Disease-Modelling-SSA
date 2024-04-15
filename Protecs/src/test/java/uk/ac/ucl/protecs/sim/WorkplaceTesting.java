package uk.ac.ucl.protecs.sim;

import uk.ac.ucl.protecs.helperFunctions.*;

import org.junit.Assert;
import org.junit.Test;

public class WorkplaceTesting{
	
	@Test
	public void checkPopulationWorkplacesAreBeingLoaded() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt", false, false);
		sim.start();
		Assert.assertTrue(sim.workplaces.size() > 0);

	}
}