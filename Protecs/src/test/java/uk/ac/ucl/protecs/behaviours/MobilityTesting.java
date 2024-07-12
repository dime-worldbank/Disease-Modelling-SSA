package uk.ac.ucl.protecs.behaviours;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.sim.Params;
import uk.ac.ucl.protecs.sim.WorldBankCovid19Sim;
import uk.ac.ucl.protecs.behaviours.MovementBehaviourFramework.mobilityNodeTitle;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.helperFunctions.helperFunctions.NodeOption;
import uk.ac.ucl.protecs.objects.Location.LocationCategory;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class MobilityTesting {
	// ==================================== Testing ==========================================================================
	// === These tests are designed to ensure that the transition between different locations are working as intended. =======
	// === These tests will be split into perfect and imperfect mixing parts, as each form of the model will have different ==
	// === expected behaviours. The perfect mixing tests show that people exhibit the correct movement behaviour and end up ==
	// === at the correct locations, specifically, people go from the community behaviour node to the home behaviour node at =
	// === the end of the day. People go from the community location to the home location at the end of the day. =============
	// === People go switch from the home behaviour node to the community behaviour node at the start of the day. ============
	// === People go from the home location to their community location at the start of the day. =============================
	// === We also test that triggering lockdowns reduces the number of outbound trips that take place in the simulatio. =====
	// =======================================================================================================================
		

	// TESTS FOR PERFECT MIXING
	
	private final String params;
	
	public MobilityTesting(String fileName) {
		this.params = fileName;
	}

	@Test
	public void PeopleDoingTheCommunityNodeBehaviourSwitchToTheHomeNodeBehviourAtTheEndOfDay() {
		// set up the simulation
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt");
		sim.start();
		// make everyone go to the community
		helperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.movementFramework.setMobilityNodeForTesting(mobilityNodeTitle.COMMUNITY), 
				NodeOption.MovementBehaviour);
		
		// people will go home once the day has ended, therefore we need to run this until the end of the time they will be out in the community.
		// There are 4 hours per tick, meaning 6 ticks per day. We check they are home after the 5th tick of the simulation.
		List<String> uniqueNodesInRun = helperFunctions.getFinalBehaviourNodesInSim(sim, 5.01 / sim.params.ticks_per_day, NodeOption.MovementBehaviour);
		// only expect people to be at home
		List<String> expectedNodes = Arrays.asList(mobilityNodeTitle.HOME.key);

		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun) && uniqueNodesInRun.containsAll(expectedNodes));
	}
	
	@Test
	public void PeopleWithinTheCommunityLocationGoBackToHomeLocationAtTheEndOfDay() {
		// set up the simulation
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt");
		sim.start();
		// make everyone go to the community
		helperFunctions.SetFractionObjectsWithCertainBehaviourNode(1.0, sim, sim.movementFramework.setMobilityNodeForTesting(mobilityNodeTitle.COMMUNITY), 
				NodeOption.MovementBehaviour);
		// forcibly transfer people to the community location
		for (Person p: sim.agents) {
			p.transferTo(p.getCommunityLocation());
		}
		// people will go home once the day has ended, therefore we need to run this until the end of the time they will be out in the community.
		// There are 4 hours per tick, meaning 6 ticks per day. We check they are home after the 5th tick of the simulation.
		List<String> _unused = helperFunctions.getFinalBehaviourNodesInSim(sim, 5.01 / sim.params.ticks_per_day, NodeOption.MovementBehaviour);
		// Create a hashset to store the whether everyone is at their home location
		
		HashSet<Boolean> allAtHome =  new HashSet<Boolean>();
		for (Person p: sim.agents) {
			allAtHome.add(p.getHousehold().getPeople().contains(p));
		}
		// if everyone is at home, then allAtHome should not have false in it
		Assert.assertFalse(allAtHome.contains(false));
	}
	@Test
	public void PeopleDoingTheHomeNodeSwitchToCommunityNodeBehaviourAtTheStartOfDay() {
		// set up the simulation
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt");
		sim.start();
		helperFunctions.makePeopleAlwaysLeaveHome(sim);
		// people start at home and then go to the community afterwards
		List<String> uniqueNodesInRun = helperFunctions.getFinalBehaviourNodesInSim(sim, 2.01 / sim.params.ticks_per_day, NodeOption.MovementBehaviour);
		// only expect people to be at home
		List<String> expectedNodes = Arrays.asList(mobilityNodeTitle.COMMUNITY.key);

		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun) && uniqueNodesInRun.containsAll(expectedNodes));
	}
	@Test
	public void PeopleWithinTheHomeLocationGoToTheCommunityLocationAtTheStartOfDay() {
		// set up the simulation
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/params_no_district_movement.txt");
		sim.start();
		// people start at home and then go to the community afterwards
		helperFunctions.makePeopleAlwaysLeaveHome(sim);
		List<String> _unused = helperFunctions.getFinalBehaviourNodesInSim(sim, 2.01 / sim.params.ticks_per_day, NodeOption.MovementBehaviour);
		// Create a hashset to store the whether everyone is at their community location
		
		HashSet<Boolean> allAtCommunity =  new HashSet<Boolean>();
		for (Person p: sim.agents) {
			allAtCommunity.add(p.getCommunityLocation().getPeople().contains(p));
		}
		// if everyone is at the community, then allAtHome should not have false in it
		Assert.assertFalse(allAtCommunity.contains(false));
	}
	
	@Test
	public void MakeSureThatPeopleOnlyDoTheCommunityAndHomeNodeBehavioursWithPerfectMixing() {
		//Arrange
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt", false);
		sim.start();
		// ensure that perfect mixing is turned on
		sim.params.setting_perfectMixing = true;
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueNodesInRun = helperFunctions.getUniqueNodesOverCourseofSim(sim, numDays, NodeOption.MovementBehaviour, 0.0);
		// we would expect only the home and community node to appear in the simulation
		List<String> expectedNodes = Arrays.asList(mobilityNodeTitle.HOME.key, mobilityNodeTitle.COMMUNITY.key);
		// Make sure than no other movement behaviour nodes are reaching in the simulation
		Assert.assertTrue(expectedNodes.containsAll(uniqueNodesInRun) && uniqueNodesInRun.containsAll(expectedNodes));
	}
	
	@Test
	public void MakeSureThatPeopleOnlyGoToTheCommunityAndHomeLocationsWithPerfectMixing() {
		//Arrange
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt");
		sim.start();
		// ensure that perfect mixing is turned on
		sim.params.setting_perfectMixing = true;
		int numDays = 100; 
		// Run the simulation and record the infectious behaviour nodes reached in this simulation
		HashSet<String> uniqueLocationTypesInRun = helperFunctions.getUniqueLocationsOverCourseOfSimulation(sim, numDays);
		// we would expect only the home and community node to appear in the simulation
		List<String> expectedLocationTypes = Arrays.asList(LocationCategory.HOME.key, LocationCategory.COMMUNITY.key);
		// Make sure than no other movement behaviour nodes are reaching in the simulation
		Assert.assertTrue(expectedLocationTypes.containsAll(uniqueLocationTypesInRun) && uniqueLocationTypesInRun.containsAll(expectedLocationTypes));
	}
	
