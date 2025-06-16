package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;

public class CholeraInWaterTesting {
	// ============================================== Cholera in humans testing suit ==============================================================================
	// Here we aim to test the instantiation of Cholera in the Person object and the disease progression behaviour following subsequent infection.
	// We test that: 
	// 1) cholera cases cases are loaded in from the line list
	// 2) the entry point is always the 'exposed' behaviour node
	// 3) the exposed node will lead to susceptible, asymptomatic, mild and severe cholera only
	// 4) asymptomatic cholera will lead to recovery only
	// 5) mild cholera will lead to recovery only
	// 6) severe cholera will lead to critical, death or recovery only
	// 7) critical cholera will lead to death or recovery only
	// 8) death leads only to death
	// 9) recovery leads to susceptibility only
	// ============================================================================================================================================================
	private final static String paramsDir = "src/test/resources/";
	
	@Test
	public void householdsAreLinkedToCommunityWaterSources() {
		// Test that cholera infections are created and loaded in via the line list
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		// assume no cases have been loaded in the the person objects
		boolean housesLinkedToWatersource = true;
		// iterate over the population to try and find a cholera infection via their disease set
		for (Household h: sim.households) {
			if (!h.getWater().getSource().getLocationType().equals(LocationCategory.COMMUNITY)) {
				// if we found a cholera case, alter our assumption that none have been loaded in and stop the search
				housesLinkedToWatersource = false;
				break;
			}
		}
		// test whether infections have been loaded in
		Assert.assertTrue(housesLinkedToWatersource);
	}
	
	
}