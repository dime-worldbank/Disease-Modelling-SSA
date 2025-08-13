package uk.ac.ucl.protecs.sim;

import java.util.HashSet;

import org.junit.Assert;
import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.hosts.Person;

import org.junit.Test;


public class CommunityTesting {
	// ==================================== Testing ==================================================================
	// === These tests are designed to ensure that all aspects of the community spaces are working as intended =======
	// === We test that varied community contact counts are loaded if using them (and not loaded if they aren't). ====
	// === We test that if they are loaded, then varied community counts are being set each day, and they vary from ==
	// === person to person.
	
	private final static String paramsDir = "src/test/resources/";
	
	@Test
	public void CheckVariedCommunityCountsAreLoaded() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_varied_community_counts.txt");
		sim.start();
		Assert.assertTrue(sim.params.community_interaction_counts.size() > 0);
		Assert.assertTrue(sim.params.community_interaction_percentages.size() > 0);

	}
	
	@Test
	public void CheckVariedCommunityAreNotLoadedIfNotUsing() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params.txt");
		sim.start();
		Assert.assertTrue(sim.params.community_interaction_counts == null);
		Assert.assertTrue(sim.params.community_interaction_percentages == null);
		Assert.assertTrue(sim.params.communityContactCountsFilename == null);


	}
	@Test
	public void CheckCommunityCountsAreSet() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_varied_community_counts.txt");
		sim.start();
		HelperFunctions.makePeopleLeaveTheHouseEachDay(sim);
		HelperFunctions.runSimulationForTicks(sim, 3);
		for (Person p: sim.agents) {
			if (p.getNumberOfCommunityInteractions() < 0) {
				Assert.fail();
			}
		}
	}
	@Test
	public void CheckCommunityCountsAreVaried() {
		WorldBankCovid19Sim sim = HelperFunctions.CreateDummySim(paramsDir + "params_varied_community_counts.txt");
		sim.start();
		HelperFunctions.makePeopleLeaveTheHouseEachDay(sim);
		HelperFunctions.runSimulationForTicks(sim, 3);
		HashSet<Integer> uniqueCommunityCounts = new HashSet<Integer>();
		for (Person p: sim.agents) {
			uniqueCommunityCounts.add(p.getNumberOfCommunityInteractions());
		}
		Assert.assertTrue(uniqueCommunityCounts.size() > 0);
	}
}
