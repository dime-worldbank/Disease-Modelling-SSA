package uk.ac.ucl.protecs.sim;

import uk.ac.ucl.protecs.helperFunctions.*;
import uk.ac.ucl.protecs.objects.Household;
import uk.ac.ucl.protecs.objects.Person;
import uk.ac.ucl.protecs.objects.Workplace;
import uk.ac.ucl.protecs.objects.Location.LocationCategory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WorkplaceTesting{
	// ================================================ Testing =======================================================================
	// ===== Here we test that the model is reading in workplaces from the census csv file and are being stored as workplace objects. =
	// ===== We check that subsequently the workplace bubbles are created and are associated with the workplace location created. =====
	// ===== We check that the bubbles are then created for each person, and that those who share a workplace have everyone associated=
	// ===== with that workplace has been included in the bubble.                                                               =======
	// ===== We check that people travel to their workplace location in the model.                                              =======
	// ===== We check that the parameters being used to predict workplace contacts are being loaded                             =======
	// ================================================================================================================================
	private final String params;
	
	public WorkplaceTesting(String fileName) {
		this.params = fileName;
	}

	@Test
	public void checkPopulationWorkplacesAreBeingLoaded() {
		// workplaces are read in from the csv file now, initialise the simulation
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt");
		sim.start();
		// check that the workplace locations have been created
		Assert.assertTrue(sim.workplaces.size() > 0);

	}
	@Test
	public void checkWorkplaceBubblesAreBeingMade() {
		// bubbles are created after during the loading in process of the population
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt");
		sim.start();
		// check that everyone has a bubble associated with their workplace
		boolean hasBeenGivenABubble = true;
		for (Person p: sim.agents) {
			if (p.getWorkBubble().isEmpty()) {hasBeenGivenABubble = false;}
		}
		Assert.assertTrue(hasBeenGivenABubble);
	}
	@Test
	public void checkWorkplaceBubblesContainEveryoneInWorkplace() {
		// bubbles are created after during the loading in process of the population

		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt");
		sim.start();
		// create a function to search through the population and get people belonging to certain bubbles
		Map<String, List<Person>> belongingToBubble = sim.agents.stream().collect(
				Collectors.groupingBy(
						Person::checkWorkplaceID
						)
				);
		boolean bubbleContainsEveryoneInWorkplace = true;
		// check that for each person, everyone who belongs to the workplace features in their bubble
		for (Workplace w: sim.workplaces) {
			String workplaceID = w.getId();
			List<Person> thisBubble = belongingToBubble.get(workplaceID);
			HashSet<Person> bubbleAsHashset = new HashSet<Person>(thisBubble);
			for (Person p: thisBubble) {
				bubbleContainsEveryoneInWorkplace = p.getWorkBubble().containsAll(bubbleAsHashset);
			}
		}
		Assert.assertTrue(bubbleContainsEveryoneInWorkplace);

	}
	@Test
	public void checkPeopleGoToTheirWorkplace() {
		// check the movement of the population to their workplaces
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt");
		makePeopleLeaveTheHouseEachDay(sim);
		// make everyone decide to go to their workplace
		sim.params.prob_go_to_work = 1.1d;
		sim.start();
		// run for three ticks (people leave the house at tick 2 and leave work at tick 4)
		int numTicks = 3;
		helperFunctions.runSimulationForTicks(sim, numTicks);
		// determine if everyone has travelled to their workplace
		// some jobs are based in the community, change to match
		boolean allAtWork = true;
		for (Person p: sim.agents) {
			if (!(p.getLocation() instanceof Workplace) && !p.isUnemployed() && !p.visitingNow()) {
				if (!sim.params.OccupationConstraintList.containsKey(p.getEconStatus()))
					allAtWork = false;
				}
		}
		Assert.assertTrue(allAtWork);
		
	}
	@Test
	public void testWorkplaceContactsCountDataisBeingLoaded() {
		// check the parameters associated with workplace contacts are being loaded
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_params.txt");
		sim.start();
		boolean contactCountDataLoaded = sim.params.workplaceContactCounts.size() > 0;
		boolean contactProbabilityDataLoaded = sim.params.workplaceContactProbability.keySet().size() > 0;
		boolean contactOccupationDataLoaded = sim.params.workplaceContactProbability.values().size() > 0;
		Assert.assertTrue(contactCountDataLoaded & contactProbabilityDataLoaded & contactOccupationDataLoaded);
		
	}
	@Test
	public void testWorkplaceConstraintsAreBeingLoaded() {
		// check the parameters associated with workplace constraints
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_with_constraints.txt");
		sim.start();
		boolean occupationsNamed = sim.params.OccupationConstraintList.keySet().size() > 0;
		boolean constraintsLoaded = sim.params.OccupationConstraintList.values().size() > 0;
		Assert.assertTrue(occupationsNamed & constraintsLoaded);
		
	}
	
	@Test
	public void testThoseConstrainedToHomeAreImmobilised() {
		// check the parameters associated with workplace constraints
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_with_constraints.txt");
		sim.start();
		// run for three ticks (people leave the house at tick 2 and leave work at tick 4)
		int numTicks = 3;
		helperFunctions.runSimulationForTicks(sim, numTicks);
		// create some boolean variables to catch errors if the come up
		boolean thoseImmobilisedStayAtHome = true;
		boolean hasimmobilisedProperty = true;
		// iterate over the simulation population and check that those who are constrained to home are in fact at home and have had the immobilised property set
		for (Person p: sim.agents) {
			if (sim.params.OccupationConstraintList.containsKey(p.getEconStatus())) {
				if (sim.params.OccupationConstraintList.get(p.getEconStatus()).equals(LocationCategory.HOME)) {
					hasimmobilisedProperty = (p.isImmobilised() == true);
					if (!(p.getLocation() instanceof Household)) {
						thoseImmobilisedStayAtHome = false;
					}
				}
			}
		}
		Assert.assertTrue(thoseImmobilisedStayAtHome & hasimmobilisedProperty);
		
	}
	@Test
	public void testThoseConstrainedToTheCommunityAreNotAtWork() {
		// check the parameters associated with workplace constraints
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_with_constraints.txt");
		// make sure that everyone leaves the house that day
		makePeopleLeaveTheHouseEachDay(sim);
		sim.start();
		// run for three ticks (people leave the house at tick 2 and leave work at tick 4)
		int numTicks = 3;
		helperFunctions.runSimulationForTicks(sim, numTicks);
		// create some boolean variables to catch errors if the come up
		boolean thoseConstrainedAreInCommunity = true;
		// iterate over the simulation population and check that those who are constrained to the community are at their community spaces
		for (Person p: sim.agents) {
			if (sim.params.OccupationConstraintList.containsKey(p.getEconStatus())) {
				if (sim.params.OccupationConstraintList.get(p.getEconStatus()).equals(LocationCategory.COMMUNITY)) {
					if ((p.getLocation() instanceof Household) || (p.getLocation() instanceof Workplace)) {
						thoseConstrainedAreInCommunity = false;
					}
				}
			}
		}
		Assert.assertTrue(thoseConstrainedAreInCommunity);
		
	}
	@Test
	public void workplaceConstraintsDoNotBreakCode() {
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim(params + ".txt");
		sim.start();
		int numTicks = 90;
		helperFunctions.runSimulation(sim, numTicks);
		
	}
	
	
	@Test
	public void runToDev() {
		// check the parameters associated with workplace constraints
		WorldBankCovid19Sim sim = helperFunctions.CreateDummySim("src/main/resources/workplace_bubbles_with_constraints.txt");
		sim.params.infection_beta = 0.3;
		// make sure that everyone leaves the house that day
		sim.start();
		// run for three ticks (people leave the house at tick 2 and leave work at tick 4)
		int numTicks = 90;
		helperFunctions.runSimulation(sim, numTicks);
		System.out.println("Number of called interactions at home: " + sim.home_interaction_counter);
		System.out.println("Number of called interactions at community: " + sim.community_interaction_counter);
		System.out.println("Number of called interactions at work: " + sim.work_interaction_counter);
		System.out.println("Number of outbound trips: " + sim.outbound_trip_counter);
		System.out.println("Number of stayed home: " + sim.stay_home_counter);


	
		
	}
	@Parameterized.Parameters
	public static List<String> params() {
	    return Arrays.asList(
	            new String[]{"src/main/resources/wp_all_community", "src/main/resources/wp_all_home"}
	    
	    );
	}
	private void makePeopleLeaveTheHouseEachDay(WorldBankCovid19Sim sim) {
		for (String key : sim.params.economic_status_weekday_movement_prob.keySet()) {
			sim.params.economic_status_weekday_movement_prob.put(key, (double) 1);         
		}
	}
	
}