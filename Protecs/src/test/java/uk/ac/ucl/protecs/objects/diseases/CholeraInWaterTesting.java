package uk.ac.ucl.protecs.objects.diseases;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim.DISEASE;
import uk.ac.ucl.protecs.behaviours.diseaseProgression.CholeraDiseaseProgressionFramework.CholeraBehaviourNodeInWater;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Water;
import uk.ac.ucl.protecs.objects.locations.CommunityLocation;
import uk.ac.ucl.protecs.objects.locations.Household;
import uk.ac.ucl.protecs.objects.locations.Location.LocationCategory;

public class CholeraInWaterTesting {
	// ============================================== Cholera in water testing suit ==============================================================================
	// This suite of tests is designed to check that how water is initiated, and interacted with is working as intended. Currently we test:
	// 1) Household water supplies are linked to a community based water source
	// 2) We can seed cholera into water sources
	// 3) All initial sources of cholera are instantiated in the hyperinfectious state (NOTE THIS MAY CHANGE DEPENDING ON FUTURE WORK)
	// 4) Cholera can be spread from people to water
	// 5) Cholera can be spread from water to people
	// 6) Cholera in water reverts to an active but not culturable state in the short term
	// 7) Cholera in water will eventually subsisde without reinfection.
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
		// assume no cases have been loaded in the the water objects
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
	public void checkThatCholeraInWaterStartsAtTheHyperinfectiousNode() {
		// Test that cholera infections are created and loaded in via the line list
		// create a simulation and start
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		// assume cholera in water does not exhibit the contagious node
		boolean choleraInWaterIsContaminated = true;
		// iterate over the diseases in water to try and find a cholera infection that isn't doing the contaminated behaviour
		for (Disease d: sim.other_infections) {
			if (!((d.isOfType(DISEASE.CHOLERA)) & (d.getCurrentBehaviourNode().getTitle().equals(CholeraBehaviourNodeInWater.HYPERINFECTIOUS.key)))) {
				choleraInWaterIsContaminated = false;
				break;
			}
		}
		// test whether infections have been loaded in
		Assert.assertTrue(choleraInWaterIsContaminated);
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
	
	@Test
	public void checkContagiousWaterRevertsToActiveButNonCulturableInTheShortTerm() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		// get initial set of water
		ArrayList<Water> originalContaminatedWater = new ArrayList<Water>();
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) originalContaminatedWater.add(w);

		}
		// make sure there are no new contamination events from shedding
		sim.params.cholera_prob_shed = 0;
		// run for two ticks
		int numTicks = 2;
		HelperFunctions.runSimulationForTicks(sim, numTicks);
		// check that all of the initial set of water infections are ABNC
		boolean all_abnc = true;
		for (Water w: originalContaminatedWater) {
			if (!w.getDiseaseSet().get(DISEASE.CHOLERA.key).getCurrentBehaviourNode().getTitle().equals(CholeraBehaviourNodeInWater.ABNC.key)) {
				// if we find one that isn't active but non culturable break the loop
				all_abnc = false;
				break;
			}
		}
		Assert.assertTrue(all_abnc);
		}
	
	@Test
	public void checkContagiousWaterRevertsToCleanInTheLongTerm() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_in_water.txt");
		sim.start();
		// get initial set of water
		ArrayList<Water> originalContaminatedWater = new ArrayList<Water>();
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().containsKey(DISEASE.CHOLERA.key)) originalContaminatedWater.add(w);

		}
		// make sure there are no new contamination events from shedding
		sim.params.cholera_prob_shed = 0;
		// run for 30 days
		int numDays = 30;
		HelperFunctions.runSimulation(sim, numDays);
		// check that all of the initial set of water infections are ABNC
		boolean all_clean = true;
		for (Water w: originalContaminatedWater) {
			System.out.println(w.getDiseaseSet().get(DISEASE.CHOLERA.key).getCurrentBehaviourNode().getTitle());
			if (!w.getDiseaseSet().get(DISEASE.CHOLERA.key).getCurrentBehaviourNode().getTitle().equals(CholeraBehaviourNodeInWater.CLEAN.key)) {					
				// if we find one that isn't clean break the loop
				all_clean = false;
				break;
			}
		}
		Assert.assertTrue(all_clean);
	}
	
	@Test
	public void seedingInCommunityLocationsLeadsToSpreadToOtherLocations() {
		// create a simulation without any cases being seeded in
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_cholera_force_no_cases.txt");
		sim.start();
		// get a community location and create an infection
		for (CommunityLocation l: sim.communityLocations) {
			if (l.isWaterSource()) {
			Cholera inf = new Cholera(l.getWater(), null, sim.choleraFramework.getStandardEntryPointForWater(), sim, 0);
			sim.schedule.scheduleOnce(0, sim.param_schedule_infecting, inf);
			break;
			}
		}
		// increase the likelihood of interacting with water, ingesting cholera and shedding cholera
		sim.params.cholera_prob_ingest = 0.8;
		sim.params.cholera_prob_shed = 0.8;
		sim.params.dummy_prob_interact_with_water = 0.8;
		// run for 30 days
		int numDays = 30;
		HelperFunctions.runSimulation(sim, numDays);
		int number_of_contaminated_watersources = 0;
		for (Water w: sim.waterInSim) {
			if (w.getDiseaseSet().size() > 0) number_of_contaminated_watersources++;
		}
		boolean other_water_sources_contaminated = number_of_contaminated_watersources > 1;

		Assert.assertTrue(other_water_sources_contaminated);
	}
	
}