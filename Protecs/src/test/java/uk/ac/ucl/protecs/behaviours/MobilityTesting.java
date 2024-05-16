package uk.ac.ucl.protecs.behaviours;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.helperFunctions.NodeOption;


public class MobilityTesting {
	// ==================================== Testing ======================================================================
	// === These tests are designed to ensure that the transition between different locations are working as intended. ===
	// ===================================================================================================================
		
	// TESTS FOR PERFECT MIXING
	@Test
	public void PeopleInTheCommunityAlwaysGoHomeAfterwards() {
		// TODO
		Assert.assertTrue(true);
	}
	
	@Test
	public void OfficeWorkerBehaviours() {
		//Arrange
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/params.txt", false);
		sim.start();
		sim.schedule.step(sim);
		
		Person sut = sim.agents.get(0);
		
		//Act
		sut.step(sim);
		
		//Assert
		Assert.assertFalse(sut.atWorkNow()); // it is morning - they should not be at work
	}
	
	
	@Test
	public void MakeSureThatPeopleGoToOnlyTheCommunityAndHomeLocationsWithPerfectMixing() {
		//Arrange
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/params.txt", false);
		sim.start();
		// ensure that perfect mixing is turned on
		sim.params.setting_perfectMixing = true;
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = helperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.MovementBehaviour, 0.0);
		// we would expect only the home and community node to appear in the simulation
		List<String> expectedNodes = Arrays.asList("Home", "In community");
		// Make sure than no other movement behaviour nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun) && uniqueNodesInRun.containsAll(expectedNodes));
	}
	
	@Test
	public void LockdownReducesTheNumberOfVisitsToTheCommunity() {
		// TODO This is something that will probably have to be developed. Lockdown doesn't seem to do anything to prevent people going into the 
		// community at the moment
		Assert.assertTrue(true);
	}
	
	@Test
	public void LockdownReducesTheNumberOfVisitsToOtherAdminZones() {
		WorldBankCovid19Sim sim_no_lockdown = helperFunctions.CreateDummySim("src/main/resources/params.txt", false);
		sim_no_lockdown.start();
		
		int noLockdownOutboundTripCounts = outboundTripCountInSim(sim_no_lockdown, 100);
		
		WorldBankCovid19Sim sim_with_lockdown = helperFunctions.CreateDummySim("src/main/resources/params_with_lockdown.txt", false);
		sim_with_lockdown.start();

		int lockdownOutboundTripCounts = outboundTripCountInSim(sim_with_lockdown, 100);
		Assert.assertTrue(noLockdownOutboundTripCounts > lockdownOutboundTripCounts);
	}
	
	// TESTS FOR IMPERFECT MIXING
	
	// TODO: Develop mobility tests for going to work when that is in the model
	@Test
	public void PeopleAtWorkGoToTheCommunityOrHomeAfterwards() {
		// TODO
		Assert.assertTrue(true);
	}
	
	public int outboundTripCountInSim(WorldBankCovid19Sim world, int numDaysToRun) {
		// Simulate over the time period and get the disease stages present in the simulation
		int number_of_visits_to_other_admin_zones = 0;
		while(world.schedule.getTime() < Params.ticks_per_day * numDaysToRun && !world.schedule.scheduleComplete()){
			world.schedule.step(world);
			if (world.schedule.getTime() % world.params.ticks_per_day == 2.0) {
				for (Person p: world.agents) {
					if (p.visitingNow()) {
						number_of_visits_to_other_admin_zones++;
					}				
				}
			}
		}
		
		return number_of_visits_to_other_admin_zones;
	}
	
}