//	@Test
//	public void LockdownReducesTheNumberOfVisitsToTheCommunity() {
//		// TODO This is something that will probably have to be developed. Lockdown doesn't seem to do anything to prevent people going into the 
//		// community at the moment
//		Assert.assertTrue(true);
//	}
//	
	@Test
	public void LockdownReducesTheNumberOfVisitsToOtherAdminZones() {
		WorldBankCovid19Sim sim_no_lockdown = helperFunctions.CreateDummySim(params + ".txt");
		sim_no_lockdown.start();
		
		int noLockdownOutboundTripCounts = outboundTripCountInSim(sim_no_lockdown, 100);
		
		WorldBankCovid19Sim sim_with_lockdown = helperFunctions.CreateDummySim(params + "_with_lockdown.txt");
		sim_with_lockdown.start();

		int lockdownOutboundTripCounts = outboundTripCountInSim(sim_with_lockdown, 100);
		Assert.assertTrue(noLockdownOutboundTripCounts > lockdownOutboundTripCounts);
	}
	
	// TESTS FOR IMPERFECT MIXING
	
	// TODO: Develop mobility tests for going to work when that is in the model
//	@Test
//	public void PeopleAtWorkGoToTheCommunityOrHomeAfterwards() {
//		// TODO
//		Assert.assertTrue(true);
//	}
//	// TODO: People do not currently go to work. Redo this when workplaces are in the model
//		@Test
//		public void OfficeWorkerBehaviours() {
//			//Arrange
//			WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/params.txt", false);
//			sim.start();
//			sim.schedule.step(sim);
//			
//			Person sut = sim.agents.get(0);
//			
//			//Act
//			sut.step(sim);
//			
//			//Assert
//			Assert.assertFalse(sut.atWorkNow()); // it is morning - they should not be at work
//		}
	
	
	@Parameterized.Parameters
	public static List<String> params() {
	    return Arrays.asList(
	            new String[]{"src/main/resources/params", "src/main/resources/params_ward_dummy"}
	    
	    );
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
