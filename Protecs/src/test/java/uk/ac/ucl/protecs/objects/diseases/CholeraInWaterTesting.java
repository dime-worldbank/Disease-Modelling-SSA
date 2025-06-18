package uk.ac.ucl.protecs.objects.diseases;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;

public class CholeraInWaterTesting {
	// ============================================== Cholera in water testing suit ==============================================================================
	
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
	
	@Test
	public void choleraCanBeSeededIntoWaterSources() {
		// Test that cholera infections are created and loaded in via the line list
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		// assume no cases have been loaded in the the person objects
		boolean choleraSeededInWater = false;
		// iterate over the population to try and find a cholera infection via their disease set
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) {
				// if we found a cholera case, alter our assumption that none have been loaded in and stop the search
				choleraSeededInWater = true;
				break;
			}
		}
		// test whether infections have been loaded in
		Assert.assertTrue(choleraSeededInWater);
	}
	
	@Test
	public void checkCholeraIsSpreadToWater() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		int number_of_initial_infections_in_water = 0;

		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) number_of_initial_infections_in_water ++;

		}
		int numDays = 50;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections_in_water = 0;

		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) number_of_new_infections_in_water ++;

		}
		Assert.assertTrue(number_of_new_infections_in_water > number_of_initial_infections_in_water);
		}
	
	@Test
	public void checkCholeraIsPickedUpFromWater() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		int number_of_initial_infections_in_humans = 0;

		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) number_of_initial_infections_in_humans ++;

		}
		int numDays = 50;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_new_infections_in_humans = 0;

		for (Person p: sim.agents) {
			if (p.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) number_of_new_infections_in_humans ++;

		}
		Assert.assertTrue(number_of_new_infections_in_humans > number_of_initial_infections_in_humans);
		}
	
}