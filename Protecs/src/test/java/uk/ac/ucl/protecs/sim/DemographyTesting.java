package uk.ac.ucl.protecs.sim;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;

import uk.ac.ucl.protecs.helperFunctions.*;

public class DemographyTesting {
	@Test
	public void testBirths() {
		// turn off deaths to only focus on births.
		
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(12345, "src/main/resources/params.txt", true);
		
		
	}
	
}